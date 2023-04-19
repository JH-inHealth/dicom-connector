package org.mule.module.dicom.internal.source;

import org.mule.module.dicom.api.content.DicomObject;

import java.io.IOException;
import java.util.concurrent.*;

public class GetScuListener {
    private static final ConcurrentMap<String, GetScuResults> sources = new ConcurrentHashMap<>();

    private GetScuListener() { }

    public static void start(String flowName, String seriesName) throws IOException {
        GetScuResults source = sources.get(flowName);
        if (source != null) {
            synchronized (source.getLock()) {
                GetScuMessage message = new GetScuMessage(seriesName, "start");
                source.send(message);
                message.waitForCompletion();
            }
        } else {
            throw new IOException(String.format("There is no Flow called %s with a Get Scu Results listener.", flowName));
        }
    }

    public static void publish(String flowName, String seriesName, String iuid, DicomObject dicomObject) throws IOException {
        GetScuResults source = sources.get(flowName);
        if (source != null) {
            synchronized (source.getLock()) {
                GetScuMessage message = new GetScuMessage(seriesName, "receive", iuid, dicomObject);
                source.send(message);
                message.waitForCompletion();
            }
        }
    }

    public static void stop(String flowName, String seriesName) throws IOException {
        GetScuResults source = sources.get(flowName);
        if (source != null) {
            synchronized (source.getLock()) {
                GetScuMessage message = new GetScuMessage(seriesName, "stop");
                source.send(message);
                message.waitForCompletion();
            }
        }
    }

    public static void attach(GetScuResults getScuResults) {
        sources.put(getScuResults.getFlowName(), getScuResults);
    }

    public static void detach(GetScuResults getScuResults) {
        sources.remove(getScuResults.getFlowName());
    }

}
