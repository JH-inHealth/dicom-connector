/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.source;

import org.mule.metadata.api.model.NullType;
import org.mule.module.dicom.api.parameter.SopClass;
import org.mule.module.dicom.internal.config.DicomObjectOutputResolver;
import org.mule.module.dicom.internal.connection.ScpConnection;
import org.mule.module.dicom.internal.store.MuleProcessStore;
import org.mule.module.dicom.internal.config.ScpConfiguration;
import org.mule.module.dicom.internal.exception.ScpErrorsProvider;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
import org.mule.runtime.extension.api.annotation.param.*;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.error.MuleErrors;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;

import java.io.IOException;
import java.security.GeneralSecurityException;

@DisplayName("Store SCP")
@Summary("Performs C-STORE as a Service Class Provider (Listener)")
@Throws(ScpErrorsProvider.class)
@MetadataScope(outputResolver = DicomObjectOutputResolver.class)
public class StoreScp extends Source<Object, NullType> {
    @Parameter
    @DisplayName("Storage SOP Classes")
    @Summary("Storage Service-Order Pair (SOP) Classes and their TransferSyntax. Choose None to accept all SOP Classes.")
    @Optional
    private SopClass[] sopClasses;

    @Config
    private ScpConfiguration config;

    @Connection
    private ConnectionProvider<ScpConnection> serviceProvider;

    private ScpConnection dicomServer;

    @Override
    public void onStart(SourceCallback<Object, NullType> sourceCallback) throws MuleException {
        dicomServer = serviceProvider.connect();
        MuleProcessStore store = new MuleProcessStore(sourceCallback);
        try {
            dicomServer.start(store, sopClasses);
        } catch (IOException e) {
            throw new ModuleException(MuleErrors.CONNECTIVITY, e);
        } catch (GeneralSecurityException e) {
            throw new ModuleException(MuleErrors.SERVER_SECURITY, e);
        }
    }

    @Override
    public void onStop() {
        if (dicomServer != null) {
            dicomServer.stop();
        }
    }

}