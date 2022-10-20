/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.operation;

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
import java.util.HashMap;
import java.util.Map;

public class EchoScu {
    private static final Logger log = LoggerFactory.getLogger(EchoScu.class);
    private final int messageId;
    public int getMessageId() { return messageId; }
    private final String statusText;
    public String getStatusText() { return statusText; }
    private final Map<String, Object> attributes;
    public Map<String, Object> getAttributes() { return attributes; }

    private final boolean success;
    public boolean getSuccess() { return success; }

    private final String errorMessage;
    public String getErrorMessage() { return errorMessage; }

    private EchoScu(MuleDimseRSPHandler handler) {
        attributes = new HashMap<>();
        if (handler == null) {
            success = false;
            errorMessage = "Response Handler is NULL";
            messageId = -1;
            statusText = "Unset";
        } else {
            messageId = handler.getMessageID();
            statusText = "Success";
            AttribUtils.upsertMap(handler.getCommand(), attributes);
            success = true;
            errorMessage = null;
        }
    }

    public static EchoScu execute(ScuConnection connection, ScuOperationConfig scuOperationConfig) {
        MuleDimseRSPHandler handler;
        try {
            connection.start(scuOperationConfig, null);
            log.info("{}: C-ECHO", connection);
            handler = connection.execute(null, null);
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
        return new EchoScu(handler);
    }
}
