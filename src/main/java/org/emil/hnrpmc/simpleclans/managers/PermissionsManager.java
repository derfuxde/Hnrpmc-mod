package org.emil.hnrpmc.simpleclans.managers;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.simpleclans.*;
import org.emil.hnrpmc.simpleclans.events.EconomyTransactionEvent;
import org.emil.hnrpmc.simpleclans.events.EconomyTransactionEvent.Cause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public final class PermissionsManager {

    private final SimpleClans plugin;
    private final HashMap<String, List<String>> permissions = new HashMap<>();
    private LuckPerms luckPerms;

    // In Forge gibt es keine PermissionAttachments.
    // Wir verlassen uns auf die LuckPerms API oder das native Command-Permission-System.

    public PermissionsManager() {
        plugin = SimpleClans.getInstance();
    }

    /**
     * Prüft, ob ein Spieler eine Berechtigung hat.
     * Nutzt in Forge primär das LuckPerms API oder die native OP-Stufe.
     */
    public static boolean has(Player player, String permission) {
        LuckPerms lp = Hnrpmc.getLuckPerms();

        if (player == null) {
            return false;
        }

        if (lp == null) {
            return player.hasPermissions(4);
        }

        User user = lp.getUserManager().getUser(player.getUUID());
        if (user == null) return false;//player.hasPermissions(4);

        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
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
    public void addClanPermissions(ClanPlayer cp) {
        if (!plugin.getSettingsManager().is(ENABLE_AUTO_GROUPS) || cp == null) {
            return;
        }
        ServerPlayer player = (ServerPlayer) cp.toPlayer();
        if (player == null) return;

        // Hier müsstest du LuckPerms Befehle ausführen oder das API nutzen
        // player.getServer().getCommands().performPrefixedCommand(...)
    }

    public boolean playerHasMoney(UUID uuid, double money) {
        // Da es kein Vault gibt, musst du hier ein Economy-Mod API aufrufen
        // Beispiel: return EconomyMod.getBalance(uuid) >= money;
        return true;
    }

    public void addPlayerPermissions(@Nullable ClanPlayer cp) {
        if (cp == null) return;

        Clan clan = cp.getClan();
        if (clan == null) return;

        ServerPlayer player = (ServerPlayer) cp.toPlayer();
        if (player == null) return;

        List<String> clanPerms = getPermissions(clan);
        if (clanPerms.isEmpty() && !plugin.getSettingsManager().is(PERMISSIONS_AUTO_GROUP_GROUPNAME)) {
            return;
        }

        LuckPerms lp;
        try {
            lp = LuckPermsProvider.get();
        } catch (Throwable t) {
            // Kein LuckPerms installiert -> Fallback: nichts setzen (deine has()-Checks müssen dann intern laufen)
            return;
        }

        User user = lp.getUserManager().getUser(player.getUUID());
        if (user == null) {
            // User evtl. noch nicht geladen -> später erneut versuchen
            // z.B. plugin.runTaskLater(() -> addPlayerPermissions(cp), 20);
            return;
        }

        // 1) Node(s) für die Clan-Permissions
        for (String perm : clanPerms) {
            if (perm == null || perm.isBlank()) continue;
            user.data().add(Node.builder(perm.trim()).value(true).build());
        }

        // 2) Optional: "group.<tag>" analog
        if (plugin.getSettingsManager().is(PERMISSIONS_AUTO_GROUP_GROUPNAME)) {
            // A) Wenn du wirklich "group.<tag>" als Permission Node willst:
            user.data().add(Node.builder("group." + clan.getTag()).value(true).build());

            // B) Besser: echte LuckPerms-Gruppe (InheritanceNode) – nur wenn Gruppe existiert!
            // user.data().add(InheritanceNode.builder(clan.getTag()).build());
        }

        lp.getUserManager().saveUser(user);
    }

    /**
     * Removes permissions for a clan (when it gets disbanded for example)
     */
    public void removeClanPermissions(Clan clan) {
        for (ClanPlayer cp : clan.getMembers()) {
            removeClanPlayerPermissions(cp);
            //removeClanPermissions(cp.getClan());
        }
    }

    public void removeClanPlayerPermissions(@Nullable ClanPlayer cp) {
        if (cp == null) return;

        Clan clan = cp.getClan();
        if (clan == null) return;

        ServerPlayer player = cp.toPlayer();
        if (player == null) return;


        LuckPerms lp;
        try {
            lp = LuckPermsProvider.get();
        } catch (Throwable t) {
            return;
        }

        User user = lp.getUserManager().getUser(player.getUUID());
        if (user == null) return;

        // Entferne Clan-Permissions
        for (String perm : getPermissions(clan)) {
            if (perm == null || perm.isBlank()) continue;
            user.data().remove(Node.builder(perm.trim()).value(true).build());
        }

        // Entferne optional group.<tag>
        if (plugin.getSettingsManager().is(PERMISSIONS_AUTO_GROUP_GROUPNAME)) {
            user.data().remove(Node.builder("group." + clan.getTag()).value(true).build());
            // oder: user.data().remove(InheritanceNode.builder(clan.getTag()).build());
        }

        lp.getUserManager().saveUser(user);
    }



    public boolean chargePlayer(@NotNull UUID uuid, double money, @Nullable Cause cause) {
        // Logik für Abzug von Geld
        if (!playerHasMoney(uuid, money)) return false;

        // Event feuern
        EconomyTransactionEvent event = new EconomyTransactionEvent(uuid, money, cause, EconomyTransactionEvent.TransactionType.WITHDRAW);
        NeoForge.EVENT_BUS.post(event);

        if (event.isCanceled()) return false;

        // Hier tatsächlichen Abzug durchführen
        return true;
    }

    public boolean has(ServerPlayer player, RankPermission permission, boolean notify) {
        if (player == null || permission == null) return false;

        ClanPlayer clanPlayer = plugin.getClanManager().getClanPlayer(player.getUUID());
        if (clanPlayer == null) {
            if (notify) player.sendSystemMessage(Component.literal(lang("not.a.member.of.any.clan", player)).withStyle(ChatFormatting.RED));
            return false;
        }

        // Checke native Permission (z.B. simpleclans.member.kick)
        if (!has(player, permission.getNeoPermission())) {
            if (notify) ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.RED + lang("insufficient.permissions", player));
            return false;
        }

        boolean hasLevel = hasLevel(clanPlayer, permission.getPermissionLevel());
        boolean hasRankPermission = false;

        Clan clan = clanPlayer.getClan();
        if (clan != null) {
            Rank rank = clan.getRank(clanPlayer.getRankId());
            if (rank != null) {
                hasRankPermission = rank.getPermissions().contains(permission.toString());
            }
        }

        if (notify && !hasLevel && !hasRankPermission) {
            String msg = MessageFormat.format(lang("you.must.be.0.or.have.the.permission.1.to.use.this", player),
                    permission.getPermissionLevel() == PermissionLevel.LEADER ? lang("leader", player) : lang("trusted", player), permission.toString());
            ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.RED + msg);
        }

        return hasLevel || hasRankPermission;
    }

    /**
     * Lädt die Berechtigungen für jeden Clan aus der Config.
     */
    public void loadPermissions() {
        permissions.clear();
        for (Clan clan : plugin.getClanManager().getClans()) {
            // Wir greifen auf den SettingsManager zu, um die StringList zu erhalten
            List<String> clanPerms = plugin.getSettingsManager().getConfig().getStringList("permissions." + clan.getTag());
            if (clanPerms != null) {
                permissions.put(clan.getTag(), clanPerms);
            }
        }
    }


    /**
     * Speichert die Berechtigungen für jeden Clan in die Config.
     */
    public void savePermissions() {
        for (Clan clan : plugin.getClanManager().getClans()) {
            if (permissions.containsKey(clan.getTag())) {
                // Wir setzen den Wert direkt in der Config-Instanz
                plugin.getSettingsManager().getConfig().set("permissions." + clan.getTag(), getPermissions(clan));
            }
        }
        // WICHTIG: Die Datei muss physisch geschrieben werden
        plugin.getSettingsManager().save();
    }

    /**
     * Gibt die Berechtigungen eines Clans zurück.
     */
    public List<String> getPermissions(Clan clan) {
        return permissions.getOrDefault(clan.getTag(), new ArrayList<>());
    }

    /**
     * Aktualisiert die Berechtigungen für alle Mitglieder eines Clans.
     */
    public void updateClanPermissions(Clan clan) {
        for (ClanPlayer cp : clan.getMembers()) {
            addPlayerPermissions(cp);
        }
    }


    private boolean hasLevel(@NotNull ClanPlayer cp, @Nullable PermissionLevel level) {
        if (level == null) return false;
        return switch (level) {
            case LEADER -> cp.isLeader();
            case TRUSTED -> cp.isTrusted();
            default -> false;
        };
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