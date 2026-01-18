package org.emil.hnrpmc.hnclaim.listeners;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.emil.hnrpmc.hnclaim.Claim;
import org.emil.hnrpmc.hnclaim.HNClaims;
import org.emil.hnrpmc.hnclaim.managers.ClaimManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public class ClaimPlayerListener {

    private final HNClaims plugin;
    //private final SettingsManager settingsManager;
    private final ClaimManager claimManager;

    Map<String, ServerBossEvent> bossbarclaim = new HashMap<>();
    Map<String, Claim> playerlastclaim = new HashMap<>();

    public ClaimPlayerListener(@NotNull HNClaims plugin) {
        this.plugin = plugin;
        //this.settingsManager = plugin.getSettingsManager();
        this.claimManager = plugin.getClaimManager();
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
    }

    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
    }

    Map<UUID, Integer> countdown = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Claim claim = claimManager.getClaimbyPlayerPos(player);
        if (claim != null) {
            ServerLevel level = player.serverLevel();

            if (playerlastclaim != null) {
                String playerUUID = player.getUUID().toString();
                Claim lastClaim = playerlastclaim.get(playerUUID);

                if (lastClaim == null || !lastClaim.getownerUUID().equals(claim.getownerUUID())) {

                    String plname = plugin.getServer().getProfileCache().get(claim.getownerUUID())
                            .map(profile -> profile.getName())
                            .orElse("Unbekannt");

                    player.displayClientMessage(Component.literal("Du hast den Claim von " + plname + " betreten"), true);

                    if (claim.getBanPlayers().contains(player.getUUID())) {
                        player.displayClientMessage(Component.literal("Bitte verlass diesen claim du bist hier gebannt " + countdown.get(player.getUUID())), true);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (claimManager.getClaimbyPlayerPos(player) == null) return;
                                BlockPos spawnPos = level.getSharedSpawnPos();

                                int x = spawnPos.getX();
                                int y = spawnPos.getY();
                                int z = spawnPos.getZ();
                                player.teleportTo(x, y, z);
                            }
                        }, 3000);
                    }

                    playerlastclaim.put(playerUUID, claim);
                }
            }

            claimManager.spawnParticleLine(level, claim.getCorner1(), claim.getCorner2(), player);
            claimManager.spawnParticleLine(level, claim.getCorner2(), claim.getCorner3(), player);
            claimManager.spawnParticleLine(level, claim.getCorner3(), claim.getCorner4(), player);
            claimManager.spawnParticleLine(level, claim.getCorner4(), claim.getCorner1(), player);
        } else if (claim == null && !playerlastclaim.isEmpty() && playerlastclaim.get(player.getUUID().toString()) != null) {
            String plname = plugin.getServer().getProfileCache().get(player.getUUID()).get().getName();
            player.displayClientMessage(Component.literal("Du hast den Claim von " + plname + " verlassen"), true);
            playerlastclaim.remove(player.getUUID().toString());
        }

        if (claim != null && claim.getBanPlayers() != null && claim.getBanPlayers().contains(player.getUUID())) {
            if (player.tickCount % 20 != 0) {
                return;
            }
            if (countdown.containsKey(player.getUUID())) {
                if (countdown.get(player.getUUID()) > 0) {
                    player.displayClientMessage(Component.literal("Bitte verlass diesen claim du bist hier gebannt " + countdown.get(player.getUUID())), true);
                    countdown.put(player.getUUID(),countdown.get(player.getUUID())-1 );
                }else if (countdown.get(player.getUUID()) <= 0) {
                    Vec3 teleportPos = Vec3.atBottomCenterOf(player.level().getSharedSpawnPos());
                    player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                }

            }

        }
        if (player.tickCount % 60 != 0) {
            return;
        }

        boolean holdsTool = isHoldingTool(player.getMainHandItem()) || isHoldingTool(player.getOffhandItem());

        if (holdsTool) {
            for (Claim ssclaim : claimManager.getClaims()) {
                claimManager.displayClaim(ssclaim, player);
            }
        }else {
            claimManager.cleardisplay(null, player);
        }

        List<Claim> claimlist = claimManager.getClaims();

        for (Claim sclaim : claimManager.getClaims()) {
            for (int i = 0; i <= 1; i++) {
                String subfix = i == 1 ? "_owner" : "";
                if (!bossbarclaim.containsKey(sclaim.getName() + subfix)) {
                    GameProfile claimowner = plugin.getServer().getProfileCache().get(sclaim.getownerUUID()).get();
                    String title = i == 0 ? "Du befindest dich im Claim von " + claimowner.getName() : "Du befindest dich in deinem Claim";
                    BossEvent.BossBarColor color = i == 0 ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.GREEN;
                    ServerBossEvent claimBar = new ServerBossEvent(
                            Component.literal(title),
                            color,
                            BossEvent.BossBarOverlay.PROGRESS
                    );
                    bossbarclaim.put(sclaim.getName() + subfix, claimBar);
                }
            }
        }

        //plugin.getLogger().debug("hier alle claim centers {}", claimlist.stream().map(Claim::getCenter).toList());

        for (ServerBossEvent bossEvent : bossbarclaim.values()) {
            if (bossEvent.getPlayers().contains(player)) {
                bossEvent.removePlayer(player);
            }
        }

        if (claim != null) {
            String subfix = player.getUUID().equals(claim.getownerUUID()) ? "_owner" : "";
            ServerBossEvent barforclaim = bossbarclaim.get(claim.getName() + subfix);
            if (!barforclaim.getPlayers().contains(player)) {
                barforclaim.addPlayer(player);

            }
        }

        if (player.tickCount % 6000 != 0) {
            if (plugin.getSettingsManager().is(PERFORMANCE_SAVE_PERIODICALLY)) {
                plugin.getStorageManager().saveModified();
            }
            plugin.getStorageManager().updateAllClaim();
            plugin.getLogger().debug("All claims are updating");
            return;
        }
    }

    private final Map<UUID, EditingSession> editingSessions = new HashMap<>();

    private record EditingSession(Claim claim, int cornerIndex) {}

    public static boolean isHoldingTool(ItemStack item) {
        return item != null && item.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().contains("hnclaim_tool");
    }

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isHoldingTool(event.getItemStack())) return;
        event.setCanceled(true);

        BlockPos clickedPos = event.getPos();
        //Claim claim = claimManager.getClaimbyPlayerPos(player);
        Vec3 clickVec3 = new Vec3(clickedPos.getX(), clickedPos.getY(), clickedPos.getZ());
        Claim claim = claimManager.getClaimbyPos(clickVec3, player.level().dimension().location().toString());


        if (claim == null || !claim.getownerUUID().equals(player.getUUID())) {
            player.displayClientMessage(Component.literal("§cDu musst in deinem eigenen Claim stehen, um Ecken zu verschieben!"), true);
            return;
        }

        List<BlockPos> claimcorners = new ArrayList<>();
        claimcorners.add(claimManager.parseString(claim.getCorner1()));
        claimcorners.add(claimManager.parseString(claim.getCorner2()));
        claimcorners.add(claimManager.parseString(claim.getCorner3()));
        claimcorners.add(claimManager.parseString(claim.getCorner4()));

        if (!claimcorners.contains(clickedPos.atY(0))) {
            player.displayClientMessage(Component.literal("§cDu musst die ecke eines Claims auswählen"), true);
            return;
        }

        int closestCorner = claimManager.getClosestCornerIndex(claim, clickedPos);
        editingSessions.put(player.getUUID(), new EditingSession(claim, closestCorner));

        player.displayClientMessage(Component.literal("§6Ecke " + closestCorner + " ausgewählt! §7Mache einen §eRechtsklick§7, um sie zu verschieben."), true);
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isHoldingTool(event.getItemStack())) return;

        UUID uuid = player.getUUID();
        if (!editingSessions.containsKey(uuid)) return; // Normales Erstellen, wenn keine Ecke gewählt wurde

        event.setCanceled(true);
        EditingSession session = editingSessions.get(uuid);
        BlockPos newPos = event.getPos().atY(0);
        int othercornerindex = ((session.cornerIndex + 2) % 4);
        if (othercornerindex == 0) {
            othercornerindex = 4;
        }

        BlockPos othercorner = claimManager.getBlockPosbyCornerindex(session.claim, othercornerindex);

        Vec3 othercornerVec3 = new Vec3(othercorner.getX(), 360, othercorner.getZ());
        Vec3 newPosVec3 = new Vec3(newPos.getX(), 0.0, newPos.getZ());

        for (Claim claim : claimManager.getClaims()) {
            if (Objects.equals(claim.getName(), session.claim.getName())) continue;
            Vec3 c1 = claimManager.stringToVec3(claim.getCorner1(), 0);
            Vec3 c3 = claimManager.stringToVec3(claim.getCorner3(), 360);
            if (c1 != null && c3 != null) {
                AABB claimBox = new AABB(c1, c3).inflate(1.0);


                AABB claimBox2 = new AABB(newPosVec3, othercornerVec3).inflate(0.0);

                if (claimBox.intersects(claimBox2)) {
                    player.displayClientMessage(Component.literal("§cClaims dürfen sich nicht mit anderen überschneiden!"), true);
                    return;
                }

            }
        }

        if (claimManager.getClaimbyPos(Vec3.atBottomCenterOf(newPos), player.level().dimension().location().toString(), 0.0) != null && claimManager.getClaimbyPos(Vec3.atBottomCenterOf(newPos), player.level().dimension().location().toString(), 0.0).getName() != session.claim.getName()) {
            player.displayClientMessage(Component.literal("§cClaims dürfen sich nicht mit anderen überschneiden!"), true);
            return;
        }

        claimManager.moveCorner(session.claim(), session.cornerIndex(), newPos);

        player.displayClientMessage(Component.literal("§aEcke wurde verschoben!"), true);
        editingSessions.remove(uuid);

        plugin.getStorageManager().updateClaim(session.claim());
    }
}