/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.util;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttribUtils {
    private static final Logger log = LoggerFactory.getLogger(AttribUtils.class);

    private static final String UNKNOWN = "Unknown";
    private static final Pattern hexTag = Pattern.compile("^0[xX][0-9a-fA-F]{8}$");
    private static final Pattern intTag = Pattern.compile("^\\d+$");
    private static final Pattern pairTag = Pattern.compile("^([0-9a-fA-F]{4})\\W([0-9a-fA-F]{4})$");
    private static final Pattern tagName = Pattern.compile("\\$\\{([A-Za-z0-9,]+?)}");

    private AttribUtils() { }

    /**
     * Serializes an Attributes object into an InputStream
     */
    public static InputStream serialize(Attributes data) throws IOException {
        InputStream inputStream;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(data);
                oos.flush();
            }
            inputStream = new ByteArrayInputStream(baos.toByteArray());
        }
        return inputStream;
    }

    /**
     * Deserializes an InputStream back into an Attributes object
     */
    public static Attributes deserialize(InputStream data) throws IOException, ClassNotFoundException {
        Object obj;
        try (ObjectInputStream ois = new ObjectInputStream(data)) {
            obj = ois.readObject();
        }
        if (!(obj instanceof Attributes)) throw new ClassNotFoundException("Data is not of type Attributes");
        return (Attributes)obj;
    }

    /**
     * Modifies the tags of an Attributes object (i.e. image)
     * @param data The image data
     * @param changeTags Map of tag names to create or change.
     */
    public static void updateTags(Attributes data, Map<String, String> changeTags) {
        // Apply tag changes to the image
        if (changeTags != null && !changeTags.isEmpty()) {
            Map<String, Object> tags = new HashMap<>();
            for (Map.Entry<String, String> entry : changeTags.entrySet()) {
                String value = entry.getValue();
                // Check for replacement tags
                List<String> tagNames = extractTagReplacements(value);
                if (!tagNames.isEmpty()) {
                    Map<String, Object> map = extractTags(data, tagNames);
                    for (Map.Entry<String, Object> copyEntry : map.entrySet()) {
                        value = applyTagReplacement(value, copyEntry.getKey(), (String) copyEntry.getValue());
                    }
                    changeTags.replace(entry.getKey(), value);
                }
                tags.put(entry.getKey(), value);
            }
            AttribUtils.addKeys(data, tags);
        }
    }

    private static List<String> extractTagReplacements(String value) {
        List<String> matchString = new ArrayList<>();
        Matcher m = tagName.matcher(value);
        while (m.find()) {
            matchString.add(m.group(1));
        }
        return matchString;
    }
    private static String applyTagReplacement(String value, String tagName, String replaceValue) {
        String regex = String.format("\\$\\{%s\\}", tagName);
        return value.replaceAll(regex, replaceValue);
    }

    private static int stringToTag(String name) {
        int tag = -1;
        if (hexTag.matcher(name).matches()) {
            tag = Integer.decode(name);
        } else if (intTag.matcher(name).matches()) {
            tag = Integer.parseInt(name);
        } else if (pairTag.matcher(name).matches()) {
            Matcher m = pairTag.matcher(name);
            if (m.find()) {
                int group = Integer.decode("0x" + m.group(1));
                int element = Integer.decode("0x" + m.group(2));
                tag = (group << 16) + element;
            }
        } else {
            tag = ElementDictionary.tagForKeyword(name, null);
        }
        if (tag == -1) {
            throw new IllegalArgumentException(name);
        }
        return tag;
    }

    private static void addKeys(Attributes keys, Map<String, Object> entities) {
        Map<Integer,Object> privateTags = new HashMap<>();
        for (Map.Entry<String, Object> key : entities.entrySet()) {
            int tag = stringToTag(key.getKey());
            if (TagUtils.isPrivateCreator(tag)) {
                keys.setString(tag, VR.LO, (String) key.getValue());
            } else if (TagUtils.isPrivateTag(tag)) {
                privateTags.put(tag, key.getValue());
            } else {
                VR vr = ElementDictionary.vrOf(tag, null);
                keys.setValue(tag, vr, key.getValue());
            }
        }
        // Add the private tags
        for (Map.Entry<Integer, Object> key : privateTags.entrySet()) {
            int tag = key.getKey();
            int creatorTag = TagUtils.creatorTagOf(tag);
            String privateCreator = keys.getString(creatorTag, UNKNOWN);
            keys.setString(privateCreator, tag, VR.LO, (String)key.getValue());
        }
    }
    public static Attributes toKeys(Map<String, Object> entities) {
        Attributes keys = new Attributes();
        addKeys(keys, entities);
        return keys;
    }

    public static Map<String, Object> extractTags(Attributes data, List<String> tags) {
        Map<String, Object> map = new HashMap<>();
        for (String tagName : tags) {
            int tag = stringToTag(tagName);
            tagToMap(data, tag, map);
        }
        return map;
    }

    public static Map<String,Object> attributesToMap(Attributes data) {
        Map<String,Object> map = new HashMap<>();
        if (data != null) {
            for (int tag : data.tags()) {
                tagToMap(data, tag, map);
            }
        }
        return map;
    }

    public static void upsertMap(String key, Object value, Map<String, Object> map) {
        if (map.containsKey(key)) map.replace(key, value);
        else map.put(key, value);
    }
    public static void upsertMap(Map<String, Object> fromMap, Map<String, Object> toMap) {
        for (Map.Entry<String, Object> entry : fromMap.entrySet()) {
            upsertMap(entry.getKey(), entry.getValue(), toMap);
        }
    }

    public static int getMapInteger(Map<String, Object> map, String key, int defaultValue) {
        if (map.containsKey(key)) {
            Object value = map.get(key);
            String simpleName = value.getClass().getSimpleName();
            if (simpleName.equals("Integer")) return (int)value;
        }
        return defaultValue;
    }

    public static String getFirstString(Attributes data, Integer[] tags) {
        for (int tag : tags) {
            String value = data.getString(tag);
            if (value != null) return value;
        }
        return null;
    }

    private static void tagToMap(Attributes data, int tag, Map<String,Object> map) {
        // Get private creator of this tag
        String privateCreator = null;
        String tagName;
        if (TagUtils.isPrivateCreator(tag)) return;
        else if (TagUtils.isPrivateTag(tag)) {
            int creatorTag = TagUtils.creatorTagOf(tag);
            privateCreator = data.getString(creatorTag, UNKNOWN);
            tagName = String.format("%s (%04x,%04x)", privateCreator, tag >> 16, tag & 0xFFFF);
        } else {
            tagName = ElementDictionary.keywordOf(tag, null);
        }
        if (tagName.isEmpty()) tagName = UNKNOWN;
        Object tagValue = data.getValue(privateCreator, tag);
        if (tagValue instanceof Sequence) {
            Sequence tagSeq = (Sequence)tagValue;
            List<Map<String,Object>> list = new ArrayList<>();
            for (Attributes d : tagSeq) {
                Map<String,Object> m = new HashMap<>();
                for (int t : d.tags()) {
                    tagToMap(d, t, m);
                }
                list.add(m);
            }
            map.put(tagName, list);
        } else {
            vrToMap(data, tag, tagName, map);
        }
    }

    private static void vrToMap(Attributes data, int tag, String tagName, Map<String,Object> map) {
        VR vr = data.getVR(tag);
        if (vr == null) map.put(tagName, null);
        else if (vr.isIntType()) map.put(tagName, data.getInt(tag, 0));
        else if (vr.isStringType()) map.put(tagName, data.getString(tag));
        else {
            switch (vr) {
                case FD: // Floating Point Double (double)
                case OD: // Other Double (double)
                    map.put(tagName, data.getDouble(tag, 0));
                    break;
                case FL: // Floating Point Single (float)
                case OF: // Other Float (float)
                    map.put(tagName, data.getFloat(tag, 0));
                    break;
                case OL: // Other Long (int)
                case SL: // Signed Long (int)
                case US: // Unsigned Short (ushort)
                    map.put(tagName, data.getInt(tag, 0));
                    break;
                case OW: // Other Word (short)
                case SS: // Signed Short (short)
                    map.put(tagName, (short)data.getInt(tag, 0));
                    break;
                case OV: // Other 64-bit Very Long (long)
                case SV: // Signed 64-bit Long (long)
                case UL: // Unsigned Long (uint)
                case UV: // Unsigned 64-bit Long (ulong)
                    map.put(tagName, data.getLong(tag, 0));
                    break;
                case AT: // tag
                    break;
                case OB: // Other Byte (byte)
                case UN: // Unknown (byte)
                    try {
                        map.put(tagName, data.getBytes(tag));
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                    break;
                case SQ: // Sequence of Items
                    List<Map<String, Object>> list = new ArrayList<>();
                    Sequence seq = data.getSequence(tag);
                    for (Attributes a : seq) {
                        Map<String, Object> seqMap = attributesToMap(a);
                        if (!seqMap.isEmpty()) list.add(seqMap);
                    }
                    map.put(tagName, list);
                    break;
                default: // String
                    map.put(tagName, data.getString(tag));
                    break;
            }
        }
    }
}