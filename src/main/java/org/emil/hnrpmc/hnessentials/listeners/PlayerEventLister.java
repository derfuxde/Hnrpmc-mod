package org.emil.hnrpmc.hnessentials.listeners;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import org.emil.hnrpmc.hnessentials.HNPlayerData;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.Home;
import org.emil.hnrpmc.hnessentials.network.ScoreSyncPayload;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

            System.out.println("Spieler " + player.getName().getString() +
                    " hat neuen Spawn bei: " + pos.toString());

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
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        plugin.getStorageManager().loadPlayerData(player.getUUID());
        HNPlayerData playerData = plugin.getStorageManager().getOrCreatePlayerData(player.getUUID());

        int score = getPlayerScore(player, "VIPs");

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
