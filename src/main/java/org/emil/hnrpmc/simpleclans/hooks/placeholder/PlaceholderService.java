package org.emil.hnrpmc.simpleclans.hooks.placeholder;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PlaceholderService {

    private final String prefix;
    private final Map<String, PlaceholderResolver> resolvers = new ConcurrentHashMap<>();

    public PlaceholderService(@NotNull String prefix) {
        this.prefix = prefix.toLowerCase(Locale.ROOT);
    }

    public String getPrefix() {
        return prefix;
    }

    public void register(@NotNull String id, @NotNull PlaceholderResolver resolver) {
        resolvers.put(id.toLowerCase(Locale.ROOT), resolver);
    }

    public void unregister(@NotNull String id) {
        resolvers.remove(id.toLowerCase(Locale.ROOT));
    }

    public boolean has(@NotNull String id) {
        return resolvers.containsKey(id.toLowerCase(Locale.ROOT));
    }

    public String apply(@NotNull PlaceholderContext ctx, String input) {
        if (input == null || input.isEmpty()) return input;

        StringBuilder out = new StringBuilder(input.length());
        int i = 0;

        while (i < input.length()) {
            int start = input.indexOf('%', i);
            if (start < 0) {
                out.append(input, i, input.length());
                break;
            }
            int end = input.indexOf('%', start + 1);
            if (end < 0) {
                out.append(input, i, input.length());
                break;
            }

            out.append(input, i, start);

            String token = input.substring(start + 1, end);
            String replaced = resolveToken(ctx, token);
            if (replaced == null) {
                out.append('%').append(token).append('%');
            } else {
                out.append(replaced);
            }

            i = end + 1;
        }

        return out.toString();
    }

    private String resolveToken(PlaceholderContext ctx, String token) {
        String lower = token.toLowerCase(Locale.ROOT);
        if (!lower.startsWith(prefix + "_")) return null;

        // "rest" ist alles nach "prefix_"
        String rest = token.substring(prefix.length() + 1);
        if (rest.isEmpty()) return null;

        // Wir splitten beim ersten "_" nach der ID, um die Config-Parameter zu trennen
        // Beispiel: "topplayers_limit=5;field=kdr" -> ["topplayers", "limit=5;field=kdr"]
        String[] parts = rest.split("_", 2);
        String id = parts[0].toLowerCase(Locale.ROOT);

        PlaceholderResolver resolver = resolvers.get(id);
        if (resolver == null) return null;

        // Parameter parsen
        Map<String, String> config = new HashMap<>();
        if (parts.length > 1) {
            config = parseConfig(parts[1]);
        }

        try {
            // Hier ist die angepasste Zeile für NeoForge/Minecraft:
            // Wir nutzen ctx.player().getGameProfile() wie gewünscht.
            return resolver.resolve(
                    ctx.player() != null ? ctx.player().getGameProfile() : null,
                    ctx.object(), // Falls du den Context/Object-Parameter im Interface behalten hast
                    token,
                    config
            );
        } catch (Throwable t) {
            return "";
        }
    }

    /**
     * Hilfsmethode zum Parsen von "key1=val1;key2=val2" in eine Map.
     */
    private Map<String, String> parseConfig(String configStr) {
        Map<String, String> map = new HashMap<>();
        if (configStr == null || configStr.isEmpty()) return map;

        // Wir splitten bei ";" für mehrere Parameter
        String[] pairs = configStr.split(";");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0].toLowerCase(Locale.ROOT), kv[1]);
            }
        }
        return map;
    }
}
