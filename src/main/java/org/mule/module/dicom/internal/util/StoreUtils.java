/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.PDVInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreUtils {
    private static final Logger log = LoggerFactory.getLogger(StoreUtils.class);
    private StoreUtils() { }

    /**
     * Create File Meta Information
     * @param iuid MediaStorageSOPInstanceUID
     * @param cuid MediaStorageSOPClassUID
     * @param tsuid TransferSyntaxUID
     * @param icuid ImplementationClassUID
     * @param ivn ImplementationVersionName
     * @param aet SourceApplicationEntityTitle
     */
    public static Attributes createFileMetaInformation(String iuid, String cuid, String tsuid, String icuid, String ivn, String aet) {
        Attributes fmi = new Attributes(7);
        fmi.setBytes(Tag.FileMetaInformationVersion, VR.OB, new byte[]{0, 1});
        if (cuid != null) fmi.setString(Tag.MediaStorageSOPClassUID, VR.UI, cuid);
        if (iuid != null) fmi.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, iuid);
        fmi.setString(Tag.TransferSyntaxUID, VR.UI, tsuid);
        fmi.setString(Tag.ImplementationClassUID, VR.UI, icuid);
        if (ivn != null) {
            fmi.setString(Tag.ImplementationVersionName, VR.SH, ivn);
        }
        fmi.setString(Tag.SourceApplicationEntityTitle, VR.AE, aet);
        return fmi;
    }

    public static void writeTo(OutputStream file, Attributes content, Attributes fmi) throws IOException {
        try (DicomOutputStream out = new DicomOutputStream(file, UID.ExplicitVRLittleEndian)) {
            out.writeDataset(fmi,  content);
        }
    }

    public static void writeTo(OutputStream file, byte[] content, Attributes fmi) throws IOException {
        try (DicomOutputStream out = new DicomOutputStream(file, UID.ExplicitVRLittleEndian)) {
            out.writeFileMetaInformation(fmi);
            out.write(content);
        }
    }

    public static void writeTo(OutputStream file, PDVInputStream content, Attributes fmi) throws IOException {
        try (DicomOutputStream out = new DicomOutputStream(file, UID.ExplicitVRLittleEndian)) {
            out.writeFileMetaInformation(fmi);
            content.copyTo(out);
        }
    }

    public static void deleteFolder(String outputFilePath) {
        if (outputFilePath == null || outputFilePath.isEmpty()) return;
        try {
            Path dir = Paths.get(outputFilePath);
            if (Files.exists(dir)) {
                if (dir.toFile().isDirectory()) {
                    // Empty the content
                    try (Stream<Path> pathStream = Files.walk(dir)) {
                        List<Path> fileList = pathStream.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
                        for (Path p : fileList) {
                            Files.delete(p);
                        }
                    }
                    Files.deleteIfExists(dir);
                } else {
                    Files.delete(dir);
                }
            }
        } catch (IOException e) {
            log.error("Unable to delete " + outputFilePath, e);
        }
    }

    public static List<Path> getFileList(String outputFilePath) {
        if (outputFilePath == null || outputFilePath.isEmpty()) return new ArrayList<>();
        try {
            Path dir = Paths.get(outputFilePath);
            if (Files.exists(dir)) {
                try (Stream<Path> pathStream = Files.walk(dir)
                        .filter(file -> !Files.isDirectory(file))) {
                    return pathStream.collect(Collectors.toList());
                }
            } else {
                return new ArrayList<>();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}