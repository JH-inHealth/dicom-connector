/**
 * Copyright (c) 2023 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 * This class is used to perform C-STORE operations to a target AE during a transfer. It gets called from a thread pool
 * inside the MuleTransferStore as each C-GET image is received from the source.
 */
package org.mule.module.dicom.internal.operation;

import org.dcm4che3.data.Attributes;
import org.mule.module.dicom.internal.config.ScuOperationConfig;
import org.mule.module.dicom.internal.connection.ScuConnection;
import org.mule.module.dicom.internal.connection.TransferConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class TransferScu implements Callable<String> {
    private final TransferConnection connection;
    private final ScuOperationConfig scuOperationConfig;
    private final Attributes data;

    public TransferScu(TransferConnection connection, ScuOperationConfig scuOperationConfig, Attributes data) {
        this.connection = connection;
        this.scuOperationConfig = scuOperationConfig;
        this.data = data;
    }

    @Override
    public String call() {
        List<String> iuidList = new ArrayList<>();
        ScuConnection scuConnection = connection.getTargetConnection();
        try {
            StoreScu.execute(scuConnection, scuOperationConfig, data, null, iuidList);
        } finally {
            scuConnection.disconnect();
        }
        return iuidList.remove(0);
    }
}
