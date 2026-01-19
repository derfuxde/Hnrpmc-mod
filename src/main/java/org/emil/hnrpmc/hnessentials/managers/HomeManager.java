package org.emil.hnrpmc.hnessentials.managers;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.emil.hnrpmc.hnclaim.HNClaims;
import org.emil.hnrpmc.hnclaim.managers.ClaimManager;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.Home;
import org.emil.hnrpmc.hnessentials.commands.home.HomeCommands;

import java.util.*;

public class HomeManager {
    private HNessentials plugin;
    private StorageManager storageManager;

    private Map<UUID, List<Home>> homeList = new HashMap<>();

    public HomeManager(HNessentials plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
    }

    public void setHome(Vec3 pos, String name, UUID ownerUUID, Level level) {
        Home newhome = new Home(ownerUUID, pos, name, level.dimension().location().toString());
        List<Home> homeList1 = homeList.getOrDefault(ownerUUID, new ArrayList<>());
        homeList1.add(newhome);
        homeList.put(ownerUUID, homeList1);

        HNPlayerData playerData = storageManager.getOrCreatePlayerData(ownerUUID);

        storageManager.setPlayerData(ownerUUID,playerData);
    }

    public List<Home> getHomes(UUID ownerUUID) {
        storageManager.loadAllPlayerDatas();
        HNPlayerData playerData = storageManager.getOrCreatePlayerData(ownerUUID);
        if (playerData == null) return new ArrayList<>();
        homeList.put(ownerUUID, playerData.getPlayerHomes());
        return homeList.get(ownerUUID);
    }

    public Home getHomeByName(String homeName, String playername, ServerPlayer player) {
        //String playername = homeName.split(":")[0];
        //String homeName2 = homeName.split(":")[1];

        if (playername == null || homeName == null || !plugin.getServer().getProfileCache().get(playername).isPresent()) return null;
        if (homeList.get(player.getUUID()) == null) return null;
        UUID uuid = plugin.getServer().getProfileCache().get(playername).get().getId();

        List<Home> Lhome = homeList.get(uuid).stream().filter(hh -> hh.getHomename().equals(homeName)).toList();

        if (Lhome.isEmpty()) {
            return null;
        }

        Home home = Lhome.getFirst();

        if (home == null) return null;

        return home;

    }

    public Map<UUID, List<Home>> getAllHomes() {
        storageManager.loadAllPlayerDatas();
        Map<String, HNPlayerData> playerDatas = storageManager.getAllPlayerData();
        for (HNPlayerData playerData : playerDatas.values()) {
            homeList.put(playerData.getPlayerUUID(), playerData.getPlayerHomes());
        }

        return homeList;
    }


}
