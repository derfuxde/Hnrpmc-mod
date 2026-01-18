package org.emil.hnrpmc.simpleclans.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.io.*;
import java.util.*;

public class YamlConfig {
    private final File file;
    // TreeMap sorgt für alphabetische Sortierung der Hauptkategorien
    private Map<String, Object> data = new TreeMap<>();
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()      // Das sorgt für das "untereinander" Speichern
            .disableHtmlEscaping()    // Sorgt dafür, dass '&' nicht zu '\u0026' wird
            .create();

    public YamlConfig(File file) {
        this.file = file;
    }

    public void load() {
        if (!file.exists()) {
            save();
            return;
        }

        try (Reader reader = new FileReader(file)) {
            java.lang.reflect.Type type = new com.google.common.reflect.TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> loaded = gson.fromJson(reader, type);

            if (loaded != null) {
                this.data.clear();
                this.data.putAll(loaded);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeMaps(Map<String, Object> defaultData, Map<String, Object> loadedData) {
        for (Map.Entry<String, Object> entry : loadedData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map && defaultData.get(key) instanceof Map) {
                mergeMaps((Map<String, Object>) defaultData.get(key), (Map<String, Object>) value);
            } else {
                defaultData.put(key, value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Object get(String path) {
        String[] keys = path.split("\\.");
        Object current = data;
        for (String key : keys) {
            if (!(current instanceof Map)) return null;
            current = ((Map<String, Object>) current).get(key);
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    public void set(String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = data;
        for (int i = 0; i < keys.length - 1; i++) {
            // Auch hier TreeMap für Unterkategorien nutzen
            current = (Map<String, Object>) current.computeIfAbsent(keys[i], k -> new TreeMap<String, Object>());
        }
        current.put(keys[keys.length - 1], value);
    }

    // Getter-Hilfsmethoden
    public String getString(String path, String def) {
        Object val = get(path);
        return (val != null) ? val.toString() : def;
    }

    public boolean getBoolean(String path, boolean def) {
        Object val = get(path);
        return (val instanceof Boolean) ? (Boolean) val : def;
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path) {
        Object val = get(path);
        if (val instanceof List) return (List<String>) val;
        return new ArrayList<>();
    }

    public List<Map<?,?>> getMapList(String path) {
        Object val = get(path);
        if (val instanceof List) return (List<Map<?,?>>) val;
        return new ArrayList<>();
    }
}