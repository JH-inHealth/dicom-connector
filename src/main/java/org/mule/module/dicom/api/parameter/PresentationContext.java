/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.parameter;

import org.mule.runtime.extension.api.annotation.Ignore;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class PresentationContext {
    @Ignore
    public static final String PARAMETER_GROUP = "Presentation Context";

    @Parameter
    @DisplayName("Information Model")
    @Optional(defaultValue = "STUDY_ROOT")
    @Placement(tab = "Presentation Context")
    private InformationModel informationModel;
    public InformationModel getInformationModel() { return informationModel; }
    public void setInformationModel(InformationModel informationModel) { this.informationModel = informationModel; }

    @Parameter
    @DisplayName("Retrieve Level")
    @Optional
    @Placement(tab = "Presentation Context")
    private RetrieveLevel retrieveLevel;
    public RetrieveLevel getRetrieveLevel() { return retrieveLevel; }
    public void setRetrieveLevel(RetrieveLevel retrieveLevel) { this.retrieveLevel = retrieveLevel; }

    @Parameter
    @DisplayName("Transfer Syntax")
    @Summary("Preferred compression of VR tags")
    @Optional(defaultValue = "IMPLICIT_FIRST")
    @Placement(tab = "Presentation Context")
    private TransferSyntax transferSyntax;
    public TransferSyntax getTransferSyntax() { return transferSyntax; }
    public void setTransferSyntax(TransferSyntax transferSyntax) { this.transferSyntax = transferSyntax; }

}
