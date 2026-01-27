package org.emil.hnrpmc.hnessentials.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.emil.hnrpmc.hnclaim.HNClaims;
import org.emil.hnrpmc.hnessentials.GeneralDefaultData;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.cosmetics.ConfigCosmetic;
import org.emil.hnrpmc.simpleclans.Helper;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class StorageManager {
    private final HNessentials plugin;
    private final File file;
    private Map<String, HNPlayerData> data = new TreeMap<>();
    private GeneralDefaultData generalData = null;

    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public StorageManager(HNessentials plugin) {
        this.plugin = plugin;
        Path worldRoot = plugin.getServer().getWorldPath(LevelResource.ROOT);
        Path dbPath = worldRoot.resolve("clans").resolve("general.json");

        try {
            Files.createDirectories(dbPath.getParent());
        } catch (IOException e) {
            System.err.println("Es geb ein fehler Beim erstellen der General Datei: " + e);
            e.printStackTrace();
        }
        this.file = dbPath.toFile();
    }

    private GeneralDefaultData DefaultGeneralData() {
        ConfigCosmetic LONG_HAT = new ConfigCosmetic("Lang Hut", "longhat", "hat");
        ConfigCosmetic WITCH_HAT = new ConfigCosmetic("Hexen Hut", "witchhat", "hat");
        ConfigCosmetic WASSER_MELONE = new ConfigCosmetic("Wassermelonen Hut", "wassermelone", "hat");
        ConfigCosmetic MASK = new ConfigCosmetic("Player Mask", "mask", "hat");

        List<ConfigCosmetic> LCC = List.of(LONG_HAT, WITCH_HAT, WASSER_MELONE, MASK);

        GeneralDefaultData GDD = new GeneralDefaultData(plugin);
        GDD.setCosmetics(LCC);
        return GDD;
    }

    public void loadGeneralData() {
        if (!file.exists() && generalData == null) {
            this.generalData = DefaultGeneralData();
            saveGeneralData();
        }
        try (Reader reader = new FileReader(file)) {
            GeneralDefaultData GDD = gson.fromJson(reader, GeneralDefaultData.class);
            if (GDD == null) return;
            this.generalData = GDD;
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Spielerdaten!");
            e.printStackTrace();
        }
    }

    public GeneralDefaultData getGeneralData() {
        loadGeneralData();
        return generalData;
    }

    public void updateGeneralData(GeneralDefaultData GDD) {
        this.generalData = GDD;
    }

    public void saveGeneralData() {
        List<ServerPlayer> listPlayers = plugin.getServer().getPlayerList().getPlayers();
        Map<UUID, String> currentPlayers = new HashMap<>(listPlayers.stream().collect(Collectors.toMap(ServerPlayer::getUUID, sp -> sp.getName().getString())));
        //loadGeneralData();
        generalData.setPlayerCache(currentPlayers);
        try (Writer writer = new FileWriter(file)) {
            if (generalData == null) {
                this.generalData = DefaultGeneralData();
            }
            gson.toJson(generalData, writer);
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Generaldaten");
            e.printStackTrace();
        }
    }

    public void loadAllPlayerDatas() {
        Path worldRoot = plugin.getServer().getWorldPath(LevelResource.ROOT);
        Path dbPath = worldRoot.resolve("clans").resolve("userData");
        for (Path myPath : Helper.getPathsIn(dbPath.toString(), null)) {
            File playerFile = myPath.toFile();
            if (!playerFile.exists()) return;

            try (Reader reader = new FileReader(playerFile)) {
                // Definiert den Typ Map<String, HNPlayerData> für GSON
                Type type = new TypeToken<TreeMap<String, HNPlayerData>>(){}.getType();
                HNPlayerData singleData = gson.fromJson(reader, HNPlayerData.class);

                if (singleData != null) {
                    this.data.put(singleData.getPlayerUUID().toString(), singleData);
                }
            } catch (IOException e) {
                System.err.println("Fehler beim Laden der Spielerdaten!");
                e.printStackTrace();
            }
        }
    }

    public void loadPlayerData(UUID uuid) {
        File playerFile = getPlayerFile(uuid);
        if (!playerFile.exists()) return;

        try (Reader reader = new FileReader(playerFile)) {
            // Definiert den Typ Map<String, HNPlayerData> für GSON
            Type type = new TypeToken<TreeMap<String, HNPlayerData>>(){}.getType();
            HNPlayerData singleData = gson.fromJson(reader, HNPlayerData.class);

            if (singleData != null) {
                this.data.put(uuid.toString(), singleData);
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Spielerdaten!");
            e.printStackTrace();
        }
    }

    public File getPlayerFile(UUID uuid) {
        Path worldRoot = plugin.getServer().getWorldPath(LevelResource.ROOT);
        Path dbPath = worldRoot.resolve("clans").resolve("userData").resolve(uuid + ".json");

        try {
            Files.createDirectories(dbPath.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dbPath.toFile();
    }

    public void save(UUID uuid) {
        File playerFile = getPlayerFile(uuid);
        try (Writer writer = new FileWriter(playerFile)) {
            gson.toJson(data.get(uuid.toString()), writer);
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Spielerdaten!");
            e.printStackTrace();
        }
    }

    // Zugriffsmethoden
    public HNPlayerData getOrCreatePlayerData(UUID uuid) {
        String id = uuid.toString();
        //loadPlayerData(uuid);
        if (!data.containsKey(id)) {
            ServerPlayer player = HNClaims.getInstance().getClaimManager().getServerPlayer(uuid);
            if (player == null) return null;

            HNPlayerData newData = new HNPlayerData(player);
            data.put(id, newData);
            newData.setPlayerHomes(plugin.getHomeManager().getHomes(uuid));
        }
        return data.get(id);
    }

    public Map<String, HNPlayerData> getAllPlayerData() {
        return data;
    }

    public void setPlayerData(UUID uuid, HNPlayerData playerData) {
        data.put(uuid.toString(), playerData);
        save(uuid);
    }
}