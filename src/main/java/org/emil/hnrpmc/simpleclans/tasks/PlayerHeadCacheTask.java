package org.emil.hnrpmc.simpleclans.tasks;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerHeadCacheTask {

    private final SimpleClans plugin;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public PlayerHeadCacheTask(@NotNull SimpleClans plugin) {
        this.plugin = plugin;
    }

    public void start() {
        // Startet nach 0 Sekunden und wiederholt alle 3660 Sekunden (1 Std + 60 Sek)
        scheduler.scheduleAtFixedRate(this::run, 0, 3660, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    public void run() {
        plugin.getLogger().info("Caching player heads (fetching profiles)...");
        long begin = System.currentTimeMillis();

        List<ClanPlayer> players = plugin.getClanManager().getAllClanPlayers();
        players.sort(Comparator.comparing(ClanPlayer::getName));

        for (ClanPlayer cp : players) {
            // In Minecraft Native "cached" man Köpfe, indem man das GameProfile auflöst.
            // SkullBlockEntity.updateGameprofile sorgt dafür, dass die Texturen geladen werden.

            ItemStack head = new ItemStack(Items.PLAYER_HEAD);

            // Wir nutzen die Minecraft-interne Methode, um Profile asynchron zu laden
            SkullBlockEntity.fetchGameProfile(cp.getUniqueId()).thenAccept(profileOpt -> {
                profileOpt.ifPresent(profile -> {
                    head.set(DataComponents.PROFILE, new ResolvableProfile(profile));
                });
            });

            // Da du im Original-Code ein 9er Inventar als Dummy genutzt hast:
            // In NeoForge gibt es kein direktes Äquivalent für Dummy-Inventare ohne Container.
            // Das reine Erstellen des ItemStacks oben reicht bereits aus, um das Profil in den
            // internen Minecraft-Skin-Cache zu schieben.
        }

        plugin.getLogger().info(String.format("Finished caching head profiles! It took %d milliseconds...",
                System.currentTimeMillis() - begin));
    }
}