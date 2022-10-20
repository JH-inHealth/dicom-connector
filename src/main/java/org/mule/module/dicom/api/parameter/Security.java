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
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

public class Security {
    @Ignore
    public static final String PARAMETER_GROUP = "Security";

    @Parameter
    @Optional
    @Placement(tab = "Security")
    private String username;
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @Parameter
    @Optional
    @Password
    @Placement(tab = "Security")
    private String password;
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean hasUsernamePassword() {
        return (username != null && password != null && !username.isEmpty() && !password.isEmpty());
    }
}
