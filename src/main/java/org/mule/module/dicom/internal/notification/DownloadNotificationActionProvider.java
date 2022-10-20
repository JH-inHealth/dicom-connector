/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.notification;

import org.mule.runtime.extension.api.annotation.notification.NotificationActionProvider;
import org.mule.runtime.extension.api.notification.NotificationActionDefinition;

import java.util.HashSet;
import java.util.Set;

public class DownloadNotificationActionProvider implements NotificationActionProvider {
    @SuppressWarnings("rawtypes")
    @Override
    public Set<NotificationActionDefinition> getNotificationActions() {
        HashSet<NotificationActionDefinition> actions = new HashSet<>();
        actions.add(DownloadNotificationAction.SAVED);
        actions.add(DownloadNotificationAction.FINISHED);
        return actions;
    }
}
