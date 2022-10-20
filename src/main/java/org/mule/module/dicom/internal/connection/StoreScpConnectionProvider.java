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
import org.mule.module.dicom.internal.config.ScpType;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Alias("scp-connection")
public class StoreScpConnectionProvider implements ConnectionProvider<ScpConnection> {
  private static final Logger log = LoggerFactory.getLogger(StoreScpConnectionProvider.class);

  @ParameterGroup(name = "Local Server")
  private AetConnection aetConnection;
  @ParameterGroup(name = ConnectionBuffer.PARAMETER_GROUP, showInDsl = true)
  private ConnectionBuffer connectionBuffer;
  @ParameterGroup(name = ConnectionTimings.PARAMETER_GROUP, showInDsl = true)
  private ConnectionTimings connectionTimings;
  @Parameter
  @DisplayName("TLS Configuration")
  @Optional
  @Placement(tab = "TLS Context")
  private TlsContextFactory tlsContextFactory;

  @Inject
  private SchedulerService schedulerService;

  @Override
  public ScpConnection connect() {
    if (log.isTraceEnabled()) log.trace("Connecting {}", aetConnection);
    Scheduler scheduler = schedulerService.ioScheduler();
    if (tlsContextFactory instanceof Initialisable) {
      try {
        ((Initialisable) tlsContextFactory).initialise();
      } catch (InitialisationException e) {
        throw new ModuleException(DicomError.CLIENT_SECURITY, e);
      }
    }
    ScpConnection scpConnection = new ScpConnection(ScpType.STORE, aetConnection, tlsContextFactory, scheduler);
    Connection conn = scpConnection.getConnection();
    // Set Buffers
    conn.setMaxOpsInvoked(connectionBuffer.getMaxOpsInvoked());
    conn.setMaxOpsPerformed(connectionBuffer.getMaxOpsPerformed());
    conn.setReceivePDULength(connectionBuffer.getReceivePduLength());
    conn.setSendPDULength(connectionBuffer.getSendPduLength());
    conn.setSendBufferSize(connectionBuffer.getSendBufferSize());
    conn.setReceiveBufferSize(connectionBuffer.getReceiveBufferSize());
    // Set Timings
    conn.setConnectTimeout(connectionTimings.getConnectionTimeout());
    conn.setRequestTimeout(connectionTimings.getRequestTimeout());
    conn.setAcceptTimeout(connectionTimings.getAcceptTimeout());
    conn.setReleaseTimeout(connectionTimings.getReleaseTimeout());
    conn.setSendTimeout(connectionTimings.getSendTimeout());
    conn.setResponseTimeout(connectionTimings.getResponseTimeout());
    conn.setIdleTimeout(connectionTimings.getIdleTimeout());
    conn.setSocketCloseDelay(connectionTimings.getSocketCloseDelay());
    return scpConnection;
  }

  @Override
  public void disconnect(ScpConnection scpConnection) {
    if (log.isTraceEnabled()) log.trace("Disconnecting {}", aetConnection);
    scpConnection.stop();
  }

  @Override
  public ConnectionValidationResult validate(ScpConnection scpConnection) {
    if (log.isTraceEnabled()) log.trace("Validating {}", aetConnection);
    try {
      scpConnection.validate();
    } catch (IOException | GeneralSecurityException e) {
      return ConnectionValidationResult.failure(e.getMessage(), e);
    }
    return ConnectionValidationResult.success();
  }
}
