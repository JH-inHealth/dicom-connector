/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.store;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Locale;

public enum DicomFileType {
    DIRECTORY, DICOM, DICOMDIR, GZIP, TAR, UNKNOWN, CANNOT_READ;

    public static boolean canStore(DicomFileType value) {
        switch(value) {
            case DICOM:
            case GZIP:
            case TAR:
                return true;
            default:
                return false;
        }
    }

    /**
     * Reads the magic number of a file to determine what kind of file it is.
     * @param filename Can be a filename or a directory
     */
    public static DicomFileType parse(String filename) {
        final byte[] sigGz = new byte[]{0x1f, (byte)0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00}; // offset 0
        final byte[] sigDcm = new byte[]{0x44, 0x49, 0x43, 0x4d, 0x00, 0x00, 0x00, 0x00}; // offset 128
        final byte[] sigTar1 = new byte[]{0x75, 0x73, 0x74, 0x61, 0x72, 0x00, 0x30, 0x30}; // offset 257
        final byte[] sigTar2 = new byte[]{0x75, 0x73, 0x74, 0x61, 0x72, 0x20, 0x20, 0x00}; // offset 257

        Path file = Paths.get(filename);
        if (!Files.exists(file)) return UNKNOWN;
        if (Files.isDirectory(file)) return DIRECTORY;
        try (InputStream inputStream = Files.newInputStream(file, StandardOpenOption.READ)) {
            byte[] buffer = new byte[8];
            int size = inputStream.read(buffer, 0, 3);
            if (size < 3) return UNKNOWN;
            if (Arrays.equals(buffer, sigGz)) {
                return GZIP;
            }
            long skipped = inputStream.skip(125);
            if (skipped < 125) return UNKNOWN;
            size = inputStream.read(buffer, 0, 4);
            if (size < 4) return UNKNOWN;
            if (Arrays.equals(buffer, sigDcm)) {
                if (filename.toUpperCase(Locale.ROOT).endsWith("DICOMDIR")) return DICOMDIR;
                return DICOM;
            }
            skipped = inputStream.skip(125);
            if (skipped < 125) return UNKNOWN;
            size = inputStream.read(buffer, 0, 8);
            if (size < 8) return UNKNOWN;
            if (Arrays.equals(buffer, sigTar1) || Arrays.equals(buffer, sigTar2)) {
                return TAR;
            }
        } catch (IOException ignore) {
            return CANNOT_READ;
        }
        return UNKNOWN;
    }
}
