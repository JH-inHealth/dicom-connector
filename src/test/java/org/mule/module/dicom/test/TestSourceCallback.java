/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.test;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.mule.metadata.api.model.NullType;
import org.mule.module.dicom.api.content.DicomObject;
import org.mule.module.dicom.internal.exception.DicomError;
import org.mule.module.dicom.internal.util.AttribUtils;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestSourceCallback implements SourceCallback<Object, NullType> {
    private final List<String> data = new ArrayList<>();
    public List<String> getData() { return data; }
    private final List<ConnectionException> exceptions = new ArrayList<>();
    public List<ConnectionException> getExceptions() { return exceptions; }
    private String testTag;
    public String getTestTag() { return testTag; }

    public void clear() {
        data.clear();
        exceptions.clear();
        testTag = null;
    }

    @Override
    public void handle(Result<Object, NullType> result) {
        handle(result, null);
    }

    @Override
    public void handle(Result<Object, NullType> result, SourceCallbackContext sourceCallbackContext) {
        DicomObject dicomObject = (DicomObject) result.getOutput();
        Attributes image = dicomObject.getContent();
        String iuid = AttribUtils.getFirstString(image, new Integer[]{Tag.AffectedSOPInstanceUID, Tag.MediaStorageSOPInstanceUID, Tag.SOPInstanceUID});
        String testTag = image.getString("JohnsHopkinsMedicine", 0x67811000);
        if (testTag != null) this.testTag = testTag;
        data.add(iuid);
    }

    @Override
    public void onConnectionException(ConnectionException e) {
        exceptions.add(e);
    }

    @Override
    public SourceCallbackContext createContext() {
        return null;
    }
}
