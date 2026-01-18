package org.emil.hnrpmc.simpleclans.hooks.placeholder;

import com.mojang.authlib.GameProfile;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.util.Locale;
import java.util.Map;

public final class SimpleClansPlaceholders {

    private final SimpleClans plugin;
    private final PlaceholderService service;

    public SimpleClansPlaceholders(SimpleClans plugin, PlaceholderService service) {
        this.plugin = plugin;
        this.service = service;
    }

    public void registerAll() {
        // Syntax: (GameProfile profile, Object context, String placeholder, Map<String, String> config)

        service.register("tag", (profile, obj, ph, config) -> {
            if (profile == null) return "";
            Clan c = plugin.getClanManager().getClanByPlayerUniqueId(profile.getId());
            return c == null ? "" : c.getTag();
        });

        service.register("name", (profile, obj, ph, config) -> {
            if (profile == null) return "";
            Clan c = plugin.getClanManager().getClanByPlayerUniqueId(profile.getId());
            return c == null ? "" : c.getStringName();
        });

        service.register("rank", (profile, obj, ph, config) -> {
            if (profile == null) return "";
            ClanPlayer cp = plugin.getClanManager().getClanPlayer(profile.getId());
            if (cp == null) return "";
            Clan c = cp.getClan();
            if (c == null) return "";
            var r = c.getRank(cp.getRankId());
            return r == null ? "" : r.getDisplayName();
        });

        service.register("kdr", (profile, obj, ph, config) -> {
            if (profile == null) return "";
            ClanPlayer cp = plugin.getClanManager().getClanPlayer(profile.getId());
            if (cp == null) return "";
            return String.format(Locale.ROOT, "%.2f", cp.getKDR());
        });

        service.register("topplayers", (profile, obj, ph, config) -> {
            // Wir nutzen die Map "config" statt eines String-Arrays "args"
            // Beispiel Platzhalter: %clan_topplayers_limit=5;field=kdr%

            int n = 10;
            try {
                if (config.containsKey("limit")) {
                    n = Integer.parseInt(config.get("limit"));
                }
            } catch (NumberFormatException e) { n = 10; }

            String field = config.getOrDefault("field", "name").toLowerCase(Locale.ROOT);

            var list = plugin.getClanManager().getTopPlayers(n);
            if (list == null) return "";

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                ClanPlayer cp = list.get(i);
                String val = switch (field) {
                    case "name" -> cp.getName();
                    case "kills" -> String.valueOf(cp.getAllyKills() + cp.getAllyKills());
                    case "deaths" -> String.valueOf(cp.getDeaths());
                    case "kdr" -> String.format(Locale.ROOT, "%.2f", cp.getKDR());
                    default -> cp.getName();
                };
                if (i > 0) sb.append(", ");
                sb.append(val == null ? "" : val);
            }
            return sb.toString();
        });
    }
}