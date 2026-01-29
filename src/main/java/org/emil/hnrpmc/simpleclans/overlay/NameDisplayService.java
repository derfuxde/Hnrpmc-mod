package org.emil.hnrpmc.simpleclans.overlay;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.config.NameRulesStore;

import java.util.List;

public final class NameDisplayService {

    public static void update(SimpleClans plugin, ServerPlayer p) {
        NameRulesStore.Root root = NameRulesStore.get(p.server);

        NameRulesStore.Rule tabOrHeadRule = pickRule(plugin, root.rules(), p, "tablist");
        if (tabOrHeadRule == null) tabOrHeadRule = pickRule(plugin, root.rules(), p, "head");
        if (tabOrHeadRule == null) return;

        String formatted = ClanScoreboard.formatplaceholder(plugin, tabOrHeadRule.format(), p);

        applyTeamName(p, plugin.getSettingsManager().parseConditionalMessage(p, formatted));
    }


    public static String formatForChat(SimpleClans plugin, ServerPlayer p) {
        NameRulesStore.Root root = NameRulesStore.get(p.server);
        NameRulesStore.Rule rule = pickRule(plugin, root.rules(), p, "chat");
        if (rule == null || rule.where() == null || !rule.where().contains("chat")) {
            return p.getGameProfile().getName();
        }
        return ClanScoreboard.formatplaceholder(plugin, rule.format(), p);
    }

    public static NameRulesStore.Rule pickRule(SimpleClans plugin, List<NameRulesStore.Rule> rules, ServerPlayer p, String where) {
        if (rules == null) return null;

        for (NameRulesStore.Rule r : rules) {
            if (r.where() == null || !r.where().contains(where)) continue;

            String when = ClanScoreboard.formatplaceholder(plugin, r.when(), p);
            if (ClanScoreboard.checkconditions(when, p)) {
                return r;
            }
        }
        return null;
    }


    private static void applyTeamName(ServerPlayer p, String coloredFormatted) {
        String baseName = p.getGameProfile().getName();

        String prefix;
        String suffix;

        int idx = coloredFormatted.indexOf(baseName);
        if (idx >= 0) {
            prefix = coloredFormatted.substring(0, idx);
            suffix = coloredFormatted.substring(idx + baseName.length());
        } else {
            prefix = coloredFormatted + " ";
            suffix = "";
        }

        Scoreboard sb = p.server.getScoreboard();

        // Team-ID muss kurz bleiben. 16 Zeichen Limit kann greifen -> stark kürzen.
        String teamId = ("hn" + p.getUUID().toString().replace("-", "")).substring(0, 16);

        PlayerTeam team = sb.getPlayerTeam(teamId);
        if (team == null) {
            team = sb.addPlayerTeam(teamId);
        }

        // Sicherstellen, dass der Spieler im Team ist
        sb.addPlayerToTeam(p.getScoreboardName(), team);

        team.setPlayerPrefix(Component.literal(prefix));
        team.setPlayerSuffix(Component.literal(suffix));
    }
}