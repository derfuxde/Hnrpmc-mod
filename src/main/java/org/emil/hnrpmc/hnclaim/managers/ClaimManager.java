package org.emil.hnrpmc.hnclaim.managers;

import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import org.apache.logging.log4j.core.jmx.Server;
import org.emil.hnrpmc.hnclaim.Claim;
import org.emil.hnrpmc.hnclaim.HNClaims;
import org.emil.hnrpmc.hnclaim.claimperms;
import org.emil.hnrpmc.hnclaim.events.CreateClaimEvent;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.emil.hnrpmc.simpleclans.overlay.Tablist;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.ChatFormatting.AQUA;
import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

enum AXIS{
    X("X"),
    Y("Y"),
    Z("Z");

    private String axisname;

    AXIS(String axisname) {
        this.axisname = axisname;
    }

    public String getAxisname() {
        return axisname;
    }
}

/**
 * @author phaed
 */
public final class ClaimManager {

    private final HNClaims plugin;
    private final ConcurrentHashMap<String, Claim> claims = new ConcurrentHashMap<>();

    Map<UUID, Map<String, List<BlockPos>>> displayblocklist = new HashMap<>();

    /**
     *
     */
    public ClaimManager() {
        plugin = HNClaims.getInstance();
    }

    public Claim getClaimByName(String name) {
        for (Map.Entry<String, Claim> clan : claims.entrySet()) {
            if (Objects.equals(clan.getValue().getName(), name)) {
                return clan.getValue();
            }
        }
        return null;
    }

    /**
     * Deletes all clans and clan players in memory
     */
    public void cleanData() {
        claims.clear();
    }

    private ServerPlayer getPlayerFromSource(CommandSourceStack source) {
        return source.getEntity() instanceof ServerPlayer p ? p : null;
    }


    public ServerPlayer getServerPlayer(UUID uuid) {
        return SimpleClans.getInstance().getServer().getPlayerList().getPlayer(uuid);
    }

    /**
     * Import a clan into the in-memory store
     */
    public void importClan(Claim claim) {
        this.claims.put(claim.getName(), claim);
    }

    public double getAllClaimsize(ServerPlayer player, double minus) {
        double size = 0;
        for (Claim claim : getClaimsByOwnerUniqueId(player.getUUID())) {
            BlockPos pos = parseString(claim.getCorner1());
            BlockPos pos2 = parseString(claim.getCorner3());

            int breiteX = Math.abs(pos.getX() - pos2.getX()) + 1;
            int tiefeZ = Math.abs(pos2.getZ() - pos2.getZ()) + 1;
            int flaeche = breiteX * tiefeZ;

            size = size + flaeche;
        }

        return size - minus;
    }

    public double getAllClaimsize(ServerPlayer player) {
        return getAllClaimsize(player, 0.0);
    }



    /**
     * Create a new clan
     */
    public void createClaim(ServerPlayer player, String name) {

        Vec3 PlayerPos = player.getPosition(0);

        String playerposstring = String.valueOf(PlayerPos.get(Direction.Axis.X)).split("\\.")[0] + ".0, 0.0, " + String.valueOf(PlayerPos.get(Direction.Axis.Z)).split("\\.")[0] + ".0";

        Vec3 newplayerpos = Vec3.atBottomCenterOf(parseString(playerposstring));
        Vec3 Corner1 = newplayerpos.add(3.0, 0.0, 3.0);
        Vec3 Corner3 = newplayerpos.add(-3.0, 0.0, -3.0);
        Vec3 Corner4 = newplayerpos.add(-3.0, 0.0, 3.0);
        Vec3 Corner2 = newplayerpos.add(3.0, 0.0, -3.0);

        ClaimManager claimManager = plugin.getClaimManager();

        AABB claimBox2 = new AABB(Corner1, Corner3).inflate(0.0);

        String maxblocks = plugin.getSettingsManager().getString(MAX_CLAIMED_BLOCKS);
        String enmax = maxblocks;
        if (maxblocks.contains("%")) {
            enmax = plugin.getSettingsManager().parseConditionalMessage(player, plugin.getSettingsManager().getString(MAX_CLAIMED_BLOCKS));
        }


        if (claimBox2.getSize() + getAllClaimsize(player) > Integer. parseInt(enmax)) {
            player.sendSystemMessage(Component.literal("§cDu hast die maximale anzahl an geclaimten blöcken erreicht"));
            return;
        }

        for (Claim claim : claimManager.getClaims()) {
            Vec3 c1 = claimManager.stringToVec3(claim.getCorner1(), 0);
            Vec3 c3 = claimManager.stringToVec3(claim.getCorner3(), 360);
            if (c1 != null && c3 != null) {
                AABB claimBox = new AABB(c1, c3).inflate(1.0);




                if (claimBox.intersects(claimBox2)) {
                    player.displayClientMessage(Component.literal("§cClaims dürfen sich nicht mit anderen überschneiden!"), true);
                    return;
                }

            }
        }

        Claim claim = new Claim(name, player, Corner1.toString(), Corner2.toString(), Corner3.toString(), Corner4.toString(), newplayerpos.toString());
        claim.setOwnerUUID(player.getUUID());

        displayClaim(claim, player);

        plugin.getStorageManager().insertClan(claim);
        importClan(claim);

        NeoForge.EVENT_BUS.post(new CreateClaimEvent(claim));
    }

    public void spawnParticleWall(Level level, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4) {
        // Draw the four edges of the rectangle
        int timer = 200;
        int stimer = timer;
        for (int i = 0; i < 50; i++) {
            plugin.getLogger().debug("part");
            //spawnParticleLine((ServerLevel) level, p1, p2);
            //spawnParticleLine((ServerLevel) level, p2, p3);
            //spawnParticleLine((ServerLevel) level, p3, p4);
            //spawnParticleLine((ServerLevel) level, p4, p1);

        }
    }

    public BlockPos parseString(String s) {
        try {
            String clean = s.replaceAll("[^0-9.,-]", "");
            String[] parts = clean.split(",");

            int x = (int) Double.parseDouble(parts[0]);
            int y = (int) Double.parseDouble(parts[1]);
            int z = (int) Double.parseDouble(parts[2]);

            //plugin.getLogger().debug("string ge filtert {}, {}, {}", x, y, z);

            return new BlockPos(x, 0, z);
        } catch (Exception e) {
            plugin.getLogger().error("Fehler beim Parsen der Corner: " + s);
            return new BlockPos(0, 0, 0);
        }
    }

    static class Column {
        public Column() {}
        public Column(String c) {}
    }

    public void displayClaim(Claim claim, ServerPlayer player) {
        List<BlockPos> poslist = new ArrayList<>();
        poslist.add(parseString(claim.getCorner1()));
        poslist.add(parseString(claim.getCorner2()));
        poslist.add(parseString(claim.getCorner3()));
        poslist.add(parseString(claim.getCorner4()));

        //plugin.getLogger().debug("hier die poslist {}", poslist);

        Map<String, String> betwenlist = new HashMap<>();
        betwenlist.put(claim.getCorner1(), claim.getCorner2());
        betwenlist.put(claim.getCorner2(), claim.getCorner3());
        betwenlist.put(claim.getCorner3(), claim.getCorner4());
        betwenlist.put(claim.getCorner4(), claim.getCorner2());

        if (!claim.getDimension().equals(player.level().dimension().location().toString())) return;


        //plugin.getLogger().debug("es sind {} in betwenlist {}", betwenlist.size(), betwenlist.entrySet()
                //.stream()
                //.collect(Collectors.toMap(Map.Entry::getKey,
                        //e -> new Column(e.getValue()))));
        int canter = 0;

        List<BlockPos> mappingmap = new ArrayList<>();
        for (int i = 0; i < betwenlist.size(); i++) {
            int canter2 = 0;
            BlockPos start = poslist.get(i);
            BlockPos end = poslist.get((i + 1) % 4);
            for (BlockPos pos : BlockPos.betweenClosed(start, end)) {
                BlockPos staticblock = new BlockPos(pos.getX(), 0, pos.getZ());
                mappingmap.add(staticblock);
                //plugin.getLogger().debug("ist jetzt beim block {} der counter1 ist {} und der counter2 ist {} hier die values {} und {}", staticblock, canter, canter2, start.toString().replace("BlockPos", ""),  end.toString().replace("BlockPos", ""));

                canter2++;
                if (canter > 100 || canter2 > 1000) {
                    return;
                }
            }
            canter++;
        }

        //plugin.getLogger().debug("die mappingmap hat {} einträe {}", mappingmap.size(), mappingmap);

        List<BlockPos> bplist = new ArrayList<>();
        Map<String, List<BlockPos>> innermap = new HashMap<>();
        if (displayblocklist == null) return;
        displayblocklist.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        if (!displayblocklist.isEmpty() && !displayblocklist.get(player.getUUID()).isEmpty()) {
            innermap = displayblocklist.get(player.getUUID());
        }
        for (BlockPos pos : mappingmap) {
            //plugin.getLogger().debug("now to {}", pos.toString());
            for (int i = 0; i < 320; i++) {
                int x = pos.getX();
                int z = pos.getZ();
                //int x = Integer.parseInt(pos.split(", ")[0].split("\\.")[0].replaceAll("[^0-9.]", ""));
                //int z = Integer.parseInt(pos.split(", ")[2].split("\\.")[0].replaceAll("[^0-9.]", ""));
                BlockPos blockPos = new BlockPos(x, i, z);
                BlockPos blockPos2 = new BlockPos(x, i-1, z);
                if (!player.serverLevel().getBlockState(blockPos).isAir()) {
                    continue;
                }
                if (player.serverLevel().getBlockState(blockPos2).isAir()) {
                    continue;
                }

                BlockState state = Blocks.RED_GLAZED_TERRACOTTA.defaultBlockState();
                if (poslist.contains(pos)) {
                    state = Blocks.REDSTONE_BLOCK.defaultBlockState();
                }
                if (player.getUUID().equals(claim.getownerUUID())) {
                    state = Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.defaultBlockState();
                    if (poslist.contains(pos)) {
                        state = Blocks.DIAMOND_BLOCK.defaultBlockState();
                    }
                }

                ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(blockPos2, state);

                bplist.add(blockPos2);

                player.connection.send(packet);
            }
        }
        innermap.put(claim.getName(), bplist);
        displayblocklist.put(player.getUUID(), innermap);
    }

    public void cleardisplay(@Nullable Claim claim, ServerPlayer player) {
        if (displayblocklist.isEmpty()) {
            return;
        }
        Map<String, List<BlockPos>> inntermap = displayblocklist.get(player.getUUID());
        if (inntermap != null || displayblocklist.isEmpty() && inntermap.isEmpty()) {
            if (claim != null) {
                List<BlockPos> claimblockposs = inntermap.get(claim.getName());
                for (BlockPos bpp : claimblockposs) {
                    BlockState realState = player.serverLevel().getBlockState(bpp);
                    player.connection.send(new ClientboundBlockUpdatePacket(bpp, realState));
                }
                inntermap.remove(claim.getName());

                displayblocklist.put(player.getUUID(), inntermap);
            } else {
                for (Map.Entry<String, List<BlockPos>> mapmap : inntermap.entrySet()) {
                    for (BlockPos bps : mapmap.getValue()) {
                        BlockState realState = player.serverLevel().getBlockState(bps);
                        player.connection.send(new ClientboundBlockUpdatePacket(bps, realState));
                    }
                }

                displayblocklist.remove(player.getUUID());
            }
        }
    }

    public int calculateValue(BlockPos pos1, BlockPos pos2) {
        int xLength = Math.abs(pos1.getX() - pos2.getX()) + 1;
        int zLength = Math.abs(pos1.getZ() - pos2.getZ()) + 1;

        int area = xLength * zLength;

        return area * 16;
    }

    public void spawnParticleLine(ServerLevel level, String stringstart, String stringend, @Nullable ServerPlayer player) {
        Vec3 start = Vec3.atBottomCenterOf(parseString(stringstart));
        Vec3 end = Vec3.atBottomCenterOf(parseString(stringend));
        double distance = start.distanceTo(end);
        double step = 0.5;

        Vec3 centerof = start.add(end).scale(0.5);

        Map<Direction.Axis, Double> dis = new HashMap<>();
        dis.put(Direction.Axis.X, 0.0);
        dis.put(Direction.Axis.Z, 0.0);
        if ((centerof.x - start.x) != 0) {
            dis.put(Direction.Axis.X, (centerof.x-end.x)/2);
        }
        if ((centerof.z - start.z) != 0) {
            dis.put(Direction.Axis.Z, (centerof.z-end.z)/2);
        }
        //double disX = centerof.x - start.x;
        //double disZ = centerof.z - start.z;



        if (player != null) {
            BlockPos startbp = new BlockPos( (int) start.x, (int) start.y, (int) start.z);
            BlockPos endbp = new BlockPos( (int) end.x, (int) end.y, (int) end.z);
            int ccv = calculateValue(startbp, endbp);

            ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
                    ParticleTypes.GLOW,
                    false,
                    centerof.x,
                    64.0,
                    centerof.z,
                    dis.get(Direction.Axis.X).floatValue(),
                    10.0f,
                    dis.get(Direction.Axis.Z).floatValue(),
                    0.0f,
                    ccv
            );

            player.connection.send(packet);

        } else {
            level.sendParticles(ParticleTypes.GLOW, centerof.x, 64.0, centerof.z, 70, dis.get(Direction.Axis.X),  10.0, dis.get(Direction.Axis.Z), 0);
        }

    }

    public Vec3 stringToVec3(String input, int hight) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        try {
            String[] parts = input.replace(" ", "").replace("(", "").replace(")", "").split(",");

            if (parts.length == 3) {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                double z = Double.parseDouble(parts[2]);
                return new Vec3(x, hight, z);
            }
        } catch (NumberFormatException e) {
            SimpleClans.getInstance().getLogger().error("Fehler beim Umwandeln von String zu Vec3: " + input);
        }

        return null;
    }

    public @Nullable Claim getClaimbyPlayerPos(ServerPlayer player) {
        List<Claim> claimsinsamedim = getClaims().stream().filter(claim -> Objects.equals(claim.getDimension(), player.level().dimension().location().toString())).toList();
        for (Claim claim : claimsinsamedim) {
            Vec3 c1 = stringToVec3(claim.getCorner1(), 0);
            Vec3 c3 = stringToVec3(claim.getCorner3(), 360);

            //plugin.getLogger().debug("c1 pos {} c3 pos {} alle claims {} und die gefilterten {}", c1.toString(), c3.toString(), claims.values().stream().map(Claim::getName).toList(), claimsinsamedim.stream().map(Claim::getName).toList());

            if (c1 != null && c3 != null) {
                AABB claimBox = new AABB(c1, c3);

                if (claimBox.contains(player.position())) {
                    return claim;
                }
            }
        }
        return null;
    }

    public @Nullable Claim getClaimbyPlayerPos(Player player) {
        ServerPlayer sp = getServerPlayer(player.getUUID());
        return getClaimbyPlayerPos(sp);
    }

    public int getClosestCornerIndex(Claim claim, BlockPos clickedPos) {
        BlockPos c1 = parseString(claim.getCorner1());
        BlockPos c2 = parseString(claim.getCorner2());
        BlockPos c3 = parseString(claim.getCorner3());
        BlockPos c4 = parseString(claim.getCorner4());

        double d1 = clickedPos.distSqr(c1);
        double d2 = clickedPos.distSqr(c2);
        double d3 = clickedPos.distSqr(c3);
        double d4 = clickedPos.distSqr(c4);

        double min = Math.min(Math.min(d1, d2), Math.min(d3, d4));

        if (min == d1) return 1;
        if (min == d2) return 2;
        if (min == d3) return 3;
        return 4;
    }

    public BlockPos getBlockPosbyCornerindex(Claim claim, int cornerindex) {
        if (cornerindex > 4 || cornerindex < 1) {
            return null;
        }
        List<BlockPos> blockposses = new ArrayList<>();
        blockposses.add(parseString(claim.getCorner1()));
        blockposses.add(parseString(claim.getCorner2()));
        blockposses.add(parseString(claim.getCorner3()));
        blockposses.add(parseString(claim.getCorner4()));

        return blockposses.get(cornerindex - 1);
    }

    public void moveCorner(Claim claim, int cornerIndex, BlockPos newPos) {
        BlockPos c1 = parseString(claim.getCorner1());
        BlockPos c3 = parseString(claim.getCorner3());

        if (cornerIndex == 1) c1 = newPos;
        else if (cornerIndex == 3) c3 = newPos;
        else if (cornerIndex == 2) {
            c1 = new BlockPos(c1.getX(), c1.getY(), newPos.getZ());
            c3 = new BlockPos(newPos.getX(), c3.getY(), c3.getZ());
        }
        else if (cornerIndex == 4) {
            c1 = new BlockPos(newPos.getX(), c1.getY(), c1.getZ());
            c3 = new BlockPos(c3.getX(), c3.getY(), newPos.getZ());
        }

        AABB claimBox2 = new AABB(c1.getBottomCenter(), c3.getBottomCenter());

        ServerPlayer pl = getServerPlayer(claim.getownerUUID());

        String maxblocks = plugin.getSettingsManager().getString(MAX_CLAIMED_BLOCKS);
        String enmax = maxblocks;
        int breiteX = Math.abs(c1.getX() - c3.getX()) + 1;
        int tiefeZ = Math.abs(c1.getZ() - c3.getZ()) + 1;
        int flaeche = breiteX * tiefeZ;
        if (maxblocks.contains("%")) {
            enmax = plugin.getSettingsManager().parseConditionalMessage(pl, plugin.getSettingsManager().getString(MAX_CLAIMED_BLOCKS));
        }

        if (flaeche + getAllClaimsize(pl) > Integer. parseInt(enmax)) {
            pl.sendSystemMessage(Component.literal("§cDu hast die maximale anzahl an geclaimten blöcken erreicht"));
            return;
        }

        int minX = Math.min(c1.getX(), c3.getX());
        int maxX = Math.max(c1.getX(), c3.getX());
        int minZ = Math.min(c1.getZ(), c3.getZ());
        int maxZ = Math.max(c1.getZ(), c3.getZ());

        claim.setCorner1(new Vec3(minX, c1.getY(), minZ).toString());
        claim.setCorner2(new Vec3(maxX, c1.getY(), minZ).toString());
        claim.setCorner3(new Vec3(maxX, c1.getY(), maxZ).toString());
        claim.setCorner4(new Vec3(minX, c1.getY(), maxZ).toString());

        claim.setCenter(new Vec3(minX, c1.getY(), minZ).add(new Vec3(maxX, c1.getY(), maxZ)).scale(0.5).toString());
        for (ServerPlayer player : plugin.getServer().getPlayerList().getPlayers()) {
            cleardisplay(claim, player);
            displayClaim(claim, player);
        }
    }

    @Nullable
    public Claim getClaimbyPos(Vec3 pos, String dim) {
        List<Claim> claimsinsamedim = getClaims().stream().filter(claim -> Objects.equals(claim.getDimension(), dim)).toList();
        for (Claim claim : claimsinsamedim) {
            Vec3 c1 = stringToVec3(claim.getCorner1(), 0);
            Vec3 c3 = stringToVec3(claim.getCorner3(), 360);

            if (c1 != null && c3 != null) {
                AABB claimBox = new AABB(c1, c3).inflate(1.0);

                if (claimBox.contains(pos)) {
                    return claim;
                }
            }
        }
        return null;
    }

    @Nullable
    public Claim getClaimbyPos(Vec3 pos, String dim, double inflate) {
        List<Claim> claimsinsamedim = getClaims().stream().filter(claim -> Objects.equals(claim.getDimension(), dim)).toList();
        for (Claim claim : claimsinsamedim) {
            Vec3 c1 = stringToVec3(claim.getCorner1(), 0);
            Vec3 c3 = stringToVec3(claim.getCorner3(), 360);

            if (c1 != null && c3 != null) {
                AABB claimBox = new AABB(c1, c3).inflate(inflate);

                if (claimBox.contains(pos)) {
                    return claim;
                }
            }
        }
        return null;
    }


    /**
     * Remove a clan from memory
     */
    public void removeClaim(String name) {
        for (ServerPlayer player : plugin.getServer().getPlayerList().getPlayers()) {
            cleardisplay(claims.get(name), player);
        }
        claims.remove(name);
    }

    /**
     * Whether the tag belongs to a clan
     */
    public boolean isClan(String name) {
        return claims.containsKey(name);
    }

    /**
     * Returns the clan the tag belongs to
     */
    public Claim getClaim(String name) {
        return claims.get(name);
    }

    /**
     * Get a player's clan
     *
     * @return null if not in a clan
     */
    @Nullable
    public List<Claim> getClaimsByOwnerUniqueId(UUID playerUniqueId) {
        List<Claim> claims1 = new ArrayList<>();
        for (Claim claim : getClaims()) {
            if (claim.getownerUUID() == playerUniqueId) {
                claims1.add(claim);
            }
        }

        return claims1;
    }

    /**
     * @return the clans
     */
    public List<Claim> getClaims() {
        return new ArrayList<>(claims.values());
    }

    /**
     * Announce message to the server
     *
     * @param msg the message
     */
    public void serverAnnounce(String msg) {
        if (SimpleClans.getInstance().getSettingsManager().is(DISABLE_MESSAGES)) {
            return;
        }

        //plugin.getProxyManager().sendMessage("ALL", ChatFormatting.DARK_GRAY + "* " + AQUA + msg);
    }

    @SuppressWarnings("deprecation")
    public void ban(String playerName, String claimname) {
        ban(SimpleClans.getInstance().getServer().getProfileCache().get(playerName).get().getId(), claimname);
    }

    /**
     * Bans a player from clan commands
     *
     * @param uuid the player's uuid
     */
    public void ban(UUID uuid, String claimname) {
        Claim claim = null;

        claims.get(claimname);

        if (claim != null) {
            List<UUID> baned = claim.getBanPlayers();
            if (!baned.contains(uuid)) {
                baned.add(uuid);
                claim.setBanPlayers(baned);
                plugin.getStorageManager().updateClaim(claim);
            }
        }
    }

    /**
     * Sort clans by founded date
     */
    public void sortClaimsByFounded(List<Claim> claims, boolean asc) {
        claims.sort((c1, c2) -> {
            int o = 1;
            if (!asc) {
                o = -1;
            }

            return Long.compare(c1.getCreateDate(), c2.getCreateDate()) * o;
        });
    }

    /**
     * Sort clans by name
     */
    public void sortClaimsByName(List<Claim> claims, boolean asc) {
        claims.sort((c1, c2) -> {
            int o = 1;
            if (!asc) {
                o = -1;
            }

            return c1.getName().compareTo(c2.getName()) * o;
        });
    }
}
