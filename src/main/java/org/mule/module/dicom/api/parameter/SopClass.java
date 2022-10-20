/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.parameter;

import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

public class SopClass {
    @Parameter
    @DisplayName("Storage SOP Class UID")
    private String sopClassUid;
    public String getSopClassUid() { return sopClassUid; }
    public void setSopClassUid(String sopClassUid ) { this.sopClassUid = sopClassUid; }

    @Parameter
    private String[] transferSyntax;
    public String[] getTransferSyntax() { return transferSyntax; }
    public void setTransferSyntax(String[] transferSyntax) { this.transferSyntax = transferSyntax; }
}
