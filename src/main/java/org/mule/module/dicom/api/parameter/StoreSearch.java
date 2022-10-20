/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.parameter;

import org.mule.runtime.extension.api.annotation.Ignore;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.util.Map;

public class StoreSearch {
    @Ignore
    public static final String PARAMETER_GROUP = "Search";

    @Parameter
    @Content(primary = true)
    @DisplayName("Search Keys")
    @Summary("Tag identity/Value map of search keys. e.g. #[{\"PatientID\": \"PAT001\"}]")
    private Map<String, Object> searchKeys;
    public Map<String, Object> getSearchKeys() { return searchKeys; }
    public void setSearchKeys(Map<String, Object> searchKeys) { this.searchKeys = searchKeys; }

    @Parameter
    @DisplayName("Storage SOP Classes")
    @Summary("Name/Value map of Storage Service-Order Pair (SOP) Classes and their TransferSyntax. Choose None to use default values.")
    @Optional
    private SopClass[] sopClasses;
    public SopClass[] getSopClasses() { return sopClasses; }
    public void setSopClasses(SopClass[] sopClasses) { this.sopClasses = sopClasses; }

}
