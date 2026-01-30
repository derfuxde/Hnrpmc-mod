package org.emil.hnrpmc.hnessentials.listeners;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.emil.hnrpmc.hnessentials.CosmeticSlot;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.cosmetics.*;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CosmeticaAPI;
import org.emil.hnrpmc.hnessentials.cosmetics.model.Models;
import org.emil.hnrpmc.hnessentials.cosmetics.screens.fakeplayer.Playerish;
import net.neoforged.bus.api.Event;
import org.emil.hnrpmc.hnessentials.cosmetics.utils.TextComponents;
import org.emil.hnrpmc.hnessentials.menu.PetMorphScreen;
import org.emil.hnrpmc.hnessentials.network.CosmeticRegistry;
import org.emil.hnrpmc.hnessentials.network.requestPlayerData;

import java.io.File;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

@EventBusSubscriber(modid = "hnrpmc", value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide && event.getEntity() instanceof Wolf wolf) {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new PlayerDataRequestPayload(wolf.getUUID())
            );
        }
    }

    @SubscribeEvent
    public static void onPetInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Level level = event.getLevel();

        if (!level.isClientSide) return;

        if (player.isShiftKeyDown() && event.getHand() == InteractionHand.MAIN_HAND) {
            if (event.getTarget() instanceof TamableAnimal pet && pet.isTame()) {
                if (player.getUUID().equals(pet.getOwnerUUID())) {

                    PacketDistributor.sendToServer(new PlayerDataRequestPayload(pet.getUUID()));

                    if (HNessentials.clientVipScore == 2) {
                        openPetScreen(pet);
                    }
                }
            }
        }
    }

    private static void openPetScreen(TamableAnimal pet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(new PetMorphScreen(pet, HNessentials.getInstance()));
        }
    }







    static Player secPlayer = null;

    @SubscribeEvent
    public static void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft mc = Minecraft.getInstance();

        // Entspricht der "fake server" Logik deines Mixins
        String address = "fake server " + System.currentTimeMillis();
        ServerData serverData = mc.getCurrentServer();


        Cosmetica.api = CosmeticaAPI.newUnauthenticatedInstance();

        if (serverData != null && !Objects.equals(serverData.ip, Cosmetica.authServer)) {
            address = serverData.ip;
        }

        // Cache-Logik prüfen und ausführen
        if (Cosmetica.currentServerAddressCache.isEmpty() || !Objects.equals(Cosmetica.currentServerAddressCache, address)) {
            Cosmetica.currentServerAddressCache = address;
            Cosmetica.clearAllCaches();

            // PlayerData laden (mc.player ist beim LoggedIn Event bereits vorhanden)
            if (mc.player != null) {
                PlayerData.get(mc.player);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;

        // Sicherstellen, dass die Welt geladen ist und wir nicht im Menü sind
        if (level != null) {
            // Entspricht: if (this.getGameTime() % 600 == 0)
            if (level.getGameTime() % 600 == 0) {
                Cosmetica.runOffthread(() ->
                                Cosmetica.safari(minecraft, false, false),
                        ThreadPool.GENERAL_THREADS
                );
            }
        }
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (event.getEntity() instanceof Playerish player) {
            Component content = event.getContent();
            if (!content.getString().isEmpty() && content.getString().charAt(0) == '\u2001') {
                // Hier kannst du direkt rendern
                Cosmetica.renderIcon(event.getPoseStack(), event.getMultiBufferSource(),
                        player, Minecraft.getInstance().font,
                        event.getPackedLight(), content);
            }
        }

        Component title = event.getContent();

        // Prüfen, ob das Entity "Playerish" ist und das Icon-Zeichen hat
        if (event.getEntity() instanceof Playerish player &&
                !title.getString().isEmpty() && title.getString().charAt(0) == '\u2001') {

            // NeoForge liefert uns den PoseStack und Buffer direkt im Event
            Cosmetica.renderIcon(
                    event.getPoseStack(),
                    event.getMultiBufferSource(),
                    player,
                    Minecraft.getInstance().font,
                    event.getPackedLight(),
                    title
            );
        }

        Minecraft mc = Minecraft.getInstance();

        // Wenn es der lokale Spieler ist und wir in der Third-Person-Ansicht sind
        if (event.getEntity() == mc.getCameraEntity() && event.getEntity() instanceof Player) {
            boolean isThirdPerson = mc.options.getCameraType() != CameraType.FIRST_PERSON;

            if (isThirdPerson) {
                // ALLOW erzwingt das Rendering, auch wenn Minecraft es normalerweise unterdrückt
                event.setCanRender(TriState.TRUE);
            }
        }
    }

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String message = event.getMessage();

        // Prüfen, ob die Nachricht mit '?' beginnt (wie in deinem Mixin)
        if (!message.isEmpty() && message.startsWith("?")) {
            String[] args = message.split(" ");

            if (args[0].equals("?cosmetica")) {
                // Event abbrechen, damit die Nachricht nicht an den Server gesendet wird
                event.setCanceled(true);

                executeCommand(args);
            }
        }
    }

    private static void executeCommand(String[] args) {
        Minecraft mc = Minecraft.getInstance();

        if (args.length == 2) { // Cache commands
            switch (args[1]) {
                case "infocache" ->
                        mc.gui.getChat().addMessage(TextComponents.literal(HNPlayerData.getCachedPlayers().toString()));
                case "modelcache" ->
                        mc.gui.getChat().addMessage(TextComponents.literal(Models.getCachedModels().toString()));
                case "clearcache" -> {
                    Models.resetCaches();
                    mc.gui.getChat().addMessage(TextComponents.literal(Models.getCachedModels().toString()));
                }
            }
        }
        else if (args.length == 3) {
            if (args[1].equals("staticsb")) {
                if (args[2].equals("true")) {
                    ShoulderBuddies.staticOverride = OptionalInt.of(1);
                    mc.gui.getChat().addMessage(TextComponents.literal("Static Shoulder Buddies enabled."));
                } else if (args[2].equals("false")) {
                    ShoulderBuddies.staticOverride = OptionalInt.empty();
                    mc.gui.getChat().addMessage(TextComponents.literal("Static Shoulder Buddies disabled."));
                }
            }else if (args[1].equals("hat")) {
                if (mc.player == null) return;

                HNPlayerData data = HNessentials.getInstance().HNplayerDataMap.get(mc.player.getUUID());

                if (args[2].equalsIgnoreCase("clear")) {
                    data.setCosmetic(CosmeticSlot.HAT, null);
                    mc.gui.getChat().addMessage(TextComponents.literal(
                            "Hat cleared."
                    ));
                    return;
                }

                String hatId = args[2];

                if (CosmeticRegistry.get(hatId) == null) {
                    mc.gui.getChat().addMessage(TextComponents.literal(
                            "Hat '" + hatId + "' does not exist."
                    ));
                    return;
                }

                data.setCosmetic(CosmeticSlot.HAT, hatId);
                mc.gui.getChat().addMessage(TextComponents.literal(
                        "Hat set to '" + hatId + "'."
                ));
            }
        }

    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        // Für jeden Player-Skin-Typ (slim/wide) die Layer hinzufügen
        for (PlayerSkin.Model skinType : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skinType);
            if (renderer != null) {
                renderer.addLayer(new Hats<>(renderer));
                renderer.addLayer(new ShoulderBuddies<>(renderer, event.getEntityModels()));
                renderer.addLayer(new BackBling<>(renderer));
            }
        }
    }
}