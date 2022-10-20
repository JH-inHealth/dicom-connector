/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal;

import static org.mule.runtime.api.meta.Category.COMMUNITY;

import org.mule.module.dicom.internal.config.ScpConfiguration;
import org.mule.module.dicom.internal.config.TransferConfiguration;
import org.mule.module.dicom.internal.notification.DownloadNotificationAction;
import org.mule.module.dicom.internal.config.ScuConfiguration;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.module.dicom.internal.operation.FileOperations;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.*;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;
import org.mule.runtime.extension.api.annotation.notification.NotificationActions;

@Xml(prefix = "dicom")
@Extension(name = "DICOM", vendor = "Johns Hopkins University", category = COMMUNITY)
@Import(type = TlsContextFactory.class)
@Configurations({ScpConfiguration.class, ScuConfiguration.class, TransferConfiguration.class})
@NotificationActions(DownloadNotificationAction.class)
@ErrorTypes(DicomError.class)
@Operations(FileOperations.class)
public class DicomExtension { }
