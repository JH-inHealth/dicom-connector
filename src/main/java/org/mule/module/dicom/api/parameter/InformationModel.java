/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.parameter;

public enum InformationModel {
    PATIENT_ROOT("PatientRoot"),
    PATIENT_STUDY_ONLY("PatientStudyOnly"),
    COMPOSITE_INSTANCE_ROOT("CompositeInstanceRoot"),
    HANGING_PROTOCOL("HangingProtocol"),
    COLOR_PALETTE("ColorPalette"),
    STUDY_ROOT("StudyRoot");

    final String displayName;
    InformationModel(String displayName) {
        this.displayName = displayName;
    }
    @Override
    public String toString() {
        return displayName;
    }
}
