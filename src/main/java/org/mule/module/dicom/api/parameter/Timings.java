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

public class Timings {
    @Ignore
    public static final String PARAMETER_GROUP = "Timings";

    @Parameter
    @DisplayName("Cancel After")
    @Summary("Duration in milliseconds (0 is infinite)")
    @Optional(defaultValue = "0")
    @Placement(tab = "Timings")
    private int cancelAfter;
    public int getCancelAfter() { return cancelAfter; }
    public void setCancelAfter(int cancelAfter) { this.cancelAfter = cancelAfter; }
}
