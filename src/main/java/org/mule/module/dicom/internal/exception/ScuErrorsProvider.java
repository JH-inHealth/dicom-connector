/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.exception;

import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.HashSet;
import java.util.Set;

public class ScuErrorsProvider implements ErrorTypeProvider {
    @SuppressWarnings("rawtypes")
    @Override
    public Set<ErrorTypeDefinition> getErrorTypes() {
        HashSet<ErrorTypeDefinition> errors = new HashSet<>();
        errors.add(DicomError.CONNECTIVITY);
        errors.add(DicomError.SSL);
        errors.add(DicomError.CANCELED);
        errors.add(DicomError.CLIENT_SECURITY);
        errors.add(DicomError.FILE_IO);
        errors.add(DicomError.INVALID_DICOM_OBJECT);
        errors.add(DicomError.REQUEST_ERROR);
        errors.add(DicomError.NOT_FOUND);
        errors.add(DicomError.MISSING_UID);
        return errors;
    }
}
