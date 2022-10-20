/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.test;

import org.junit.jupiter.api.*;
import org.mule.module.dicom.api.content.DicomValue;
import org.mule.module.dicom.api.content.EchoScuPayload;
import org.mule.module.dicom.api.content.FindScuPayload;
import org.mule.module.dicom.api.content.GetScuPayload;
import org.mule.module.dicom.api.parameter.*;
import org.mule.module.dicom.internal.connection.ScuConnection;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.module.dicom.internal.operation.ScuOperations;
import org.mule.module.dicom.internal.util.StoreUtils;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

class PublicServerTest {
    static final ScuOperations scuOperations = new ScuOperations();
    static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(8);
    static ScuConnection scuConnection;

    // Test Configurations
    static final String dicomFilenameDcm = TestUtils.getSampleFilename("sample.dcm");
    static final PresentationContext presentationContext = new PresentationContext();
    static final Timings timings = TestUtils.getTimings();
    static final StoreTimings storeTimings = TestUtils.getStoreTimings();
    static String tempDirectory = null;

    @BeforeAll
    static void setup() {
        final String localAetName = "JHU-MULESOFT";
        final AetConnection aetConnection = new AetConnection();
        aetConnection.setAetName("PUBLIC-SERVER");
        aetConnection.setHostname("www.dicomserver.co.uk");
        aetConnection.setPort(104);
        Security security = new Security();
        scuConnection = new ScuConnection(localAetName, aetConnection, security, null, scheduledExecutorService);

        // Test Configuration Data
        presentationContext.setInformationModel(InformationModel.STUDY_ROOT);
        presentationContext.setTransferSyntax(TransferSyntax.IMPLICIT_FIRST);
        try {
            tempDirectory = Files.createTempDirectory("dicom_").toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void tearDown() {
        scuConnection.disconnect();
        scheduledExecutorService.shutdown();
        if (tempDirectory != null) StoreUtils.deleteFolder(tempDirectory);
    }

    // Helper Method
    TagSearch getTagSearch(String patientId) {
        final TagSearch tagSearch = new TagSearch();
        tagSearch.setSearchKeys(new HashMap<>());
        tagSearch.getSearchKeys().put("PatientID", patientId);
        tagSearch.setResponseTags(new ArrayList<>());
        tagSearch.getResponseTags().add("AccessionNumber");
        return tagSearch;
    }

    StoreSearch getStoreSearch(String patientId, String accessionNumber) {
        StoreSearch search = new StoreSearch();
        Map<String, Object> searchKeys = new HashMap<>();
        searchKeys.put("PatientID", patientId);
        searchKeys.put("AccessionNumber", accessionNumber);
        search.setSearchKeys(searchKeys);
        SopClass[] sopClasses = new SopClass[1];
        sopClasses[0] = new SopClass();
        sopClasses[0].setSopClassUid("EnhancedCTImageStorage");
        sopClasses[0].setTransferSyntax(new String[]{"ImplicitVRLittleEndian", "ExplicitVRLittleEndian"});
        search.setSopClasses(sopClasses);
        return search;
    }

    @Test
    void echoScu() {
        // Given

        try {
            // When
            EchoScuPayload payload = scuOperations.echoScu(scuConnection, TestUtils.getTimings());

            // Then
            Assertions.assertEquals(1, payload.getMessageId());
        } catch (ModuleException e) {
            Assertions.fail(TestUtils.getStackTrace(e));
        } catch (Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    void findScu() {
        // Given
        TagSearch tagSearch = getTagSearch("PAT001");

        try {
            // When
            FindScuPayload findScuPayload = scuOperations.findScu(scuConnection, tagSearch, presentationContext, timings);

            // Then
            List<Map<String, DicomValue>> payload = findScuPayload.getResults();
            Assertions.assertEquals(1, payload.size());
            Assertions.assertEquals("123", payload.get(0).get("AccessionNumber").getAsString());
        } catch (ModuleException e) {
            Assertions.fail(TestUtils.getStackTrace(e));
        } catch (Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    void findScuNotFound() {
        // Given
        TagSearch tagSearch = getTagSearch("FOOBAR");

        try {
            // When
            FindScuPayload findScuPayload = scuOperations.findScu(scuConnection, tagSearch, presentationContext, timings);

            // Then
            Assertions.fail("Operation did not throw NOT_FOUND exception");
        } catch (ModuleException e) {
            Assertions.assertEquals(DicomError.NOT_FOUND, e.getType());
        } catch (Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    void storeScu() {
        // Given
        StoreImage storeImage = TestUtils.setStoreConfig(dicomFilenameDcm);

        try {
            // When
            List<String> actualList = scuOperations.storeScu(scuConnection, storeImage, false, null, timings);

            // Then
            Assertions.assertArrayEquals(new String[]{"1.3.6.1.4.1.5962.1.1.50.1.1.1166562673.14401"}, actualList.toArray(new String[]{}));
        } catch (ModuleException e) {
            Assertions.fail(TestUtils.getStackTrace(e));
        } catch (Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    void getScuEmptyResults() {
        // Given
        StoreSearch search = getStoreSearch("PAT001", "123");

        try {
            // When
            scuOperations.getScu(scuConnection, tempDirectory, false, search, presentationContext, storeTimings, null);

            // Then
            Assertions.fail("Operation did not throw NOT_FOUND exception");
        } catch (ModuleException e) {
            Assertions.assertEquals(DicomError.NOT_FOUND, e.getType());
        } catch (Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    void getScuNotFound() {
        // Given
        StoreSearch search = getStoreSearch("FOOBAR", "000");

        try {
            // When
            GetScuPayload payload = scuOperations.getScu(scuConnection, tempDirectory, false, search, presentationContext, storeTimings, null);

            // Then
            Assertions.assertNull(payload);
        } catch (ModuleException e) {
            Assertions.assertEquals(DicomError.NOT_FOUND, e.getType());
        } catch (Exception e) {
            Assertions.fail(e.toString());
        }
    }}