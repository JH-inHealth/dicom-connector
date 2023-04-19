/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.config;

import org.mule.module.dicom.internal.connection.StoreScpConnectionProvider;
import org.mule.module.dicom.internal.source.GetScuResults;
import org.mule.module.dicom.internal.source.StoreScp;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

@Configuration(name="provider")
@ConnectionProviders(StoreScpConnectionProvider.class)
@Sources({StoreScp.class, GetScuResults.class})
public class ScpConfiguration { }
