/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.notification;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.extension.api.notification.NotificationActionDefinition;

public enum DownloadNotificationAction implements NotificationActionDefinition<DownloadNotificationAction> {
    SAVED(DataType.fromType(String.class)),

    FINISHED(DataType.fromType(Boolean.class));

    private final DataType dataType;

    DownloadNotificationAction(DataType dataType) {
        this.dataType = dataType;
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }
}
