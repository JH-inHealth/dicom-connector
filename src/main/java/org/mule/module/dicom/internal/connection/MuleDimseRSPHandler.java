/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.connection;

import org.mule.module.dicom.internal.util.AttribUtils;
import org.mule.module.dicom.api.content.DicomValue;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MuleDimseRSPHandler extends DimseRSPHandler {
    private static final Logger log = LoggerFactory.getLogger(MuleDimseRSPHandler.class);
    private int status = -1;
    public int getStatus() { return status; }

    public MuleDimseRSPHandler(int msgId) {
        super(msgId);
    }

    private final Map<String, Object> command = new HashMap<>();
    public Map<String, Object> getCommand() { return command; }

    private final DicomValue data = new DicomValue(new ArrayList<DicomValue>());
    public DicomValue getData() { return data; }

    @Override
    public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
        super.onDimseRSP(as, cmd, data);
        if (this.isCanceled()) {
            status = Status.Cancel;
        } else {
            if (cmd != null && !cmd.isEmpty()) {
                status = cmd.getInt(Tag.Status, -1);
                Map<String, Object> cmdMap = AttribUtils.attributesToMap(cmd);
                AttribUtils.upsertMap(cmdMap, command);
            }
            if (data != null && !data.isEmpty()) {
                Map<String, Object> objectMap = AttribUtils.attributesToMap(data);
                this.data.getAsList().add(new DicomValue(objectMap));
            }
        }
    }

    public String getStatusText() {
        if (status < 0) return "NotSet";
        String value = "Unknown";
        for (Field f : Status.class.getFields()) {
            try {
                Object v = f.get(null);
                if ((v instanceof Integer) && ((int)v == status)) {
                    value = f.getName();
                    break;
                }
            } catch (IllegalArgumentException | IllegalAccessException ignore) {
                log.trace("Illegal Argument Ignored");
            }
        }
        // By specification, statuses C000 through CFFF are considered Unable to Process
        if (value.equals("Unknown") && status >= 0xC000 && status <= 0xCFFF) value = "UnableToProcess";
        return value;
    }
}
