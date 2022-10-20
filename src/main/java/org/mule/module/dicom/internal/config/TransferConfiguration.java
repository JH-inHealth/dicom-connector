/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.config;

import org.mule.module.dicom.internal.connection.TransferConnectionProvider;
import org.mule.module.dicom.internal.operation.TransferOperations;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

@Configuration(name="transfer")
@ConnectionProviders(TransferConnectionProvider.class)
@Operations(TransferOperations.class)
public class TransferConfiguration {
}
