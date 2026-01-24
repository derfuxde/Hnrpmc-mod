package org.emil.hnrpmc.hnessentials.cosmetics.utils;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * General utilities used by the implementation.
 */
public class Yootil {
    private static final Pattern UNDASHED_UUID_GAPS = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
    private static final String UUID_DASHIFIER_REPLACEMENT = "$1-$2-$3-$4-$5";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Read json string but only if the element is a JSON primitive. Otherwise, return null.
     * @param elem the json element.
     * @return the parsed value; see method description.
     */
    @Nullable
    public static String readNullableJsonString(JsonElement elem) {
        if (elem.isJsonPrimitive()) {
            return elem.getAsString();
        }
        else {
            return null;
        }
    }

    public static String urlFlag(String flag, boolean toggle) {
        return toggle ? flag : "";
    }

    public static String urlEncode(@Nullable UUID value) {
        return value == null ? "" : value.toString();
    }

    public static String urlEncode(@Nullable String value) {
        if (value == null) return "";

        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    public static String hash(byte[]... bytes) {
        // concatenate the arrays
        int space = 0;
        for (byte[] arr : bytes) space += arr.length;
        byte[] theBigMan = new byte[space];

        space = 0;

        for (byte[] arr : bytes) {
            System.arraycopy(arr, 0, theBigMan, space, arr.length);
            space += arr.length;
        }

        try {
            // Minecraft Hash
            // https://gist.github.com/unascribed/70e830d471d6a3272e3f
            return new BigInteger(MessageDigest.getInstance("SHA-1").digest(theBigMan)).toString(16);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("For some reason your computer's java install thinks SHA-1 is not a hashing algorithm.", e);
        }
    }

    public static String loadOrCache(File file, @Nullable String value) {
        try {
            if (value != null) {
                file.createNewFile();

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(value);
                }
            } else if (file.isFile()) {
                value = new String(Files.readAllBytes(file.toPath())).trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    public static String base64Ip(InetSocketAddress ip) {
        byte[] arr = (ip.getAddress().getHostAddress() + ":" + ip.getPort()).getBytes(StandardCharsets.UTF_8);
        return Base64.encodeBase64String(arr);
    }

    public static String base64(String text) {
        return Base64.encodeBase64String(text.getBytes(StandardCharsets.UTF_8));
    }

    public static UUID toUUID(String uuid) {
        return UUID.fromString(uuid.length() == 36 ? uuid : UNDASHED_UUID_GAPS.matcher(uuid).replaceAll(UUID_DASHIFIER_REPLACEMENT));
    }

    public static List<String> toStringList(JsonArray arr) {
        List<String> result = new ArrayList<>();

        for (JsonElement e : arr) {
            result.add(e.getAsString());
        }

        return result;
    }

    public static <T> List<T> map(JsonArray arr, Function<JsonElement, T> mapping) {
        List<T> result = new ArrayList<>();

        for (JsonElement e : arr) {
            result.add(mapping.apply(e));
        }

        return result;
    }

    public static <T> List<T> mapObjects(JsonArray arr, Function<JsonObject, T> mapping) {
        List<T> result = new ArrayList<>();

        for (JsonElement e : arr) {
            result.add(mapping.apply(e.getAsJsonObject()));
        }

        return result;
    }

    public static <T> Map<String, T> mapObject(JsonObject arr, Function<JsonElement, T> mapping) {
        Map<String, T> result = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : arr.entrySet()) {
            String key = entry.getKey();
            result.put(key, mapping.apply(arr.get(key)));
        }

        return result;
    }

    public static byte[] randomBytes(int number) {
        byte[] result = new byte[number];
        SECURE_RANDOM.nextBytes(result);
        return result;
    }

    public static String firstNonNull(Object... objects) throws IllegalArgumentException {
        for (Object o : objects) {
            if (o != null) {
                return String.valueOf(o);
            }
        }

        throw new IllegalArgumentException("All objects are null!");
    }
}