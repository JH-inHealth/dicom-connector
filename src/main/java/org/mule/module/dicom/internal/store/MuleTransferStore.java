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
import org.dcm4che3.net.Association;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.pdu.PresentationContext;
import org.mule.module.dicom.internal.config.ScuOperationConfig;
import org.mule.module.dicom.internal.connection.TransferConnection;
import org.mule.module.dicom.internal.operation.StoreScu;
import org.mule.module.dicom.internal.util.AttribUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MuleTransferStore implements MuleStore {
    private final TransferConnection connection;
    private final ScuOperationConfig scuOperationConfig;
    private final Map<String, String> changeTags;
    private final List<String> iuidList;
    private String currentFileName = "";
    @Override
    public String getCurrentFileName() { return currentFileName; }

    public MuleTransferStore(TransferConnection connection, ScuOperationConfig scuOperationConfig, Map<String, String> changeTags) {
        this.connection = connection;
        this.scuOperationConfig = scuOperationConfig;
        this.changeTags = changeTags;
        iuidList = new ArrayList<>();
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public void waitForFinish() {
        // StoreScu blocks until it is complete
    }

    @Override
    public List<String> getFileList() {
        return iuidList;
    }

    @Override
    public void process(Association as, PresentationContext pc, PDVInputStream payload) throws IOException {
        String tsuid = pc.getTransferSyntax();
        Attributes image = payload.readDataset(tsuid);
        AttribUtils.updateTags(image, changeTags);
        currentFileName = AttribUtils.getFirstString(image, new Integer[]{Tag.AffectedSOPInstanceUID, Tag.MediaStorageSOPInstanceUID, Tag.SOPInstanceUID});
        StoreScu.execute(connection.getTargetConnection(), scuOperationConfig, image, null, iuidList);
    }
}
