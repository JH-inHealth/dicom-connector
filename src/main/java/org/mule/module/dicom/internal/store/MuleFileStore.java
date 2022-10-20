/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.store;

import org.dcm4che3.net.Association;
import org.dcm4che3.net.pdu.PresentationContext;
import org.mule.module.dicom.internal.notification.DownloadNotificationAction;
import org.mule.module.dicom.internal.util.StoreUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.PDVInputStream;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.notification.NotificationEmitter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MuleFileStore implements MuleStore {
    private final String outputFilePath;
    private final NotificationEmitter notificationEmitter;
    private final List<String> fileList;
    @Override
    public List<String> getFileList() { return fileList; }
    private final boolean compressFiles;
    private final String compressFilename;
    private final CompressAsync compress;
    private final ExecutorService executor;

    public MuleFileStore(String outputFilePath, NotificationEmitter notificationEmitter, boolean compressFiles) throws IOException {
        String guid = UUID.randomUUID().toString();
        this.outputFilePath = Paths.get(outputFilePath, guid).toString();
        this.notificationEmitter = notificationEmitter;
        this.compressFiles = compressFiles;
        fileList = new ArrayList<>();
        Files.createDirectories(Paths.get(this.outputFilePath));
        if (compressFiles) {
            compressFilename = Paths.get(outputFilePath, guid + ".tar.gz").toString();
            compress = new CompressAsync(compressFilename);
            executor = Executors.newFixedThreadPool(1);
            executor.execute(compress);

        } else {
            compressFilename = null;
            compress = null;
            executor = null;
        }
    }

    @Override
    public void waitForFinish() throws IOException {
        if (!compressFiles) return;
        compress.setFinished();
        boolean isTerm = false;
        while (compress.isRunning() && !isTerm) {
            try {
                isTerm = executor.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignore) {
                compress.setFinished();
                Thread t = Thread.currentThread();
                t.interrupt();
            }
        }
        if (!isTerm) executor.shutdown();
        if (compress.getLastException() != null) {
            Exception e = compress.getLastException();
            throw new IOException(e.getMessage(), e);
        } else {
            fileList.clear();
            fileList.add(compressFilename);
            Files.deleteIfExists(Paths.get(this.outputFilePath));
        }
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public void process(Association as, PresentationContext pc, PDVInputStream payload) throws IOException {
        // Setup tags and preface
        String tsuid = pc.getTransferSyntax();
        String icuid = as.getRemoteImplClassUID();
        String ivn = as.getRemoteImplVersionName();
        String aet = as.getRemoteAET();
        Attributes fmi = StoreUtils.createFileMetaInformation(null, null, tsuid, icuid, ivn, aet);

        // Save to the file
        String guid = UUID.randomUUID().toString();
        Path file = Paths.get(outputFilePath, guid + ".dcm");
        try (OutputStream output = Files.newOutputStream(file, StandardOpenOption.CREATE)) {
            StoreUtils.writeTo(output, payload, fmi);
        }
        fileList.add(file.toString());
        String filename = file.toString();
        if (compressFiles) {
            compress.add(filename);
        } else if (notificationEmitter != null)  {
            notificationEmitter.fire(DownloadNotificationAction.SAVED, TypedValue.of(filename));
        }
    }
}
