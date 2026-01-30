package org.emil.hnrpmc.simpleclans.commands.clan;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.Hnrpmod;
import org.emil.hnrpmc.hnclaim.Claim;
import org.emil.hnrpmc.hnclaim.HNClaims;
import org.emil.hnrpmc.hnclaim.claimperms;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.Home;
import org.emil.hnrpmc.simpleclans.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static net.minecraft.commands.SharedSuggestionProvider.suggest;

public final class Suggestions {

    public static SuggestionProvider<CommandSourceStack> clansHideOwn(SimpleClans plugin) {
        return (ctx, b) -> suggest(plugin.getClanManager().getAllClanTagsHideOwn(ctx.getSource()), b);
    }

    public static SuggestionProvider<CommandSourceStack> Allclans(SimpleClans plugin) {
        return (ctx, b) -> suggest(plugin.getClanManager().getClans().stream().map(Clan::getStringName), b);
    }

    public static SuggestionProvider<CommandSourceStack> rivals(SimpleClans plugin) {
        return (ctx, b) -> suggest(plugin.getClanManager().getRivalTags(ctx.getSource()), b);
    }

    public static SuggestionProvider<CommandSourceStack> allclaimperms(HNClaims plugin) {
        return (ctx, b) -> suggest(Arrays.stream(claimperms.values()).map(cperm -> cperm.getPermName().toLowerCase().replace(" ", "_")),b);
    }

    public static SuggestionProvider<CommandSourceStack> claimsfromplayer(HNClaims plugin) {

        return (ctx, b) -> {
            List<String> claimlist = new ArrayList<>(plugin.getClaimManager().getClaims().stream().filter(claim -> claim.getownerUUID().equals(ctx.getSource().getPlayer().getUUID())).map(Claim::getName).toList());
            claimlist.add("alle");
            claimlist.sort((o1, o2) -> {
                if (o1.equals("alle")) return -1;
                if (o2.equals("alle")) return 1;
                return o1.compareToIgnoreCase(o2);
            });
            return SharedSuggestionProvider.suggest(claimlist,b);
        };
    }

    public static SuggestionProvider<CommandSourceStack> clanColors(SimpleClans plugin) {
        List<String> colorlist = new ArrayList<>();
        for (ClanColors color: ClanColors.values()) {
            colorlist.add(color.name());
        }
        return (ctx, b) -> suggest(colorlist, b);
    }

    public static SuggestionProvider<CommandSourceStack> getPlayerandClansNotOwn(HNClaims plugin) {
        return (ctx, b) -> {
            try {
                List<String> outlist = new ArrayList<>();
                List<String> pllist = plugin.getServer().getPlayerList().getPlayers().stream().filter(player -> player.getUUID() != ctx.getSource().getPlayer().getUUID()).map(sp -> sp.getName().getString()).toList();
                List<String> clanlist = SimpleClans.getInstance().getClanManager().getClans().stream().map(clan -> "." + clan.getStringName()).toList();
                outlist.addAll(clanlist);
                outlist.addAll(pllist);
                return suggest(outlist, b);
            } catch (IllegalArgumentException e) {
                return b.buildFuture();
            }
        };
    }

    public static SuggestionProvider<CommandSourceStack> alliedClans(SimpleClans plugin) {
        return (ctx, b) -> suggest(plugin.getClanManager().getAlliedTags(ctx.getSource()), b);
    }

    public static SuggestionProvider<CommandSourceStack> PlayerHomes(HNessentials plugin) {
        return (ctx, b) -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            if (SimpleClans.getInstance().getPermissionsManager().has(sp, "essentials.home.other")) {
                List <String> resultlist = new ArrayList<>();
                for (Map.Entry<UUID, List<Home>> uuidListEntry: plugin.getHomeManager().getAllHomes().entrySet()) {
                    String playername = plugin.getServer().getProfileCache().get(uuidListEntry.getKey()).get().getName();
                    for (Home home : uuidListEntry.getValue()) {
                        resultlist.add(playername + ":" + home.getHomename());
                        if (home.getOwnerUUID().equals(sp.getUUID())) {
                            resultlist.add(home.getHomename());
                        }
                    }
                }
                return suggest(resultlist, b);
            } else if (!SimpleClans.getInstance().getPermissionsManager().has(sp, "essentials.home.other")) {
                List <String> resultlist = new ArrayList<>();
                for (Home home : plugin.getHomeManager().getHomes(sp.getUUID())) {
                    resultlist.add(home.getHomename());
                    //resultlist.add(sp.getName().getString() + ":" + home.getHomename());
                }

                return suggest(resultlist, b);
            }
            return suggest(new ArrayList<>(), b);
        };
    }

    public static SuggestionProvider<CommandSourceStack> allPlayers(Hnrpmod plugin) {
        boolean hhideme = false;
        return allPlayers(plugin, hhideme);
    }

    public static SuggestionProvider<CommandSourceStack> allWarps(HNessentials plugin) {
        boolean hhideme = false;
        return (ctx, b) -> suggest(plugin.getStorageManager().getGeneralData().getWarps().keySet().stream().toList(), b);
    }

    public static SuggestionProvider<CommandSourceStack> allPlayers(Hnrpmod plugin, boolean hhideme) {
        //boolean hhideme = true;
        /*return (ctx, b) -> suggest(
                plugin.getClanManager().getAllClanPlayers().stream()
                        .map(ClanPlayer::getName)
                        .filter(name -> {
                            if (hhideme && ctx.getSource().getPlayer() != null) {
                                // Filtere den eigenen Namen heraus, wenn hhideme true ist
                                return !name.equals(ctx.getSource().getPlayer().getName().getString());
                            }
                            return true;
                        })
                        .toList(),
                b);*/
        return (ctx, b) -> suggest(HNessentials.getInstance().getStorageManager().getGeneralData().getPlayerCache().values().stream().filter(pn -> {

            if (hhideme && ctx.getSource().getPlayer() != null) {
                return !pn.equals(ctx.getSource().getPlayer().getName().getString());
            }
            return false;
        }), b);
    }

    public static SuggestionProvider<CommandSourceStack> allPlayerNameFromHomes(HNessentials plugin) {
        //boolean hhideme = true;
        return (ctx, b) -> suggest( plugin.getStorageManager().getAllPlayerData().values().stream().map(pd -> pd.getPlayerName()).toList(),b);
    }


    public static List<String> getOfflinePlayerNames(MinecraftServer server) {
        List<String> names = new ArrayList<>();
        // Pfad zur usercache.json im Server-Root
        Path cachePath = server.getServerDirectory().toAbsolutePath().resolve("usercache.json");

        try {
            if (Files.exists(cachePath)) {
                String content = Files.readString(cachePath);
                JsonArray array = JsonParser.parseString(content).getAsJsonArray();
                array.forEach(element -> {
                    names.add(element.getAsJsonObject().get("name").getAsString());
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return names;
    }

    public static SuggestionProvider<CommandSourceStack> getRequests(HNessentials plugin) {
        boolean hhideme = true;
        return (ctx, b) -> suggest(plugin.getTpaRequester().getRequests(ctx.getSource().getPlayer().getUUID()).stream().map(tpa -> tpa.getRequester().getName().getString()).toList(), b);
    }

    public static SuggestionProvider<CommandSourceStack> onlinePlayers(SimpleClans plugin) {
        boolean hhideme = true;
        return (ctx, b) -> suggest(plugin.getServer().getPlayerNames(), b);
    }

    public static SuggestionProvider<CommandSourceStack> banedPlayers(HNClaims plugin) {
        return (ctx, b) -> {
            String claim = StringArgumentType.getString(ctx, "claim");
            if (claim.equals("alle")) {
                List<String> playernames = new ArrayList<>();
                for (Claim myclaim : plugin.getClaimManager().getClaims().stream().filter(clai -> clai.getownerUUID().equals(ctx.getSource().getPlayer().getUUID())).toList()) {
                    playernames.addAll(myclaim.getBanPlayers().stream().map(uuid -> plugin.getClaimManager().getServerPlayer(uuid).getName().getString()).toList());
                }
                return suggest(playernames, b);

            }

            return suggest(plugin.getClaimManager().getClaim(claim.substring(1)).getBanPlayers().stream().map(bp -> plugin.getServer().getProfileCache().get(bp).get().getName()), b);

        };
    }

    public static SuggestionProvider<CommandSourceStack> notsamerank(SimpleClans plugin) {
        return (ctx, b) -> {
            try {
                // Hier holen wir den Rank dynamisch aus dem Kontext des aktuellen Befehls
                String rank = StringArgumentType.getString(ctx, "rank");

                // Jetzt rufen wir deine Logik auf
                return suggest(plugin.getClanManager().getPlayerwithRank(ctx.getSource(), rank, true), b);
            } catch (IllegalArgumentException e) {
                // Falls "rank" aus irgendeinem Grund nicht gefunden wird, leere Vorschläge
                return b.buildFuture();
            }
        };
    }

    public static SuggestionProvider<CommandSourceStack> warringClans(SimpleClans plugin) {
        return (ctx, b) -> suggest(plugin.getClanManager().getWarringTags(ctx.getSource()), b);
    }

    public static SuggestionProvider<CommandSourceStack> getAllRanks(SimpleClans plugin) {
        return (ctx, builder) -> {
            ServerPlayer player = ctx.getSource().getPlayer();
            if (player == null) return builder.buildFuture();

            // 1. Clan holen
            Clan clan = plugin.getClanManager().getClanByPlayerName(player.getName().getString());

            // 2. Prüfen, ob der Clan existiert (Null-Check!)
            if (clan == null) {
                // Wenn kein Clan gefunden wurde, schlage nichts vor oder gib eine leere Liste zurück
                return builder.buildFuture();
            }

            List<String> rankNames = clan.getRanks().stream()
                    .map(rank -> rank.getName()) // oder rank.getDisplayName()
                    .toList();
            // 3. Nur wenn der Clan nicht null ist, auf getRanks() zugreifen
            return SharedSuggestionProvider.suggest(rankNames, builder);
        };
    }

    public static SuggestionProvider<CommandSourceStack> nonMembers(SimpleClans plugin) {
        return (ctx, b) -> suggest(plugin.getClanManager().getOnlineNonMembers(ctx.getSource()), b);
    }

    public static SuggestionProvider<CommandSourceStack> getRankPerms(SimpleClans plugin) {
        return (ctx, b) -> {
            List<String> rankPerms = new ArrayList<>();
            for (RankPermission rp : RankPermission.values()) {
                rankPerms.add(rp.getNeoPermission());
            }

            return SharedSuggestionProvider.suggest(rankPerms, b);
        };
    }

    public static SuggestionProvider<CommandSourceStack> clanMembersHideOwn(SimpleClans plugin) {
        return (ctx, b) -> suggest(plugin.getClanManager().getOnlineClanMembersHideOwn(ctx.getSource()), b);
    }

    public static SuggestionProvider<CommandSourceStack> clanMembersHideMe(SimpleClans plugin) {
        return (ctx, b) -> suggest(plugin.getClanManager().getClanByPlayerName(ctx.getSource().getPlayer().getName().getString()).getMembers().stream().filter(cp -> cp.getName() != ctx.getSource().getPlayer().getName().getString()).map(ClanPlayer::getName), b);
    }

    public static SuggestionProvider<CommandSourceStack> lookupsugests(Hnrpmod plugin) {
        return (ctx, b) -> {
            String input = b.getInput().toLowerCase();
            String remaining = b.getRemaining().toLowerCase();
            String[] args = b.getRemaining().toLowerCase().split("(?= )");
            List<String> argslist = Arrays.stream(args).toList();


            List<String> allKeys = List.of("time:", "radius:", "player:", "action:");
            List<String> allactions = List.of("BLOCK_BREAK", "BLOCK_PLACE", "ITEM_PICKUP", "ITEM_DROP", "KILL", "CHAT");

            List<String> timeformats = List.of("m", "h", "d", "w");
            List<String> timenumbers = List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");


            String lastarg = "";
            String beforelastarg = "";
            if (argslist != null && !argslist.isEmpty()) {
                lastarg = argslist.getLast();
                if (argslist.size() >= 2) {
                    beforelastarg = argslist.get(argslist.size() - 2);
                }
            }

            String keyPart = lastarg.equals(" ") || lastarg.isEmpty() ? "" : allKeys.stream().filter(lastarg::startsWith).toList().getFirst();

            SuggestionsBuilder subBuilder = remaining.endsWith(" ") ? b.createOffset(b.getStart() + keyPart.length()) : b;

            for (String key : allKeys) {
                if (lastarg.isEmpty()) {
                    b.suggest(key);
                } else {
                    if (allKeys.stream().noneMatch(lastarg::endsWith) && argslist.getLast().equals(" ")) { // wenn list allkey keine ding mit der endung von lastarg hat
                        if (allKeys.stream().noneMatch(beforelastarg::endsWith)) {
                            if (argslist.stream().noneMatch(s -> s.startsWith(key))) {
                                subBuilder.suggest(key);
                            }
                        }
                    }
                }


                //if (!input.contains(key) && key.startsWith(remaining)) {
                /*if (argslist.stream().noneMatch(s -> s.startsWith(key)) && Character.toString(remaining.charAt(remaining.length() - 1)).equals(" ")) {
                    b.suggest(key);
                }*/
            }

            if (lastarg.replace(" ", "").startsWith("time:") || allKeys.stream().noneMatch(lastarg::endsWith) && beforelastarg.replace(" ", "").startsWith("time:")) {
                List<String> spaceargs = new ArrayList<>();
                argslist.forEach(str -> spaceargs.add(argslist.getFirst().equals(str) ? removeLastTwo(str) : " " + str));
                int plus = (spaceargs.getLast().equals(" ") || spaceargs.getLast().isEmpty() ? 0 : 2);
                subBuilder = spaceargs.getLast().length() <= 2 || spaceargs.getLast().isEmpty() ? b.createOffset(b.getStart() + remaining.length()) : b.createOffset(b.getStart() + remaining.length() - (spaceargs.getLast().length() + plus ));
                if (remaining.matches(".*[0-9]")) {
                    for (String timeformat : timeformats) {
                        subBuilder.suggest((lastarg + timeformat).replace(" ", ""));
                    }
                } else if (timeformats.stream().noneMatch(lastarg::endsWith) && lastarg.matches(".*[^0-9]") && input.matches(".*:[ ]?")) {
                    for (String number : timenumbers) {
                        subBuilder.suggest((lastarg + number).replace(" ", ""));
                    }
                }
            } else if (lastarg.equals("radius:") || beforelastarg.equals("radius:")) {
                if (remaining.matches(".*[^0-9]")) {
                    for (String number : timenumbers) {
                        subBuilder.suggest((lastarg + number).replace(" ", ""));
                    }
                }
            } else if (lastarg.equals("player:")) {
                List<String> playerstring = HNessentials.getInstance().getStorageManager().getGeneralData().getPlayerCache().values().stream().toList();
                for (String playername : playerstring) {
                    subBuilder.suggest((lastarg + playername).replace(" ", ""));
                }
            } else if (lastarg.equals("action:")) {
                for (String aktion : allactions) {
                    subBuilder.suggest((lastarg + aktion).replace(" ", ""));
                }
            }

            return subBuilder.buildFuture();
        };
    }

    public static String removeLastTwo(String str) {
        if (str == null || str.length() < 2) {
            return str;
        }
        return str.substring(0, str.length() - 2);
    }

}

