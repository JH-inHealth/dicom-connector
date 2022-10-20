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
import org.mule.metadata.api.model.NullType;
import org.mule.module.dicom.api.content.DicomObject;
import org.mule.module.dicom.internal.util.AttribUtils;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MuleProcessStore implements MuleStore {
    private final SourceCallback<Object, NullType> sourceCallback;

    public MuleProcessStore(SourceCallback<Object, NullType> sourceCallback) {
        this.sourceCallback = sourceCallback;
        this.fileList = new ArrayList<>();
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public void waitForFinish() {
        // Source Callback blocks until it is complete
    }

    private final List<String> fileList;
    @Override
    public List<String> getFileList() { return fileList; }

    @Override
    public void process(Association as, PresentationContext pc, PDVInputStream payload) throws IOException {
        String tsuid = pc.getTransferSyntax();
        Attributes image = payload.readDataset(tsuid);
        // Make sure the TransferSyntax is on the image
        if (image.getString(Tag.TransferSyntaxUID) == null) {
            Map<String, String> setTags = new HashMap<>();
            setTags.put("TransferSyntaxUID", tsuid);
            AttribUtils.updateTags(image, setTags);
        }
        DicomObject dicomObject = new DicomObject(image, pc.getTransferSyntax(), as.getRemoteAET(), as.getRemoteImplClassUID(), as.getRemoteImplVersionName());
        String iuid = AttribUtils.getFirstString(image, new Integer[]{Tag.AffectedSOPInstanceUID, Tag.MediaStorageSOPInstanceUID, Tag.SOPInstanceUID});

        sourceCallback.handle(Result.<Object, NullType>builder()
                .output(dicomObject)
                .build());
        if (iuid != null) fileList.add(iuid + ".dcm");
    }
}
