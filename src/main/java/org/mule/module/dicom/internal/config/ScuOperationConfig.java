/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.config;

import org.mule.module.dicom.api.parameter.InformationModel;
import org.mule.module.dicom.api.parameter.RetrieveLevel;
import org.mule.module.dicom.api.parameter.SopClass;
import org.mule.module.dicom.api.parameter.TransferSyntax;
import org.dcm4che3.data.UID;

import java.lang.reflect.Field;

public class ScuOperationConfig {
    private final ScuType operation;
    public ScuType getOperation() { return operation; }

    private InformationModel informationModel = InformationModel.STUDY_ROOT;
    public InformationModel getInformationModel() { return informationModel; }
    public void setInformationModel(InformationModel informationModel) { this.informationModel = informationModel; }

    private RetrieveLevel retrieveLevel = null;
    public RetrieveLevel getRetrieveLevel() { return retrieveLevel; }
    public void setRetrieveLevel(RetrieveLevel retrieveLevel) { this.retrieveLevel = retrieveLevel; }

    private TransferSyntax transferSyntax = TransferSyntax.IMPLICIT_FIRST;
    public TransferSyntax getTransferSyntax() { return transferSyntax; }
    public void setTransferSyntax(TransferSyntax transferSyntax) { this.transferSyntax = transferSyntax; }

    private SopClass[] sopClasses = null;
    public SopClass[] getSopClasses() { return sopClasses; }
    public void setSopClasses(SopClass[] sopClasses) { this.sopClasses = sopClasses; }

    private int storeTimeout = 0;
    public int getStoreTimeout() { return storeTimeout; }
    public void setStoreTimeout(int storeTimeout) { this.storeTimeout = storeTimeout; }

    private int cancelAfter = 0;
    public int getCancelAfter() { return cancelAfter; }
    public void setCancelAfter(int cancelAfter) { this.cancelAfter = cancelAfter; }

    private String transferSyntaxUid = null;
    public String getTransferSyntaxUid() { return transferSyntaxUid; }
    public void setTransferSyntaxUid(String transferSyntaxUid) { this.transferSyntaxUid = transferSyntaxUid; }

    private String sopClassUid = null;
    public String getSopClassUid() { return sopClassUid; }
    public void setSopClassUid(String sopClassUid) { this.sopClassUid = sopClassUid; }

    public ScuOperationConfig(ScuType operation) {
        this.operation = operation;
    }

    public String getOperationName() {
        return operation.getName();
    }

    public String getInformationModelCuid() {
        if (sopClassUid != null) return sopClassUid;
        String cuidType;
        switch (informationModel) {
            case COMPOSITE_INSTANCE_ROOT:
                cuidType = "Retrieve";
                break;
            case HANGING_PROTOCOL:
                cuidType = "InformationModel";
                break;
            default:
                cuidType = "QueryRetrieveInformationModel";
                break;
        }
        String cuidName = informationModel + cuidType + getOperationName();
        String cuid;
        // Lookup the CUID in the UID class
        try {
            Field field = UID.class.getField(cuidName);
            cuid = (String)field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {
            cuid = "";
        }
        return cuid;
    }

    public String getRetrieveLevelDefault() {
        String level;
        if (retrieveLevel == null) {
            switch (informationModel) {
                case COMPOSITE_INSTANCE_ROOT:
                    level = RetrieveLevel.IMAGE.toString();
                    break;
                case HANGING_PROTOCOL:
                case COLOR_PALETTE:
                    level = null;
                    break;
                default:
                    level = RetrieveLevel.STUDY.toString();
                    break;
            }
        } else level = retrieveLevel.toString();
        return level;
    }

    public String[] getTransferSyntaxCodes() {
        if (transferSyntaxUid != null) return new String[] { transferSyntaxUid };
        String[] codes;
        switch (transferSyntax) {
            case IMPLICIT_FIRST:
                codes = new String[] { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian };
                break;
            case EXPLICIT_FIRST:
                codes = new String[] { UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian, UID.ImplicitVRLittleEndian };
                break;
            case IMPLICIT_ONLY:
                codes = new String[] { UID.ImplicitVRLittleEndian };
                break;
            case EXPLICIT_ONLY:
                codes = new String[] { UID.ExplicitVRLittleEndian };
                break;
            default:
                codes = new String[] { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian };
                break;
        }
        return codes;
    }

}
