/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.operation;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.mule.module.dicom.internal.config.ScuOperationConfig;
import org.mule.module.dicom.internal.connection.MuleDimseRSPHandler;
import org.mule.module.dicom.internal.connection.ScuConnection;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.module.dicom.internal.store.DicomFileType;
import org.mule.module.dicom.internal.util.AttribUtils;
import org.mule.module.dicom.internal.util.StoreUtils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.*;

public class StoreScu {
    private static final Logger log = LoggerFactory.getLogger(StoreScu.class);
    private final ScuConnection connection;
    private final ScuOperationConfig scuOperationConfig;
    private final Map<String, String> changeTags;
    private final List<String> iuidList;

    private StoreScu(ScuConnection connection, ScuOperationConfig scuOperationConfig, Map<String, String> changeTags, List<String> iuidList) {
        this.connection = connection;
        this.scuOperationConfig = scuOperationConfig;
        this.changeTags = changeTags;
        this.iuidList = iuidList;
    }

    public void execute(String fileName) throws IOException {
        Attributes data;
        Attributes fmi;
        File file = new File(fileName);
        try (DicomInputStream dis = new DicomInputStream(file)) {
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            fmi = dis.getFileMetaInformation();
            data = dis.readDataset();
        }
        AttribUtils.updateTags(data, changeTags);
        StoreScu.execute(connection, scuOperationConfig, data, fmi, iuidList);
    }

    public static void execute(ScuConnection connection, ScuOperationConfig scuOperationConfig, Attributes data, Attributes fmi, List<String> iuidList) {
        String tsuid = null;
        if (fmi != null) tsuid = AttribUtils.getFirstString(fmi, new Integer[]{Tag.TransferSyntaxUID});
        if (tsuid == null) tsuid = AttribUtils.getFirstString(data, new Integer[]{Tag.TransferSyntaxUID});
        if (tsuid == null) tsuid = UID.ExplicitVRLittleEndian;
        String cuid = AttribUtils.getFirstString(data, new Integer[]{Tag.AffectedSOPClassUID, Tag.MediaStorageSOPClassUID, Tag.SOPClassUID});
        scuOperationConfig.setTransferSyntaxUid(tsuid);
        scuOperationConfig.setSopClassUid(cuid);

        String iuid = AttribUtils.getFirstString(data, new Integer[]{Tag.AffectedSOPInstanceUID, Tag.MediaStorageSOPInstanceUID, Tag.SOPInstanceUID});
        if (iuid == null || iuid.isEmpty()) {
            throw new ModuleException(DicomError.MISSING_UID, new IOException("Missing SOP Instance UID"));
        }
        try {
            connection.start(scuOperationConfig, null);
            log.info("{}: C-STORE {}", connection, iuid);
            MuleDimseRSPHandler handler = connection.execute(data, iuid);
            if (handler == null || handler.isCanceled()) throw new ModuleException(DicomError.CANCELED, new RuntimeException("Canceled"));
            iuidList.add(iuid);
        } catch (SSLException e) {
            throw new ModuleException(DicomError.SSL, e);
        } catch (IOException e) {
            throw new ModuleException(DicomError.CONNECTIVITY, e);
        } catch (GeneralSecurityException e) {
            throw new ModuleException(DicomError.CLIENT_SECURITY, e);
        } finally {
            connection.stop();
        }
    }

    public static void execute(ScuConnection connection, ScuOperationConfig scuOperationConfig, String fileName, DicomFileType dft, Map<String, String> changeTags, List<String> iuidList) throws IOException {
        StoreScu storeScu = new StoreScu(connection, scuOperationConfig, changeTags, iuidList);
        Path f = Paths.get(fileName);
        switch (dft) {
            case DICOM:
                storeScu.execute(fileName);
                break;
            case GZIP:
                try (InputStream fileInputStream = Files.newInputStream(f)) {
                    String tmpFolder = Files.createTempDirectory("dicom_storescu_").toString();
                    unGzip(tmpFolder, fileName, fileInputStream, storeScu);
                    StoreUtils.deleteFolder(tmpFolder);
                }
                break;
            case TAR:
                try (InputStream fileInputStream = Files.newInputStream(f)) {
                    String tmpFolder = Files.createTempDirectory("dicom_storescu_").toString();
                    unTar(tmpFolder, fileInputStream, storeScu);
                    StoreUtils.deleteFolder(tmpFolder);
                }
                break;
            default:
                break;
        }
    }

    private static void unGzip(String folderName, String filename, InputStream inputStream, StoreScu storeScu) throws IOException {
        try (GzipCompressorInputStream gzInputStream = new GzipCompressorInputStream(inputStream)) {
            if (filename.endsWith(".tar.gz") || filename.endsWith((".tgz"))) {
                unTar(folderName, gzInputStream, storeScu);
            } else {
                String randomName = UUID.randomUUID().toString();
                Path extractedFile = Paths.get(folderName, randomName);
                extractAndSend(folderName, extractedFile, gzInputStream, storeScu);
            }
        }

    }

    private static void unTar(String folderName, InputStream inputStream, StoreScu storeScu) throws IOException {
        try (TarArchiveInputStream tarInputStream = new TarArchiveInputStream(inputStream)) {
            TarArchiveEntry entry;
            while ((entry = tarInputStream.getNextTarEntry()) != null) {
                if (entry.isFile()) {
                    Path extractedFile = Paths.get(folderName, entry.getName());
                    extractAndSend(folderName, extractedFile, tarInputStream, storeScu);
                }
            }
        }
    }

    private static void extractAndSend(String folderName, Path extractedFile, InputStream inputStream, StoreScu storeScu) throws IOException {
        Files.createDirectories(extractedFile.getParent());
        try (OutputStream outputStream = Files.newOutputStream(extractedFile, StandardOpenOption.CREATE_NEW)) {
            byte[] content = new byte[32767];
            int i;
            while ((i = inputStream.read(content)) != -1) {
                outputStream.write(content, 0, i);
            }
            outputStream.flush();
        }
        // Send the extracted file, which could be anything
        DicomFileType dft = DicomFileType.parse(extractedFile.toString());
        switch (dft) {
            case DICOM:
                storeScu.execute(extractedFile.toString());
                break;
            case GZIP:
                try (InputStream fileInputStream = Files.newInputStream(extractedFile)) {
                    unGzip(folderName, extractedFile.toString(), fileInputStream, storeScu);
                }
                break;
            case TAR:
                try (InputStream fileInputStream = Files.newInputStream(extractedFile)) {
                    unTar(folderName, fileInputStream, storeScu);
                }
                break;
            default:
                // Quietly ignore anything else
                break;
        }
        Files.deleteIfExists(extractedFile);
    }

}
