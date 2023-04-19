package org.mule.module.dicom.internal.store;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.pdu.PresentationContext;
import org.mule.module.dicom.api.content.DicomObject;
import org.mule.module.dicom.internal.source.GetScuListener;
import org.mule.module.dicom.internal.util.AttribUtils;

import java.io.IOException;
import java.util.*;

public class MuleGetScuStore implements MuleStore {
    private final String flowName;
    private final String seriesName;

    public MuleGetScuStore(String flowName, String seriesName) throws IOException {
        fileList = new ArrayList<>();
        this.flowName = flowName;
        this.seriesName = seriesName;
        GetScuListener.start(flowName, seriesName);
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public void waitForFinish() throws IOException {
        GetScuListener.stop(flowName, seriesName);
    }

    private final List<String> fileList;
    @Override
    public List<String> getFileList() { return fileList; }

    private String currentFileName;
    @Override
    public String getCurrentFileName() { return currentFileName; }

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
        if (iuid != null) currentFileName = iuid;
        else currentFileName = "";
        if (iuid != null) {
            fileList.add(iuid);
            GetScuListener.publish(flowName, seriesName, iuid, dicomObject);
        }
    }
}
