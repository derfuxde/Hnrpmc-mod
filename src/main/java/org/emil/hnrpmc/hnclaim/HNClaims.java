package org.emil.hnrpmc.hnclaim;

import com.google.gson.*;
import com.hypherionmc.craterlib.core.event.CraterEventBus;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.emil.hnrpmc.hnclaim.commands.ClaimCommandManager;
import org.emil.hnrpmc.hnclaim.listeners.ClaimEventHandler;
import org.emil.hnrpmc.hnclaim.listeners.ClaimPlayerListener;
import org.emil.hnrpmc.hnclaim.managers.ClaimManager;
import org.emil.hnrpmc.hnclaim.managers.PermissionsManager;
import org.emil.hnrpmc.hnclaim.managers.StorageManager;
import org.emil.hnrpmc.hnclaim.proxy.BungeeManager;
import org.emil.hnrpmc.hnclaim.proxy.ProxyManager;
import org.emil.hnrpmc.hnclaim.storage.DBCore;
import org.emil.hnrpmc.hnclaim.storage.SQLiteCore;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.SCCommandManager;
import org.emil.hnrpmc.simpleclans.hooks.placeholder.PlaceholderService;
import org.emil.hnrpmc.simpleclans.hooks.placeholder.SimpleClansPlaceholders;
import org.emil.hnrpmc.simpleclans.language.LanguageResource;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import static javax.swing.UIManager.get;
import static javax.swing.UIManager.getString;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.DEBUG;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.PERFORMANCE_SAVE_PERIODICALLY;

public class HNClaims {

    private static HNClaims instance;
    public static final Logger LOGGER = LogUtils.getLogger();


    private MinecraftServer server; // Die Server-Instanz

    private static LanguageResource languageResource;
    private DBCore dbCore;
    private ClaimCommandManager commandManager;
    private ClaimManager claimManager;
    private StorageManager storageManager;
    private PermissionsManager permissionsManager;
    public ProxyManager proxyManager;

    private PlaceholderService placeholderService;
    private SQLiteCore core;

    public HNClaims(IEventBus modEventBus) {
        instance = this;
        //SCCommandManager.init(this);

        // WICHTIG: Mod-Phasen Events (Setup)
        modEventBus.addListener(this::startsetup);

        // WICHTIG: Server-Events müssen auf den Forge Bus
        NeoForge.EVENT_BUS.addListener(this::setup);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);

        NeoForge.EVENT_BUS.addListener(ClaimCommandManager::onRegisterCommands);
    }


    private void startsetup(final FMLCommonSetupEvent event) {
        this.gson = new GsonBuilder()
                // ... deine anderen Adapter ...
                .registerTypeAdapter(java.util.Optional.class, (com.google.gson.JsonSerializer<java.util.Optional<?>>) (src, typeOfSrc, context) ->
                        (src == null || !src.isPresent()) ? com.google.gson.JsonNull.INSTANCE : context.serialize(src.get()))
                .registerTypeAdapter(java.util.Optional.class, (com.google.gson.JsonDeserializer<java.util.Optional<?>>) (json, typeOfT, context) -> {
                    if (json == null || json.isJsonNull()) return java.util.Optional.empty();
                    java.lang.reflect.Type innerType = ((java.lang.reflect.ParameterizedType) typeOfT).getActualTypeArguments()[0];
                    return java.util.Optional.of(context.deserialize(json, innerType));
                })
                .create();
        claimManager = new ClaimManager();
        permissionsManager = new PermissionsManager();
        languageResource = new LanguageResource();
        exportDefaultLanguages();
    }

    private void setup(final ServerStartingEvent event) {
        this.server = event.getServer();
        serverStartup(server);
    }

    public void serverStartup(MinecraftServer eventserver) {
        LOGGER.info("ServerStarting-Instanz erfolgreich für HNClaims registriert.");

        this.dbCore = new SQLiteCore(eventserver);

        if (DBCore.getConnection() == null) {
            LOGGER.error("FEHLER: Die Datenbankverbindung konnte nicht hergestellt werden! Der Mod wird nicht funktionieren.");
            //return;
        }

        this.proxyManager = new BungeeManager(this);
        this.storageManager = new StorageManager(dbCore);
        ClaimCommandManager.init(this);
        commandManager = new ClaimCommandManager();

        registerEvents();

        LOGGER.info("HNClaims für NeoForge wurde geladen!");

    }

    private void registerEvents() {
        var bus = NeoForge.EVENT_BUS;
        bus.register(new ClaimPlayerListener(this));
        bus.register(new ClaimEventHandler(this));
    }

    private void onServerStopping(ServerStoppingEvent event) {
        if (getSettingsManager().is(PERFORMANCE_SAVE_PERIODICALLY)) {
            getStorageManager().saveModified();
        }
        getStorageManager().closeConnection();
        getPermissionsManager().savePermissions();
        this.server = null; // Clean up
    }

    public Gson getGSON() {
        return gson;
    }

    Gson gson;

    public ProxyManager getProxyManager() {
        return proxyManager;
    }



    // --- Bukkit-Compatibility-Getter ---

    public static HNClaims getInstance() {
        return instance;
    }

    public PlaceholderService getPlaceholderService() {
        return placeholderService;
    }

    /**
     * Entspricht Bukkit.getServer() oder JavaPlugin.getServer()
     */
    public MinecraftServer getServer() {
        return this.server;
    }

    public Optional<ModContainer> getModContainer() {
        return (Optional<ModContainer>) ModList.get().getModContainerById("hnrpmc");
    }

    public String getVersion() {
        return getModContainer()
                .map(c -> c.getModInfo().getVersion().toString())
                .orElse("dev");
    }

    /**
     * NeoForge hat kein 1:1 PluginDescriptionFile.
     * Wenn du unbedingt "getDescription()" willst:
     */
    public Description getDescription() {
        return new Description(getVersion(), getAuthors());
    }

    public List<String> getAuthors() {
        // Variante B (robust): als Konstante pflegen
        // Trage hier deine Author(s) ein:
        return List.of("emil");

        // Variante A (nur nutzen, wenn du sicher weißt, dass deine NeoForge-Version das liefert):
        // return getModContainer()
        //        .map(c -> c.getModInfo().getConfig().<List<String>>get("authors"))
        //        .orElse(List.of());
    }

    public static void debug(String msg) {
        //instance may be null during tests
        if (SimpleClans.getInstance() == null || SimpleClans.getInstance().getSettingsManager().is(DEBUG)) {
            LOGGER.debug( msg);
        }
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public void exportDefaultLanguages() {
        // Liste aller Dateien, die du exportieren willst
        String[] langFiles = {
                "messages.properties",
                "messages_de_DE.properties",
                "messages_es_ES.properties",
                "messages_fi_FI.properties",
                "messages_el_GR.properties"
        };
    }

    public static final class Description {
        private final String version;
        private final List<String> authors;

        public Description(String version, List<String> authors) {
            this.version = version;
            this.authors = authors;
        }

        public String getVersion() {
            return version;
        }

        public List<String> getAuthors() {
            return authors;
        }
    }

    public java.nio.file.Path getGameDirectory() {
        return net.neoforged.fml.loading.FMLPaths.GAMEDIR.get();
    }

    public void runTaskLater(Runnable runnable, long ticks) {
        // Ein Tick in Minecraft dauert 50 Millisekunden
        long delayMs = ticks * 50;

        CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS).execute(() -> {
            // Sicherstellen, dass der Code auf dem Haupt-Thread des Servers läuft
            if (this.server != null) {
                this.server.execute(runnable);
            }
        });
    }

    /**
     * Entspricht JavaPlugin.getLogger()
     */
    public Logger getLogger() {
        return LOGGER;
    }

    public SettingsManager getSettingsManager() {
        return SimpleClans.getInstance().getSettingsManager();
    }

    public LanguageResource getLanguageResource() {
        return languageResource;
    }

    // --- Manager-Getter ---

    public ClaimManager getClaimManager() { return claimManager; }

    public PermissionsManager getPermissionsManager() { return permissionsManager; }
    //public SettingsManager getSettingsManager() { return settingsManager; }
    public StorageManager getStorageManager() {
        //LOGGER.info("check Storage manager {}", this.storageManager != null ? "ist da" : "ist offline");

        return this.storageManager;
    }

    // --- Tasks & Metrics ---
}

