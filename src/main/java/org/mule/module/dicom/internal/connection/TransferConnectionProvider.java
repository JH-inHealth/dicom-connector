/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.connection;

import org.dcm4che3.net.Connection;
import org.mule.module.dicom.api.parameter.AetConnection;
import org.mule.module.dicom.api.parameter.ConnectionBuffer;
import org.mule.module.dicom.api.parameter.ConnectionTimings;
import org.mule.module.dicom.api.parameter.Security;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Ignore;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@Alias("transfer-connection")
public class TransferConnectionProvider implements CachedConnectionProvider<TransferConnection> {
    private static final Logger log = LoggerFactory.getLogger(TransferConnectionProvider.class);
    @Ignore
    private static final String SOURCE_PARAMETER_GROUP = "Source Server";
    @Ignore
    private static final String TARGET_PARAMETER_GROUP = "Target Server";

    @Parameter
    @DisplayName("Local AE Title")
    @Summary("Application Entity Title")
    private String localAetName;

    @Alias("source")
    @ParameterGroup(name = SOURCE_PARAMETER_GROUP, showInDsl = true)
    private AetConnection sourceAetConnection;

    @Alias("target")
    @ParameterGroup(name = TARGET_PARAMETER_GROUP, showInDsl = true)
    private AetConnection targetAetConnection;

    @Alias("source")
    @ParameterGroup(name = SOURCE_PARAMETER_GROUP + " " + Security.PARAMETER_GROUP, showInDsl = true)
    private Security sourceSecurity;

    @Alias("target")
    @ParameterGroup(name = TARGET_PARAMETER_GROUP + " " + Security.PARAMETER_GROUP, showInDsl = true)
    private Security targetSecurity;

    @Alias("source")
    @ParameterGroup(name = SOURCE_PARAMETER_GROUP + " " + ConnectionBuffer.PARAMETER_GROUP, showInDsl = true)
    private ConnectionBuffer sourceBuffer;
    @Alias("target")
    @ParameterGroup(name = TARGET_PARAMETER_GROUP + " " + ConnectionBuffer.PARAMETER_GROUP, showInDsl = true)
    private ConnectionBuffer targetBuffer;

    @Alias("source")
    @ParameterGroup(name = SOURCE_PARAMETER_GROUP + " " + ConnectionTimings.PARAMETER_GROUP, showInDsl = true)
    private ConnectionTimings sourceTimings;
    @Alias("target")
    @ParameterGroup(name = TARGET_PARAMETER_GROUP + " " + ConnectionTimings.PARAMETER_GROUP, showInDsl = true)
    private ConnectionTimings targetTimings;

    @Parameter
    @DisplayName("TLS Configuration")
    @Optional
    @Placement(tab = "TLS Context")
    private TlsContextFactory tlsContextFactory;

    @Parameter
    @DisplayName("Use TLS Context in Source")
    @Placement(tab = "TLS Context")
    @Optional(defaultValue = "false")
    private boolean useTlsInSource;

    @Parameter
    @DisplayName("Use TLS Context in Target")
    @Placement(tab = "TLS Context")
    @Optional(defaultValue = "false")
    private boolean useTlsInTarget;

    @Inject
    private SchedulerService schedulerService;

    @Override
    public TransferConnection connect() {
        if (log.isTraceEnabled()) log.trace("Connecting {}->{}", localAetName, sourceAetConnection);
        if (log.isTraceEnabled()) log.trace("Connecting {}->{}", localAetName, targetAetConnection);
        Scheduler scheduler = schedulerService.ioScheduler();
        if (tlsContextFactory instanceof Initialisable) {
            try {
                ((Initialisable) tlsContextFactory).initialise();
            } catch (InitialisationException e) {
                throw new ModuleException(DicomError.CLIENT_SECURITY, e);
            }
        }
        TlsContextFactory sourceTlsContext = useTlsInSource ? tlsContextFactory : null;
        TlsContextFactory targetTlsContext = useTlsInTarget ? tlsContextFactory : null;

        ScuConnection sourceConnection = new ScuConnection(localAetName, sourceAetConnection, sourceSecurity, sourceTlsContext, scheduler);
        Connection sourceConn = sourceConnection.getConnection();
        // Set Buffers
        sourceConn.setMaxOpsInvoked(sourceBuffer.getMaxOpsInvoked());
        sourceConn.setMaxOpsPerformed(sourceBuffer.getMaxOpsPerformed());
        sourceConn.setReceivePDULength(sourceBuffer.getReceivePduLength());
        sourceConn.setSendPDULength(sourceBuffer.getSendPduLength());
        sourceConn.setSendBufferSize(sourceBuffer.getSendBufferSize());
        sourceConn.setReceiveBufferSize(sourceBuffer.getReceiveBufferSize());
        // Set Timings
        sourceConn.setConnectTimeout(sourceTimings.getConnectionTimeout());
        sourceConn.setRequestTimeout(sourceTimings.getRequestTimeout());
        sourceConn.setAcceptTimeout(sourceTimings.getAcceptTimeout());
        sourceConn.setReleaseTimeout(sourceTimings.getReleaseTimeout());
        sourceConn.setSendTimeout(sourceTimings.getSendTimeout());
        sourceConn.setResponseTimeout(sourceTimings.getResponseTimeout());
        sourceConn.setIdleTimeout(sourceTimings.getIdleTimeout());
        sourceConn.setSocketCloseDelay(sourceTimings.getSocketCloseDelay());

        return new TransferConnection(sourceConnection, localAetName, targetAetConnection, targetSecurity, targetTlsContext, targetBuffer, targetTimings);
    }

    @Override
    public void disconnect(TransferConnection transferConnection) {
        if (log.isTraceEnabled()) log.trace("Disconnecting {}->{}", localAetName, sourceAetConnection);
        if (log.isTraceEnabled()) log.trace("Disconnecting {}->{}", localAetName, targetAetConnection);
        transferConnection.disconnect();
    }

    @Override
    public ConnectionValidationResult validate(TransferConnection transferConnection) {
        if (log.isTraceEnabled()) log.trace("Validating {}->{}", localAetName, sourceAetConnection);
        if (log.isTraceEnabled()) log.trace("Validating {}->{}", localAetName, targetAetConnection);
        return ConnectionValidationResult.success();
    }
}
