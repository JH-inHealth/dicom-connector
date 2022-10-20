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
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class ConnectionTimings {
    @Ignore
    public static final String PARAMETER_GROUP = "Timings";

    @Parameter
    @Summary("Default is 0 (unlimited)")
    @Optional(defaultValue = "0")
    @Placement(order = 1, tab = "Timings")
    private int connectionTimeout;
    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    @Parameter
    @Summary("Default is 0 (unlimited)")
    @Optional(defaultValue = "0")
    @Placement(order = 2, tab = "Timings")
    private int requestTimeout;
    public int getRequestTimeout() { return requestTimeout; }
    public void setRequestTimeout(int requestTimeout) { this.requestTimeout = requestTimeout; }
    @Parameter
    @Summary("Default is 0 (unlimited)")
    @Optional(defaultValue = "0")
    @Placement(order = 3, tab = "Timings")
    private int acceptTimeout;
    public int getAcceptTimeout() { return acceptTimeout; }
    public void setAcceptTimeout(int acceptTimeout) { this.acceptTimeout = acceptTimeout; }
    @Parameter
    @Summary("Default is 0 (unlimited)")
    @Optional(defaultValue = "0")
    @Placement(order = 4, tab = "Timings")
    private int releaseTimeout;
    public int getReleaseTimeout() { return releaseTimeout; }
    public void setReleaseTimeout(int releaseTimeout) { this.releaseTimeout = releaseTimeout; }
    @Parameter
    @Summary("Default is 0 (unlimited)")
    @Optional(defaultValue = "0")
    @Placement(order = 5, tab = "Timings")
    private int sendTimeout;
    public int getSendTimeout() { return sendTimeout; }
    public void setSendTimeout(int sendTimeout) { this.sendTimeout = sendTimeout; }
    @Parameter
    @Summary("Default is 0 (unlimited)")
    @Optional(defaultValue = "0")
    @Placement(order = 6, tab = "Timings")
    private int responseTimeout;
    public int getResponseTimeout() { return responseTimeout; }
    public void setResponseTimeout(int responseTimeout) { this.responseTimeout = responseTimeout; }
    @Parameter
    @Summary("Default is 0 (unlimited)")
    @Optional(defaultValue = "0")
    @Placement(order = 7, tab = "Timings")
    private int idleTimeout;
    public int getIdleTimeout() { return idleTimeout; }
    public void setIdleTimeout(int idleTimeout) { this.idleTimeout = idleTimeout; }
    @Parameter
    @Summary("Default is 50")
    @Optional(defaultValue = "50")
    @Placement(order = 8, tab = "Timings")
    private int socketCloseDelay;
    public int getSocketCloseDelay() { return socketCloseDelay; }
    public void setSocketCloseDelay(int socketCloseDelay) { this.socketCloseDelay = socketCloseDelay; }
}
