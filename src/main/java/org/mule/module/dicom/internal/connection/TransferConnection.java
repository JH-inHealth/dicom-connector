/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.connection;

public final class TransferConnection {
    private final ScuConnection sourceConnection;
    public ScuConnection getSourceConnection() { return sourceConnection; }
    private final ScuConnection targetConnection;
    public ScuConnection getTargetConnection() { return targetConnection; }

    public TransferConnection(ScuConnection sourceConnection, ScuConnection targetConnection) {
        this.sourceConnection = sourceConnection;
        this.targetConnection = targetConnection;
    }

    public void disconnect() {
        sourceConnection.disconnect();
        targetConnection.disconnect();
    }
}
