package org.emil.hnrpmc.hnessentials;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.emil.hnrpmc.hnessentials.commands.CommandHelper;
import org.emil.hnrpmc.hnessentials.commands.HNECommandManager;
import org.emil.hnrpmc.hnessentials.listeners.PlayerDataRequestPayload;
import org.emil.hnrpmc.hnessentials.listeners.PlayerEventLister;
import org.emil.hnrpmc.hnessentials.managers.HomeManager;
import org.emil.hnrpmc.hnessentials.managers.StorageManager;
import org.emil.hnrpmc.hnessentials.managers.TpaManager;
import org.emil.hnrpmc.hnessentials.menu.PetMorphScreen;
import org.emil.hnrpmc.hnessentials.network.PlayerDataResponsePayload;
import org.emil.hnrpmc.hnessentials.network.SaveSkinPayload;
import org.emil.hnrpmc.hnessentials.network.ScoreSyncPayload;
import org.emil.hnrpmc.hnessentials.requester.TpaRequester;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HNessentials {
    private StorageManager storageManager;
    private MinecraftServer server;
    private TpaManager tpaRequester;
    private HomeManager homeManager;
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final List<String> skins = List.of("Standard", "Shadow", "Gold", "Diamond", "Rainbow");
    public static final Map<java.util.UUID, Integer> clientPetSkins = new HashMap<>();
    public static int clientVipScore = 0;

    private CommandHelper commandHelper;

    private static HNessentials instance;

    public HNessentials(IEventBus modEventBus) {
        instance = this;
        //SCCommandManager.init(this);

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
        this.tpaRequester = new TpaManager();
        serverStartup(server);
    }

    public void serverStartup(MinecraftServer eventserver) {
        this.storageManager = new StorageManager(this);
        this.homeManager = new HomeManager(this);

        storageManager.loadAllPlayerDatas();
        registerEvents();
    }

    private void onServerStopping(ServerStoppingEvent event) {
        this.server = null;
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final var registrar = event.registrar("hnrpmc");

        // --- CLIENT -> SERVER: Skin speichern ---
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

                        // Optional: Broadcast an alle Spieler in der Nähe, damit der Skin sofort aktualisiert wird
                        context.reply(new PlayerDataResponsePayload(payload.PetUUID(), payload.skinint()));
                    }
                })
        );

        // --- CLIENT -> SERVER: Daten anfragen ---
        registrar.playToServer(
                PlayerDataRequestPayload.TYPE,
                PlayerDataRequestPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();

                    // WICHTIG: Zu ServerLevel casten, um getEntity(UUID) nutzen zu können
                    if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {

                        // Nutze die UUID aus dem Payload
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

        // --- SERVER -> CLIENT: Score empfangen ---
        registrar.playToClient(
                ScoreSyncPayload.TYPE,
                ScoreSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    HNessentials.clientVipScore = payload.scoreValue();
                })
        );

        // --- SERVER -> CLIENT: Pet-Daten empfangen (NUR EINMAL REGISTRIEREN!) ---
        registrar.playToClient(
                PlayerDataResponsePayload.TYPE,
                PlayerDataResponsePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    UUID petUuid = UUID.fromString(payload.PetUUID());

                    // 1. In den Cache für den Renderer (GoldWolfLayer) speichern
                    clientPetSkins.put(petUuid, payload.selected());

                    // 2. GUI updaten, falls der Spieler gerade im Menü ist
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc.screen instanceof PetMorphScreen morphScreen) {
                        morphScreen.updateSelectedSkin(payload.selected());
                    }

                    // 3. Den Namen des Skins global für das Menü setzen
                    if (payload.selected() >= 0 && payload.selected() < skins.size()) {
                        PetMorphScreen.currentSkinName = skins.get(payload.selected());
                    }
                })
        );
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
}
