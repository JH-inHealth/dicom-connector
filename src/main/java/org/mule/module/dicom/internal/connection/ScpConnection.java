/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.connection;

import org.dcm4che3.tool.common.CLIUtils;
import org.mule.module.dicom.api.parameter.AetConnection;
import org.mule.module.dicom.api.parameter.SopClass;
import org.mule.module.dicom.internal.config.ScpType;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.module.dicom.internal.store.MuleCStoreSCP;
import org.mule.module.dicom.internal.store.MuleNullStore;
import org.mule.module.dicom.internal.store.MuleStore;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.TransferCapability;
import org.mule.module.dicom.internal.util.SecurityUtils;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ScheduledExecutorService;

public final class ScpConnection {
    private static final Logger log = LoggerFactory.getLogger(ScpConnection.class);
    private volatile boolean started;
    private final Device device;
    private final Connection connection;
    private final ApplicationEntity ae;
    public Connection getConnection() { return connection; }
    private final MuleCStoreSCP cStoreSCP;

    public ScpConnection(ScpType name, AetConnection aetConnection, TlsContextFactory tlsContextFactory, ScheduledExecutorService scheduledExecutorService) {
        started = false;
        device = new Device(name.getName() + "SCP");
        connection = new Connection();
        cStoreSCP = new MuleCStoreSCP();

        connection.setReceivePDULength(Connection.DEF_MAX_PDU_LENGTH);
        connection.setSendPDULength(Connection.DEF_MAX_PDU_LENGTH);
        connection.setPackPDV(true);
        connection.setTcpNoDelay(true);
        connection.setPort(aetConnection.getPort());
        connection.setHostname(aetConnection.getHostname());
        // Default connection settings
        connection.setReceivePDULength(16378);
        connection.setSendPDULength(16378);
        connection.setMaxOpsInvoked(0);
        connection.setMaxOpsPerformed(0);
        connection.setConnectTimeout(0);
        connection.setRequestTimeout(0);
        connection.setAcceptTimeout(0);
        connection.setReleaseTimeout(0);
        connection.setSendTimeout(0);
        connection.setStoreTimeout(0);
        connection.setResponseTimeout(0);
        connection.setIdleTimeout(0);
        connection.setSocketCloseDelay(50);
        connection.setSendBufferSize(0);
        connection.setReceiveBufferSize(0);
        if (tlsContextFactory != null) {
            try {
                SecurityUtils.setTls(device, connection, tlsContextFactory);
            } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException |
                     IOException | UnrecoverableKeyException e) {
                throw new ModuleException(DicomError.SERVER_SECURITY, e);
            }
        }

        // Create the Application Entity
        ae = new ApplicationEntity(aetConnection.getAetName());
        ae.setAssociationAcceptor(true);
        ae.addConnection(connection);

        // Configure the Device
        device.setDimseRQHandler(cStoreSCP.createServiceRegistry());
        device.addConnection(connection);
        device.addApplicationEntity(ae);
        device.setExecutor(scheduledExecutorService);
        device.setScheduledExecutor(scheduledExecutorService);
    }

    public void start(MuleStore store, SopClass[] sopClasses) throws IOException, GeneralSecurityException {
        log.trace("Starting");
        cStoreSCP.setStore(store);
        if (sopClasses == null || sopClasses.length == 0) {
            // Accept all transfer types
            ae.addTransferCapability(new TransferCapability(null, "*", TransferCapability.Role.SCP, UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian));
        } else {
            for (SopClass sopClass : sopClasses) {
                log.trace("Adding SOP {}", sopClass.getSopClassUid());
                setSOP(sopClass.getSopClassUid(), sopClass.getTransferSyntax());
            }
        }
        started = true;
        device.bindConnections();
    }

    private void setSOP(String cuid, String... tsuids) {
        // Convert names to UID's if necessary
        if (!cuid.contains(".")) cuid = CLIUtils.toUID(cuid);
        for(int i=0; i<tsuids.length; i++) {
            if (!tsuids[i].contains(".")) tsuids[i] = CLIUtils.toUID(tsuids[i]);
        }
        TransferCapability tc = new TransferCapability(null, cuid, TransferCapability.Role.SCP, tsuids);
        ae.addTransferCapability(tc);
    }

    public void stop() {
        log.trace("Stopping");
        device.unbindConnections();
        cStoreSCP.setStore(new MuleNullStore());
        started = false;
        device.setScheduledExecutor(null);
        device.setExecutor(null);
    }

    public void validate() throws GeneralSecurityException, IOException {
        log.trace("Validating");
        if (!started) {
            device.bindConnections();
            device.unbindConnections();
        }
    }
}
