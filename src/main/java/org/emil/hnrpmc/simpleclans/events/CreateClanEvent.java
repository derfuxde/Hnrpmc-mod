package org.emil.hnrpmc.simpleclans.events;

import net.neoforged.bus.api.Event; // WICHTIGER IMPORT
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.Rank;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;

import java.util.*;

import static org.emil.hnrpmc.simpleclans.SimpleClans.getInstance;
import static org.emil.hnrpmc.simpleclans.chat.ChatHandler.plugin;
import static org.emil.hnrpmc.simpleclans.chat.ChatHandler.settingsManager;
import java.util.Map;

public class CreateClanEvent extends Event {
    private final Clan clan;

    public CreateClanEvent(Clan clan) {
        this.clan = clan;
        plugin.getLogger().info("Create Clan");

        Object rawRanks = settingsManager.getStringList(SettingsManager.ConfigField.CLAN_STATER_RANK);

        String clandefaultrank = settingsManager.getString(SettingsManager.ConfigField.CLAN_DEFAULT_RANK);

        List<String> ranknames = new ArrayList<>();

        if (rawRanks instanceof List<?> ranksList) {
            getInstance().getLogger().info("Registriere Starter-Ränge (Liste gefunden) {}", ranksList);

            for (Object obj : ranksList) {
                getInstance().getLogger().info("gettin rank {}", obj);
                // GSON lädt Objekte innerhalb von Listen als Maps (LinkedTreeMap)
                if (obj instanceof Map<?, ?> rankData) {
                    // In der Liste steht der Name im Feld "name" (siehe deine config.json)
                    getInstance().getLogger().info("creating rank {}", rankData.get("name"));
                    String rankId = rankData.get("name").toString();
                    String displayName = rankData.get("displayName").toString();

                    ranknames.add(rankId);

                    // GSON lädt Listen standardmäßig als ArrayList
                    List<String> permsList = (List<String>) rankData.get("permissions");
                    Set<String> permissions = new HashSet<>(permsList);

                    if (rankId != null) {
                        clan.createRank(rankId);
                        Rank newRank = clan.getRank(rankId);

                        if (newRank != null) {
                            newRank.setDisplayName(displayName);
                            newRank.setPermissions(permissions);

                            getInstance().getLogger().info("Neuer Rank erstellt: {} (Display: {})", rankId, displayName);
                        }
                    }
                }else {
                    getInstance().getLogger().info("Rank ist nicht instaceof map sondern {}", obj.getClass().getSimpleName());
                    getInstance().getLogger().error("Inhalt: " + obj.toString());
                }
            }
        } else {
            getInstance().getLogger().info("Konnte Ränge nicht laden. Typ ist: " + (rawRanks == null ? "null" : rawRanks.getClass().getSimpleName()));
        }

        plugin.getStorageManager().updateClan(clan);

        Rank lessperms = null;
        int lasPermcount = 0;

        plugin.getStorageManager().updateClan(clan);

        if (clandefaultrank  != null) {
            if (ranknames.contains(clandefaultrank)){
                clan.setDefaultRank(clandefaultrank);
                for (ClanPlayer cp : clan.getMembers()) {
                    cp.setRank(clandefaultrank);

                    plugin.getStorageManager().updateClanPlayer(cp);
                }
            }
        }



        plugin.getStorageManager().updateClan(clan);
    }

    public Clan getClan() {
        return clan;
    }
}