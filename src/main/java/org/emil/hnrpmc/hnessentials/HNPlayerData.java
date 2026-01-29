package org.emil.hnrpmc.hnessentials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.emil.hnrpmc.hnessentials.cosmetics.CapeData;
import org.emil.hnrpmc.hnessentials.cosmetics.impl.UserInfoImpl;
import org.emil.hnrpmc.hnessentials.cosmetics.model.BakableModel;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.*;

public class HNPlayerData {
    private String playerName;
    private UUID playerUUID;
    private Map<UUID, Integer> PetSelectedTexture;
    private Vec3 lastLocation;
    private String ip_adress;
    private boolean teleportEnabled = true;
    private boolean teleportAuto = false;
    private boolean godMode = false;
    private boolean muted = false;
    private boolean jailed = false;
    private boolean afk = false;

    private Map<CosmeticSlot, String> equippedCosmetics = new HashMap<>();
    //private transient UserInfoImpl userInfo;


    private Map<String, Integer> timestamps = new HashMap<>();
    private double money = 0.0;
    private Map<String, Object> logoutLocation = new HashMap<>();
    private InetAddress ipAddress;
    private List<Home> playerHomes = new ArrayList<>();

    public static ResourceLocation getSkinValue(ServerPlayer player) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/slim/steve.png");
    }

    public HNPlayerData(ServerPlayer player) {
        initDefaults();
        playerName = player.getName().getString();
        playerUUID = player.getUUID();
        lastLocation = player.getPosition(0);
        teleportEnabled = true;
        ip_adress = player.getIpAddress();
        PetSelectedTexture = new HashMap<>();
        equippedCosmetics = new HashMap<>();

        this.lore = "";
        this.upsideDown = false;
        this.prefix = "";
        this.suffix = "";
        this.hatIds = new ArrayList<>();
        this.capeId = null;
        this.leftShoulderBuddyId = null;
        this.rightShoulderBuddyId = null;
        this.backBlingId = null;
        this.skin = getSkinValue(player);
        this.slim = false;
        // 1.2.2
        this.icon = null;
        this.online = true;

        /*userInfo = new UserInfoImpl(
                getSkinValue(player).getPath(),
                false,
                lore,
                "java",
                "default",
                false,
                prefix(),
                suffix,
                null,
                online,
                hatIds,
                rightShoulderBuddyId,
                leftShoulderBuddyId,
                backBlingId,
                "",
                ""
        );*/
        //playerHomes = plugin.getHomeManager().getHomes(playerUUID);
    }

    private void initDefaults() {
        // Standardwerte für Timestamps
        timestamps.put("lastteleport", 0);
        timestamps.put("lastheal", 0);
        timestamps.put("jail", 0);
        timestamps.put("onlinejail", 0);
        timestamps.put("logout", 0);
        timestamps.put("login", 0);

        // Standardwerte für Logout-Location
        logoutLocation.put("world-name", "");
        logoutLocation.put("x", 0.0);
        logoutLocation.put("y", 0.0);
        logoutLocation.put("z", 0.0);
        logoutLocation.put("yaw", 0.0);
        logoutLocation.put("pitch", 0.0);
    }

    // Getter und Setter
    public List<Home> getPlayerHomes() { return playerHomes; }
    public void addPlayerHome(Home home) { this.playerHomes.add(home); }
    public void setPlayerHomes(List<Home> homes) { this.playerHomes = homes; }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void setLogoutLocation(Map<String, Object> logoutLocation) {
        this.logoutLocation = logoutLocation;
        this.lastLocation = new Vec3(Double.parseDouble(logoutLocation.get("x").toString()), Double.parseDouble(logoutLocation.get("y").toString()), Double.parseDouble(logoutLocation.get("z").toString()));
    }

    public void updateLastLocation(Vec3 lastLocation) {
        this.lastLocation = lastLocation;
    }

    public void setPetSelectedTextureForPet(int index, UUID uuid) {
        if (PetSelectedTexture == null) {
            this.PetSelectedTexture = new HashMap<>();
        }
        this.PetSelectedTexture.put(uuid, index);
    }

    public void setPetSelectedTexture(Map<UUID, Integer> petmap) {
        this.PetSelectedTexture = petmap;
    }

    public int getPetSelectedTextureForPet(UUID uuid) {
        if (PetSelectedTexture == null) return 0;
        if (PetSelectedTexture.get(uuid) == null) return 0;
        if (!PetSelectedTexture.containsKey(uuid)) return 0;
        return PetSelectedTexture.get(uuid);
    }

    public Map<UUID, Integer> getAllPetSelectedTexture() {
        if (PetSelectedTexture == null) return new HashMap<>();
        return PetSelectedTexture;
    }

    public boolean isGodMode() {
        return this.godMode;
    }

    public boolean isJailed() {
        return jailed;
    }

    public boolean isMuted() {
        return muted;
    }

    public boolean isTeleportAuto() {
        return teleportAuto;
    }

    public void setTeleportAuto(boolean teleportAuto) {
        this.teleportAuto = teleportAuto;
    }

    public void setTeleportEnabled(boolean teleportEnabled) {
        this.teleportEnabled = teleportEnabled;
    }

    public boolean isTeleportEnabled() {
        return teleportEnabled;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public Map<String, Object> getLogoutLocation() {
        return logoutLocation;
    }

    public Vec3 getLastLocation() {
        return lastLocation;
    }

    /*public UserInfoImpl getUserInfo() {
        return userInfo;
    }*/


    public void setCosmetic(CosmeticSlot slot, String cosmeticId) {
        if (equippedCosmetics == null) this.equippedCosmetics = new HashMap<>();
        equippedCosmetics.put(slot, cosmeticId);
        if (slot == CosmeticSlot.HAT) {
            hatIds = new ArrayList<>();
            if (hatIds != null) {
                hatIds.add(cosmeticId);

            }
        }
        /*userInfo = new UserInfoImpl(
                getSkinValue(null).getPath(),
                slim,
                lore,
                "java",
                "default",
                upsideDown,
                prefix,
                suffix,
                null,
                online,
                hatIds,
                rightShoulderBuddyId,
                leftShoulderBuddyId,
                backBlingId,
                capeId,
                ""
        );*/
    }



    public List<String> getEquippedCosmetics() {
        if (equippedCosmetics == null) return new ArrayList<>();
        if (equippedCosmetics.isEmpty()) return new ArrayList<>();
        return equippedCosmetics.values().stream().toList();
    }

    public String getCosmetic(CosmeticSlot slot) {
        if (equippedCosmetics == null) return null;
        if (equippedCosmetics.isEmpty()) return null;
        return equippedCosmetics.getOrDefault(slot, null);
    }

    public void setGodMode(boolean godMode) {
        this.godMode = godMode;
    }


    public boolean isAfk() {
        return afk;
    }

    public void setAfk(boolean afk) {
        this.afk = afk;
    }

    public void setIp_adress(String ip_adress) {
        this.ip_adress = ip_adress;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setJailed(boolean jailed) {
        this.jailed = jailed;
    }

    public double getMoney() { return money; }
    public void setMoney(double money) { this.money = money; }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Map<String, Integer> getTimestamps() { return timestamps; }

    // -- CosmeticDatas --

    private final String lore;
    private final boolean upsideDown;
    private final @Nullable ResourceLocation icon;
    private final boolean online;

    private final String prefix;
    private final String suffix;
    //private List<BakableModel> hats;
    //private final CapeData cape;
    //private final @Nullable BakableModel leftShoulderBuddy;
    //private final @Nullable BakableModel rightShoulderBuddy;
    //private final @Nullable BakableModel backBling;
    private List<String> hatIds = new ArrayList<>();
    private String capeId; // Oder CapeData, sofern CapeData keine Client-Klassen enthält
    private @Nullable String leftShoulderBuddyId;
    private @Nullable String rightShoulderBuddyId;
    private @Nullable String backBlingId;
    private final ResourceLocation skin;
    private final boolean slim;
    private static Map<UUID, HNPlayerData> playerDataCache = new HashMap<>();


    public String lore() {
        return lore;
    }

    public boolean upsideDown() {
        return upsideDown;
    }

    public String prefix() {
        return prefix;
    }

    public String suffix() {
        return suffix;
    }

    public List<String> hats() {
        if (hatIds == null) return List.of("none");
        if (hatIds.isEmpty()) return List.of("none");
        return hatIds;
    }

    public String leftShoulderBuddy() {
        return leftShoulderBuddyId;
    }

    public String rightShoulderBuddy() {
        return rightShoulderBuddyId;
    }

    public String backBling() {
        return backBlingId;
    }

    public String cape() {
        return this.capeId;
    }

    public ResourceLocation skin() {
        return skin;
    }

    public boolean slim() {
        return slim;
    }

    public ResourceLocation icon() {
        return icon;
    }

    public boolean online() {
        return online;
    }

    public static boolean has(UUID uuid) {
        synchronized (playerDataCache) {
            return playerDataCache.containsKey(uuid);
        }
    }

    public static HNPlayerData getCached(UUID player) {
        synchronized (playerDataCache) {
            return playerDataCache.get(player);
        }
    }

    public static void clear(UUID uuid) {
        synchronized (playerDataCache) {
            playerDataCache.remove(uuid);
        }
    }

    public static int getCacheSize() {
        synchronized (playerDataCache) {
            return playerDataCache.size();
        }
    }

    public static Collection<UUID> getCachedPlayers() {
        synchronized (playerDataCache) {
            return playerDataCache.keySet();
        }
    }

    public static void clearCaches() {
        playerDataCache = new HashMap<>();
    }
}