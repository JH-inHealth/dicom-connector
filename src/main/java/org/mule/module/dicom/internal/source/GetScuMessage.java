package org.mule.module.dicom.internal.source;

import org.mule.module.dicom.api.content.DicomObject;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class GetScuMessage {
    private final Semaphore semaphore = new Semaphore(1);

    private final String seriesName;
    public String getSeriesName() { return seriesName; }

    private final String action;
    public String getAction() { return action; }

    private final String iuid;
    public String getIuid() { return iuid; }

    private final DicomObject dicomObject;
    public DicomObject getDicomObject() { return dicomObject; }

    private org.mule.runtime.api.message.Error error;
    public void setError(org.mule.runtime.api.message.Error error) { this.error = error; }
    public org.mule.runtime.api.message.Error getError() { return error; }

    public GetScuMessage(String seriesName, String action) {
        this.error = null;
        this.seriesName = seriesName;
        this.action = action;
        this.iuid = null;
        this.dicomObject = null;
    }
    public GetScuMessage(String seriesName, String action, String iuid, DicomObject dicomObject) {
        this.error = null;
        this.seriesName = seriesName;
        this.action = action;
        this.iuid = iuid;
        this.dicomObject = dicomObject;
    }

    public void lock() throws InterruptedException {
        semaphore.acquire();
    }
    public void release() {
        semaphore.release();
    }
    public void waitForCompletion() throws IOException {
        try {
            semaphore.acquire();
            if (error != null) {
                throw new IOException(error.getCause());
            }
        } catch (InterruptedException e) {
            Thread t = Thread.currentThread();
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
            t.interrupt();
        } finally {
            semaphore.release();
        }
    }
}
