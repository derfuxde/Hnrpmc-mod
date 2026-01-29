package org.emil.hnrpmc.simpleclans.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ClanConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record Root(List<Board> boards, List<Tablist> tablists) {}
    public record Board(String id, String conditions, String title, List<String> lines) {}
    public record Tablist(String id, String conditions, String header, String footer) {}

    private static Root cached;

    public static Root get(MinecraftServer server) {
        if (cached == null) cached = load(server);
        return cached;
    }

    public static void reload(MinecraftServer server) {
        cached = load(server);
    }

    private static Root load(MinecraftServer server) {
        Path dir = server.getFile("config/hnrpmc").toAbsolutePath();
        Path file = dir.resolve("scoreboards.json");

        try {
            Files.createDirectories(dir);

            if (!Files.exists(file)) {
                Root def = defaultRoot();
                Files.writeString(file, GSON.toJson(def));
                return def;
            }

            String json = Files.readString(file);
            Root root = GSON.fromJson(json, Root.class);
            if (root == null || root.boards == null || root.boards.isEmpty()) return defaultRoot();
            return root;

        } catch (IOException | JsonParseException e) {
            return defaultRoot();
        }
    }

    private static Root defaultRoot() {
        return new Root(List.of(
                new Board("default", "true", "§6§lMein Server", List.of(
                        "§fName: §7%playername%",
                        "§fSpielzeit: §7%playtime%",
                        "§eTritt einem Clan bei!"
                )),
                new Board("clan", "%in_clan% == true", "§6§lMein Server", List.of(
                        "§fName: §7%playername%",
                        "§fSpielzeit: §7%playtime%",
                        "§8----------------",
                        "§fClan: §a%clan_name%",
                        "§fRang: §b%clan_rank%"
                ))), List.of(
                new Tablist("tab", "true",
                             """
                             &0&m                                                &7
                             &r&3&lSurvival
                             &r&7&l>> Willkommen&3 &l%playername%&7&l! &7&l<<
                             &r&7Aktive Spieler: &f%server_players%
                             
                             """,
                        """
                        %hnph_ani_time%
                        
                        &0&m                                                &7
                        """
                )
        ));
    }
}
