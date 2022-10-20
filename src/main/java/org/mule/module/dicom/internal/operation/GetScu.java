/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.operation;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.mule.module.dicom.internal.config.ScuOperationConfig;
import org.mule.module.dicom.internal.connection.MuleDimseRSPHandler;
import org.mule.module.dicom.internal.connection.ScuConnection;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.module.dicom.internal.store.MuleStore;
import org.mule.module.dicom.internal.util.AttribUtils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetScu {
    private static final Logger log = LoggerFactory.getLogger(GetScu.class);

    private final int messageId;
    public int getMessageId() { return messageId; }

    private final String statusText;
    public String getStatusText() { return statusText; }

    private final Map<String, Object> attributes;
    public Map<String, Object> getAttributes() { return attributes; }

    private final List<String> payload;
    public List<String> getPayload() { return payload; }


    private final boolean hasError;
    public boolean getHasError() { return hasError; }

    private final String errorMessage;
    public String getErrorMessage() { return errorMessage; }

    private GetScu(MuleDimseRSPHandler handler, List<String> fileList) {
        attributes = new HashMap<>();
        payload = fileList;
        if (handler == null) {
            hasError = true;
            errorMessage = "Response Handler is NULL";
            messageId = -1;
            statusText = "Unset";
        } else {
            messageId = handler.getMessageID();
            statusText = handler.getStatusText();
            AttribUtils.upsertMap(handler.getCommand(), attributes);
            if (handler.getStatus() == 0) {
                hasError = false;
                errorMessage = null;
            } else {
                hasError = true;
                errorMessage = String.format("%s: %s", statusText, attributes.getOrDefault("ErrorComment", "Unknown Error"));
            }
        }
    }

    public static GetScu execute(ScuConnection connection, ScuOperationConfig scuOperationConfig, Map<String, Object> searchKeys, MuleStore muleStore) {
        Attributes keys = AttribUtils.toKeys(searchKeys);
        // Put retrieve level in the search keys
        String level = scuOperationConfig.getRetrieveLevelDefault();
        if (level != null) keys.setString(Tag.QueryRetrieveLevel, VR.CS, level);
        MuleDimseRSPHandler handler;
        try {
            connection.start(scuOperationConfig, muleStore);
            log.info("{}: C-GET {}", connection, searchKeys);
            handler = connection.execute(keys, null);
            if (handler == null || handler.isCanceled()) throw new ModuleException(DicomError.CANCELED, new RuntimeException("Canceled"));
        } catch (SSLException e) {
            throw new ModuleException(DicomError.SSL, e);
        } catch (IOException e) {
            throw new ModuleException(DicomError.CONNECTIVITY, e);
        } catch (GeneralSecurityException e) {
            throw new ModuleException(DicomError.CLIENT_SECURITY, e);
        } finally {
            connection.stop();
        }
        try {
            muleStore.waitForFinish();
        } catch (IOException e) {
            throw new ModuleException(DicomError.FILE_IO, e);
        }
        return new GetScu(handler, muleStore.getFileList());
    }
}
