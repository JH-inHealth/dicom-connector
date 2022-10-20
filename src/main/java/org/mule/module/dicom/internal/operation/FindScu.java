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
import org.mule.module.dicom.api.content.DicomValue;
import org.mule.module.dicom.internal.config.ScuOperationConfig;
import org.mule.module.dicom.internal.connection.MuleDimseRSPHandler;
import org.mule.module.dicom.internal.connection.ScuConnection;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.module.dicom.internal.util.AttribUtils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindScu {
    private static final Logger log = LoggerFactory.getLogger(FindScu.class);

    private final int messageId;
    public int getMessageId() { return messageId; }

    private final String statusText;
    public String getStatusText() { return statusText; }

    private final Map<String, Object> attributes;
    public Map<String, Object> getAttributes() { return attributes; }

    private final List<Map<String, DicomValue>> payload;
    public List<Map<String, DicomValue>> getPayload() { return payload; }

    private final boolean success;
    public boolean getSuccess() { return success; }

    private final String errorMessage;
    public String getErrorMessage() { return errorMessage; }

    private FindScu(MuleDimseRSPHandler handler) {
        attributes = new HashMap<>();
        payload = new ArrayList<>();
        if (handler == null) {
            success = false;
            errorMessage = "Response Handler is NULL";
            messageId = -1;
            statusText = "Unset";
        } else {
            messageId = handler.getMessageID();
            statusText = handler.getStatusText();
            AttribUtils.upsertMap(handler.getCommand(), attributes);
            success = handler.getStatus() == 0;
            if (success) errorMessage = null;
            else errorMessage = String.format("%s: %s", statusText, attributes.getOrDefault("ErrorComment", "Unknown Error"));
            for (DicomValue dicomValue : handler.getData().getAsList()) {
                payload.add(dicomValue.getAsMap());
            }
        }
    }

    public static FindScu execute(ScuConnection connection, ScuOperationConfig scuOperationConfig, Map<String, Object> searchKeys) {
        Attributes keys = AttribUtils.toKeys(searchKeys);
        String level = scuOperationConfig.getRetrieveLevelDefault();
        if (level != null) keys.setString(Tag.QueryRetrieveLevel, VR.CS, level);
        MuleDimseRSPHandler handler;
        try {
            connection.start(scuOperationConfig, null);
            log.info("{}: C-FIND {}", connection, searchKeys);
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
        return new FindScu(handler);
    }
}
