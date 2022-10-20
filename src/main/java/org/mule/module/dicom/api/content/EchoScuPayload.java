/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.content;

import org.mule.module.dicom.internal.operation.EchoScu;

public class EchoScuPayload {
    private final int messageId;
    public int getMessageId() { return messageId; }
    public EchoScuPayload(EchoScu echoScu) {
        messageId = echoScu.getMessageId();
    }
}
