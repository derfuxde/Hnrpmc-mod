package org.emil.hnrpmc.simpleclans;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Flags {

    private static final Gson gson = SimpleClans.getInstance().getGSON();
    private final JsonObject flags;

    public Flags(@Nullable String json) {
        if (json != null && !json.isEmpty()) {
            flags = gson.fromJson(json, JsonObject.class);
        } else {
            flags = new JsonObject();
        }
    }

    @NotNull
    public List<String> getStringList(@NotNull String key) {
        JsonElement object = flags.get(key);
        ArrayList<String> list = new ArrayList<>();
        if (object != null && object.isJsonArray()) {
            for (JsonElement element : object.getAsJsonArray()) {
                list.add(element.getAsString());
            }
        }
        return list;
    }

    @Nullable
    public String getString(@NotNull String key) {
        JsonElement element = flags.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsString();
    }

    @NotNull
    public String getString(@NotNull String key, @NotNull String def) {
        String string = getString(key);
        if (string == null) {
            return def;
        }
        return string;
    }

    @NotNull
    public Number getNumber(@NotNull String key) {
        JsonElement element = flags.get(key);
        if (element == null || element.isJsonNull()) {
            return 0;
        }
        return element.getAsNumber();
    }

    public boolean getBoolean(@NotNull String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(@NotNull String key, boolean def) {
        JsonElement element = flags.get(key);
        if (element == null || element.isJsonNull()) {
            return def;
        }
        return element.getAsBoolean();
    }

    public void put(@NotNull String key, @NotNull List<?> value) {
        flags.add(key, gson.toJsonTree(value));
    }

    public void put(@NotNull String key, @NotNull Object object) {
        flags.add(key, gson.toJsonTree(object));
    }

    public String toJSONString() {
        return flags.toString();
    }

    @NotNull
    public String getOrDefault(@NotNull String key, @NotNull String def) {
        JsonElement element = flags.get(key);
        if (element == null || element.isJsonNull()) {
            return def;
        }
        try {
            return element.getAsString();
        } catch (Exception e) {
            return def;
        }
    }

    @NotNull
    public Number getOrDefault(@NotNull String key, @NotNull Number def) {
        JsonElement element = flags.get(key);
        if (element == null || element.isJsonNull()) {
            return def;
        }
        try {
            return element.getAsNumber();
        } catch (Exception e) {
            return def;
        }
    }

    public int getIntOrDefault(@NotNull String key, int def) {
        return getOrDefault(key, def).intValue();
    }

    public double getDoubleOrDefault(@NotNull String key, double def) {
        return getOrDefault(key, def).doubleValue();
    }

    public boolean getOrDefault(@NotNull String key, boolean def) {
        JsonElement element = flags.get(key);
        if (element == null || element.isJsonNull()) {
            return def;
        }
        try {
            return element.getAsBoolean();
        } catch (Exception e) {
            return def;
        }
    }

    @NotNull
    public List<String> getListOrDefault(@NotNull String key, @NotNull List<String> def) {
        JsonElement element = flags.get(key);
        if (element == null || !element.isJsonArray()) {
            return def;
        }

        List<String> list = new ArrayList<>();
        try {
            for (JsonElement e : element.getAsJsonArray()) {
                list.add(e.getAsString());
            }
            return list;
        } catch (Exception e) {
            return def;
        }
    }

}
