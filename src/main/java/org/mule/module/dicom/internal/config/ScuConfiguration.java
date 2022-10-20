/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.config;

import org.mule.module.dicom.internal.connection.ScuConnectionProvider;
import org.mule.module.dicom.internal.operation.ScuOperations;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

@Configuration(name="user")
@ConnectionProviders(ScuConnectionProvider.class)
@Operations(ScuOperations.class)
public class ScuConfiguration {

}
