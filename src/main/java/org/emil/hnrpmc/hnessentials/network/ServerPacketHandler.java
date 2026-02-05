package org.emil.hnrpmc.hnessentials.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.emil.hnrpmc.hnessentials.Cosmetic;
import org.emil.hnrpmc.hnessentials.CosmeticSlot;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.cosmetics.Cosmetica;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CosmeticPosition;
import org.emil.hnrpmc.hnessentials.cosmetics.api.CustomCosmetic;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers;

public class ServerPacketHandler {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public static void sendData(UUID target) {
        UUID uuid = target;

        HNessentials plugin = HNessentials.getInstance();

        HNPlayerData playerData = plugin.getStorageManager().getOrCreatePlayerData(uuid);
        Map<String, HNPlayerData> playerDatas = plugin.getStorageManager().getAllPlayerData();//new HashMap<>();
        //playerDatas.put(String.valueOf(target), plugin.getStorageManager().getOrCreatePlayerData(uuid));

        ServerPlayer sp = plugin.getServer().getPlayerList().getPlayer(uuid);
        if (sp == null) return;

        if (playerDatas != null) {
            String jsonString = gson.toJson(playerDatas);
            PacketDistributor.sendToPlayer(sp, new responsePlayerData(jsonString));
        }
    }

    public static void syncEntityData(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            EntityNBTPayload payload = new EntityNBTPayload(entity.getId(), entity.getPersistentData());

            PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
        }
    }

    public static void sendDataToAll(UUID target) {
        UUID uuid = target;

        HNessentials plugin = HNessentials.getInstance();

        HNPlayerData playerData = plugin.getStorageManager().getOrCreatePlayerData(uuid);
        Map<String, HNPlayerData> playerDatas = plugin.getStorageManager().getAllPlayerData();//new HashMap<>();
        //playerDatas.put(String.valueOf(target), plugin.getStorageManager().getOrCreatePlayerData(uuid));

        ServerPlayer sp = plugin.getServer().getPlayerList().getPlayer(uuid);
        if (sp == null) return;

        if (playerDatas != null) {
            String jsonString = gson.toJson(playerDatas);
            PacketDistributor.sendToAllPlayers(new responsePlayerData(jsonString));
        }
    }

    public void loadCustomCosmetics() {
        File cosmeticDir = new File("config/hnrpmc/cosmetics");
        if (!cosmeticDir.exists()) cosmeticDir.mkdirs();

        File[] files = cosmeticDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;

        for (File file : files) {
            try {
                String jsonContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                String id = file.getName().replace(".json", "");

                // Speichere die ID und den Inhalt in einer Server-Registry
                // Diese schickst du dann per Netzwerk-Paket an den Client
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void handleAdminUpdate(final AdminUpdateDataPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            // Der Spieler, der das Paket gesendet hat (der Admin)
            ServerPlayer admin = (ServerPlayer) context.player();

            /*if (!Conditions.permission(admin, "essentials.admin")) {
                admin.sendSystemMessage(Component.literal("§cDu hast keine Berechtigung, Daten zu ändern!"));
                return;
            }*/

            // Die Daten des Ziel-Spielers laden
            HNessentials plugin = HNessentials.getInstance();
            HNPlayerData targetData = plugin.getStorageManager().getOrCreatePlayerData(payload.target());

            if (targetData == null) {
                admin.sendSystemMessage(Component.literal("§cFehler: Spielerdaten konnten nicht gefunden werden."));
                return;
            }

            // Feld basierend auf dem Paket-Inhalt aktualisieren
            String field = payload.field().toLowerCase();
            String value = payload.value();

            try {
                switch (field) {
                    case "money" -> targetData.setMoney(Double.parseDouble(value));
                    case "godmode" -> targetData.setGodMode(Boolean.parseBoolean(value));
                    case "muted" -> targetData.setMuted(Boolean.parseBoolean(value));
                    case "cosmetic" -> {
                        if (value.equalsIgnoreCase("none")) {
                            targetData.setCosmetic(CosmeticSlot.HAT, "none");
                        } else {
                            CustomCosmetic cosmetic = CosmeticRegistry.get(value);
                            if (cosmetic != null) {
                                targetData.setCosmetic(cosmetic.getasCosmetic().getSlot(), cosmetic.getId());
                                plugin.getStorageManager().setPlayerData(targetData.getPlayerUUID(), targetData);
                                plugin.getStorageManager().save(targetData.getPlayerUUID());
                            } else {
                                admin.sendSystemMessage(Component.literal("§cFehler: Cosmetic '" + value + "' existiert nicht!"));
                                return; // Verhindert das Senden des Update-Pakets
                            }
                        }
                    }
                    case "skineffect" -> {
                        if (value.equalsIgnoreCase("none")) {
                            targetData.setSelectedskineffect("none");
                        } else {
                            targetData.setSelectedskineffect(payload.value());
                            plugin.getStorageManager().setPlayerData(targetData.getPlayerUUID(), targetData);
                            plugin.getStorageManager().save(targetData.getPlayerUUID());
                        }
                    }
                    default -> {
                        admin.sendSystemMessage(Component.literal("§cUnbekanntes Feld: " + field));
                        return;
                    }
                }

                plugin.getStorageManager().setPlayerData(payload.target(), targetData);

                plugin.getStorageManager().save(payload.target());

                sendToAllPlayers(
                        new CosmeticUpdatePayload(payload.target(), value)
                );
                sendDataToAll(payload.target());

                //sendData(payload.target());

                if (!payload.field().equals("cosmetic") && !payload.field().equals("skineffect")) {
                    admin.sendSystemMessage(Component.literal("§aErfolgreich aktualisiert: §e" + field + " §f= §7" + value));
                }

            } catch (Exception e) {
                admin.sendSystemMessage(Component.literal("§cFehler beim Verarbeiten des Wertes: " + e.getMessage()));
            }
        });
    }

    private static CosmeticPosition positionOf(int selected, CosmeticSlot type) {
        if (type == CosmeticSlot.HAT) {
            return selected > 0 ? CosmeticPosition.SECOND_HAT : CosmeticPosition.HAT; // hat by default unless specifically second
        }
        else if (type == CosmeticSlot.SHOULDER_BUDDY) {
            return selected == 0 ? CosmeticPosition.RIGHT_SHOULDER_BUDDY : CosmeticPosition.LEFT_SHOULDER_BUDDY;
        }
        else if (type == CosmeticSlot.CAPE) {
            return CosmeticPosition.CAPE;
        }
        else {
            return CosmeticPosition.BACK_BLING;
        }
    }
}