/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.test;

import org.mule.module.dicom.api.parameter.StoreImage;
import org.mule.module.dicom.api.parameter.StoreTimings;
import org.mule.module.dicom.api.parameter.Timings;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.runtime.core.api.util.FileUtils;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestUtils {
    private static final TestUtils testUtils = new TestUtils();
    private TestUtils() { }
    private String getFilename(String resourceName) {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File file = new File(Objects.requireNonNull(classLoader.getResource(resourceName)).getFile());
        return file.getAbsolutePath();
    }
    private String getResource(String resourceName) throws IOException {
        return FileUtils.getResourcePath(resourceName, this.getClass());
    }

    public static String getSampleFilename(String resourceName) {
        return testUtils.getFilename(resourceName);
    }
    public static String getResourcePath(String resourceName) throws IOException { return testUtils.getResource(resourceName); }

    public static Timings getTimings() {
        Timings timings = new Timings();
        timings.setCancelAfter(0);
        return timings;
    }
    public static StoreTimings getStoreTimings() {
        StoreTimings timings = new StoreTimings();
        timings.setStoreTimeout(0);
        timings.setCancelAfter(0);
        return timings;
    }

    public static String getStackTrace(ModuleException ex) {
        ErrorTypeDefinition<?> etd = ex.getType();
        StringWriter stringWriter = new StringWriter();
        if (etd instanceof DicomError) stringWriter.write("DICOM:");
        stringWriter.write(etd.getType());
        stringWriter.write("\r\n");
        ex.getCause().printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    public static StoreImage setStoreConfig(String filename) {
        StoreImage storeImage = new StoreImage();
        try {
            storeImage.setDicomObject(null);
            storeImage.setFileName(filename);
            storeImage.setFolderName(null);
            storeImage.setListOfFiles(null);
        } catch (Exception ignore) {
            //Ignore the improbable
        }
        return storeImage;
    }
}
