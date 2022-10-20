/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.test;

import org.dcm4che3.data.Tag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mule.module.dicom.api.content.DicomObject;
import org.mule.module.dicom.api.content.DicomValue;
import org.mule.module.dicom.internal.operation.FileOperations;
import org.mule.module.dicom.internal.store.DicomFileType;
import org.mule.module.dicom.internal.util.StoreUtils;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class FileOperationsTest {
    final FileOperations fileOperations = new FileOperations();

    @Test
    void readFile() {
        // Given
        String filename = TestUtils.getSampleFilename("sample.dcm");

        try {
            // When
            DicomObject file = (DicomObject)fileOperations.readFile(filename);

            // Then
            Assertions.assertEquals("1.2.840.10008.1.2.1", file.getTransferSyntaxUid());
            Assertions.assertEquals("CLUNIE1", file.getSourceApplicationEntityTitle());
            Assertions.assertEquals("1.3.6.1.4.1.5962.2", file.getImplementationClassUid());
            Assertions.assertEquals("DCTOOL100", file.getImplementationVersionName());
        } catch (Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    void applyPreamble() throws IOException {
        // Given
        String filename = TestUtils.getSampleFilename("sample.dcm");
        DicomObject file = (DicomObject)fileOperations.readFile(filename);
        String tmpFolder = Files.createTempDirectory("dicom_file_").toString();

        try {
            // When
            TypedValue<OutputStream> output = fileOperations.applyPreamble(file, null);

            // Then
            Path outputFilename = Paths.get(tmpFolder, "sample.dcm");
            try (OutputStream outputStream = Files.newOutputStream(outputFilename, StandardOpenOption.CREATE_NEW)){
                ByteArrayOutputStream baos = (ByteArrayOutputStream)output.getValue();
                baos.writeTo(outputStream);
                baos.flush();
            } catch (Exception e) {
                Assertions.fail(e.toString());
            }
            DicomFileType dft = DicomFileType.parse(outputFilename.toString());
            Assertions.assertEquals(DicomFileType.DICOM, dft, "Output file is not a DICOM file type");
            StoreUtils.deleteFolder(tmpFolder);
        } catch (Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    void extractTags() {
        // Given
        String filename = TestUtils.getSampleFilename("sample.dcm");
        DicomObject file = (DicomObject)fileOperations.readFile(filename);
        List<String> tagNames = new ArrayList<>();
        tagNames.add("PatientID");
        tagNames.add("0x00080080"); // InstitutionName
        tagNames.add("0008,0020"); // StudyDate
        tagNames.add(Integer.toString(Tag.AccessionNumber));

        try {
            // When
            Map<String, DicomValue> tags = fileOperations.extractTags(file, tagNames);

            // Then
            Assertions.assertEquals("0050", tags.get("PatientID").toString());
            Assertions.assertEquals("St. Nowhere Hospital", tags.get("InstitutionName").toString());
            Assertions.assertEquals("20061219", tags.get("StudyDate").toString());
            Assertions.assertEquals("0050", tags.get("AccessionNumber").toString());
        } catch (Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    void extractAllTags() {
        // Given
        String filename = TestUtils.getSampleFilename("sample.dcm");

        try {
            // When
            DicomObject file = (DicomObject)fileOperations.readFile(filename);

            // Then
            Map<String, DicomValue> tags = fileOperations.extractTags(file, null);
            Assertions.assertEquals("0050", tags.get("PatientID").toString());
            Assertions.assertEquals("St. Nowhere Hospital", tags.get("InstitutionName").toString());
            Assertions.assertEquals("20061219", tags.get("StudyDate").toString());
            Assertions.assertEquals("0050", tags.get("AccessionNumber").toString());
        } catch (Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    void storeFile() throws IOException {
        // Given
        String filename = TestUtils.getSampleFilename("sample.dcm");
        DicomObject file = (DicomObject)fileOperations.readFile(filename);
        String tmpFolder = Files.createTempDirectory("dicom_file_").toString();

        try {
            // When
            String outputFilename = fileOperations.storeFile(tmpFolder, "sample.dcm",file, null);

            // Then
            DicomFileType dft = DicomFileType.parse(outputFilename);
            Assertions.assertEquals(DicomFileType.DICOM, dft, "Output file is not a DICOM file type");
            StoreUtils.deleteFolder(tmpFolder);
        } catch (Exception e) {
            Assertions.fail(e.toString());
        }
    }
}