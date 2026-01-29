package org.emil.hnrpmc.simpleclans;

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
import org.emil.hnrpmc.Hnrpmod;
import org.emil.hnrpmc.simpleclans.commands.SCCommandManager;
import org.emil.hnrpmc.simpleclans.commands.staff.NoVanishService;
import org.emil.hnrpmc.simpleclans.commands.staff.VanishService;
import org.emil.hnrpmc.simpleclans.hooks.placeholder.PlaceholderService;
import org.emil.hnrpmc.simpleclans.language.LanguageResource;
import org.emil.hnrpmc.simpleclans.listeners.*;
import org.emil.hnrpmc.simpleclans.loggers.BankLogger;
import org.emil.hnrpmc.simpleclans.loggers.CSVBankLogger;
import org.emil.hnrpmc.simpleclans.managers.*;
import org.emil.hnrpmc.simpleclans.proxy.BungeeManager;
import org.emil.hnrpmc.simpleclans.proxy.ProxyManager;
import org.emil.hnrpmc.simpleclans.storage.DBCore;
import org.emil.hnrpmc.simpleclans.storage.SQLiteCore;
import org.emil.hnrpmc.simpleclans.ui.InventoryController;
import org.emil.hnrpmc.simpleclans.utils.TagValidator;
import org.emil.hnrpmc.simpleclans.tasks.*;
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

import org.emil.hnrpmc.simpleclans.hooks.placeholder.SimpleClansPlaceholders;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import static javax.swing.UIManager.get;
import static javax.swing.UIManager.getString;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public class SimpleClans extends Hnrpmod {

    private static SimpleClans instance;
    public static final Logger LOGGER = LogUtils.getLogger();


    private MinecraftServer server; // Die Server-Instanz

    private static LanguageResource languageResource;
    private DBCore dbCore;
    private SCCommandManager commandManager;
    private ClanManager clanManager;
    private RequestManager requestManager;
    private StorageManager storageManager;
    private SettingsManager settingsManager;
    private PermissionsManager permissionsManager;
    private TeleportManager teleportManager;
    private ChatManager chatManager;
    private BankLogger bankLogger;
    private TagValidator tagValidator;
    private ProxyManager proxyManager;
    private VanishService vanishService;

    private PlaceholderService placeholderService;
    private SQLiteCore core;

    public SimpleClans(IEventBus modEventBus) {
        instance = this;
        //SCCommandManager.init(this);

        // WICHTIG: Mod-Phasen Events (Setup)
        modEventBus.addListener(this::startsetup);

        // WICHTIG: Server-Events müssen auf den Forge Bus
        NeoForge.EVENT_BUS.addListener(this::setup);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);

        NeoForge.EVENT_BUS.addListener(SCCommandManager::onRegisterCommands);
    }


    private void startsetup(final FMLCommonSetupEvent event) {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(java.util.Optional.class, (com.google.gson.JsonSerializer<java.util.Optional<?>>) (src, typeOfSrc, context) ->
                        (src == null || !src.isPresent()) ? com.google.gson.JsonNull.INSTANCE : context.serialize(src.get()))
                .registerTypeAdapter(java.util.Optional.class, (com.google.gson.JsonDeserializer<java.util.Optional<?>>) (json, typeOfT, context) -> {
                    if (json == null || json.isJsonNull()) return java.util.Optional.empty();
                    java.lang.reflect.Type innerType = ((java.lang.reflect.ParameterizedType) typeOfT).getActualTypeArguments()[0];
                    return java.util.Optional.of(context.deserialize(json, innerType));
                })
                .create();
        settingsManager = new SettingsManager(this);
        permissionsManager = new PermissionsManager();
        requestManager = new RequestManager();
        clanManager = new ClanManager();
        chatManager = new ChatManager(this);
        languageResource = new LanguageResource();
        exportDefaultLanguages();

        placeholderService = new PlaceholderService("simpleclans");
        new SimpleClansPlaceholders(this, placeholderService).registerAll();

        tagValidator = new TagValidator(settingsManager, permissionsManager);
    }

    private void setup(final ServerStartingEvent event) {
        this.server = event.getServer();
        serverStartup(server);
    }

    public void serverStartup(MinecraftServer eventserver) {
        LOGGER.info("ServerStarting-Instanz erfolgreich für SimpleClans registriert.");

        this.dbCore = new SQLiteCore(eventserver);

        if (DBCore.getConnection() == null) {
            LOGGER.error("FEHLER: Die Datenbankverbindung konnte nicht hergestellt werden! Der Mod wird nicht funktionieren.");
            //return;
        }

        this.proxyManager = new BungeeManager(this);
        this.vanishService = new NoVanishService(this);
        this.storageManager = new StorageManager(dbCore);
        getLogger().info("Storage manager ist {}", storageManager != null ? "ist da" : "ist offline");
        teleportManager = new TeleportManager(this);
        SCCommandManager.init(this);
        commandManager = new SCCommandManager();

        registerEvents();

        bankLogger = new CSVBankLogger(this);

        LOGGER.info("SimpleClans für NeoForge wurde geladen!");

        startTasks();
    }

    private void registerEvents() {
        var bus = NeoForge.EVENT_BUS;
        CraterEventBus.INSTANCE.registerEventListener(chatManager);
        bus.register(new PlayerDeath(this));
        bus.register(new SCPlayerListener(this));
        bus.register(InventoryController.class);
        bus.register(new TamableMobsSharing(this));
        bus.register(new PvPOnlyInWar(this));
        bus.register(new FriendlyFire(this));
        if (chatManager.isDiscordHookEnabled(this)){
            bus.register(chatManager.getDiscordHook(this));
            CraterEventBus.INSTANCE.registerEventListener(chatManager.getDiscordHook(this));
        }

        NeoForge.EVENT_BUS.register(TeleportManager.class);


    }

    private void onServerStopping(ServerStoppingEvent event) {
        if (getSettingsManager().is(PERFORMANCE_SAVE_PERIODICALLY)) {
            getStorageManager().saveModified();
        }
        getStorageManager().closeConnection();
        getPermissionsManager().savePermissions();
        getStorageManager().updateAllClanandPlayers();
        this.server = null;
    }

    public Gson getGSON() {
        return gson;
    }

    private Gson gson;



    // --- Bukkit-Compatibility-Getter ---

    public static SimpleClans getInstance() {
        return instance;
    }

    public PlaceholderService getPlaceholderService() {
        return placeholderService;
    }

    public TagValidator getTagValidator() {
        return this.tagValidator;
    }

    public VanishService getVanishService() {
        return this.vanishService;
    }

    public BankLogger getBankLogger() {
        return bankLogger;
    }

    /**
     * Entspricht Bukkit.getServer() oder JavaPlugin.getServer()
     */
    public MinecraftServer getServer() {
        return this.server;
    }

    public ChatManager getChatManager() {
        return this.chatManager;
    }

    public RequestManager getRequestManager() {
        return this.requestManager;
    }

    public ProxyManager getProxyManager() {
        return this.proxyManager;
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
        if (getInstance() == null || getInstance().getSettingsManager().is(DEBUG)) {
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

    public File getDataFolder() {
        // FMLPaths.CONFIGDIR zeigt auf den /config Ordner des Servers/Clients
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("simpleclans");
        File file = configPath.toFile();

        // Sicherstellen, dass der Ordner existiert
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    private Path langfiles;

    public Path getResourcePath() {
        return net.neoforged.fml.ModList.get()
                .getModContainerById("hnrpmc")
                .map(container -> container.getModInfo().getOwningFile().getFile().findResource("inaktive/simpleclans/lang"))
                .orElse(null);
    }

    public Path getLangDataFolder() {

        // 3. Dateien aus der JAR (Source) nach Außen (Target) kopieren
        Path sourcePath = getResourcePath();
        if (sourcePath != null) {
            try (Stream<Path> stream = Files.walk(sourcePath, 1)) {
                return sourcePath.toAbsolutePath();
            } catch (IOException e) {
                instance.getLogger().error("Fehler beim Kopieren der Sprachen", e);
            }
        }

        return null;
    }

    // 1. Die Hauptmethode (Basis für alle anderen)
    public static String lang(@NotNull String key, @Nullable ServerPlayer player, Object... arguments) {
        try {
            return lang(key, (Player) player, arguments);
        } catch (Exception e) {
            return key;
        }
    }


    // 2. Die UUID-Variante (die du angefragt hast)
    @NotNull
    public static String lang(@NotNull String key, @Nullable UUID playerUniqueId, Object... arguments) {
        ServerPlayer player = null;
        if (playerUniqueId != null && getInstance().getServer() != null) {
            player = getInstance().getServer().getPlayerList().getPlayer(playerUniqueId);
        }
        // Ruft die Hauptmethode (Nr. 1) auf
        return lang(key, player, arguments);
    }

    // 3. Die CommandSourceStack-Variante (für Commands)
    @NotNull
    public static String lang(@NotNull String key, @Nullable CommandSourceStack stack, Object... arguments) {
        if (stack != null && stack.getEntity() instanceof ServerPlayer player) {
            return lang(key, player, arguments);
        }
        return lang(key, (ServerPlayer) null, arguments);
    }

    public static String lang(String key, @Nullable Player player, Object... args) {
        Path langFolder = getInstance().getLangDataFolder();


        String langTag = "en";

        if (player != null) {
            if (getInstance().getClanManager().getClanPlayer(player.getUUID()) != null) {
                if (getInstance().getClanManager().getClanPlayer(player.getUUID()).getLocale() != null) {
                    langTag = getInstance().getClanManager().getClanPlayer(player.getUUID()).getLocale().toLanguageTag();
                }

            }
        }
        // Wir suchen z.B. nach messages_de_DE.properties
        String fileName = (langTag.equals("en")) ? "messages.properties" : "messages_" + langTag.replace("-", "_") + ".properties";



        String Filepath = langFolder.toString() + "/" + fileName;

        // 1. Den Pfad zur Datei innerhalb der Mod-JAR finden
        // Wichtig: Ohne führenden Slash bei findResource!
        Path pathInJar = ModList.get().getModContainerById("hnrpmc")
                .map(c -> c.getModInfo().getOwningFile().getFile().findResource(Filepath))
                .orElse(null);

        if (pathInJar == null) {
            LOGGER.error("Sprachdatei messages.properties wurde in der JAR nicht gefunden!");
            return key;
        }

        // 2. Die Datei direkt aus der JAR lesen
        try (InputStream is = Files.newInputStream(pathInJar);
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {

            PropertyResourceBundle bundle = new PropertyResourceBundle(isr);

            if (bundle.containsKey(key)) {
                String message = bundle.getString(key);
                return MessageFormat.format(message.replace("&", "§"), args);
            }
        } catch (IOException e) {
            LOGGER.error("Fehler beim Lesen der Sprachdatei aus der JAR", e);
        }

        return key;
    }

    @NotNull
    public static String lang(@NotNull String key) {
        return lang(key, (ServerPlayer) null, (Object) null);
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

    public boolean isBlacklistedWorld(ServerPlayer player) {
        String worldName = player.level().dimension().location().toString();
        // Falls WORLD_BLACKLIST Fehler wirft, stelle sicher, dass es in ConfigField existiert
        return settingsManager.getList(SETTINGS_WORLD_BLACKLIST).contains(worldName);
    }

    /**
     * Entspricht JavaPlugin.getLogger()
     */
    public Logger getLogger() {
        return LOGGER;
    }



    public LanguageResource getLanguageResource() {
        return languageResource;
    }

    // --- Manager-Getter ---

    public ClanManager getClanManager() { return clanManager; }
    public SettingsManager getSettingsManager() { return settingsManager; }
    public StorageManager getStorageManager() {
        //LOGGER.info("check Storage manager {}", this.storageManager != null ? "ist da" : "ist offline");

        return this.storageManager;
    }
    public PermissionsManager getPermissionsManager() { return permissionsManager; }
    public TeleportManager getTeleportManager() { return teleportManager; }

    // --- Tasks & Metrics ---

    private void startTasks() {
        if (getSettingsManager().is(PERFORMANCE_SAVE_PERIODICALLY)) {
            new SaveDataTask().start();
        }
        if (getSettingsManager().is(ECONOMY_MEMBER_FEE_ENABLED)) {
            new CollectFeeTask().start();
        }
        if (getSettingsManager().is(ECONOMY_UPKEEP_ENABLED)) {
            new CollectUpkeepTask().start();
            new UpkeepWarningTask().start();
        }
        if (getSettingsManager().is(PERFORMANCE_HEAD_CACHING)) {
            new PlayerHeadCacheTask(this).start();
        }
    }
}

