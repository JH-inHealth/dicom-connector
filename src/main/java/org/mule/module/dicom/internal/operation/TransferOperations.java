/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.operation;

import org.mule.module.dicom.api.parameter.PresentationContext;
import org.mule.module.dicom.api.parameter.StoreSearch;
import org.mule.module.dicom.api.parameter.Timings;
import org.mule.module.dicom.internal.config.ScuOperationConfig;
import org.mule.module.dicom.internal.config.ScuType;
import org.mule.module.dicom.internal.connection.TransferConnection;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.module.dicom.internal.exception.ScuErrorsProvider;
import org.mule.module.dicom.internal.store.MuleTransferStore;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.*;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.List;
import java.util.Map;

public class TransferOperations {
    @MediaType("application/java")
    @DisplayName("Transfer")
    @Summary("Performs C-GET with a source Application Entity and C-STORE on each received DICOM file to a target Application Entity.")
    @Throws(ScuErrorsProvider.class)
    public List<String>
    transfer(@Connection TransferConnection connection,
             @ParameterGroup(name=StoreSearch.PARAMETER_GROUP)
             StoreSearch storeSearch,
             @ParameterGroup(name=PresentationContext.PARAMETER_GROUP)
             PresentationContext presentationContext,
             @Optional @Content
             @DisplayName("Change Tags")
             @Summary("Create or update tags on each image received")
             @Example("#[{\"PatientID\": \"XXXXXXXX\", \"0x67810010\": \"JohnsHopkinsMedicine\", \"0x67811000\": \"${StudyDate}_${AccessionNumber}\"}]")
             Map<String, String> changeTags,
             @ParameterGroup(name=Timings.PARAMETER_GROUP)
             Timings timings
    ) {
        synchronized (this) {
            ScuOperationConfig scuGetConfig = new ScuOperationConfig(ScuType.GET);
            scuGetConfig.setInformationModel(presentationContext.getInformationModel());
            scuGetConfig.setRetrieveLevel(presentationContext.getRetrieveLevel());
            scuGetConfig.setTransferSyntax(presentationContext.getTransferSyntax());
            scuGetConfig.setSopClasses(storeSearch.getSopClasses());
            scuGetConfig.setCancelAfter(timings.getCancelAfter());

            ScuOperationConfig scuStoreConfig = new ScuOperationConfig(ScuType.STORE);
            scuStoreConfig.setCancelAfter(timings.getCancelAfter());
            MuleTransferStore muleStore = new MuleTransferStore(connection, scuStoreConfig, changeTags);

            GetScu getScu = GetScu.execute(connection.getSourceConnection(), scuGetConfig, storeSearch.getSearchKeys(), muleStore);
            if (getScu.getHasError()) {
                throw new ModuleException(DicomError.REQUEST_ERROR, new RuntimeException(getScu.getErrorMessage()));
            }
            if (getScu.getPayload().isEmpty()) {
                throw new ModuleException(DicomError.NOT_FOUND, new RuntimeException("C-GET Received 0 Files"));
            }
            return getScu.getPayload();
        }
    }
}
