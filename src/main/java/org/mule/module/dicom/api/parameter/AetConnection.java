/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.parameter;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class AetConnection {
    @Parameter
    @DisplayName("AE Title")
    @Summary("Application Entity Title")
    private String aetName;
    public String getAetName() { return aetName; }
    public void setAetName(String aetName) { this.aetName = aetName; }
    @Parameter
    @Optional(defaultValue = "0.0.0.0")
    private String hostname;
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }
    @Parameter
    @Optional(defaultValue = "104")
    private int port;
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    @Override
    public String toString() {
        return String.format("%s@%s:%s", aetName, hostname, port);
    }
}
