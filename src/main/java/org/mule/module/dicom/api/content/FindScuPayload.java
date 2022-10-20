/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.content;

import org.mule.module.dicom.internal.operation.FindScu;

import java.util.List;
import java.util.Map;

public class FindScuPayload extends ScuPayload {
    private final List<Map<String, DicomValue>> results;
    public List<Map<String, DicomValue>> getResults() { return results; }

    public FindScuPayload(FindScu findScu) {
        super(findScu.getMessageId(), findScu.getAttributes());
        this.results = findScu.getPayload();
    }
}
