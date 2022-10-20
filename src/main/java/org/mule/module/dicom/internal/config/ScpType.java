/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.config;

public enum ScpType {
    STORE("Store");

    private final String name;
    public String getName() { return name; }
    ScpType(String name) {
        this.name = name;
    }

}