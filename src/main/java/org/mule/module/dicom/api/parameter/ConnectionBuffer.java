/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.parameter;

import org.mule.runtime.extension.api.annotation.Ignore;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class ConnectionBuffer {
    @Ignore
    public static final String PARAMETER_GROUP = "Buffer";

    @Parameter
    @Summary("Max Operations Invoked. Defaults to 0 (unlimited)")
    @Optional(defaultValue = "0")
    @Placement(order = 1, tab = "Buffer")
    private int maxOpsInvoked;
    public int getMaxOpsInvoked() { return maxOpsInvoked; }
    public void setMaxOpsInvoked(int maxOpsInvoked) { this.maxOpsInvoked = maxOpsInvoked; }
    @Parameter
    @Summary("Max Operations Performed. Defaults to 0 (unlimited)")
    @Optional(defaultValue = "0")
    @Placement(order = 2, tab = "Buffer")
    private int maxOpsPerformed;
    public int getMaxOpsPerformed() { return maxOpsPerformed; }
    public void setMaxOpsPerformed(int maxOpsPerformed) { this.maxOpsPerformed = maxOpsPerformed; }
    @Parameter
    @DisplayName("Receive PDU Length")
    @Summary("Receive Protocol Data Units Length. Default is the max (16378)")
    @Optional(defaultValue = "16378")
    @Placement(order = 3, tab = "Buffer")
    private int receivePduLength;
    public int getReceivePduLength() { return receivePduLength; }
    public void setReceivePduLength(int receivePduLength) { this.receivePduLength = receivePduLength; }
    @Parameter
    @DisplayName("Send PDU Length")
    @Summary("Send Protocol Data Units Length. Default is the max (16378)")
    @Optional(defaultValue = "16378")
    @Placement(order = 4, tab = "Buffer")
    private int sendPduLength;
    public int getSendPduLength() { return sendPduLength; }
    public void setSendPduLength(int sendPduLength) { this.sendPduLength = sendPduLength; }
    @Parameter
    @Summary("Default is 0 (unlimited)")
    @Optional(defaultValue = "0")
    @Placement(order = 5, tab = "Buffer")
    private int sendBufferSize;
    public int getSendBufferSize() { return sendBufferSize; }
    public void setSendBufferSize(int sendBufferSize) { this.sendBufferSize = sendBufferSize; }
    @Parameter
    @Summary("Default is 0 (unlimited)")
    @Optional(defaultValue = "0")
    @Placement(order = 6, tab = "Buffer")
    private int receiveBufferSize;
    public int getReceiveBufferSize() { return receiveBufferSize; }
    public void setReceiveBufferSize(int receiveBufferSize) { this.receiveBufferSize = receiveBufferSize; }
}
