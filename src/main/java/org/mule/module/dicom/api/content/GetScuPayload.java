/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.content;

import org.mule.module.dicom.internal.operation.GetScu;

import java.util.List;

public class GetScuPayload extends ScuOpsPayload {
    private final List<String> filenames;
    public List<String> getFilenames() { return filenames; }

    public GetScuPayload(GetScu getScu) {
        super(getScu.getMessageId(), getScu.getAttributes());
        this.filenames = getScu.getPayload();
    }
}
