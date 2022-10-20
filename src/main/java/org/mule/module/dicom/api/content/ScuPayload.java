/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.content;

import java.util.Map;

abstract class ScuPayload {
    private final int messageId;
    public int getMessageId() { return messageId; }
    private final int messageIdBeingRespondedTo;
    public int getMessageIdBeingRespondedTo() { return messageIdBeingRespondedTo; }
    private final String affectedSopClassUid;
    public String getAffectedSopClassUid() { return affectedSopClassUid; }

    protected ScuPayload(int messageId, Map<String, Object> attributes) {
        this.messageId = messageId;
        this.messageIdBeingRespondedTo = (int)attributes.getOrDefault("MessageIDBeingRespondedTo", -1);
        if (attributes.containsKey("AffectedSOPClassUID")) affectedSopClassUid = (String)attributes.get("AffectedSOPClassUID");
        else affectedSopClassUid = null;
    }

}
