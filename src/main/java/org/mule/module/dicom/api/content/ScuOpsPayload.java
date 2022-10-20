/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.content;

import java.util.Map;

abstract class ScuOpsPayload extends ScuPayload {
    private final int numberOfCompletedSuboperations;
    public int getNumberOfCompletedSuboperations() { return numberOfCompletedSuboperations; }
    private final int numberOfFailedSuboperations;
    public int getNumberOfFailedSuboperations() { return numberOfFailedSuboperations; }

    private final int numberOfRemainingSuboperations;
    public int getNumberOfRemainingSuboperations() { return numberOfRemainingSuboperations; }
    private final int numberOfWarningSuboperations;
    public int getNumberOfWarningSuboperations() { return numberOfWarningSuboperations; }

    protected ScuOpsPayload(int messageId, Map<String, Object> attributes) {
        super(messageId, attributes);
        this.numberOfCompletedSuboperations = (int)attributes.getOrDefault("NumberOfCompletedSuboperations", 0);
        this.numberOfFailedSuboperations = (int)attributes.getOrDefault("NumberOfFailedSuboperations", 0);
        this.numberOfRemainingSuboperations = (int)attributes.getOrDefault("NumberOfRemainingSuboperations", 0);
        this.numberOfWarningSuboperations = (int)attributes.getOrDefault("NumberOfWarningSuboperations", 0);
    }
}
