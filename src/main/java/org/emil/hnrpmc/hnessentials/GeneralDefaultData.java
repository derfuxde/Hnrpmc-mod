package org.emil.hnrpmc.hnessentials;

import net.minecraft.world.phys.Vec3;
import org.emil.hnrpmc.hnessentials.cosmetics.ConfigCosmetic;
import org.emil.hnrpmc.hnessentials.cosmetics.api.SimpleCosmetic;

import java.util.*;

public class GeneralDefaultData {
    private List<ConfigCosmetic> cosmetics = new ArrayList<>();
    private transient HNessentials plugin;
    private Map<UUID, String> playerCache = new HashMap<>();
    private Map<String, Vec3> warps = new HashMap<>();

    public GeneralDefaultData(HNessentials plugin) {
        this.plugin = plugin;
        this.cosmetics = new ArrayList<>();
        this.playerCache = new HashMap<>();
        this.warps = new HashMap<>();
    }

    public List<ConfigCosmetic> getCosmetics() {
        return cosmetics;
    }

    public void setCosmetics(List<ConfigCosmetic> CC) {
        this.cosmetics = CC;
    }

    public Map<UUID, String> getPlayerCache() {
        if (playerCache == null) return new HashMap<>();
        if (playerCache.isEmpty()) return new HashMap<>();
        return playerCache;
    }

    public void setPlayerCache(Map<UUID, String> newPlayerMap) {
        this.playerCache = newPlayerMap;
    }

    public Map<String, Vec3> getWarps() {
        if (warps == null) return new HashMap<>();
        if (warps.isEmpty()) return new HashMap<>();
        return warps;
    }

    public void setWarps(Map<String, Vec3> warps) {
        this.warps = warps;
    }

}
