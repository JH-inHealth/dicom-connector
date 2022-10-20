/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.api.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DicomValue implements Serializable {
    private static final long serialVersionUID = -4247869292949159940L;

    private final DicomValueType type;
    public DicomValueType getType() { return type; }
    private final String asString;
    public String getAsString() { return asString; }
    private final int asInteger;
    public int getAsInteger() { return asInteger; }
    private final double asDouble;
    public double getAsDouble() { return asDouble; }
    private final float asFloat;
    public float getAsFloat() { return asFloat; }
    private final short asShort;
    public short getAsShort() { return asShort; }
    private final long asLong;
    public long getAsLong() { return asLong; }
    private final List<DicomValue> asList;
    public List<DicomValue> getAsList() { return asList; }
    private final Map<String, DicomValue> asMap;
    public Map<String, DicomValue> getAsMap() { return asMap; }

    public boolean isEmpty() {
        boolean isEmpty;
        switch (type) {
            case NULL:
                isEmpty = true;
                break;
            case LIST:
                isEmpty = asList.isEmpty();
                break;
            case MAP:
                isEmpty = asMap.isEmpty();
                break;
            default:
                isEmpty = false;
                break;
        }
        return isEmpty;
    }

    public DicomValue() {
        type = DicomValueType.NULL;
        asString = null;
        asInteger = 0;
        asDouble = 0;
        asFloat = 0;
        asShort = 0;
        asLong = 0;
        asMap = null;
        asList = null;
    }
    public DicomValue(Object value) {
        if (value == null) {
            type = DicomValueType.NULL;
            asString = null;
            asInteger = 0;
            asDouble = 0;
            asFloat = 0;
            asShort = 0;
            asLong = 0;
            asMap = null;
            asList = null;
        } else {
            String simpleName = value.getClass().getSimpleName();
            switch (simpleName) {
                case "String":
                    type = DicomValueType.STRING;
                    asString = (String) value;
                    // Cast to other types if possible
                    asInteger = 0;
                    asDouble = 0;
                    asFloat = 0;
                    asShort = 0;
                    asLong = 0;
                    asMap = null;
                    asList = null;
                    break;
                case "Integer":
                    type = DicomValueType.INTEGER;
                    asInteger = (int) value;
                    // Cast to other types if possible
                    asString = Integer.toString(asInteger);
                    asDouble = asInteger;
                    asFloat = asInteger;
                    asShort = 0;
                    asLong = asInteger;
                    asMap = null;
                    asList = null;
                    break;
                case "Double":
                    type = DicomValueType.DOUBLE;
                    asDouble = (double) value;
                    // Cast to other types if possible
                    asString = Double.toString(asDouble);
                    asInteger = 0;
                    asFloat = 0;
                    asShort = 0;
                    asLong = 0;
                    asMap = null;
                    asList = null;
                    break;
                case "Float":
                    type = DicomValueType.FLOAT;
                    asFloat = (float) value;
                    // Cast to other types if possible
                    asString = Float.toString(asFloat);
                    asInteger = 0;
                    asDouble = asFloat;
                    asShort = 0;
                    asLong = 0;
                    asMap = null;
                    asList = null;
                    break;
                case "Short":
                    type = DicomValueType.SHORT;
                    asShort = (short) value;
                    // Cast to other types if possible
                    asString = Short.toString(asShort);
                    asInteger = asShort;
                    asDouble = asShort;
                    asFloat = asShort;
                    asLong = asShort;
                    asMap = null;
                    asList = null;
                    break;
                case "Long":
                    type = DicomValueType.LONG;
                    asLong = (long) value;
                    // Cast to other types if possible
                    asString = Long.toString(asLong);
                    asInteger = 0;
                    asDouble = asLong;
                    asFloat = 0;
                    asShort = 0;
                    asMap = null;
                    asList = null;
                    break;
                case "ArrayList":
                    type = DicomValueType.LIST;
                    asList = new ArrayList<>();
                    List<?> list = (List<?>) value;
                    for (Object item : list) {
                        asList.add(new DicomValue(item));
                    }
                    // Cast to other types if possible
                    asString = null;
                    asInteger = 0;
                    asDouble = 0;
                    asFloat = 0;
                    asShort = 0;
                    asLong = 0;
                    asMap = null;
                    break;
                case "HashMap":
                    type = DicomValueType.MAP;
                    asMap = new HashMap<>();
                    Map<?, ?> map = (Map<?, ?>) value;
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (entry.getKey() instanceof String) {
                            asMap.put((String) entry.getKey(), new DicomValue(entry.getValue()));
                        }
                    }
                    // Cast to other types if possible
                    asString = null;
                    asInteger = 0;
                    asDouble = 0;
                    asFloat = 0;
                    asShort = 0;
                    asLong = 0;
                    asList = null;
                    break;
                default:
                    type = DicomValueType.NULL;
                    asString = null;
                    asInteger = 0;
                    asDouble = 0;
                    asFloat = 0;
                    asShort = 0;
                    asLong = 0;
                    asMap = null;
                    asList = null;
                    break;
            }
        }
    }

    @Override
    public String toString() {
        String value;
        switch (type) {
            case NULL:
                value = "NULL";
                break;
            case MAP:
                value = "{HashMap} size = " + asMap.size();
                break;
            case LIST:
                value = "{ArrayList} size = " + asList.size();
                break;
            default:
                value = asString;
                break;
        }
        return value;
    }
}
