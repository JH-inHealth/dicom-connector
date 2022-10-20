/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.content;

import org.mule.module.dicom.internal.operation.MoveScu;

public class MoveScuPayload extends ScuOpsPayload{
    public MoveScuPayload(MoveScu moveScu) {
        super(moveScu.getMessageId(), moveScu.getAttributes());
    }
}
