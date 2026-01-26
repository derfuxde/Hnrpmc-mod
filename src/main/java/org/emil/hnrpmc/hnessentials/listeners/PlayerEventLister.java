package org.emil.hnrpmc.hnessentials.listeners;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.emil.hnrpmc.hnessentials.*;
import org.emil.hnrpmc.hnessentials.cosmetics.SyncCosmeticPayload;
import org.emil.hnrpmc.hnessentials.network.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerEventLister {
    private static HNessentials plugin;

    public PlayerEventLister(@NotNull HNessentials plugin) {
        this.plugin = plugin;
    }

    @SubscribeEvent
    public void onSpawnPointSet(PlayerSetSpawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BlockPos pos = event.getNewSpawn();
            boolean isForced = event.isForced(); // true bei /spawnpoint, false bei Bett


            if (pos == null) return;

            HNPlayerData playerData = plugin.getStorageManager().getOrCreatePlayerData(player.getUUID());
            List<Home> homes = playerData.getPlayerHomes();

            for (Home home : homes) {
                if (Objects.equals(home.getHomename(), "bed")) {
                    homes.remove(home);
                }
            }

            Home bedhome = new Home(player.getUUID(), new Vec3(pos.getX(), pos.getY(), pos.getZ()), "bed", player.level().dimension().location().toString());

            homes.add(bedhome);
            playerData.setPlayerHomes(homes);

            plugin.getStorageManager().setPlayerData(player.getUUID(), playerData);
            plugin.getStorageManager().save(player.getUUID());
        }
    }

    @SubscribeEvent
    public void onLivingTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Wolf wolf) {
            if (wolf.isTame()) {
                if (event.getTargetType() instanceof Player) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        plugin.getStorageManager().loadPlayerData(player.getUUID());
        HNPlayerData playerData = plugin.getStorageManager().getOrCreatePlayerData(player.getUUID());

        plugin.getStorageManager().setPlayerData(player.getUUID(), playerData);
        GeneralDefaultData GGD = plugin.getStorageManager().getGeneralData();
        Map<UUID, String> players = GGD.getPlayerCache();
        players.put(player.getUUID(), player.getName().getString());
        GGD.setPlayerCache(players);
        plugin.getStorageManager().updateGeneralData(GGD);
        plugin.getStorageManager().saveGeneralData();

        List<Home> homes = playerData.getPlayerHomes();

        ServerPacketHandler.sendData(player.getUUID());

        BlockPos pos = player.getRespawnPosition();

        if (homes != null && !homes.isEmpty()){
            List<Home> bedhomes = homes.stream().filter(home -> home.getHomename().equals("bed")).toList();
            if (bedhomes == null || bedhomes.isEmpty()) {
                if (pos == null) return;
                Home bedhome = new Home(player.getUUID(), new Vec3(pos.getX(), pos.getY(), pos.getZ()), "bed", player.level().dimension().location().toString());
                homes.add(bedhome);
                playerData.setPlayerHomes(homes);

                plugin.getStorageManager().setPlayerData(player.getUUID(), playerData);
                plugin.getStorageManager().save(player.getUUID());
            } else {
                Home oldbedhome = bedhomes.getFirst();
                if (pos != null) {
                    homes.removeIf(home -> Objects.equals(home.getHomename(), "bed"));
                    Home bedhome = new Home(player.getUUID(), new Vec3(pos.getX(), pos.getY(), pos.getZ()), "bed", player.level().dimension().location().toString());
                    homes.add(bedhome);
                    playerData.setPlayerHomes(homes);

                    plugin.getStorageManager().setPlayerData(player.getUUID(), playerData);
                    plugin.getStorageManager().save(player.getUUID());
                } else if (pos == null && oldbedhome==null) {
                    Home bedhome = new Home(player.getUUID(), new Vec3(0, -1000, 0), "bed", player.level().dimension().location().toString());
                    homes.add(bedhome);
                    playerData.setPlayerHomes(homes);

                    plugin.getStorageManager().setPlayerData(player.getUUID(), playerData);
                    plugin.getStorageManager().save(player.getUUID());
                }
            }
        }
        int score = getPlayerScore(player, "VIPs");

        Vec3 center = player.position();
        AABB area = new AABB(center, center).inflate(100.0);

        List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity target : entities) {
            ServerPacketHandler.syncEntityData(target);
        }

        player.connection.send(new ScoreSyncPayload(score));
    }


    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        HNPlayerData playerData = plugin.getStorageManager().getOrCreatePlayerData(player.getUUID());

        Vec3 playerpos = player.getPosition(0);
        Map<String, Object> logoutLocation = new HashMap<>();
        logoutLocation.put("world-name", "");
        logoutLocation.put("x", playerpos.x);
        logoutLocation.put("y", playerpos.y);
        logoutLocation.put("z", playerpos.z);
        logoutLocation.put("yaw", player.getYRot());
        logoutLocation.put("pitch", player.getXRot());
        playerData.setLogoutLocation(logoutLocation);

        plugin.getStorageManager().setPlayerData(player.getUUID(), playerData);
        GeneralDefaultData GGD = plugin.getStorageManager().getGeneralData();
        Map<UUID, String> players = GGD.getPlayerCache();
        players.put(player.getUUID(), player.getName().getString());
        GGD.setPlayerCache(players);
        plugin.getStorageManager().updateGeneralData(GGD);
        plugin.getStorageManager().saveGeneralData();
    }

    public static int getPlayerScore(ServerPlayer player, String objectiveName) {
        Scoreboard scoreboard = player.level().getScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);

        if (objective != null) {
            ReadOnlyScoreInfo scoreInfo = scoreboard.getPlayerScoreInfo(player, objective);
            if (scoreInfo != null) {
                return scoreInfo.value();
            }
        }
        return 0; // Standardwert, falls kein Score existiert
    }

    public static int getPlayerScore(Player player, String objectiveName) {
        Scoreboard scoreboard = player.level().getScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);

        if (objective != null) {
            ReadOnlyScoreInfo scoreInfo = scoreboard.getPlayerScoreInfo(player, objective);
            if (scoreInfo != null) {
                return scoreInfo.value();
            }
        }
        return 0; // Standardwert, falls kein Score existiert
    }

    @SubscribeEvent
    public void safePets(LivingIncomingDamageEvent event) {
        Entity target = event.getEntity();

        if (target instanceof TamableAnimal tameable) {
            LivingEntity livingEntity = tameable.getOwner();

            if (livingEntity == null) {
                return;
            }

            Scoreboard scoreboard = livingEntity.level().getScoreboard();
            if (livingEntity instanceof ServerPlayer player) {
                int val  = getPlayerScore(player, "VIPs");
                if (val > 0) {
                    event.setCanceled(true);
                }
            }
        }
    }

}
