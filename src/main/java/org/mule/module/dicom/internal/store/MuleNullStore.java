/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.store;

import org.dcm4che3.net.Association;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.pdu.PresentationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MuleNullStore implements MuleStore {
    private static final Logger log = LoggerFactory.getLogger(MuleNullStore.class);

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public void waitForFinish() {
        // It was done before it started
    }

    @Override
    public List<String> getFileList() {
        return new ArrayList<>();
    }

    @Override
    public void process(Association as, PresentationContext pc, PDVInputStream payload) {
        log.debug("NULL STORAGE");
    }
}
