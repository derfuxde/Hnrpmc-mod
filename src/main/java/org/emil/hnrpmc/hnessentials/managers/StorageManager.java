package org.emil.hnrpmc.hnessentials.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.emil.hnrpmc.hnclaim.HNClaims;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.simpleclans.Helper;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class StorageManager {
    private final HNessentials plugin;
    private final File file;
    private Map<String, HNPlayerData> data = new TreeMap<>();

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public StorageManager(HNessentials plugin) {
        this.plugin = plugin;
        Path worldRoot = plugin.getServer().getWorldPath(LevelResource.ROOT);
        // Pfad: world/HNessentials/players.json
        Path dbPath = worldRoot.resolve("clans").resolve("players.json");

        try {
            Files.createDirectories(dbPath.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.file = dbPath.toFile();
    }

    public void loadAllPlayerDatas() {
        Path worldRoot = plugin.getServer().getWorldPath(LevelResource.ROOT);
        Path dbPath = worldRoot.resolve("clans").resolve("userData");
        for (Path myPath : Helper.getPathsIn(dbPath.toString(), null)) {
            File playerFile = myPath.toFile();
            if (!playerFile.exists()) return;

            try (Reader reader = new FileReader(file)) {
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