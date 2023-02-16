/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.store;

import java.io.IOException;
import java.util.List;

import org.dcm4che3.net.Association;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.pdu.PresentationContext;

public interface MuleStore {
    String getCurrentFileName();
    boolean isNull();
    void waitForFinish() throws IOException;
    List<String> getFileList();
    void process(Association as, PresentationContext pc, PDVInputStream payload) throws IOException;
}
