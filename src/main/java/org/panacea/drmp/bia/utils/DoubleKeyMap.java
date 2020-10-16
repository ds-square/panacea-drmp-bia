package org.panacea.drmp.bia.utils;


import org.panacea.drmp.bia.domain.configFiles.AttackerType;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class DoubleKeyMap<T> {
    private Map<String, Map<AttackerType, T>> map;

    public DoubleKeyMap() {
        this.map = new HashMap<>();
    }

    public void put(String key1, Map<AttackerType, T> value) {
        this.map.put(key1, value);
    }

    public void put(String key1, AttackerType key2, T value) {
        Map<AttackerType, T> innerMap = this.map.getOrDefault(key1, new HashMap<>());
        innerMap.put(key2, value);
        this.map.put(key1, innerMap);
    }

    public Map<AttackerType, T> get(String key1) {
        return this.map.get(key1);
    }

    public T get(String key1, AttackerType key2) {
        Map<AttackerType, T> innerMap = this.map.get(key1);
        if (innerMap == null) return null;
        return innerMap.get(key2);
    }

    public T get(String key1, AttackerType key2, T defaultValue) {
        Map<AttackerType, T> innerMap = this.map.get(key1);
        if (innerMap == null) return defaultValue;
        return innerMap.getOrDefault(key2, defaultValue);
    }

    public boolean containsFirstKey(String key1) {
        return this.map.containsKey(key1);
    }

    public boolean containsBothKeys(String key1, AttackerType key2) {
        if (!this.map.containsKey(key1)) return false;
        return this.map.get(key1).containsKey(key2);
    }

    @Override
    public String toString() {
        StringJoiner result = new StringJoiner("; ");
        for (String key1 : this.map.keySet()) {
            result.add(key1 + ":" + this.map.get(key1).toString());
        }
        return "[" + result.toString() + "]";
    }
}
