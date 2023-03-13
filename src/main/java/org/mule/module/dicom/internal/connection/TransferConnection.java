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
import org.mule.runtime.api.tls.TlsContextFactory;

public final class TransferConnection {
    private final ScuConnection sourceConnection;
    public ScuConnection getSourceConnection() { return sourceConnection; }

    private final String localAetName;
    private final AetConnection aetConnection;
    private final Security security;
    private final TlsContextFactory tlsContextFactory;
    private final ConnectionBuffer targetBuffer;
    private final ConnectionTimings targetTimings;

    public ScuConnection getTargetConnection() {
        ScuConnection targetConnection = new ScuConnection(localAetName, aetConnection, security, tlsContextFactory);
        Connection targetConn = targetConnection.getConnection();
        // Set Buffers
        targetConn.setMaxOpsInvoked(targetBuffer.getMaxOpsInvoked());
        targetConn.setMaxOpsPerformed(targetBuffer.getMaxOpsPerformed());
        targetConn.setReceivePDULength(targetBuffer.getReceivePduLength());
        targetConn.setSendPDULength(targetBuffer.getSendPduLength());
        targetConn.setSendBufferSize(targetBuffer.getSendBufferSize());
        targetConn.setReceiveBufferSize(targetBuffer.getReceiveBufferSize());
        // Set Timings
        targetConn.setConnectTimeout(targetTimings.getConnectionTimeout());
        targetConn.setRequestTimeout(targetTimings.getRequestTimeout());
        targetConn.setAcceptTimeout(targetTimings.getAcceptTimeout());
        targetConn.setReleaseTimeout(targetTimings.getReleaseTimeout());
        targetConn.setSendTimeout(targetTimings.getSendTimeout());
        targetConn.setResponseTimeout(targetTimings.getResponseTimeout());
        targetConn.setIdleTimeout(targetTimings.getIdleTimeout());
        targetConn.setSocketCloseDelay(targetTimings.getSocketCloseDelay());
        return targetConnection;
    }

    public TransferConnection(ScuConnection sourceConnection, String localAetName, AetConnection aetConnection, Security security,
                              TlsContextFactory tlsContextFactory, ConnectionBuffer targetBuffer, ConnectionTimings targetTimings) {
        this.sourceConnection = sourceConnection;
        this.localAetName = localAetName;
        this.aetConnection = aetConnection;
        this.security = security;
        this.tlsContextFactory = tlsContextFactory;
        this.targetBuffer = targetBuffer;
        this.targetTimings = targetTimings;
    }

    public void disconnect() {
        sourceConnection.disconnect();
    }
}
