/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.config;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.metadata.resolving.OutputStaticTypeResolver;

public class DicomObjectOutputResolver extends OutputStaticTypeResolver {
    @Override
    public MetadataType getStaticMetadata() {
        return BaseTypeBuilder.create(MetadataFormat.JAVA).objectType().id("dicom-object").build();
    }
}
