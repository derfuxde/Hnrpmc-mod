package org.emil.hnrpmc.simpleclans.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class NameRulesStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Root cached;

    public record Rule(String id, String when, List<String> where, String format) {}
    public record Root(List<Rule> rules) {}

    public static Root get(MinecraftServer server) {
        if (cached == null) cached = load(server);
        return cached;
    }

    public static void reload(MinecraftServer server) {
        cached = load(server);
    }

    private static Root load(MinecraftServer server) {
        try {
            Path dir = server.getFile("config/hnrpmc").toAbsolutePath();
            Path file = dir.resolve("name_rules.json");
            Files.createDirectories(dir);

            if (!Files.exists(file)) {
                Root def = defaultRoot();
                Files.writeString(file, GSON.toJson(def));
                return def;
            }

            Root root = GSON.fromJson(Files.readString(file), Root.class);
            if (root == null || root.rules == null || root.rules.isEmpty()) return defaultRoot();
            return root;
        } catch (Exception e) {
            return defaultRoot();
        }
    }

    private static Root defaultRoot() {
        return new Root(List.of(
                new Rule("default", "true", List.of("head", "tablist", "chat"), "§7%playername%"),
                new Rule("clan", "%in_clan% == true", List.of("head", "tablist", "chat"),
                        "%clan_color%[%clan_tag%] §f%playername% §7(%clan_rank_name%)")
        ));
    }
}
