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
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.util.List;
import java.util.Map;

public class TagSearch {
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
    @DisplayName("Response Tags")
    @Summary("Tag identities (name, hex value, hex pair, or integer) to include in the response.")
    @Optional @Content
    @Example("#[\"AccessionNumber\"]")
    private List<String> responseTags;
    public List<String> getResponseTags() { return responseTags; }
    public void setResponseTags(List<String> responseTags) { this.responseTags = responseTags; }
}
