package org.mule.module.dicom.internal.source;

import org.mule.module.dicom.internal.config.DicomObjectOutputResolver;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.extension.api.annotation.execution.OnError;
import org.mule.runtime.extension.api.annotation.execution.OnSuccess;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@DisplayName("Get SCU Results")
@Summary("Receives each image from the Get SCU to Flow operation")
@MetadataScope(outputResolver = DicomObjectOutputResolver.class)
public class GetScuResults extends Source<Object, Map<String, String>> {
    private static final String MSGCONTEXT = "_scuMessage";
    private final String uuid = UUID.randomUUID().toString();
    public String getUuid() { return uuid; }
    private String flowName;
    public String getFlowName() { return flowName; }
    private final Object lock = new Object();
    public Object getLock() { return lock; }

    private ComponentLocation location;
    private SourceCallback<Object, Map<String, String>> sourceCallback;

    public void send(GetScuMessage message) {
        try {
            message.lock();
            SourceCallbackContext ctx = sourceCallback.createContext();
            ctx.addVariable(MSGCONTEXT, message);
            Map<String, String> attributes = new HashMap<>();
            attributes.put("action", message.getAction());
            attributes.put("seriesName", message.getSeriesName());
            if (message.getIuid() != null) {
                attributes.put("iuid", message.getIuid());
                sourceCallback.handle(Result.<Object, Map<String, String>>builder()
                        .output(message.getDicomObject())
                        .attributes(attributes)
                        .build(), ctx);
            } else {
                sourceCallback.handle(Result.<Object, Map<String, String>>builder()
                        .output(null)
                        .attributes(attributes)
                        .build(), ctx);
            }
        } catch (InterruptedException e) {
            message.release();
            Thread t = Thread.currentThread();
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
            t.interrupt();
        }
    }

    @Override
    public void onStart(SourceCallback<Object, Map<String, String>> sourceCallback) {
        this.sourceCallback = sourceCallback;
        flowName = location.getLocation().substring(0, location.getLocation().indexOf('/'));
        GetScuListener.attach(this);
    }

    @OnSuccess
    public void onSuccess(SourceCallbackContext context) {
        Optional<Object> messageObject = context.getVariable(MSGCONTEXT);
        if (messageObject.isPresent()) {
            GetScuMessage message = (GetScuMessage)messageObject.get();
            message.release();
        }
    }

    @OnError
    public void onError(SourceCallbackContext context, org.mule.runtime.api.message.Error error) {
        Optional<Object> messageObject = context.getVariable(MSGCONTEXT);
        if (messageObject.isPresent()) {
            GetScuMessage message = (GetScuMessage)messageObject.get();
            message.setError(error);
            message.release();
        }
    }

    @Override
    public void onStop() {
        GetScuListener.detach(this);
    }
}
