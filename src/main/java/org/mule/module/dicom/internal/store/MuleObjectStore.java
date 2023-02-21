package org.mule.module.dicom.internal.store;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.pdu.PresentationContext;
import org.mule.module.dicom.internal.util.AttribUtils;
import org.mule.module.dicom.internal.util.StoreUtils;
import org.mule.runtime.api.store.ObjectStore;
import org.mule.runtime.api.store.ObjectStoreException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MuleObjectStore implements MuleStore {
    private final List<String> fileList;
    private String currentFileName;
    private final ObjectStore<byte[]> objectStore;

    @Override
    public String getCurrentFileName() { return currentFileName; }

    @Override
    public boolean isNull() { return false; }

    @Override
    public void waitForFinish() {
        // Nothing to wait for
    }

    @Override
    public List<String> getFileList() { return fileList; }

    public MuleObjectStore(ObjectStore<byte[]> objectStore) {
        this.objectStore = objectStore;
        fileList = new ArrayList<>();
    }

    @Override
    public void process(Association as, PresentationContext pc, PDVInputStream payload) throws IOException {
        // Setup tags and preface
        String tsuid = pc.getTransferSyntax();
        String icuid = as.getRemoteImplClassUID();
        String ivn = as.getRemoteImplVersionName();
        String aet = as.getRemoteAET();
        Attributes fmi = StoreUtils.createFileMetaInformation(null, null, tsuid, icuid, ivn, aet);

        Attributes image = payload.readDataset(tsuid);
        // Make sure the TransferSyntax is on the image
        if (image.getString(Tag.TransferSyntaxUID) == null) {
            Map<String, String> setTags = new HashMap<>();
            setTags.put("TransferSyntaxUID", tsuid);
            AttribUtils.updateTags(image, setTags);
        }
        String iuid = AttribUtils.getFirstString(image, new Integer[]{Tag.AffectedSOPInstanceUID, Tag.MediaStorageSOPInstanceUID, Tag.SOPInstanceUID});
        currentFileName = iuid;

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            StoreUtils.writeTo(output, image, fmi);
            output.flush();
            objectStore.store(iuid, output.toByteArray());
            fileList.add(iuid);
        } catch (ObjectStoreException e) {
            throw new IOException(e);
        }
    }
}
