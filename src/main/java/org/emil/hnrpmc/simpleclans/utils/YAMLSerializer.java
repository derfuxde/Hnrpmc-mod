package org.emil.hnrpmc.simpleclans.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;


/**
 * Portiert auf NeoForge unter Verwendung von GSON (da YamlConfiguration Bukkit-spezifisch ist).
 */
public class YAMLSerializer {

    private static final Logger LOGGER = SimpleClans.getInstance().getLogger();

    private YAMLSerializer() {
    }

    /**
     * Serialisiert ein Objekt in einen JSON-String (Ersatz für YAML-Serialisierung).
     */
    public static @Nullable String serialize(@Nullable Object obj) {
        if (obj == null) return null;
        try {
            return SimpleClans.getInstance().getGSON().toJson(obj);
        } catch (Exception e) {
            LOGGER.error("Fehler beim Serialisieren des Objekts: {}" , obj.getClass().getName() , e);
            return null;
        }
    }

    /**
     * Deserialisiert einen String zurück in ein Objekt.
     */
    public static <T> @Nullable T deserialize(@Nullable String json, @NotNull Class<T> clazz) {
        if (json == null || json.isEmpty()) return null;
        try {
            return SimpleClans.getInstance().getGSON().fromJson(json, clazz);
        } catch (Exception e) {
            LOGGER.warn("Fehler beim Deserialisieren von {}... Inhalt: {}" , clazz.getSimpleName() , json);
        }

        return null;
    }
}