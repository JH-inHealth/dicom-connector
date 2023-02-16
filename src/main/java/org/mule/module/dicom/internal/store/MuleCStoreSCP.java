/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.store;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuleCStoreSCP extends BasicCStoreSCP {
    private static final Logger log = LoggerFactory.getLogger(MuleCStoreSCP.class);
    protected static final String TSUID = "TransferSyntaxUID";
    private MuleStore store;
    public MuleStore getStore() { return store; }
    public void setStore(MuleStore store) {
        this.store = store;
    }

    public MuleCStoreSCP() {
        super();
        store = new MuleNullStore();
    }


    @Override
    protected void store(Association as, PresentationContext pc,
                         Attributes rq, PDVInputStream data, Attributes rsp) {
        int status;
        try {
            if (store.isNull()) {
                status = Status.Success;
                log.error("{}: MISSING STORAGE", as);
            } else {
                store.process(as, pc, data);
                status = Status.Success;
                log.info("{}: M-WRITE to {}", as, store.getCurrentFileName());
            }
        } catch (Exception e) {
            log.error(as.toString() + ": M-WRITE to " + store.getCurrentFileName() + " - " + e.getMessage(), e);
            status = Status.ProcessingFailure;
        }
        rsp.setInt(Tag.Status, VR.US, status);
    }

    public DicomServiceRegistry createServiceRegistry() {
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(this);
        return serviceRegistry;
    }

}

