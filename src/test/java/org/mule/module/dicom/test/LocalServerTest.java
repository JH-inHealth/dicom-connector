/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mule.module.dicom.api.parameter.AetConnection;
import org.mule.module.dicom.api.parameter.StoreImage;
import org.mule.module.dicom.api.parameter.Security;
import org.mule.module.dicom.api.parameter.Timings;
import org.mule.module.dicom.internal.config.ScpType;
import org.mule.module.dicom.internal.connection.ScpConnection;
import org.mule.module.dicom.internal.connection.ScuConnection;
import org.mule.module.dicom.internal.operation.FileOperations;
import org.mule.module.dicom.internal.operation.ScuOperations;
import org.mule.module.dicom.internal.store.MuleProcessStore;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

class LocalServerTest {
    // Test Connections
    static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(8);
    static ScpConnection scpConnection;
    static TestSourceCallback sourceCallback = new TestSourceCallback();
    static final FileOperations fileOperations = new FileOperations();
    static final ScuOperations scuOperations = new ScuOperations();
    static ScuConnection scuConnection;

    // Test Variables
    static final String dicomFilenameDcm = TestUtils.getSampleFilename("sample.dcm");
    static final String dicomFilenameTgz = TestUtils.getSampleFilename("sample.tar.gz");
    static final Map<String, String> changeTags = new HashMap<>();
    static final Timings timings = TestUtils.getTimings();

    @BeforeAll
    static void setup() throws GeneralSecurityException, IOException {
        final String scuAetName = "JHU-CLIENT";
        final AetConnection aetConnection = new AetConnection();
        aetConnection.setAetName("JHU-SERVER");
        aetConnection.setHostname("0.0.0.0");
        aetConnection.setPort(104);
        Security security = new Security();

        scpConnection = new ScpConnection(ScpType.STORE, aetConnection, null, scheduledExecutorService);
        MuleProcessStore fileStore = new MuleProcessStore(sourceCallback);
        scpConnection.start(fileStore, null);
        scuConnection = new ScuConnection(scuAetName, aetConnection, security, null, scheduledExecutorService);

        // Setup Test Variable Content
        changeTags.put("0x67810010", "JohnsHopkinsMedicine");
        changeTags.put("0x67811000", "UnitTest");
    }

    @AfterAll
    static void tearDown() {
        if (scpConnection != null) scpConnection.stop();
        if (scuConnection != null) scuConnection.disconnect();
        scheduledExecutorService.shutdown();
    }

    @Test
    void storeScuFilename() {
        // Given
        StoreImage storeImage = TestUtils.setStoreConfig(dicomFilenameDcm);

        try {
            // When
            List<String> actualList = scuOperations.storeScu(scuConnection, storeImage, false, changeTags, timings);

            // Then
            Assertions.assertArrayEquals(new String[]{"1.3.6.1.4.1.5962.1.1.50.1.1.1166562673.14401"}, actualList.toArray(new String[0]));
        } catch (ModuleException ex) {
            Assertions.fail(TestUtils.getStackTrace(ex));
        } catch (Exception e) {
            Assertions.fail(e.toString());
        } finally {
            sourceCallback.clear();
        }
    }

    @Test
    void storeScuGzip() {
        // Given
        StoreImage storeImage = TestUtils.setStoreConfig(dicomFilenameTgz);

        try {
            // When
            List<String> actualList = scuOperations.storeScu(scuConnection, storeImage, false, changeTags, timings);

            // Then
            Assertions.assertArrayEquals(new String[]{
                    "1.3.6.1.4.1.5962.1.1.50.1.1.1166562673.14401",
                    "1.3.6.1.4.1.5962.1.1.70.2.1.5.1166562673.14401",
                    "1.3.6.1.4.1.5962.1.1.110.1.1.1166562673.14401"
            }, actualList.toArray(new String[0]));
        } catch (ModuleException ex) {
            Assertions.fail(TestUtils.getStackTrace(ex));
        } catch (Exception e) {
            Assertions.fail(e.toString());
        } finally {
            sourceCallback.clear();
        }
    }

    @Test
    void storeScuContent() {
        // Given
        StoreImage storeImage = new StoreImage();
        Object dicomObject = fileOperations.readFile(dicomFilenameDcm);
        storeImage.setDicomObject(dicomObject);
        storeImage.setFileName(null);
        storeImage.setFolderName(null);
        storeImage.setListOfFiles(null);

        try {
            // When
            List<String> actualList = scuOperations.storeScu(scuConnection, storeImage, false, changeTags, timings);

            // Then
            Assertions.assertArrayEquals(new String[]{"1.3.6.1.4.1.5962.1.1.50.1.1.1166562673.14401"}, actualList.toArray(new String[0]));
        } catch (ModuleException ex) {
            Assertions.fail(TestUtils.getStackTrace(ex));
        } catch (Exception e) {
            Assertions.fail(e.toString());
        } finally {
            sourceCallback.clear();
        }
    }
}