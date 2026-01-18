package org.emil.hnrpmc.hnessentials;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
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

    // WICHTIG: HashMap nutzen, damit GSON Werte beim Laden ändern darf
    private Map<String, Integer> timestamps = new HashMap<>();
    private double money = 0.0;
    private Map<String, Object> logoutLocation = new HashMap<>();
    private InetAddress ipAddress;
    private List<Home> playerHomes = new ArrayList<>();

    // Leerer Konstruktor für GSON
    public HNPlayerData(ServerPlayer player) {
        initDefaults();
        playerName = player.getName().getString();
        playerUUID = player.getUUID();
        lastLocation = player.getPosition(0);
        teleportEnabled = true;
        ip_adress = player.getIpAddress();
        PetSelectedTexture = new HashMap<>();
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

    public double getMoney() { return money; }
    public void setMoney(double money) { this.money = money; }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Map<String, Integer> getTimestamps() { return timestamps; }
}