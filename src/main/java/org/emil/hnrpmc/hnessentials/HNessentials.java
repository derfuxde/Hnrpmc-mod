package org.emil.hnrpmc.hnessentials;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.core.jmx.Server;
import org.emil.hnrpmc.Hnrpmod;
import org.emil.hnrpmc.hnessentials.ChestLocks.config.LockConfig;
import org.emil.hnrpmc.hnessentials.ChestLocks.config.LockData;
import org.emil.hnrpmc.hnessentials.commands.CommandHelper;
import org.emil.hnrpmc.hnessentials.commands.HNECommandManager;
import org.emil.hnrpmc.hnessentials.cosmetics.ConfigCosmetic;
import org.emil.hnrpmc.hnessentials.cosmetics.PlayerData;
import org.emil.hnrpmc.hnessentials.cosmetics.SyncCosmeticPayload;
import org.emil.hnrpmc.hnessentials.cosmetics.api.*;
import org.emil.hnrpmc.hnessentials.cosmetics.impl.CosmeticFetcher;
import org.emil.hnrpmc.hnessentials.cosmetics.model.BakableModel;
import org.emil.hnrpmc.hnessentials.cosmetics.model.Models;
import org.emil.hnrpmc.hnessentials.listeners.PlayerDataRequestPayload;
import org.emil.hnrpmc.hnessentials.listeners.PlayerEventLister;
import org.emil.hnrpmc.hnessentials.managers.DatabaseManager;
import org.emil.hnrpmc.hnessentials.managers.HomeManager;
import org.emil.hnrpmc.hnessentials.managers.StorageManager;
import org.emil.hnrpmc.hnessentials.managers.TpaManager;
import org.emil.hnrpmc.hnessentials.menu.PetMorphScreen;
import org.emil.hnrpmc.hnessentials.network.*;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.slf4j.Logger;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;

public class HNessentials extends Hnrpmod {
    private static StorageManager storageManager;
    private MinecraftServer server;
    private static TpaManager tpaRequester;
    private static HomeManager homeManager;
    private static DatabaseManager databaseManager;
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final List<String> skins = List.of("Standard", "Shadow", "Gold", "Diamond", "Rainbow");
    public static final Map<UUID, Integer> clientPetSkins = new HashMap<>();
    public static int clientVipScore = 0;
    public Map<UUID, HNPlayerData> HNplayerDataMap = new HashMap<>();
    public Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    private CommandHelper commandHelper;

    private static HNessentials instance;


    public static final String MODID = "simpleblocklock";

    // Registrierung des AttachmentTypes (ersetzt Capabilities)
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<LockData>> LOCK_DATA = ATTACHMENT_TYPES.register(
            "lock_data", () -> AttachmentType.builder(() -> (LockData) null)
                    .serialize(LockData.CODEC)
                    .copyOnDeath()
                    .build()
    );

    public HNessentials(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;
        //SCCommandManager.init(this);


        ATTACHMENT_TYPES.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, LockConfig.SPEC);

        // WICHTIG: Mod-Phasen Events (Setup)
        modEventBus.addListener(this::startsetup);

        // WICHTIG: Server-Events müssen auf den Forge Bus
        NeoForge.EVENT_BUS.addListener(this::setup);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);

        this.commandHelper = new CommandHelper(this);

        modEventBus.addListener(this::registerPayloads);

        NeoForge.EVENT_BUS.addListener(HNECommandManager::onRegisterCommands);
    }

    private void startsetup(final FMLCommonSetupEvent event) {

    }

    private void setup(final ServerStartingEvent event) {
        this.server = event.getServer();
        tpaRequester = new TpaManager();
        serverStartup(server);
    }

    public void serverStartup(MinecraftServer eventserver) {
        storageManager = new StorageManager(this);
        homeManager = new HomeManager(this);
        databaseManager = new DatabaseManager(this);

        storageManager.loadAllPlayerDatas();

        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("hnrpmc", "cosmetics/longhat/longhat");

        GeneralDefaultData GDD = getStorageManager().getGeneralData();

        Map<String, CosmeticType<?>> cosmeticTypeMap = Map.of("hat", CosmeticType.HAT, "cape", CosmeticType.CAPE, "backbling", CosmeticType.BACK_BLING);

        for (ConfigCosmetic CC : GDD.getCosmetics()) {
            CosmeticType<Model> CT = (CosmeticType<Model>) cosmeticTypeMap.get(CC.getType().toLowerCase());

            CosmeticRegistry.register(create(CC.getID(), CC.getName(), CT, CT.getAssociatedSlot()));
        }

        getStorageManager().saveGeneralData();


        registerEvents();
    }

    private void onServerStopping(ServerStoppingEvent event) {
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            HNPlayerData pd = storageManager.getOrCreatePlayerData(sp.getUUID());
            Vec3 playerpos = sp.getPosition(0);
            Map<String, Object> logoutLocation = new HashMap<>();
            logoutLocation.put("world-name", "");
            logoutLocation.put("x", playerpos.x);
            logoutLocation.put("y", playerpos.y);
            logoutLocation.put("z", playerpos.z);
            logoutLocation.put("yaw", sp.getYRot());
            logoutLocation.put("pitch", sp.getXRot());
            pd.setLogoutLocation(logoutLocation);
        }
        this.server = null;
    }

    public final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final var registrar = event.registrar("hnrpmc");

        // --- CLIENT -> SERVER ---
        registrar.playToServer(
                SaveSkinPayload.TYPE,
                SaveSkinPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    var player = context.player();
                    UUID petUuid = UUID.fromString(payload.PetUUID());
                    var storage = this.getStorageManager();

                    if (storage != null) {
                        var data = storage.getOrCreatePlayerData(player.getUUID());
                        data.setPetSelectedTextureForPet(payload.skinint(), petUuid);
                        storage.save(player.getUUID());

                        Entity entity = getServer().overworld().getEntity(petUuid);
                        if (entity != null) {
                            entity.getPersistentData().putInt("skinIndex", payload.skinint());
                            if (entity instanceof LivingEntity LE) {
                                ServerPacketHandler.syncEntityData(LE);
                            }
                        }



                        context.reply(new PlayerDataResponsePayload(payload.PetUUID(), payload.skinint()));
                    }
                })
        );

        registrar.playToClient(
                EntityNBTPayload.TYPE,
                EntityNBTPayload.STREAM_CODEC,
                (payload, context) -> {
                    // Logik auf dem Client
                    context.enqueueWork(() -> {
                        Entity entity = Minecraft.getInstance().level.getEntity(payload.entityId());
                        if (entity != null) {
                            // Hier schreiben wir die Daten in das lokale NBT des Clients
                            entity.getPersistentData().merge(payload.nbt());
                        }
                    });
                }
        );

        // --- CLIENT -> SERVER ---
        registrar.playToServer(
                PlayerDataRequestPayload.TYPE,
                PlayerDataRequestPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();

                    if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {

                        net.minecraft.world.entity.Entity target = serverLevel.getEntity(payload.petUUID());

                        if (target instanceof net.minecraft.world.entity.TamableAnimal animal && animal.getOwnerUUID() != null) {
                            var storage = this.getStorageManager();
                            if (storage == null) return;

                            HNPlayerData ownerData = storage.getOrCreatePlayerData(animal.getOwnerUUID());
                            int skinIndex = ownerData == null ? 0 : ownerData.getPetSelectedTextureForPet(payload.petUUID());

                            int vipScore = PlayerEventLister.getPlayerScore(player, "VIPs");

                            context.reply(new PlayerDataResponsePayload(payload.petUUID().toString(), skinIndex));
                            context.reply(new ScoreSyncPayload(vipScore));
                        }
                    }
                })
        );

        // --- SERVER -> CLIENT ---
        registrar.playToClient(
                ScoreSyncPayload.TYPE,
                ScoreSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    HNessentials.clientVipScore = payload.scoreValue();
                })
        );

        // --- SERVER -> CLIENT ---
        registrar.playToClient(
                PlayerDataResponsePayload.TYPE,
                PlayerDataResponsePayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        UUID petUuid = UUID.fromString(payload.PetUUID());

                        clientPetSkins.put(petUuid, payload.selected());


                        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                        if (mc.screen instanceof PetMorphScreen morphScreen) {
                            morphScreen.updateSelectedSkin(payload.selected());
                        }

                        if (payload.selected() >= 0 && payload.selected() < skins.size()) {
                            PetMorphScreen.currentSkinName = skins.get(payload.selected());
                        }
                    });
                }
        );

        // SERVER -> CLIENT
        registrar.playToClient(
                OpenAdminScreenPayload.TYPE,
                OpenAdminScreenPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ClientPacketHandler.handleAdminGuiOpen(payload, context);
                }
        ));

        // CLIENT -> SERVER
        registrar.playToServer(
                AdminUpdateDataPayload.TYPE,
                AdminUpdateDataPayload.STREAM_CODEC,
                ServerPacketHandler::handleAdminUpdate
        );


        // CLIENT -> SERVER
        registrar.playToServer(
                requestPlayerData.TYPE,
                requestPlayerData.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ServerPacketHandler.sendData(payload.target());
                }
        ));

        // SERVER -> CLIENT
        registrar.playToClient(
                CosmeticUpdatePayload.TYPE,
                CosmeticUpdatePayload.STREAM_CODEC,
                (payload, context) -> ClientPacketHandler.ClientDataCache.updateCosmetic(payload.playerUUID(), CosmeticSlot.HAT, payload.cosmeticId())
        );

        // Server -> Client
        registrar.playToClient(
                responsePlayerData.TYPE,
                responsePlayerData.CODEC,
                (payload, context) -> {
                    // Das hier passiert auf dem Client, wenn das Paket ankommt
                    context.enqueueWork(() -> {
                        try {
                            System.out.println("Empfange JSON: " + payload.jsonString());

                            // 1. Erstmal als generisches Element parsen
                            JsonElement element = JsonParser.parseString(payload.jsonString());

                            if (element.isJsonObject()) {
                                JsonObject rootObj = element.getAsJsonObject();
                                Map<UUID, HNPlayerData> newMap = new HashMap<>();

                                // 2. Jeden Eintrag einzeln durchgehen
                                for (Map.Entry<String, JsonElement> entry : rootObj.entrySet()) {
                                    try {
                                        UUID uuid = UUID.fromString(entry.getKey());

                                        // HIER passiert der Fehler:
                                        // Wenn entry.getValue() ein String ist, kann GSON kein HNPlayerData daraus machen.
                                        if (entry.getValue().isJsonObject()) {
                                            HNPlayerData data = gson.fromJson(entry.getValue(), HNPlayerData.class);
                                            newMap.put(uuid, data);
                                        } else {
                                            System.err.println("Überspringe Key " + entry.getKey() + " weil der Wert kein Objekt ist: " + entry.getValue());
                                        }
                                    } catch (IllegalArgumentException e) {
                                        System.err.println("Ungültige UUID im JSON-Key: " + entry.getKey());
                                    }
                                }


                                if (HNplayerDataMap == null) HNplayerDataMap = new HashMap<>();
                                HNplayerDataMap.putAll(newMap);

                                Map<UUID, PlayerData> playerDataMap1 = new HashMap<>();
                                for (HNPlayerData hnPlayerData : newMap.values()){
                                    List<BakableModel> hatbakableModels = new ArrayList<>();
                                    if (hnPlayerData.hats() != null) {
                                        for (String hatid : hnPlayerData.hats()) {
                                            Model hatmodel = CosmeticFetcher.getModel(CosmeticType.HAT, hatid);
                                            if (hatmodel != null) {
                                                BakableModel hatbm = Models.createBakableModel(hatmodel);
                                                if (hatbm != null) {
                                                    hatbakableModels.add(hatbm);
                                                }
                                            }
                                        }
                                    }


                                    BakableModel LBudybm = null;
                                    if (hnPlayerData.leftShoulderBuddy() != null) {
                                        Model LBudymodel = CosmeticFetcher.getModel(CosmeticType.HAT, hnPlayerData.leftShoulderBuddy());

                                        if (LBudymodel != null) {
                                            LBudybm = Models.createBakableModel(LBudymodel);
                                        }
                                    }

                                    BakableModel RBudybm = null;
                                    if (hnPlayerData.leftShoulderBuddy() != null) {
                                        Model RBudymodel = CosmeticFetcher.getModel(CosmeticType.HAT, hnPlayerData.rightShoulderBuddy());

                                        if (RBudymodel != null) {
                                            RBudybm = Models.createBakableModel(RBudymodel);
                                        }
                                    }

                                    BakableModel BBlingbm = null;
                                    if (hnPlayerData.backBling() != null) {
                                        Model RBudymodel = CosmeticFetcher.getModel(CosmeticType.HAT, hnPlayerData.backBling());

                                        if (RBudymodel != null) {
                                            BBlingbm = Models.createBakableModel(RBudymodel);
                                        }
                                    }

                                    PlayerData newPLData = new PlayerData(hnPlayerData.lore(), hnPlayerData.upsideDown(), hnPlayerData.icon(), hnPlayerData.online(), hnPlayerData.prefix(), hnPlayerData.suffix(), hatbakableModels, null, LBudybm, RBudybm, BBlingbm, hnPlayerData.skin(), hnPlayerData.slim());
                                    playerDataMap1.put(hnPlayerData.getPlayerUUID(), newPLData);
                                }
                                playerDataMap = playerDataMap1;
                            }
                        } catch (Exception e) {
                            System.err.println("Totaler Fehler beim PlayerData-Parsing!");
                            e.printStackTrace();
                        }
                    });
                }
        );

        //CosmeticRegistry.register(WITCH_HAT);
        //CosmeticRegistry.register(MASK);
        //CosmeticRegistry.register(WASSER_MELONE);
    }

    public static final CustomCosmetic LONG_HAT = create("longhat", "Long Hat", CosmeticType.HAT, CosmeticSlot.HAT);
    public static final CustomCosmetic WITCH_HAT = create("witchhat", "Witch Hat", CosmeticType.HAT, CosmeticSlot.HAT);
    public static final CustomCosmetic WASSER_MELONE = create("wassermelone", "Wassermelone", CosmeticType.HAT, CosmeticSlot.HAT);
    public static final CustomCosmetic MASK = create("mask", "Player Mask", CosmeticType.HAT, CosmeticSlot.HAT);
    private static CustomCosmetic create(String id, String name, CosmeticType<?> type, CosmeticSlot slot) {
        return new SimpleCosmetic(id, name, type, slot);
    }


    private void registerEvents() {
        var bus = NeoForge.EVENT_BUS;

        bus.register(new PlayerEventLister(this));
    }

    public static HNessentials getInstance() {
        return instance;
    }

    public List<String> getSkins() {
        return skins;
    }

    public CommandHelper getCommandHelper() {
        return commandHelper;
    }

    public Logger getLogger() {
        return LOGGER;
    }

    public SettingsManager getSettingsManager() {
        return SimpleClans.getInstance().getSettingsManager();
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public TpaManager getTpaRequester() {
        return tpaRequester;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
