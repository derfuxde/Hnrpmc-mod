package org.emil.hnrpmc.simpleclans.proxy.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Portierter Adapter für Objekte, die eine Map-basierte Serialisierung nutzen.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ConfigurationSerializableAdapter implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<?> rawType = type.getRawType();

        // Wir prüfen, ob die Klasse eine "serialize" Methode besitzt (Ersatz für Bukkit Interface)
        if (!hasSerializeMethod(rawType)) {
            return null;
        }

        TypeAdapter<Map> mapAdapter = gson.getAdapter(Map.class);

        return (TypeAdapter<T>) new TypeAdapter<Object>() {
            @Override
            public void write(JsonWriter out, Object value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                try {
                    // Ruft die 'serialize' Methode des Objekts auf
                    Method serialize = value.getClass().getMethod("serialize");
                    Map<String, Object> map = (Map<String, Object>) serialize.invoke(value);
                    mapAdapter.write(out, map);
                } catch (Exception e) {
                    throw new IOException("Failed to serialize object " + value.getClass().getName(), e);
                }
            }

            @Override
            public Object read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }

                Map<String, Object> map = mapAdapter.read(in);
                return deserialize(rawType, map);
            }
        };
    }

    private boolean hasSerializeMethod(Class<?> clazz) {
        try {
            clazz.getMethod("serialize");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Ersatz für ConfigurationSerialization.deserializeObject
     */
    private @Nullable Object deserialize(Class<?> clazz, Map<String, Object> map) throws IOException {
        try {
            // Bukkit Konvention: Suche nach statischer Methode 'deserialize(Map)' oder 'valueOf(Map)'
            try {
                Method method = clazz.getDeclaredMethod("deserialize", Map.class);
                return method.invoke(null, map);
            } catch (NoSuchMethodException e) {
                Method method = clazz.getDeclaredMethod("valueOf", Map.class);
                return method.invoke(null, map);
            }
        } catch (Exception e) {
            throw new IOException("Could not find a deserialization method for " + clazz.getName(), e);
        }
    }
}