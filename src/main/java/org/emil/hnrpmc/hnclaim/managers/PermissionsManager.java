package org.emil.hnrpmc.hnclaim.managers;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.hnclaim.Claim;
import org.emil.hnrpmc.hnclaim.HNClaims;
import org.emil.hnrpmc.hnclaim.claimperms;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public final class PermissionsManager {

    private final HNClaims plugin;
    private final HashMap<String, List<claimperms>> permissions = new HashMap<>();
    private LuckPerms luckPerms;

    // In Forge gibt es keine PermissionAttachments.
    // Wir verlassen uns auf die LuckPerms API oder das native Command-Permission-System.

    public PermissionsManager() {
        plugin = HNClaims.getInstance();
    }

    public String format(double value) {
        return Objects.requireNonNull("00");
    }

    /**
     * Whether economy plugin exists and is enabled
     */
    public boolean hasEconomy() {
        return false;//economy != null && economy.isEnabled();
    }

    /**
     * Da Forge keine 'PermissionAttachments' hat, müssen Gruppen-Zuweisungen
     * direkt über das LuckPerms API für Forge erfolgen.
     */
    public void addPlayerStringPermissions(UUID uuid, String ClaimName, List<String> perms) {
        ServerPlayer player = SimpleClans.getInstance().getServer().getPlayerList().getPlayer(uuid);
        if (player == null) return;

        Map<String, List<claimperms>> playerperms = plugin.getClaimManager().getClaim(ClaimName).getoverridePerms();
        List<claimperms> perrms = playerperms.get(ClaimName);
        for (String perm : perms) {
            perrms.add(claimperms.valueOf(perm));
        }


        playerperms.put(uuid.toString(), perrms);
    }

    public void addPlayerPermissions(UUID uuid, String ClaimName, List<claimperms> perms) {
        ServerPlayer player = SimpleClans.getInstance().getServer().getPlayerList().getPlayer(uuid);
        if (player == null) return;

        Map<String, List<claimperms>> playerperms = plugin.getClaimManager().getClaim(ClaimName).getoverridePerms();
        List<claimperms> perrms = new ArrayList<>();
        if (playerperms != null && !playerperms.get(ClaimName).isEmpty()) {
            perrms = playerperms.get(ClaimName);
        }

        perrms.addAll(perms);


        playerperms.put(uuid.toString(), perrms);
    }

    public void removeClaimPlayerStringPermissions(UUID uuid, String ClaimName, List<String> perms) {
        ServerPlayer player = SimpleClans.getInstance().getServer().getPlayerList().getPlayer(uuid);
        if (player == null) return;

        Map<String, List<claimperms>> playerperms = plugin.getClaimManager().getClaim(ClaimName).getoverridePerms();
        List<claimperms> perrms = playerperms.get(ClaimName);
        for (String perm : perms) {
            perrms.remove(claimperms.valueOf(perm));
        }


        playerperms.put(uuid.toString(), perrms);
    }

    public void removeClaimPlayerPermissions(UUID uuid, String ClaimName, List<claimperms> perms) {
        ServerPlayer player = SimpleClans.getInstance().getServer().getPlayerList().getPlayer(uuid);
        if (player == null) return;

        Map<String, List<claimperms>> playerperms = plugin.getClaimManager().getClaim(ClaimName).getoverridePerms();
        List<claimperms> perrms = playerperms.get(ClaimName);
        perrms.removeAll(perms);


        playerperms.put(uuid.toString(), perrms);
    }

    public boolean has(@Nullable ServerPlayer player, claimperms permission, Optional<Boolean> check, @Nullable Entity entity) {
        if (player == null && entity == null) return false;
        if (SimpleClans.getInstance().getPermissionsManager().has(player, "hnclaims.bypass")) return true;
        if (permission == null) return false;

        boolean hasRankPermission = false;
        boolean ignoreowner = check.orElse(false);

        @Nullable Claim claim;
        if (player != null) {
            claim = plugin.getClaimManager().getClaimbyPlayerPos(player);
            if (plugin.getClaimManager().getClaimbyPlayerPos(player) == null && entity != null) {
                claim = plugin.getClaimManager().getClaimbyPos(entity.getPosition(0), entity.level().dimension().location().toString());
            }
        } else {
            claim = plugin.getClaimManager().getClaimbyPos(entity.getPosition(0), entity.level().dimension().location().toString());
        }
        if (claim != null) {
            if (!claim.getPerms().contains(permission)) {
                if (player != null) {
                    if (!ignoreowner && player.getUUID().equals(claim.getownerUUID())) return true;
                    List<claimperms> playeroverride = claim.getPlayerPerms(player.getUUID());
                    Clan playerscaln = SimpleClans.getInstance().getClanManager().getClanByPlayerUniqueId(player.getUUID());
                    List<claimperms> clanoverride = playerscaln != null ? claim.getClaimPerms(playerscaln.getStringName()) : null;
                    if (playeroverride != null || clanoverride != null) {
                        if (playeroverride == null) {
                            hasRankPermission = clanoverride.contains(permission);
                        }

                        if (clanoverride == null) {
                            hasRankPermission = playeroverride.contains(permission);
                        }
                        //hasRankPermission = playeroverride.contains(permission) || clanoverride.contains(permission);
                    }
                } else if (entity != null && entity instanceof ServerPlayer sp) {
                    if (!ignoreowner && sp.getUUID().equals(claim.getownerUUID())) return true;
                    List<claimperms> playeroverride = claim.getPlayerPerms(sp.getUUID());
                    Clan playerscaln = SimpleClans.getInstance().getClanManager().getClanByPlayerUniqueId(sp.getUUID());
                    List<claimperms> clanoverride = playerscaln != null ? claim.getClaimPerms(playerscaln.getStringName()) : null;
                    if (playeroverride != null || clanoverride != null) {
                        if (playeroverride == null) {
                            hasRankPermission = clanoverride.contains(permission);
                        }

                        if (clanoverride == null) {
                            hasRankPermission = playeroverride.contains(permission);
                        }
                        //hasRankPermission = playeroverride.contains(permission) || clanoverride.contains(permission);
                    }
                }
            } else if (claim.getPerms().contains(permission)) {
                hasRankPermission = true;
            }
        } else if (claim == null) {
            return true;
        }

        return hasRankPermission;
    }

    public boolean has(@Nullable Player player, claimperms permission, Optional<Boolean> check, @Nullable Entity entity) {

        ServerPlayer sp = player != null ? plugin.getServer().getPlayerList().getPlayer(player.getUUID()) : null;
        return has(sp, permission, check, entity);
    }

    public boolean has(@Nullable Player player, claimperms permission, @Nullable Entity entity) {
        return has(player, permission, Optional.of(false), entity);
    }

    public boolean has(@Nullable ServerPlayer player, claimperms permission, @Nullable Entity entity) {
        return has(player, permission, Optional.of(false), entity);
    }

    public boolean has(@Nullable Player player, claimperms permission) {
        return has(player, permission, Optional.of(false), null);
    }

    public boolean has(@Nullable ServerPlayer player, claimperms permission) {
        return has(player, permission, Optional.of(false), null);
    }

    public boolean has(@Nullable Player player, claimperms permission, Optional<Boolean> check) {
        return has(player, permission, check, null);
    }

    public boolean has(@Nullable ServerPlayer player, claimperms permission, Optional<Boolean> check) {
        return has(player, permission, check, null);
    }

    /**
     * Lädt die Berechtigungen für jeden Clan aus der Config.
     */
    public void loadPermissions() {
        permissions.clear();
        for (Claim claim : plugin.getClaimManager().getClaims()) {
            // Wir greifen auf den SettingsManager zu, um die StringList zu erhalten
            List<claimperms> claimPerms = claim.getPerms();
            if (claimPerms != null) {
                permissions.put(claim.getName(), claimPerms);
            }
        }
    }


    /**
     * Speichert die Berechtigungen für jeden Clan in die Config.
     */
    public void savePermissions() {
        for (Claim claim : plugin.getClaimManager().getClaims()) {
            if (permissions.containsKey(claim.getName())) {
                plugin.getStorageManager().updateClaim(claim);
            }
        }
    }

    /**
     * Gibt die Berechtigungen eines Clans zurück.
     */
    public List<claimperms> getPermissions(Claim clan) {
        return permissions.getOrDefault(clan.getName(), new ArrayList<>());
    }

    // Präfixe und Suffixe in NeoForge/LuckPerms
    public String getPrefix(ServerPlayer p) {
        // In NeoForge nutzen wir Komponenten statt Strings mit §
        return "";
    }

    public String getSuffix(ServerPlayer p) {
        // In NeoForge nutzen wir Komponenten statt Strings mit §
        return "";
    }
}