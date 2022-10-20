/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.exception;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.error.MuleErrors;

import java.util.Optional;

public enum DicomError implements ErrorTypeDefinition<DicomError> {
    CONNECTIVITY(MuleErrors.CONNECTIVITY),
    SSL(MuleErrors.CONNECTIVITY),
    CANCELED(MuleErrors.CONNECTIVITY),
    CLIENT_SECURITY(MuleErrors.CLIENT_SECURITY),
    SERVER_SECURITY(MuleErrors.SERVER_SECURITY),
    FILE_IO,
    MISSING_UID,
    INVALID_DICOM_OBJECT,
    REQUEST_ERROR,
    NOT_FOUND;

    private ErrorTypeDefinition<? extends Enum<?>> parent;
    DicomError(ErrorTypeDefinition<? extends Enum<?>> parent) {
        this.parent = parent;
    }
    DicomError() {}

    @Override
    public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
        return Optional.ofNullable(parent);
    }
}
