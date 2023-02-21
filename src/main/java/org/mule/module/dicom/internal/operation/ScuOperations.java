/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.operation;

import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.mule.module.dicom.api.content.*;
import org.mule.module.dicom.api.parameter.*;
import org.mule.module.dicom.internal.config.ScuType;
import org.mule.module.dicom.internal.connection.ScuConnection;
import org.mule.module.dicom.internal.notification.DownloadNotificationAction;
import org.mule.module.dicom.internal.notification.DownloadNotificationActionProvider;
import org.mule.module.dicom.internal.store.DicomFileType;
import org.mule.module.dicom.internal.store.MuleFileStore;
import org.mule.module.dicom.internal.store.MuleObjectStore;
import org.mule.module.dicom.internal.util.AttribUtils;
import org.mule.module.dicom.internal.config.ScuOperationConfig;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.module.dicom.internal.exception.ScuErrorsProvider;
import org.dcm4che3.data.Attributes;
import org.mule.module.dicom.internal.util.StoreUtils;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.store.ObjectStore;
import org.mule.runtime.api.store.ObjectStoreManager;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.notification.Fires;
import org.mule.runtime.extension.api.annotation.param.*;
import org.mule.runtime.extension.api.annotation.param.display.*;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.notification.NotificationEmitter;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mule.runtime.api.meta.model.display.PathModel.Type.DIRECTORY;

public class ScuOperations {
    @Inject
    @Named("_muleObjectStoreManager")
    private ObjectStoreManager storeManager;

    @DisplayName("Echo SCU")
    @Summary("Performs C-ECHO as a Service Class User with a remote Application Entity.")
    @Throws(ScuErrorsProvider.class)
    public EchoScuPayload
    echoScu(@Connection ScuConnection connection,
            @ParameterGroup(name=Timings.PARAMETER_GROUP)
            Timings timings
    ) {
        ScuOperationConfig scuOperationConfig = new ScuOperationConfig(ScuType.ECHO);
        scuOperationConfig.setCancelAfter(timings.getCancelAfter());
        scuOperationConfig.setSopClassUid(UID.Verification);
        scuOperationConfig.setTransferSyntaxUid(UID.ImplicitVRLittleEndian);

        EchoScu echoScu = EchoScu.execute(connection, scuOperationConfig);
        if (!echoScu.getSuccess()) {
            throw new ModuleException(DicomError.REQUEST_ERROR, new RuntimeException(echoScu.getErrorMessage()));
        }
        return new EchoScuPayload(echoScu);
    }

    @DisplayName("Find SCU")
    @Summary("Performs C-FIND as a Service Class User with a remote Application Entity.")
    @Throws(ScuErrorsProvider.class)
    public FindScuPayload
    findScu(@Connection ScuConnection connection,
            @ParameterGroup(name=TagSearch.PARAMETER_GROUP)
            TagSearch tagSearch,
            @ParameterGroup(name=PresentationContext.PARAMETER_GROUP)
            PresentationContext presentationContext,
            @ParameterGroup(name=Timings.PARAMETER_GROUP)
            Timings timings
    ) {
        ScuOperationConfig scuOperationConfig = new ScuOperationConfig(ScuType.FIND);
        scuOperationConfig.setInformationModel(presentationContext.getInformationModel());
        scuOperationConfig.setRetrieveLevel(presentationContext.getRetrieveLevel());
        scuOperationConfig.setTransferSyntax(presentationContext.getTransferSyntax());
        scuOperationConfig.setCancelAfter(timings.getCancelAfter());

        Map<String, Object> keys = new HashMap<>(tagSearch.getSearchKeys());
        for (String tagName : tagSearch.getResponseTags()) {
            if (!keys.containsKey("tagName")) keys.put(tagName, "");
        }
        FindScu findScu = FindScu.execute(connection, scuOperationConfig, keys);
        if (!findScu.getSuccess()) {
            throw new ModuleException(DicomError.REQUEST_ERROR, new RuntimeException(findScu.getErrorMessage()));
        }
        if (findScu.getPayload().isEmpty()) {
            throw new ModuleException(DicomError.NOT_FOUND, new RuntimeException("C-FIND Received 0 Results"));
        }
        return new FindScuPayload(findScu);
    }

    @DisplayName("Get SCU to File System")
    @Summary("Performs C-GET as a Service Class User with a remote Application Entity.")
    @Fires(DownloadNotificationActionProvider.class)
    @Throws(ScuErrorsProvider.class)
    public GetScuPayload
    getScu(@Connection ScuConnection connection,
           @DisplayName("Folder Name")
           @Summary("Folder where all files are saved")
           @Path(type = DIRECTORY)
           String folderName,
           @Summary("Compress all files into a single tar/gzip")
           @Optional(defaultValue = "false")
           boolean compressFiles,
           @ParameterGroup(name=StoreSearch.PARAMETER_GROUP)
           StoreSearch storeSearch,
           @ParameterGroup(name=PresentationContext.PARAMETER_GROUP)
           PresentationContext presentationContext,
           @ParameterGroup(name=StoreTimings.PARAMETER_GROUP)
           StoreTimings timings,
           NotificationEmitter notificationEmitter
    ) {
        ScuOperationConfig scuOperationConfig = new ScuOperationConfig(ScuType.GET);
        scuOperationConfig.setInformationModel(presentationContext.getInformationModel());
        scuOperationConfig.setRetrieveLevel(presentationContext.getRetrieveLevel());
        scuOperationConfig.setTransferSyntax(presentationContext.getTransferSyntax());
        scuOperationConfig.setSopClasses(storeSearch.getSopClasses());
        scuOperationConfig.setStoreTimeout(timings.getStoreTimeout());
        scuOperationConfig.setCancelAfter(timings.getCancelAfter());

        MuleFileStore muleStore;
        try {
            muleStore = new MuleFileStore(folderName, notificationEmitter, compressFiles);
        } catch (IOException e) {
            throw new ModuleException(DicomError.FILE_IO, e);
        }
        GetScu getScu = GetScu.execute(connection, scuOperationConfig, storeSearch.getSearchKeys(), muleStore);
        if (notificationEmitter != null) notificationEmitter.fire(DownloadNotificationAction.FINISHED, TypedValue.of(!getScu.getPayload().isEmpty()));
        if (getScu.getHasError()) {
            throw new ModuleException(DicomError.REQUEST_ERROR, new RuntimeException(getScu.getErrorMessage()));
        }
        if (getScu.getPayload().isEmpty()) {
            throw new ModuleException(DicomError.NOT_FOUND, new RuntimeException("C-GET Received 0 Files"));
        }
        return new GetScuPayload(getScu);
    }

    @DisplayName("Get SCU to Object Store")
    @Summary("Performs C-GET as a Service Class User with a remote Application Entity. Send all DICOM objects to an Object Store.")
    @Fires(DownloadNotificationActionProvider.class)
    @Throws(ScuErrorsProvider.class)
    public GetScuPayload
    getScuObjectStore(@Connection ScuConnection connection,
                      @ParameterDsl(allowInlineDefinition = false) @Expression(ExpressionSupport.NOT_SUPPORTED) ObjectStore<byte[]> objectStore,
                      @ParameterGroup(name= StoreSearch.PARAMETER_GROUP)
                      StoreSearch storeSearch,
                      @ParameterGroup(name= PresentationContext.PARAMETER_GROUP)
                      PresentationContext presentationContext,
                      @ParameterGroup(name= StoreTimings.PARAMETER_GROUP)
                      StoreTimings timings,
                      NotificationEmitter notificationEmitter
    ) {
        ScuOperationConfig scuOperationConfig = new ScuOperationConfig(ScuType.GET);
        scuOperationConfig.setInformationModel(presentationContext.getInformationModel());
        scuOperationConfig.setRetrieveLevel(presentationContext.getRetrieveLevel());
        scuOperationConfig.setTransferSyntax(presentationContext.getTransferSyntax());
        scuOperationConfig.setSopClasses(storeSearch.getSopClasses());
        scuOperationConfig.setStoreTimeout(timings.getStoreTimeout());
        scuOperationConfig.setCancelAfter(timings.getCancelAfter());

        MuleObjectStore muleStore = new MuleObjectStore(objectStore);
        GetScu getScu = GetScu.execute(connection, scuOperationConfig, storeSearch.getSearchKeys(), muleStore);
        if (notificationEmitter != null) notificationEmitter.fire(DownloadNotificationAction.FINISHED, TypedValue.of(!getScu.getPayload().isEmpty()));
        if (getScu.getHasError()) {
            throw new ModuleException(DicomError.REQUEST_ERROR, new RuntimeException(getScu.getErrorMessage()));
        }
        if (getScu.getPayload().isEmpty()) {
            throw new ModuleException(DicomError.NOT_FOUND, new RuntimeException("C-GET Received 0 Files"));
        }
        return new GetScuPayload(getScu);
    }

    @DisplayName("Move SCU")
    @Summary("Performs C-MOVE as a Service Class User with a remote Application Entity.")
    @Throws(ScuErrorsProvider.class)
    public MoveScuPayload
    moveScu(@Connection ScuConnection connection,
            @ParameterGroup(name=StoreSearch.PARAMETER_GROUP)
            StoreSearch storeSearch,
            @ParameterGroup(name=PresentationContext.PARAMETER_GROUP)
            PresentationContext presentationContext,
            @ParameterGroup(name=StoreTimings.PARAMETER_GROUP)
            StoreTimings timings
    ) {
        ScuOperationConfig scuOperationConfig = new ScuOperationConfig(ScuType.MOVE);
        scuOperationConfig.setInformationModel(presentationContext.getInformationModel());
        scuOperationConfig.setRetrieveLevel(presentationContext.getRetrieveLevel());
        scuOperationConfig.setTransferSyntax(presentationContext.getTransferSyntax());
        scuOperationConfig.setSopClasses(storeSearch.getSopClasses());
        scuOperationConfig.setStoreTimeout(timings.getStoreTimeout());
        scuOperationConfig.setCancelAfter(timings.getCancelAfter());

        MoveScu moveScu = MoveScu.execute(connection, scuOperationConfig, storeSearch.getSearchKeys());
        if (!moveScu.getSuccess()) {
            throw new ModuleException(DicomError.REQUEST_ERROR, new RuntimeException(moveScu.getErrorMessage()));
        }
        return new MoveScuPayload(moveScu);
    }

    @MediaType("application/java")
    @DisplayName("Store SCU")
    @Summary("Performs C-STORE as a Service Class User with a remote Application Entity.")
    @Throws(ScuErrorsProvider.class)
    public List<String>
    storeScu(@Connection ScuConnection connection,
             @ParameterGroup(name=StoreImage.PARAMETER_GROUP)
             StoreImage storeImage,
             @DisplayName("Delete Source Files")
             @Optional(defaultValue = "false")
             boolean deleteSourceFiles,
             @Optional @Content
             @DisplayName("Change Tags")
             @Summary("Create or update tags on each image")
             @Example("#[{\"PatientID\": \"XXXXXXXX\", \"0x67810010\": \"JohnsHopkinsMedicine\", \"0x67811000\": \"${StudyDate}_${AccessionNumber}\"}]")
             Map<String, String> changeTags,
             @ParameterGroup(name=Timings.PARAMETER_GROUP)
             Timings timings
    ) {
        ScuOperationConfig scuOperationConfig = new ScuOperationConfig(ScuType.STORE);
        scuOperationConfig.setCancelAfter(timings.getCancelAfter());
        List<String> iuidList = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        try {
            if (storeImage.getDicomObject() != null) {
                Object dicomObject = storeImage.getDicomObject();
                if (!(dicomObject instanceof DicomObject)) throw new ModuleException(DicomError.INVALID_DICOM_OBJECT, new RuntimeException(dicomObject.getClass().toString()));
                DicomObject dicom = (DicomObject)dicomObject;

                // Send the content
                Attributes data = dicom.getContent();
                AttribUtils.updateTags(data, changeTags);
                StoreScu.execute(connection, scuOperationConfig, data, null, iuidList);
            } else if (storeImage.getFileName() != null) {
                String fileName = storeImage.getFileName();
                DicomFileType dft = DicomFileType.parse(fileName);
                if (DicomFileType.canStore(dft)) {
                    StoreScu.execute(connection, scuOperationConfig, fileName, dft, changeTags, iuidList);
                } else {
                    throw new ModuleException(DicomError.FILE_IO, new RuntimeException("File cannot be read or is an unknown type"));
                }
            } else if (storeImage.getFolderName() != null) {
                String folderName = storeImage.getFolderName();
                if (!DicomFileType.parse(folderName).equals(DicomFileType.DIRECTORY))
                    throw new ModuleException(DicomError.FILE_IO, new RuntimeException("Cannot read Folder"));
                List<java.nio.file.Path> fileList = StoreUtils.getFileList(folderName);
                for (java.nio.file.Path fileName : fileList) {
                    DicomFileType dft = DicomFileType.parse(fileName.toString());
                    if (DicomFileType.canStore(dft)) { // Quietly ignore anything we cannot store
                        StoreScu.execute(connection, scuOperationConfig, fileName.toString(), dft, changeTags, iuidList);
                    }
                }
            } else if (storeImage.getListOfFiles() != null) {
                // Flatten the list, since it could be a combination of filenames and/or folders
                Map<String, DicomFileType> allFiles = new HashMap<>();
                for (String fileName : storeImage.getListOfFiles()) {
                    DicomFileType dft = DicomFileType.parse(fileName);
                    if (DicomFileType.canStore(dft)) {
                        allFiles.put(fileName, dft);
                    } else if (dft.equals(DicomFileType.DIRECTORY)) {
                        List<java.nio.file.Path> fileList = StoreUtils.getFileList(fileName);
                        for (java.nio.file.Path file : fileList) {
                            DicomFileType dft2 = DicomFileType.parse(file.toString());
                            if (DicomFileType.canStore(dft2)) { // Quietly ignore anything we cannot store
                                allFiles.put(file.toString(), dft2);
                            }
                        }
                    } else {
                        throw new ModuleException(DicomError.FILE_IO, new RuntimeException("File cannot be read or is an unknown type"));
                    }
                }
                for (Map.Entry<String, DicomFileType> entry : allFiles.entrySet()) {
                    StoreScu.execute(connection, scuOperationConfig, entry.getKey(), entry.getValue(), changeTags, iuidList);
                }
            } else if (storeImage.getObjectStore() != null) {
                storeScuFromObjectStore(connection, storeImage.getObjectStore(), changeTags, scuOperationConfig, iuidList, keys);
            }
        } catch (IOException e) {
            throw new ModuleException(DicomError.CONNECTIVITY, e);
        } finally {
            if (deleteSourceFiles) {
                if (storeImage.getFileName() != null) {
                    StoreUtils.deleteFolder(storeImage.getFileName());
                } else if (storeImage.getFolderName() != null) {
                    StoreUtils.deleteFolder(storeImage.getFolderName());
                } else if (storeImage.getListOfFiles() != null) {
                    for (String fileName : storeImage.getListOfFiles()) {
                        StoreUtils.deleteFolder(fileName);
                    }
                } else if (storeImage.getObjectStore() != null) {
                    try {
                        ObjectStore<byte[]> objectStore = storeImage.getObjectStore();
                        for (String keyName : keys) {
                            objectStore.remove(keyName);
                        }
                    } catch (Exception ignore) {
                        // Ignore
                    }
                }
            }
        }
        return iuidList;
    }

    private void storeScuFromObjectStore(ScuConnection connection, ObjectStore<byte[]> objectStore, Map<String, String> changeTags, ScuOperationConfig scuOperationConfig, List<String> iuidList, List<String> keys) {
        try {
            for (String keyName : objectStore.allKeys()) {
                try (ByteArrayInputStream bais = new ByteArrayInputStream(objectStore.retrieve(keyName))) {
                    try (DicomInputStream dis = new DicomInputStream(bais)) {
                        dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
                        Attributes fmi = dis.getFileMetaInformation();
                        Attributes data = dis.readDataset();
                        AttribUtils.updateTags(data, changeTags);
                        StoreScu.execute(connection, scuOperationConfig, data, fmi, iuidList);
                        keys.add(keyName);
                    }
                }
            }
        } catch (Exception e) {
            throw new ModuleException(DicomError.FILE_IO, e);
        }
    }
}
