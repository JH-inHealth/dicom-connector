/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.connection;

import org.dcm4che3.net.pdu.RoleSelection;
import org.mule.module.dicom.api.parameter.AetConnection;
import org.mule.module.dicom.api.parameter.Security;
import org.mule.module.dicom.api.parameter.SopClass;
import org.mule.module.dicom.internal.config.ScuType;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.module.dicom.internal.store.MuleNullStore;
import org.mule.module.dicom.internal.store.MuleStore;
import org.mule.module.dicom.internal.util.SecurityUtils;
import org.mule.module.dicom.internal.util.SopUtils;
import org.mule.module.dicom.internal.store.MuleCStoreSCP;
import org.mule.module.dicom.internal.config.ScuOperationConfig;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.UserIdentityRQ;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ScuConnection {
    private static final Logger log = LoggerFactory.getLogger(ScuConnection.class);
    private final Device device;
    private final Connection connection;
    public Connection getConnection() { return connection; }
    private final Connection remote;
    private final MuleCStoreSCP cStoreSCP;
    private final ApplicationEntity ae;
    private final AAssociateRQ rq;
    private Association as;
    private ScheduledFuture<?> scheduledCancel;
    private ScuOperationConfig scuOperationConfig;
    private boolean localExecutor = false;

    public ScuConnection(String localAetName, AetConnection aetConnection, Security security, TlsContextFactory tlsContextFactory) {
        this(localAetName, aetConnection, security, tlsContextFactory, Executors.newSingleThreadScheduledExecutor());
        localExecutor = true;
    }

    public ScuConnection(String localAetName, AetConnection aetConnection, Security security, TlsContextFactory tlsContextFactory, ScheduledExecutorService scheduledExecutorService) {
        as = null;
        scheduledCancel = null;

        connection = new Connection();
        connection.setReceivePDULength(16378);
        connection.setSendPDULength(16378);
        connection.setMaxOpsInvoked(1);
        connection.setMaxOpsPerformed(0);
        connection.setPackPDV(true);
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
        connection.setTcpNoDelay(true);

        ae = new ApplicationEntity(localAetName);
        ae.addConnection(connection);

        cStoreSCP = new MuleCStoreSCP();

        device = new Device("MuleSCU");
        device.addConnection(connection);
        device.addApplicationEntity(ae);
        device.setDimseRQHandler(cStoreSCP.createServiceRegistry());
        device.setExecutor(scheduledExecutorService);
        device.setScheduledExecutor(scheduledExecutorService);

        remote = new Connection();
        remote.setHostname(aetConnection.getHostname());
        remote.setPort(aetConnection.getPort());
        remote.setHttpProxy(null);

        if (tlsContextFactory != null) {
            try {
                SecurityUtils.setTls(device, remote, tlsContextFactory);
                SecurityUtils.setTls(null, connection, tlsContextFactory);
            } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException |
                     IOException | UnrecoverableKeyException e) {
                throw new ModuleException(DicomError.CLIENT_SECURITY, e);
            }
        }

        rq = new AAssociateRQ();
        rq.setCalledAET(aetConnection.getAetName());
        if (security.hasUsernamePassword()) {
            UserIdentityRQ identity = UserIdentityRQ.usernamePasscode(security.getUsername(), security.getPassword().toCharArray(), true);
            rq.setUserIdentityRQ(identity);
        }
    }

    public void disconnect() {
        if (localExecutor) {
            ScheduledExecutorService scheduledExecutor = device.getScheduledExecutor();
            if (scheduledExecutor != null) {
                scheduledExecutor.shutdown();
                try {
                    if (!scheduledExecutor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                        scheduledExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduledExecutor.shutdownNow();
                    Thread t = Thread.currentThread();
                    t.getUncaughtExceptionHandler().uncaughtException(t, e);
                    t.interrupt();
                }
            }
        }
        device.setScheduledExecutor(null);
        device.setExecutor(null);
    }

    public void start(ScuOperationConfig scuOperationConfig, MuleStore store) throws IOException, GeneralSecurityException {
        this.scuOperationConfig = scuOperationConfig;
        if (log.isTraceEnabled()) log.trace("Starting C-{}: {} >> {}@{}:{}", scuOperationConfig.getOperation(), ae.getAETitle(), rq.getCalledAET(), remote.getHostname(), remote.getPort());

        // Remove any existing presentation contexts from the request
        List<RoleSelection> rsList = new ArrayList<>(rq.getRoleSelections());
        for (RoleSelection rs : rsList) {
            String cuid = rs.getSOPClassUID();
            rq.removeRoleSelectionFor(cuid);
            log.trace("Removed Role Selection {}", cuid);
        }
        List<PresentationContext> pcList = new ArrayList<>(rq.getPresentationContexts());
        for (PresentationContext pc : pcList) {
            rq.removePresentationContext(pc);
            log.trace("Removed Presentation Syntax {}", pc.getPCID());
        }
        // Set the new presentation contexts
        if (scuOperationConfig.getOperation() == ScuType.STORE) {
            int pcid = 1;
            String cuid = scuOperationConfig.getInformationModelCuid();
            String tsuid = scuOperationConfig.getTransferSyntaxCodes()[0];
            rq.addPresentationContext(new PresentationContext(pcid++, UID.Verification, tsuid));
            rq.addPresentationContext(new PresentationContext(pcid++, cuid, scuOperationConfig.getTransferSyntaxCodes()));
            if (!tsuid.equals(UID.ExplicitVRLittleEndian)) rq.addPresentationContext(new PresentationContext(pcid++, cuid, UID.ExplicitVRLittleEndian));
            if (!tsuid.equals(UID.ImplicitVRLittleEndian)) rq.addPresentationContext(new PresentationContext(pcid, cuid, UID.ImplicitVRLittleEndian));
        } else {
            PresentationContext pc = new PresentationContext(1, scuOperationConfig.getInformationModelCuid(), scuOperationConfig.getTransferSyntaxCodes());
            rq.addPresentationContext(pc);
            SopClass[] sopClasses = scuOperationConfig.getSopClasses();
            if (sopClasses == null || sopClasses.length == 0) {
                log.debug("Using Default SOP Classes");
                SopUtils.setDefaultSOP(rq);
            } else {
                for (SopClass sopClass : sopClasses) {
                    log.trace("Adding SOP {}", sopClass.getSopClassUid());
                    SopUtils.setSOP(rq, sopClass.getSopClassUid(), sopClass.getTransferSyntax());
                }
            }
        }

        // Set the storage
        if (store == null) cStoreSCP.setStore(new MuleNullStore());
        else cStoreSCP.setStore(store);

        // Create a new association and start it
        try {
            this.as = this.ae.connect(this.connection, this.remote, this.rq);
        } catch (InterruptedException e) {
            Thread t = Thread.currentThread();
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
            t.interrupt();
        } catch (IncompatibleConnectionException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public void stop() {
        if (scuOperationConfig == null) return;
        if (log.isTraceEnabled()) log.trace("Stopping C-{}: {} >> {}@{}:{}", scuOperationConfig.getOperation(), ae.getAETitle(), rq.getCalledAET(), remote.getHostname(), remote.getPort());
        // Release if active
        if (this.as != null && this.as.isReadyForDataTransfer()) {
            try {
                if (this.scheduledCancel != null && !this.scheduledCancel.isDone()) {
                    log.trace("Canceling the scheduled cancel");
                    this.scheduledCancel.cancel(true);
                    this.as.waitForOutstandingRSP();
                } else if (this.scheduledCancel == null) {
                    this.as.waitForOutstandingRSP();
                }
                this.as.release();
            } catch (InterruptedException e) {
                Thread t = Thread.currentThread();
                t.getUncaughtExceptionHandler().uncaughtException(t, e);
                t.interrupt();
            } catch (Exception e) {
                log.trace("Ignored exception {}", e.toString());
            }
            this.as = null;
        }
    }

    public MuleDimseRSPHandler execute(Attributes data, String iuid) throws IOException {
        if (this.as == null) return null;
        final Association association = this.as;
        final MuleDimseRSPHandler rspHandler = new MuleDimseRSPHandler(association.nextMessageID());
        String cuid = scuOperationConfig.getInformationModelCuid();
        try {
            switch (scuOperationConfig.getOperation()) {
                case MOVE:
                    String aet = this.ae.getAETitle();
                    association.cmove(cuid, 0, data, null, aet, rspHandler);
                    break;
                case FIND:
                    association.cfind(cuid, 0, data, null, rspHandler);
                    break;
                case GET:
                    association.cget(cuid, 0, data, null, rspHandler);
                    break;
                case STORE:
                    String tsuid = scuOperationConfig.getTransferSyntaxCodes()[0];
                    association.cstore(cuid, iuid, 0, new DataWriterAdapter(data), tsuid, rspHandler);
                    break;
                case ECHO:
                    association.cecho(cuid);
                    break;
            }
            int cancelAfter = scuOperationConfig.getCancelAfter();
            if (cancelAfter > 0) {
                this.scheduledCancel = device.schedule(() -> {
                    try {
                        if (log.isWarnEnabled()) log.warn("Canceling C-{}: {} >> {}@{}:{}", scuOperationConfig.getOperation(), ae.getAETitle(), rq.getCalledAET(), remote.getHostname(), remote.getPort());
                        rspHandler.cancel(association);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }

                }, cancelAfter, TimeUnit.MILLISECONDS);
            }
            if (association.isReadyForDataTransfer()) {
                association.waitForOutstandingRSP();
            }
            return rspHandler;
        } catch (InterruptedException e) {
            Thread t = Thread.currentThread();
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
            t.interrupt();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%s->%s", rq.getCallingAET(), rq.getCalledAET());
    }
}
