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
import org.mule.module.dicom.internal.operation.TransferScu;
import org.mule.module.dicom.internal.util.AttribUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MuleTransferStore implements MuleStore {
    private final TransferConnection connection;
    private final ScuOperationConfig scuOperationConfig;
    private final Map<String, String> changeTags;
    private final List<String> iuidList;
    private final ExecutorService executorService;
    private final List<Future<String>> executorResults;
    private String currentFileName = "";
    @Override
    public String getCurrentFileName() { return currentFileName; }

    public MuleTransferStore(TransferConnection connection, ScuOperationConfig scuOperationConfig, Map<String, String> changeTags) {
        this.connection = connection;
        this.scuOperationConfig = scuOperationConfig;
        this.changeTags = changeTags;
        iuidList = new ArrayList<>();
        executorResults = new ArrayList<>();
        executorService = Executors.newFixedThreadPool(2);
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public void waitForFinish() throws IOException {
        try {
            for (Future<String> result : executorResults) {
                String iuid = result.get();
                iuidList.add(iuid);
            }
            executorService.shutdown();
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread t = Thread.currentThread();
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
            t.interrupt();
        } catch (Exception e) {
            throw new IOException(e);
        }
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

        TransferScu transferScu = new TransferScu(connection, scuOperationConfig, image);
        Future<String> result = executorService.submit(transferScu);
        executorResults.add(result);
    }
}
