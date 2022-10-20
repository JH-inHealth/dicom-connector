/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.parameter;

import org.mule.module.dicom.internal.config.DicomObjectInputResolver;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.Ignore;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.ExclusiveOptionals;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Path;

import java.util.List;

import static org.mule.runtime.api.meta.ExpressionSupport.REQUIRED;
import static org.mule.runtime.api.meta.model.display.PathModel.Type.DIRECTORY;
import static org.mule.runtime.api.meta.model.display.PathModel.Type.FILE;

@ExclusiveOptionals(isOneRequired = true)
public class StoreImage {
    @Ignore
    public static final String PARAMETER_GROUP = "DICOM Image Source (Choose One)";

    @Parameter
    @DisplayName("DICOM Object")
    @Optional
    @Example("#[payload]") @Expression(REQUIRED) @TypeResolver(DicomObjectInputResolver.class)
    private Object dicomObject;
    public Object getDicomObject() { return dicomObject; }
    public void setDicomObject(Object dicomObject) { this.dicomObject = dicomObject; }

    @Parameter
    @DisplayName("Filename")
    @Optional
    @Path(type = FILE, acceptedFileExtensions = {"dcm", "*.tar.gz"})
    private String fileName;
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    @Parameter
    @DisplayName("Folder Name")
    @Optional
    @Path(type = DIRECTORY)
    private String folderName;
    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    @Parameter
    @DisplayName("List of Files")
    @Optional
    @Example("#[payload]")
    private List<String> listOfFiles;
    public List<String> getListOfFiles() { return listOfFiles; }
    public void setListOfFiles(List<String> listOfFiles) { this.listOfFiles = listOfFiles; }
}
