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
import org.mule.module.dicom.api.parameter.Security;
import org.mule.module.dicom.api.parameter.Timings;
import org.mule.module.dicom.internal.config.ScpType;
import org.mule.module.dicom.internal.connection.ScpConnection;
import org.mule.module.dicom.internal.connection.ScuConnection;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.module.dicom.internal.operation.FileOperations;
import org.mule.module.dicom.internal.operation.ScuOperations;
import org.mule.module.dicom.internal.store.MuleProcessStore;
import org.mule.runtime.api.lifecycle.CreateException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

class SecurityTest {
    static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(8);
    static TestSourceCallback sourceCallback = new TestSourceCallback();

    static final FileOperations fileOperations = new FileOperations();
    static final ScuOperations scuOperations = new ScuOperations();
    static final String scuAetName = "JHU-CLIENT";
    static final AetConnection aetConnection = new AetConnection();
    static final Security security = new Security();
    static final Timings timings = TestUtils.getTimings();

    @BeforeAll
    static void setup() {
        aetConnection.setAetName("JHU-SERVER");
        aetConnection.setHostname("0.0.0.0");
        aetConnection.setPort(2762);
    }

    @AfterAll
    static void tearDown() {
        scheduledExecutorService.shutdown();
    }

    private static final String STORE_PWD = "changeit";
    private static final String KEY_PWD = "localhost";
    private static final String STORE_TYPE = "pkcs12";
    private static final String SCP_KEYSTORE = "scp-keystore.jks";
    private static final String SCP_TRUSTSTORE = "scp-truststore.jks";
    private static final String SCU_KEYSTORE = "scu-keystore.jks";
    private static final String SCU_TRUSTSTORE = "scu-truststore.jks";

    /**
     * Both server and client require SSL Certificates
     */
    @Test
    void fullTrustSsl() {
        ScpConnection scpConnection = null;
        ScuConnection scuConnection = null;
        try {
            TlsContextFactory scpTlsContextFactory = TlsContextFactory.builder()
                    .keyStorePath(SCP_KEYSTORE).keyStorePassword(STORE_PWD).keyPassword(KEY_PWD).keyStoreType(STORE_TYPE) // Must have the SCP private key
                    .trustStorePath(SCP_TRUSTSTORE).trustStorePassword(STORE_PWD).trustStoreType(STORE_TYPE) // Must have the SCU public cert
                    .insecureTrustStore(false) // This makes the SCP require the client SSL certificate
                    .build();
            ((Initialisable) scpTlsContextFactory).initialise();
            scpConnection = new ScpConnection(ScpType.STORE, aetConnection, scpTlsContextFactory, scheduledExecutorService);
            MuleProcessStore fileStore = new MuleProcessStore(sourceCallback);
            scpConnection.start(fileStore, null);
            TlsContextFactory scuTlsContextFactory = TlsContextFactory.builder()
                    .keyStorePath(SCU_KEYSTORE).keyStorePassword(STORE_PWD).keyPassword(KEY_PWD).keyStoreType(STORE_TYPE) // Must have the SCU private key
                    .trustStorePath(SCU_TRUSTSTORE).trustStorePassword(STORE_PWD).trustStoreType(STORE_TYPE) // Must have the SCP public cert
                    .insecureTrustStore(false) // Ignored by the SCU
                    .build();
            ((Initialisable) scuTlsContextFactory).initialise();
            scuConnection = new ScuConnection(scuAetName, aetConnection, security, scuTlsContextFactory, scheduledExecutorService);

            try {
                scuOperations.echoScu(scuConnection, timings);
            } catch (ModuleException ex) {
                Assertions.fail(TestUtils.getStackTrace(ex));
            }
        } catch (GeneralSecurityException | InitialisationException | IOException | CreateException e) {
            Assertions.fail(e.toString());
        } finally {
            if (scpConnection != null) scpConnection.stop();
            if (scuConnection != null) scuConnection.disconnect();
        }
    }

    /**
     * Only the SCP requires an SSL Certificate.
     */
    @Test
    void providerSsl() {
        ScpConnection scpConnection = null;
        ScuConnection scuConnection = null;
        try {
            TlsContextFactory scpTlsContextFactory = TlsContextFactory.builder()
                    .keyStorePath(SCP_KEYSTORE).keyStorePassword(STORE_PWD).keyPassword(KEY_PWD).keyStoreType(STORE_TYPE) // Must have the SCP private key
                    .trustStorePath(SCP_KEYSTORE).trustStorePassword(STORE_PWD).trustStoreType(STORE_TYPE) // Must be defined, but won't be used
                    .insecureTrustStore(true) // This allows the SCU to communicate without its own private key
                    .build();
            ((Initialisable) scpTlsContextFactory).initialise();
            scpConnection = new ScpConnection(ScpType.STORE, aetConnection, scpTlsContextFactory, scheduledExecutorService);
            MuleProcessStore fileStore = new MuleProcessStore(sourceCallback);
            scpConnection.start(fileStore, null);
            TlsContextFactory scuTlsContextFactory = TlsContextFactory.builder()
                    .trustStorePath(SCU_TRUSTSTORE).trustStorePassword(STORE_PWD).trustStoreType(STORE_TYPE) // Must have the SCP public cert
                    .insecureTrustStore(false) // Ignored by the SCU
                    .build();
            ((Initialisable) scuTlsContextFactory).initialise();
            scuConnection = new ScuConnection(scuAetName, aetConnection, security, scuTlsContextFactory, scheduledExecutorService);

            try {
                scuOperations.echoScu(scuConnection, timings);
            } catch (ModuleException ex) {
                Assertions.fail(TestUtils.getStackTrace(ex));
            }
        } catch (GeneralSecurityException | InitialisationException | IOException | CreateException e) {
            Assertions.fail(e.toString());
        } finally {
            if (scpConnection != null) scpConnection.stop();
            if (scuConnection != null) scuConnection.disconnect();
        }
    }

    @Test
    void badClientSslCertificate() {
        ScpConnection scpConnection = null;
        ScuConnection scuConnection = null;
        try {
            TlsContextFactory scpTlsContextFactory = TlsContextFactory.builder()
                    .keyStorePath(SCP_KEYSTORE).keyStorePassword(STORE_PWD).keyPassword(KEY_PWD).keyStoreType(STORE_TYPE) // Must have the SCP private key
                    .trustStorePath(SCP_TRUSTSTORE).trustStorePassword(STORE_PWD).trustStoreType(STORE_TYPE) // Expecting the SCU public cert
                    .insecureTrustStore(false) // This makes the SCP require the client SSL certificate
                    .build();
            ((Initialisable) scpTlsContextFactory).initialise();
            scpConnection = new ScpConnection(ScpType.STORE, aetConnection, scpTlsContextFactory, scheduledExecutorService);
            MuleProcessStore fileStore = new MuleProcessStore(sourceCallback);
            scpConnection.start(fileStore, null);
            TlsContextFactory scuTlsContextFactory = TlsContextFactory.builder()
                    .keyStorePath(SCP_KEYSTORE).keyStorePassword(STORE_PWD).keyPassword(KEY_PWD).keyStoreType(STORE_TYPE) // Use the wrong client certificate
                    .trustStorePath(SCU_TRUSTSTORE).trustStorePassword(STORE_PWD).trustStoreType(STORE_TYPE) // Must have the SCP public cert
                    .insecureTrustStore(false) // Ignored by the SCU
                    .build();
            ((Initialisable) scuTlsContextFactory).initialise();
            scuConnection = new ScuConnection(scuAetName, aetConnection, security, scuTlsContextFactory, scheduledExecutorService);

            try {
                scuOperations.echoScu(scuConnection, timings);
            } catch (ModuleException ex) {
                Assertions.assertEquals(DicomError.SSL, ex.getType());
            }
        } catch (GeneralSecurityException | InitialisationException | IOException | CreateException e) {
            Assertions.fail(e.toString());
        } finally {
            if (scpConnection != null) scpConnection.stop();
            if (scuConnection != null) scuConnection.disconnect();
        }
    }
}
