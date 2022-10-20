/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.content;

import org.dcm4che3.data.Attributes;

import java.io.Serializable;

public class DicomObject implements Serializable {
    private static final long serialVersionUID = 8075573747738871134L;
    private String transferSyntaxUid;
    public String getTransferSyntaxUid() { return transferSyntaxUid; }
    public void setTransferSyntaxUid(String transferSyntaxUid) { this.transferSyntaxUid = transferSyntaxUid; }
    private String sourceApplicationEntityTitle;
    public String getSourceApplicationEntityTitle() { return sourceApplicationEntityTitle; }
    public void setSourceApplicationEntityTitle(String sourceApplicationEntityTitle) { this.sourceApplicationEntityTitle = sourceApplicationEntityTitle; }
    private String implementationClassUid;
    public String getImplementationClassUid() { return implementationClassUid; }
    public void setImplementationClassUid(String implementationClassUid) { this.implementationClassUid = implementationClassUid; }
    private String implementationVersionName;
    public String getImplementationVersionName() { return implementationVersionName; }
    public void setImplementationVersionName(String implementationVersionName) { this.implementationVersionName = implementationVersionName; }

    private Attributes content;
    public Attributes getContent() { return content; }
    public void setContent(Attributes content) { this.content = content; }

    public DicomObject() { }
    public DicomObject(Attributes content, String transferSyntaxUid, String sourceApplicationEntityTitle, String implementationClassUid, String implementationVersionName) {
        this.content = content;
        this.transferSyntaxUid = transferSyntaxUid;
        this.sourceApplicationEntityTitle = sourceApplicationEntityTitle;
        this.implementationClassUid = implementationClassUid;
        this.implementationVersionName = implementationVersionName;
    }
}
