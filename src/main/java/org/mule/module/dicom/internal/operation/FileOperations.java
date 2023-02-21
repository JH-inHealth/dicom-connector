/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.operation;

import org.dcm4che3.data.Implementation;
import org.mule.module.dicom.api.content.DicomObject;
import org.mule.module.dicom.internal.config.DicomObjectInputResolver;
import org.mule.module.dicom.api.content.DicomValue;
import org.mule.module.dicom.internal.config.DicomObjectOutputResolver;
import org.mule.module.dicom.internal.store.DicomFileType;
import org.mule.module.dicom.internal.util.AttribUtils;
import org.mule.module.dicom.internal.util.StoreUtils;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.module.dicom.internal.exception.FileErrorsProvider;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.store.ObjectStore;
import org.mule.runtime.api.store.ObjectStoreException;
import org.mule.runtime.api.store.ObjectStoreManager;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.*;
import org.mule.runtime.extension.api.exception.ModuleException;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mule.runtime.api.meta.ExpressionSupport.REQUIRED;
import static org.mule.runtime.api.meta.model.display.PathModel.Type.DIRECTORY;
import static org.mule.runtime.api.meta.model.display.PathModel.Type.FILE;

public class FileOperations {
    @Inject
    @Named("_muleObjectStoreManager")
    private ObjectStoreManager storeManager;

    @DisplayName("Apply Preamble")
    @Summary("Adds a preamble to a DICOM InputStream (serialized org.dcm4che3.data.Attributes object)")
    @Throws(FileErrorsProvider.class)
    public TypedValue<OutputStream>
    applyPreamble(@DisplayName("DICOM Object")
                  @Optional(defaultValue="#[payload]") @Expression(REQUIRED) @TypeResolver(DicomObjectInputResolver.class)
                  Object dicomObject,
                  @Optional @Content
                  @DisplayName("Change Tags")
                  @Summary("Create or update tags on the image")
                  @Example("#[{\"PatientID\": \"XXXXXXXX\", \"0x67810010\": \"JohnsHopkinsMedicine\", \"0x67811000\": \"${StudyDate}_${AccessionNumber}\"}]")
                  Map<String, String> changeTags
    ) {
        if (!(dicomObject instanceof DicomObject)) throw new ModuleException(DicomError.INVALID_DICOM_OBJECT, new RuntimeException(dicomObject.getClass().toString()));
        DicomObject dicom = (DicomObject)dicomObject;
        Attributes image = dicom.getContent();
        AttribUtils.updateTags(image, changeTags);
        String iuid = AttribUtils.getFirstString(image, new Integer[]{Tag.AffectedSOPInstanceUID, Tag.MediaStorageSOPInstanceUID, Tag.SOPInstanceUID});
        String cuid = AttribUtils.getFirstString(image, new Integer[]{Tag.AffectedSOPClassUID, Tag.MediaStorageSOPClassUID, Tag.SOPClassUID});
        Attributes fmi = StoreUtils.createFileMetaInformation(iuid, cuid, dicom.getTransferSyntaxUid(),
                dicom.getImplementationClassUid(), dicom.getImplementationVersionName(), dicom.getSourceApplicationEntityTitle());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            StoreUtils.writeTo(output, image, fmi);
        } catch (IOException e) {
            throw new ModuleException(DicomError.FILE_IO, e);
        }
        DataType outputDataType = DataType.builder().type(output.getClass()).mediaType("application/dicom").build();
        return new TypedValue<>(output, outputDataType);
    }

    @MediaType("application/java")
    @DisplayName("Extract Tags")
    @Summary("Extract tags from a DICOM InputStream (serialized org.dcm4che3.data.Attributes object) into #[attributes]")
    @Throws(FileErrorsProvider.class)
    public Map<String, DicomValue>
    extractTags(@DisplayName("DICOM Object")
                @Optional(defaultValue="#[payload]") @Expression(REQUIRED) @TypeResolver(DicomObjectInputResolver.class)
                Object dicomObject,
                @DisplayName("Tag Names")
                @Summary("Leave blank to extract all tags. Otherwise provide a list of tag identities (name, hex value, hex pair, or integer).")
                @Optional @Content
                @Example("#[\"PatientID\"]")
                List<String> tagNames
    ) {
        if (!(dicomObject instanceof DicomObject)) throw new ModuleException(DicomError.INVALID_DICOM_OBJECT, new RuntimeException(dicomObject.getClass().toString()));
        DicomObject dicom = (DicomObject)dicomObject;
        Attributes image = dicom.getContent();
        boolean allTags = (tagNames == null) || tagNames.isEmpty();
        Map<String, Object> map = allTags ? AttribUtils.attributesToMap(image) : AttribUtils.extractTags(image, tagNames);
        Map<String, DicomValue> payload = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            payload.put(entry.getKey(), new DicomValue(entry.getValue()));
        }
        return payload;
    }

    @DisplayName("Read from File System")
    @Summary("Reads a DICOM file into an InputStream (serialized org.dcm4che3.data.Attributes object)")
    @Throws(FileErrorsProvider.class)
    @OutputResolver(output = DicomObjectOutputResolver.class)
    public Object
    readFile(@DisplayName("Filename")
             @Path(type = FILE)
             String filename
    ) {
        DicomFileType dft = DicomFileType.parse(filename);
        if (!(dft.equals(DicomFileType.DICOM) || dft.equals(DicomFileType.DICOMDIR))) {
            throw new ModuleException(DicomError.FILE_IO, new Exception("File is not DICOM"));
        }
        File file = new File(filename);
        try (DicomInputStream dis = new DicomInputStream(file)) {
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            Attributes fmi = dis.getFileMetaInformation();
            Attributes content = dis.readDataset();

            return new DicomObject(content, fmi.getString(Tag.TransferSyntaxUID), fmi.getString(Tag.SourceApplicationEntityTitle, null),
                    fmi.getString(Tag.ImplementationClassUID, Implementation.getClassUID()), fmi.getString(Tag.ImplementationVersionName, Implementation.getVersionName()));
        } catch (IOException e) {
            throw new ModuleException(DicomError.FILE_IO, e);
        }
    }

    @DisplayName("Read from Object Store")
    @Summary("Reads a DICOM file into an InputStream (serialized org.dcm4che3.data.Attributes object)")
    @Throws(FileErrorsProvider.class)
    @OutputResolver(output = DicomObjectOutputResolver.class)
    public Object
    readFileObjectStore(@ParameterDsl(allowInlineDefinition = false) @Expression(ExpressionSupport.NOT_SUPPORTED) ObjectStore<byte[]> objectStore,
                        @DisplayName("Key Name")
                        String keyName
    ) {
        try {
            if (!objectStore.contains(keyName)) {
                throw new ModuleException(DicomError.NOT_FOUND, new RuntimeException("Key Not Found in Object Store"));
            }
            try (ByteArrayInputStream bais = new ByteArrayInputStream(objectStore.retrieve(keyName))) {
                try (DicomInputStream dis = new DicomInputStream(bais)) {
                    dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
                    Attributes fmi = dis.getFileMetaInformation();
                    Attributes content = dis.readDataset();

                    return new DicomObject(content, fmi.getString(Tag.TransferSyntaxUID), fmi.getString(Tag.SourceApplicationEntityTitle, null),
                            fmi.getString(Tag.ImplementationClassUID, Implementation.getClassUID()), fmi.getString(Tag.ImplementationVersionName, Implementation.getVersionName()));
                }
            }
        } catch (Exception e) {
            throw new ModuleException(DicomError.FILE_IO, e);
        }
    }

    @MediaType(MediaType.TEXT_PLAIN)
    @DisplayName("Store File to File System")
    @Summary("Saves a DICOM InputStream (serialized org.dcm4che3.data.Attributes object) as a DICOM file")
    @Throws(FileErrorsProvider.class)
    public String
    storeFile(@DisplayName("Folder Name")
              @Path(type = DIRECTORY)
              String folderName,
              @DisplayName("Filename")
              @Optional @Path(type = FILE)
              String filename,
              @DisplayName("DICOM Object")
              @Optional(defaultValue="#[payload]") @Expression(REQUIRED) @TypeResolver(DicomObjectInputResolver.class)
              Object dicomObject,
              @Optional @Content
              @DisplayName("Change Tags")
              @Summary("Create or update tags on the image")
              @Example("#[{\"PatientID\": \"XXXXXXXX\", \"0x67810010\": \"JohnsHopkinsMedicine\", \"0x67811000\": \"${StudyDate}_${AccessionNumber}\"}]")
              Map<String, String> changeTags
    ) {
        if (!(dicomObject instanceof DicomObject)) throw new ModuleException(DicomError.INVALID_DICOM_OBJECT, new RuntimeException(dicomObject.getClass().toString()));
        DicomObject dicom = (DicomObject)dicomObject;
        Attributes image = dicom.getContent();
        AttribUtils.updateTags(image, changeTags);
        String iuid = AttribUtils.getFirstString(image, new Integer[]{Tag.AffectedSOPInstanceUID, Tag.MediaStorageSOPInstanceUID, Tag.SOPInstanceUID});
        String cuid = AttribUtils.getFirstString(image, new Integer[]{Tag.AffectedSOPClassUID, Tag.MediaStorageSOPClassUID, Tag.SOPClassUID});

        java.nio.file.Path dir = Paths.get(folderName);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new ModuleException(DicomError.FILE_IO, e);
            }
        }
        java.nio.file.Path file;
        if (filename == null || filename.isEmpty()) {
            String guid = UUID.randomUUID().toString();
            file = Paths.get(folderName, String.format("%s.dcm", guid));
        } else {
            file = Paths.get(folderName, filename);
        }

        Attributes fmi = StoreUtils.createFileMetaInformation(iuid, cuid, dicom.getTransferSyntaxUid(),
                dicom.getImplementationClassUid(), dicom.getImplementationVersionName(), dicom.getSourceApplicationEntityTitle());
        try (OutputStream output = Files.newOutputStream(file, StandardOpenOption.CREATE)) {
            StoreUtils.writeTo(output,  image, fmi);
        } catch (IOException e) {
            throw new ModuleException(DicomError.FILE_IO, e);
        }
        return file.toString();
    }

    @MediaType(MediaType.TEXT_PLAIN)
    @DisplayName("Store File to Object Store")
    @Summary("Saves a DICOM InputStream (serialized org.dcm4che3.data.Attributes object) as a DICOM file in an Object Store")
    @Throws(FileErrorsProvider.class)
    public String
    storeFileObjectStore(@ParameterDsl(allowInlineDefinition = false) @Expression(ExpressionSupport.NOT_SUPPORTED) ObjectStore<byte[]> objectStore,
              @DisplayName("DICOM Object")
              @Optional(defaultValue="#[payload]") @Expression(REQUIRED) @TypeResolver(DicomObjectInputResolver.class)
              Object dicomObject,
              @Optional @Content
              @DisplayName("Change Tags")
              @Summary("Create or update tags on the image")
              @Example("#[{\"PatientID\": \"XXXXXXXX\", \"0x67810010\": \"JohnsHopkinsMedicine\", \"0x67811000\": \"${StudyDate}_${AccessionNumber}\"}]")
              Map<String, String> changeTags
    ) {
        if (!(dicomObject instanceof DicomObject)) throw new ModuleException(DicomError.INVALID_DICOM_OBJECT, new RuntimeException(dicomObject.getClass().toString()));
        DicomObject dicom = (DicomObject)dicomObject;
        Attributes image = dicom.getContent();
        AttribUtils.updateTags(image, changeTags);
        String iuid = AttribUtils.getFirstString(image, new Integer[]{Tag.AffectedSOPInstanceUID, Tag.MediaStorageSOPInstanceUID, Tag.SOPInstanceUID});
        String cuid = AttribUtils.getFirstString(image, new Integer[]{Tag.AffectedSOPClassUID, Tag.MediaStorageSOPClassUID, Tag.SOPClassUID});

        Attributes fmi = StoreUtils.createFileMetaInformation(iuid, cuid, dicom.getTransferSyntaxUid(),
                dicom.getImplementationClassUid(), dicom.getImplementationVersionName(), dicom.getSourceApplicationEntityTitle());
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            StoreUtils.writeTo(output, image, fmi);
            output.flush();
            objectStore.store(iuid, output.toByteArray());
        } catch (IOException | ObjectStoreException e) {
            throw new ModuleException(DicomError.FILE_IO, e);
        }
        return iuid;
    }
}
