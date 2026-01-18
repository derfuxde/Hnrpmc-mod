package org.emil.hnrpmc.simpleclans.overlay;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.*;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.emil.hnrpmc.simpleclans.config.ClanConfig;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.Rank;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.awt.SystemColor.text;

public final class ClanScoreboard {
    private static final String OBJECTIVE_ID = "clan_ui";

    public static void update(SimpleClans plugin, ServerPlayer player, int OBJ_ID) {
        // Eindeutige ID pro Spieler, damit sie sich nicht gegenseitig überschreiben
        String finalid = OBJECTIVE_ID + "_" + OBJ_ID;

        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective(finalid);

        ClanConfig.Board config = null;
        var root = ClanConfig.get(player.server);
        List<ClanConfig.Board> boards = root.boards();

        for (ClanConfig.Board o : boards) {
            String cons = formatplaceholder(plugin, o.conditions(), player);
            if (checkconditions(cons, player)) {
                config = o;
                break; // Wichtig: Nimm das erste passende Board
            }
        }

        if (config == null) return;

        // 1. Objective erstellen/holen
        if (obj == null) {
            obj = board.addObjective(finalid, ObjectiveCriteria.DUMMY,
                    Component.literal(config.title()), ObjectiveCriteria.RenderType.INTEGER, true, null);
        } else {
            obj.setDisplayName(Component.literal(config.title()));
        }

        // 2. Sidebar-Slot NUR für diesen Spieler setzen via Paket
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, obj));

        // 3. WICHTIG: Scores auf dem echten Board zurücksetzen
        // Da wir eine Player-eindeutige finalid haben, stören wir andere nicht.
        for (ScoreHolder holder : board.getTrackedPlayers()) {
            board.resetAllPlayerScores(holder);
        }

        List<String> configLines = config.lines();
        int currentScore = configLines.size();
        for (String line : configLines) {
            String formattedLine = formatplaceholder(plugin, line, player);
            add(player, board, obj, currentScore--, formattedLine);
        }
    }

    public static boolean checkconditions(String cons, ServerPlayer player) {
        if (cons == null) return false;
        cons = cons.trim();
        if (cons.isEmpty()) return false;

        // OR hat die niedrigere Priorität → zuerst splitten
        String[] orParts = cons.split("\\s*\\|\\|\\s*");
        if (orParts.length > 1) {
            for (String part : orParts) {
                if (checkconditions(part, player)) {
                    return true;
                }
            }
            return false;
        }

        // AND
        String[] andParts = cons.split("\\s*&&\\s*");
        if (andParts.length > 1) {
            for (String part : andParts) {
                if (!checkconditions(part, player)) {
                    return false;
                }
            }
            return true;
        }

        // Einzelbedingung
        return evalSingle(cons);
    }


    private static boolean evalSingle(String cons) {
        cons = cons.trim();

        if (cons.equalsIgnoreCase("true")) return true;
        if (cons.equalsIgnoreCase("false")) return false;

        String[] parts = cons.split("\\s+");
        if (parts.length != 3) return false;

        String leftS = parts[0];
        String op    = parts[1];
        String rightS= parts[2];

        if (isInt(leftS) && isInt(rightS)) {
            int left = Integer.parseInt(leftS);
            int right = Integer.parseInt(rightS);
            return switch (op) {
                case "<"  -> left < right;
                case ">"  -> left > right;
                case "<=" -> left <= right;
                case ">=" -> left >= right;
                case "==" -> left == right;
                case "!=" -> left != right;
                default -> false;
            };
        }

        return switch (op) {
            case "==" -> leftS.equalsIgnoreCase(rightS);
            case "!=" -> !leftS.equalsIgnoreCase(rightS);
            default -> false;
        };
    }


    private static boolean isInt(String s) {
        try { Integer.parseInt(s); return true; }
        catch (Exception e) { return false; }
    }


    public static String formatplaceholder(SimpleClans plugin, String string, ServerPlayer player) {
        Clan clan = plugin.getClanManager().getClanByPlayerUniqueId(player.getUUID());
        boolean hasClan = (clan != null);

        String clanName = hasClan ? clan.getStringName() : "";
        String clanTag = hasClan ? clan.getColorTag() : "";
        String clanColor = clan != null ? (clan.getClanColor() != null && !Objects.equals(clan.getClanColor(), "") ? clan.getClanColor() : "§b") : "§b";
        String clanmembers = String.valueOf(hasClan ? clan.getMembers().size() : 0);

        String serverplayercount = String.valueOf(player.server.getPlayerCount());
        String servermaxplayers = String.valueOf(player.server.getMaxPlayers());
        String clanRankId = "";
        if (hasClan) {
            ClanPlayer member = plugin.getClanManager().getClanPlayer(player.getUUID().toString());
            clanRankId = (member != null) ? member.getRankId() : "Member";
        }
        String clanRankName = "";
        if (hasClan) {

            ClanPlayer cp = plugin.getClanManager().getClanPlayer(player.getUUID());

            if (cp != null) {
                clanRankName = cp.getRankDisplayName(); // Nutzt unsere reparierte Logik
            }


            clanRankName = (cp != null) ? clanRankName : "Member";
        }
        String onlineCount = hasClan ? String.valueOf(clan.getOnlineMembers().size()) : "0";

        String isinclan = hasClan ? "yes" : "no";

        String ping = String.valueOf(player.connection.latency());

        String pingcolor = "§a";
        if (player.connection.latency() >= 50) {
            pingcolor = "§6";
        }else if (player.connection.latency() >= 100) {
            pingcolor = "§c";
        }

        String coloredping = pingcolor + ping;

        Pattern pattern = Pattern.compile("%scoreboard_([^%]+)%");
        Matcher matcher = pattern.matcher(string);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            // Text vor dem Platzhalter hinzufügen
            sb.append(string, lastEnd, matcher.start());

            String objectiveName = matcher.group(1);
            var objective = player.getScoreboard().getObjective(objectiveName);

            // Score auslesen (mit deiner getScore Logik)
            int scoreValue = 0;
            if (objective != null) {
                var scoreInfo = player.getScoreboard().getPlayerScoreInfo(player, objective);
                if (scoreInfo != null) {
                    scoreValue = scoreInfo.value();
                }
            }

            sb.append(scoreValue);
            lastEnd = matcher.end();
        }
        sb.append(string.substring(lastEnd));
        String currentString = sb.toString();

        String allowflying = player.mayFly() ? "true" : "false";

        Inventory playerinv = player.getInventory();
        ItemStack h = playerinv.armor.get(3);
        String player_armor_helmet_name =  "";
        String player_armor_helmet_durability = "";
        if (!h.isEmpty()) {
            player_armor_helmet_name = h.getDisplayName().getString();

            player_armor_helmet_durability = String.valueOf((h.getMaxDamage() - h.getDamageValue()));
        }

        ItemStack c = playerinv.armor.get(2);
        String player_armor_chestplate_name =  "";
        String player_armor_chestplate_durability = "";
        if (!c.isEmpty()) {
            player_armor_chestplate_name = c.getDisplayName().getString();

            player_armor_chestplate_durability = String.valueOf((c.getMaxDamage() - c.getDamageValue()));
        }

        ItemStack l = playerinv.armor.get(1);
        String player_armor_leggings_name =  "";
        String player_armor_leggings_durability = "";
        if (!c.isEmpty()) {
            player_armor_leggings_name = l.getDisplayName().getString();

            player_armor_leggings_durability = String.valueOf((l.getMaxDamage() - l.getDamageValue()));
        }

        ItemStack b = playerinv.armor.get(0);
        String player_armor_boots_name =  "";
        String player_armor_boots_durability = "";
        if (!b.isEmpty()) {
            player_armor_boots_name = b.getDisplayName().getString();

            player_armor_boots_durability = String.valueOf((b.getMaxDamage() - b.getDamageValue()));
        }

        String plhealth = String.valueOf(player.getHealth());

        return currentString
                .replace("%playername%", player.getGameProfile().getName())
                .replace("%player_allow_flight%", allowflying)
                .replace("%playtime%", formatPlaytime(player))
                .replace("%player_armor_helmet_name%", player_armor_helmet_name)
                .replace("%player_armor_helmet_durability%", player_armor_helmet_durability)
                .replace("%player_armor_chestplate_name%", player_armor_chestplate_name)
                .replace("%player_armor_chestplate_durability%", player_armor_chestplate_durability)
                .replace("%player_armor_leggings_name%", player_armor_leggings_name)
                .replace("%player_armor_leggings_durability%", player_armor_leggings_durability)
                .replace("%player_armor_boots_name%", player_armor_boots_name)
                .replace("%player_armor_boots_durability%", player_armor_boots_durability)
                .replace("%player_health%", plhealth)
                .replace("%clan_name%", clanName)
                .replace("%clan_tag%", clanTag)
                .replace("%clan_color%", clanColor)
                .replace("%clan_rank_name%", clanRankName)
                .replace("%clan_rank_id%", clanRankId)
                .replace("%isinclan%", isinclan)
                .replace("%clan_members%", clanmembers)
                .replace("%clan_onlinemembers%", onlineCount)
                .replace("%server_players%", serverplayercount)
                .replace("%player_ping_colored%", coloredping)
                .replace("%player_ping%", ping)
                .replace("%server_maxplayers%", servermaxplayers);
    }

    private static void add(ServerPlayer player, Scoreboard board, Objective obj, int score, String text) {
        String salt = player.getUUID().toString().substring(0, 8);
        String unique = text + ChatFormatting.RESET; //+ salt + ChatFormatting.values()[score % 16];
        board.getOrCreatePlayerScore(ScoreHolder.forNameOnly(unique), obj).set(score);
    }

    private static String formatPlaytime(ServerPlayer player) {
        int ticks = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
        int hours = (ticks / 20) / 3600;
        int minutes = ((ticks / 20) % 3600) / 60;
        return hours + "h " + minutes + "m";
    }
}