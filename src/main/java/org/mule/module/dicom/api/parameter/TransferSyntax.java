/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.parameter;

public enum TransferSyntax {
    EXPLICIT_FIRST("ExplicitFirst"),
    EXPLICIT_ONLY("ExplicitOnly"),
    IMPLICIT_FIRST("ImplicitFirst"),
    IMPLICIT_ONLY("ImplicitOnly");

    final String displayName;
    public String getDisplayName() { return displayName; }

    TransferSyntax(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
