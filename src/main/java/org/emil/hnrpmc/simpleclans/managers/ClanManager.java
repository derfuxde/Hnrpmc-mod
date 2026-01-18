package org.emil.hnrpmc.simpleclans.managers;


import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.Level;
import org.emil.hnrpmc.simpleclans.*;
import org.emil.hnrpmc.simpleclans.events.CreateClanEvent;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.emil.hnrpmc.simpleclans.uuid.UUIDMigration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.ChatFormatting.AQUA;
import static org.emil.hnrpmc.simpleclans.SimpleClans.getInstance;
import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;
import net.minecraft.world.item.Item;
import static java.util.logging.Level.WARNING;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author phaed
 */
public final class ClanManager {

    private final SimpleClans plugin;
    private final ConcurrentHashMap<String, Clan> clans = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ClanPlayer> clanPlayers = new ConcurrentHashMap<>();
    private final HashMap<ClanPlayer, List<Kill>> kills = new HashMap<>();

    /**
     *
     */
    public ClanManager() {
        plugin = SimpleClans.getInstance();
    }

    public Clan getClanByName(String name) {
        for (Map.Entry<String, Clan> clan : clans.entrySet()) {
            if (clan.getValue().getName() == name) {
                return clan.getValue();
            }
        }
        return null;
    }

    /**
     * Deletes all clans and clan players in memory
     */
    public void cleanData() {
        clans.clear();
        clanPlayers.clear();
        kills.clear();
    }

    private ServerPlayer getPlayerFromSource(CommandSourceStack source) {
        return source.getEntity() instanceof ServerPlayer p ? p : null;
    }

    public List<String> getAllClanTagsHideOwn(CommandSourceStack source) {
        ServerPlayer player = getPlayerFromSource(source);
        ClanPlayer cp = player != null ? getClanPlayer(player.getUUID()) : null;
        Clan myClan = cp != null ? cp.getClan() : null;

        return getClans().stream()
                .filter(clan -> myClan == null || !clan.getTag().equals(myClan.getTag()))
                .map(Clan::getTag)
                .collect(Collectors.toList());
    }

    public List<String> getRivalTags(CommandSourceStack source) {
        ServerPlayer player = getPlayerFromSource(source);
        ClanPlayer cp = player != null ? getClanPlayer(player.getUUID()) : null;
        if (cp == null || cp.getClan() == null) return Collections.emptyList();

        return cp.getClan().getRivals().stream()
                .collect(Collectors.toList());
    }

    public List<String> getAlliedTags(CommandSourceStack source) {
        ServerPlayer player = getPlayerFromSource(source);
        ClanPlayer cp = player != null ? getClanPlayer(player.getUUID()) : null;
        if (cp == null || cp.getClan() == null) return Collections.emptyList();

        return new ArrayList<>(cp.getClan().getAllies());
    }

    public List<String> getWarringTags(CommandSourceStack source) {
        ServerPlayer player = getPlayerFromSource(source);
        ClanPlayer cp = player != null ? getClanPlayer(player.getUUID()) : null;

        if (cp == null || cp.getClan() == null) {
            return Collections.emptyList();
        }

        return cp.getClan().getWarringClans().stream()
                .map(Clan::getTag) // Wandelt das Clan-Objekt in den Tag-String um
                .collect(Collectors.toList());
    }

    public List<String> getOnlineNonMembers(CommandSourceStack source) {
        return source.getServer().getPlayerList().getPlayers().stream()
                .filter(player -> getClanPlayer(player.getUUID()) == null)
                .map(ServerPlayer::getScoreboardName)
                .collect(Collectors.toList());
    }

    public List<String> getPlayerwithRank(CommandSourceStack source, String rank, boolean same) {
        plugin.getClanManager().getAllClanPlayers().stream().filter(cp -> (cp.getRank() == rank) == same).map(ClanPlayer::getName);
        return source.getServer().getPlayerList().getPlayers().stream()
                .filter(player -> getClanPlayer(player.getUUID()) == null)
                .map(ServerPlayer::getScoreboardName)
                .collect(Collectors.toList());
    }

    public List<String> getOnlineClanMembersHideOwn(CommandSourceStack source) {
        ServerPlayer sender = getPlayerFromSource(source);
        ClanPlayer senderCp = sender != null ? getClanPlayer(sender.getUUID()) : null;
        Clan myClan = senderCp != null ? senderCp.getClan() : null;

        // Lokaler Record als Container für den Stream
        record PlayerData(ServerPlayer player, ClanPlayer cp) {}

        return source.getServer().getPlayerList().getPlayers().stream()
                .map(p -> new PlayerData(p, getClanPlayer(p.getUUID())))
                .filter(data -> data.cp() != null) // Ist in einem Clan
                .filter(data -> data.cp().getClan() != null) // Clan-Sicherheitscheck
                .filter(data -> myClan == null || !data.cp().getClan().getTag().equals(myClan.getTag())) // Nicht der eigene Clan
                .map(data -> data.player().getScoreboardName())
                .collect(Collectors.toList());
    }

    public ServerPlayer getServerPlayer(UUID uuid) {
        return SimpleClans.getInstance().getServer().getPlayerList().getPlayer(uuid);
    }

    /**
     * Adds a kill to the memory
     */
    public void addKill(Kill kill) {
        if (kill == null) {
            return;
        }

        List<Kill> list = kills.computeIfAbsent(kill.getKiller(), k -> new ArrayList<>());

        Iterator<Kill> iterator = list.iterator();
        while (iterator.hasNext()) {
            Kill oldKill = iterator.next();
            if (oldKill.getVictim().equals(kill.getKiller())) {
                iterator.remove();
                continue;
            }

            // cleaning
            final int delay = plugin.getSettingsManager().getInt(KDR_DELAY_BETWEEN_KILLS);
            long timePassed = oldKill.getTime().until(LocalDateTime.now(), ChronoUnit.MINUTES);
            if (timePassed >= delay) {
                iterator.remove();
            }
        }

        list.add(kill);
    }

    /**
     * Checks if this kill respects the delay
     */
    public boolean isKillBeforeDelay(Kill kill) {
        if (kill == null) {
            return false;
        }
        List<Kill> list = kills.get(kill.getKiller());
        if (list == null) {
            return false;
        }

        for (Kill oldKill : list) {
            if (oldKill.getVictim().equals(kill.getVictim())) {

                final int delay = plugin.getSettingsManager().getInt(KDR_DELAY_BETWEEN_KILLS);
                long timePassed = oldKill.getTime().until(kill.getTime(), ChronoUnit.MINUTES);
                if (timePassed < delay) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Import a clan into the in-memory store
     */
    public void importClan(Clan clan) {
        this.clans.put(clan.getTag(), clan);
    }

    /**
     * Import a clan player into the in-memory store
     */
    public void importClanPlayer(ClanPlayer cp) {
        if (cp.getUniqueId() != null) {
            this.clanPlayers.put(cp.getUniqueId(), cp);
        }
    }

    /**
     * Create a new clan
     */
    public void createClan(ServerPlayer player, String colorTag, String name) {
        // 1. ClanPlayer holen oder erstellen (UUID bleibt gleich)
        ClanPlayer cp = getCreateClanPlayer(player.getUUID());

        // 2. Verification-Logik (PermissionsManager muss auf NeoForge angepasst sein)
        boolean verified = !plugin.getSettingsManager().is(REQUIRE_VERIFICATION)
                || plugin.getPermissionsManager().has(player, "simpleclans.mod.verify");

        // 3. Clan-Objekt initialisieren
        Clan clan = new Clan(colorTag, name, verified);
        clan.addPlayerToClan(cp);
        cp.setLeader(true);
        clan.getRanks().addAll(plugin.getSettingsManager().getStarterRanks());

        // Starter Ranks aus Config laden
        //clan.getRanks().addAll(plugin.getSettingsManager().getStarterRanks());

        // 4. Datenbank-Operationen (Bleiben meist gleich, da SQL unabhängig ist)
        plugin.getStorageManager().insertClan(clan);
        importClan(clan); // Registriert den Clan im Manager-Cache
        plugin.getStorageManager().updateClanPlayer(cp);

        // 5. Einladungen ablehnen
        plugin.getRequestManager().deny(cp);

        // 6. Permissions aktualisieren (LuckPerms o.ä.)
        SimpleClans.getInstance().getPermissionsManager().updateClanPermissions(clan);

        // 7. Event-Handling (Der NeoForge Weg)
        // Anstatt PluginManager nutzen wir den NeoForge.EVENT_BUS
        NeoForge.EVENT_BUS.post(new CreateClanEvent(clan));
    }

    /**
     * Reset a player's KDR
     */
    public void resetKdr(ClanPlayer cp) {
        cp.setCivilianKills(0);
        cp.setNeutralKills(0);
        cp.setRivalKills(0);
        cp.setAllyKills(0);
        cp.setDeaths(0);
        plugin.getStorageManager().updateClanPlayer(cp);
    }

    /**
     * Delete a players data file
     */
    public void deleteClanPlayer(ClanPlayer cp) {
        Clan clan = cp.getClan();
        if (clan != null) {
            clan.removePlayerFromClan(cp.getUniqueId());
        }
        clanPlayers.remove(cp.getUniqueId());
        plugin.getStorageManager().deleteClanPlayer(cp);
    }

    /**
     * Delete a player data from memory
     */
    public void deleteClanPlayerFromMemory(UUID playerUniqueId) {
        clanPlayers.remove(playerUniqueId);
    }

    /**
     * Remove a clan from memory
     */
    public void removeClan(String tag) {
        clans.remove(tag);
    }

    /**
     * Whether the tag belongs to a clan
     */
    public boolean isClan(String tag) {
        return clans.containsKey(Helper.cleanTag(tag));
    }

    /**
     * Returns the clan the tag belongs to
     */
    public Clan getClan(String tag) {
        return clans.get(Helper.cleanTag(tag));
    }

    @SuppressWarnings("deprecation")
    @Nullable
    public Clan getClanByPlayerName(String playerName) {
        return getClanByPlayerUniqueId(SimpleClans.getInstance().getServer().getProfileCache().get(playerName).get().getId());
    }

    /**
     * Get a player's clan
     *
     * @return null if not in a clan
     */
    @Nullable
    public Clan getClanByPlayerUniqueId(UUID playerUniqueId) {
        ClanPlayer cp = getCreateClanPlayer(playerUniqueId);

        if (cp != null) {
            return cp.getClan();
        }

        return null;
    }

    /**
     * @return the clans
     */
    public List<Clan> getClans() {
        return new ArrayList<>(clans.values());
    }

    /**
     * Returns the collection of all clan players, including the disabled ones
     */
    public List<ClanPlayer> getAllClanPlayers() {
        return new ArrayList<>(clanPlayers.values());
    }

    /**
     * Gets the ClanPlayer data object if a player is currently in a clan, null
     * if he's not in a clan Used for BungeeCord Reload ClanPlayer and your Clan
     */
    @Deprecated
    public @Nullable ClanPlayer getClanPlayerJoinEvent(ServerPlayer player) {
        SimpleClans.getInstance().getStorageManager().importFromDatabaseOnePlayer(player);
        return getClanPlayer(player.getUUID());
    }

    /**
     * Gets the ClanPlayer data object if a player is currently in a clan, null
     * if he's not in a clan
     */
    public @Nullable ClanPlayer getClanPlayer(@NotNull ServerPlayer player) {
        return getClanPlayer(player.getUUID());
    }

    /**
     * Gets the ClanPlayer data object if a player is currently in a clan, null
     * if he's not in a clan
     */
    public @Nullable ClanPlayer getClanPlayer(@NotNull Player player) {
        return getClanPlayer((ServerPlayer) player);
    }

    /**
     * Gets the ClanPlayer data object if a player is currently in a clan, null
     * if he's not in a clan
     */
    public @Nullable ClanPlayer getClanPlayer(UUID playerUniqueId) {
        if (playerUniqueId == null) return null;

        // getCreateClanPlayer sorgt bereits dafür, dass ein Objekt da ist
        ClanPlayer cp = getCreateClanPlayer(playerUniqueId);

        // Hier ist der wichtige Check für dein Scoreboard:
        // Wenn das Scoreboard nach dem Clan fragt, bevor der Join-Prozess fertig ist,
        // geben wir lieber null zurück als einen Absturz zu riskieren.
        return cp;
    }

    @SuppressWarnings("deprecation")
    public @Nullable ClanPlayer getClanPlayer(String playerName) {
        if (playerName == null) return null;

        ClanPlayer cp = getCreateClanPlayer(playerName);

        return cp;
    }

    /**
     * Gets the ClanPlayer data object if a player is currently in a clan, null
     * if he's not in a clan
     */
    @Deprecated
    public @Nullable ClanPlayer getClanPlayerName(String playerName) {
        UUID uuid = UUIDMigration.getForcedPlayerUUID(playerName);

        if (uuid == null) {
            return null;
        }

        ClanPlayer cp = getCreateClanPlayer(uuid);

        if (cp == null) {
            return null;
        }

        if (cp.getClan() == null) {
            return null;
        }

        return cp;
    }

    /**
     * Gets the ClanPlayer data object for the player, will retrieve disabled
     * clan players as well, these are players who used to be in a clan but are
     * not currently in one, their data file persists and can be accessed. their
     * clan will be null though.
     */

    @Nullable
    public ClanPlayer getAnyClanPlayer(UUID uuid) {
        return clanPlayers.get(uuid);
    }

    @SuppressWarnings("deprecation")
    @Nullable
    public ClanPlayer getAnyClanPlayer(String playerName) {
        for (ClanPlayer cp : getAllClanPlayers()) {
            if (cp.getName().equalsIgnoreCase(playerName)) {
                return cp;
            }
        }
        return null;
    }

    /**
     * Gets the ClanPlayer object for the player, creates one if not found
     */
    @Deprecated
    public @Nullable ClanPlayer getCreateClanPlayerUUID(String playerName) {
        UUID playerUniqueId = UUIDMigration.getForcedPlayerUUID(playerName);
        if (playerUniqueId != null) {
            return getCreateClanPlayer(playerUniqueId);
        } else {
            return null;
        }
    }

    /**
     * Gets the ClanPlayer object for the player, creates one if not found
     */
    public ClanPlayer getCreateClanPlayer(UUID uuid) {
        Objects.requireNonNull(uuid, "UUID must not be null");
        if (clanPlayers.containsKey(uuid)) {
            return clanPlayers.get(uuid);
        }

        ClanPlayer cp = new ClanPlayer(uuid);


        boolean save = true;
        for (ClanPlayer other : getAllClanPlayers()) {
            if (other.getName().equals(cp.getName())) {
                save = false;
                break;
            }
        }
        if (save) {
            plugin.getStorageManager().insertClanPlayer(cp);
            importClanPlayer(cp);
        } else if (plugin.getSettingsManager().is(DEBUG)) {
            plugin.getLogger().info(String.format("There already is a ClanPlayer with the name %s",
                    cp.getName()), new Exception());
        }

        return cp;
    }

    @SuppressWarnings("deprecation")
    public ClanPlayer getCreateClanPlayer(String playerName) {
        var profileOptional = plugin.getServer().getProfileCache().get(playerName);

        if (profileOptional.isPresent()) {
            return getCreateClanPlayer(profileOptional.get().getId());
        } else {
            // Fallback: Wenn der Spieler nicht existiert, können wir keinen ClanPlayer erstellen
            SimpleClans.debug("Profil-Cache Suche fehlgeschlagen für: " + playerName);
            return null; // Du musst dann @NotNull in @Nullable ändern!
        }
    }

    /**
     * Announce message to the server
     *
     * @param msg the message
     */
    public void serverAnnounce(String msg) {
        if (plugin.getSettingsManager().is(DISABLE_MESSAGES)) {
            return;
        }

        plugin.getProxyManager().sendMessage("ALL", ChatFormatting.DARK_GRAY + "* " + AQUA + msg);
    }

    /**
     * Update the players display name with his clan's tag
     */
    public void updateDisplayName(@Nullable Player player) {
        // do not update displayname if in compat mode

        if (plugin.getSettingsManager().is(CHAT_COMPATIBILITY_MODE)) {
            return;
        }

        if (player == null) {
            return;
        }

        if (plugin.getSettingsManager().is(DISPLAY_CHAT_TAGS)) {
            String prefix = plugin.getPermissionsManager().getPrefix((ServerPlayer) player);
            // String suffix = plugin.getPermissionsManager().getSuffix(player);
            String lastColor = plugin.getSettingsManager().is(COLOR_CODE_FROM_PREFIX_FOR_NAME)
                    ? ChatUtils.getLastColorCode(prefix)
                    : ChatFormatting.WHITE + "";
            String fullName = player.getName().getString();

            ClanPlayer cp = plugin.getClanManager().getAnyClanPlayer(player.getUUID());

            if (cp == null) {
                return;
            }



            if (cp.isTagEnabled()) {
                Clan clan = cp.getClan();

                if (clan != null) {
                    fullName = clan.getTagLabel(cp.isLeader()) + lastColor + fullName + ChatFormatting.WHITE;
                }

                player.setCustomName(Component.literal(fullName));
            } else {
                player.setCustomName(Component.literal(lastColor + fullName + ChatFormatting.WHITE));
            }
        }
    }

    /**
     * Process a player and his clan's last seen date
     */
    public void updateLastSeen(Player player) {
        ClanPlayer cp = getAnyClanPlayer(player.getUUID());

        if (cp != null) {
            cp.updateLastSeen();
            plugin.getStorageManager().updateClanPlayer(cp);

            Clan clan = cp.getClan();

            if (clan != null) {
                clan.updateLastUsed();
                plugin.getStorageManager().updateClan(clan);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void ban(String playerName) {
        ban(SimpleClans.getInstance().getServer().getProfileCache().get(playerName).get().getId());
    }

    /**
     * Bans a player from clan commands
     *
     * @param uuid the player's uuid
     */
    public void ban(UUID uuid) {
        ClanPlayer cp = getClanPlayer(uuid);
        Clan clan = null;
        if (cp != null) {
            clan = cp.getClan();
        }

        if (clan != null) {
            if (clan.getSize() == 1) {
                clan.disband(null, false, false);
            } else {
                cp.setClan(null);
                cp.addPastClan(clan.getColorTag() + (cp.isLeader() ? ChatFormatting.DARK_RED + "*" : ""));
                cp.setLeader(false);
                cp.setJoinDate(0);
                clan.removeMember(uuid);

                plugin.getStorageManager().updateClanPlayer(cp);
                plugin.getStorageManager().updateClan(clan);
            }
        }

        plugin.getSettingsManager().addBanned(uuid);
    }

    /**
     * Get a count of rivable clans
     */
    public int getRivableClanCount() {
        int clanCount = 0;

        for (Clan tm : clans.values()) {
            if (!SimpleClans.getInstance().getSettingsManager().isUnrivable(tm.getTag())) {
                clanCount++;
            }
        }

        return clanCount;
    }

    /**
     * Returns a formatted string detailing the players armor
     */
    public String getArmorString(Inventory inv) {
        String out = "";

        ItemStack h = inv.armor.get(3);
        Player player = inv.player;

        if (!h.isEmpty()) {
            if (h.is(Items.CHAINMAIL_HELMET)) {
                out += ChatFormatting.WHITE + SimpleClans.lang("armor.h", player.createCommandSourceStack());
            } else if (h.is(Items.DIAMOND_HELMET) || h.is(Items.NETHERITE_HELMET)) {
                out += AQUA + SimpleClans.lang("armor.h", player.createCommandSourceStack());
            } else if (h.is(Items.GOLDEN_HELMET)) {
                out += ChatFormatting.YELLOW + SimpleClans.lang("armor.h", player.createCommandSourceStack());
            } else if (h.is(Items.IRON_HELMET)) {
                out += ChatFormatting.GRAY + SimpleClans.lang("armor.h", player.createCommandSourceStack());
            } else if (h.is(Items.LEATHER_HELMET)) {
                out += ChatFormatting.GOLD + SimpleClans.lang("armor.h", player.createCommandSourceStack());
            } else {
                out += ChatFormatting.RED + SimpleClans.lang("armor.h", player.createCommandSourceStack());
            }
        } else {
            // Entspricht Items.AIR (leerer Slot)
            out += ChatFormatting.BLACK + SimpleClans.lang("armor.h", player.createCommandSourceStack());
        }

        ItemStack c = inv.armor.get(2); // Index 2 ist die Brustplatte

        if (c != null) {
            if (c.is(Items.CHAINMAIL_CHESTPLATE)) {
                out += ChatFormatting.WHITE + lang("armor.c", player.createCommandSourceStack());
            } else if (c.is(Items.DIAMOND_CHESTPLATE)) {
                out += AQUA + lang("armor.c", player.createCommandSourceStack());
            } else if (c.is(Items.GOLDEN_CHESTPLATE)) {
                out += ChatFormatting.YELLOW + lang("armor.c", player.createCommandSourceStack());
            } else if (c.is(Items.IRON_CHESTPLATE)) {
                out += ChatFormatting.GRAY + lang("armor.c", player.createCommandSourceStack());
            } else if (c.is(Items.LEATHER_CHESTPLATE)) {
                out += ChatFormatting.GOLD + lang("armor.c", player.createCommandSourceStack());
            } else if (c.is(Items.AIR)) {
                out += ChatFormatting.BLACK + lang("armor.c", player.createCommandSourceStack());
            } else {
                out += ChatFormatting.RED + lang("armor.c", player.createCommandSourceStack());
            }
        }
        ItemStack l = inv.armor.get(1);

        if (l != null) {
            if (l.is(Items.CHAINMAIL_LEGGINGS)) {
                out += ChatFormatting.WHITE + lang("armor.l", player.createCommandSourceStack());
            } else if (l.is(Items.DIAMOND_LEGGINGS)) {
                out += lang("armor.l", player.createCommandSourceStack());
            } else if (l.is(Items.GOLDEN_LEGGINGS)) {
                out += lang("armor.l", player.createCommandSourceStack());
            } else if (l.is(Items.IRON_LEGGINGS)) {
                out += lang("armor.l", player.createCommandSourceStack());
            } else if (l.is(Items.LEATHER_LEGGINGS)) {
                out += lang("armor.l", player.createCommandSourceStack());
            } else if (l.is(Items.AIR)) {
                out += lang("armor.l", player.createCommandSourceStack());
            } else {
                out += lang("armor.l", player.createCommandSourceStack());
            }
        }
        ItemStack b = inv.armor.get(0);

        if (b != null) {
            if (b.is(Items.CHAINMAIL_BOOTS)) {
                out += ChatFormatting.WHITE + lang("armor.B", player.createCommandSourceStack());
            } else if (b.is(Items.DIAMOND_BOOTS)) {
                out += lang("armor.B", player.createCommandSourceStack());
            } else if (b.is(Items.GOLDEN_BOOTS)) {
                out += lang("armor.B", player.createCommandSourceStack());
            } else if (b.is(Items.IRON_BOOTS)) {
                out += lang("armor.B", player.createCommandSourceStack());
            } else if (b.is(Items.LEATHER_BOOTS)) {
                out += lang("armor.B", player.createCommandSourceStack());
            } else if (b.is(Items.AIR)) {
                out += lang("armor.B", player.createCommandSourceStack());
            } else {
                out += lang("armor.B", player.createCommandSourceStack());
            }
        }

        if (out.length() == 0) {
            out = lang("none", player.createCommandSourceStack());
        }

        return out;
    }

    /**
     * Returns a formatted string detailing the players weapons
     */
    public String getWeaponString(Inventory inv) {
        String headColor = plugin.getSettingsManager().getColored(PAGE_HEADINGS_COLOR);

        String out = "";

        Player player = inv.player;

        int count = getItemCount(inv, Items.DIAMOND_SWORD);

        if (count > 0) {
            String countString = count > 1 ? count + "" : "";
            out += AQUA + lang("weapon.S", player.createCommandSourceStack()) + headColor + countString;
        }

        count = getItemCount(inv, Items.GOLDEN_SWORD);

        if (count > 0) {
            String countString = count > 1 ? count + "" : "";
            out += ChatFormatting.YELLOW + lang("weapon.S", player.createCommandSourceStack()) + headColor + countString;
        }

        count = getItemCount(inv, Items.IRON_SWORD);

        if (count > 0) {
            String countString = count > 1 ? count + "" : "";
            out += ChatFormatting.WHITE + lang("weapon.S", player.createCommandSourceStack()) + headColor + countString;
        }

        count = getItemCount(inv, Items.STONE_SWORD);

        if (count > 0) {
            String countString = count > 1 ? count + "" : "";
            out += ChatFormatting.GRAY + lang("weapon.S", player.createCommandSourceStack()) + headColor + countString;
        }

        count = getItemCount(inv, Items.WOODEN_SWORD);

        if (count > 0) {
            String countString = count > 1 ? count + "" : "";
            out += ChatFormatting.GOLD + lang("weapon.S", player.createCommandSourceStack()) + headColor + countString;
        }

        count = getItemCount(inv, Items.BOW);

        if (count > 0) {
            String countString = count > 1 ? count + "" : "";
            out += ChatFormatting.GOLD + lang("weapon.B", player.createCommandSourceStack()) + headColor + countString;
        }

        count = getItemCount(inv, Items.ARROW);
        count += getItemCount(inv, Items.SPECTRAL_ARROW);
        count += getItemCount(inv, Items.TIPPED_ARROW);

        if (count > 0) {
            out += ChatFormatting.WHITE + lang("weapon.A", player.createCommandSourceStack()) + headColor + count;
        }

        if (out.length() == 0) {
            out = lang("none", player.createCommandSourceStack());
        }

        return out;
    }

    private int getItemCount(@NotNull Inventory inv, @NotNull Item material) {
        Item parsed = material;
        if (parsed == null) {
            return 0;
        }

        return getItemCount((HashMap<Integer, ? extends ItemStack>) getAll(inv, parsed));
    }

    private int getItemCount(HashMap<Integer, ? extends ItemStack> all) {
        int count = 0;

        for (ItemStack is : all.values()) {
            count += is.getCount();
        }

        return count;
    }

    private double getFoodPoints(Inventory inv, Items material, int points, double saturation) {
        Items parsed = material;
        if (parsed == null) {
            return 0;
        }
        return getFoodPoints(inv, parsed, points, saturation);
    }

    public Map<Integer, ItemStack> getAll(Inventory inv, Item item) {
        Map<Integer, ItemStack> found = new HashMap<>();

        // Wir gehen durch die normale Item-Liste (0-35: Hotbar + Inventar)
        for (int i = 0; i < inv.items.size(); i++) {
            ItemStack stack = inv.items.get(i);
            if (stack.is(item)) {
                found.put(i, stack);
            }
        }
        return found;
    }

    private double getFoodPoints(Inventory inv, Item material, int points, double saturation) {
        return getItemCount((HashMap<Integer, ? extends ItemStack>) getAll(inv, material)) * (points + saturation);
    }

    /**
     * Returns a formatted string detailing the players food
     *
     * @param inv the Inventory
     * @return the food points string
     */
    public String getFoodString(Inventory inv) {

        Player player = inv.player;

        double count = getFoodPoints(inv, Items.APPLE, 4, 2.4);
        count += getFoodPoints(inv, Items.BAKED_POTATO, 5, 6);
        count += getFoodPoints(inv, Items.BEETROOT, 1, 1.2);
        count += getFoodPoints(inv, Items.BEETROOT_SOUP, 6, 7.2);
        count += getFoodPoints(inv, Items.BREAD, 5, 6);
        count += getFoodPoints(inv, Items.CAKE, 14, 2.8);
        count += getFoodPoints(inv, Items.CARROT, 3, 3.6);
        count += getFoodPoints(inv, Items.CHORUS_FRUIT, 4, 2.4);
        count += getFoodPoints(inv, Items.COOKED_CHICKEN, 6, 7.2);
        count += getFoodPoints(inv, Items.COOKED_MUTTON, 6, 9.6);
        count += getFoodPoints(inv, Items.COOKED_PORKCHOP, 8, 12.8);
        count += getFoodPoints(inv, Items.COOKED_RABBIT, 5, 6);
        count += getFoodPoints(inv, Items.COOKED_SALMON, 6, 9.6);
        count += getFoodPoints(inv, Items.COOKIE, 2, .4);
        count += getFoodPoints(inv, Items.GOLDEN_APPLE, 4, 9.6);
        count += getFoodPoints(inv, Items.GOLDEN_CARROT, 6, 14.4);
        count += getFoodPoints(inv, Items.MELON, 2, 1.2);
        count += getFoodPoints(inv, Items.MUSHROOM_STEW, 6, 7.2);
        count += getFoodPoints(inv, Items.POISONOUS_POTATO, 2, 1.2);
        count += getFoodPoints(inv, Items.POTATO, 1, 0.6);
        count += getFoodPoints(inv, Items.PUFFERFISH, 1, 0.2);
        count += getFoodPoints(inv, Items.PUMPKIN_PIE, 8, 4.8);
        count += getFoodPoints(inv, Items.RABBIT_STEW, 10, 12);
        count += getFoodPoints(inv, Items.BEEF, 3, 1.8);
        count += getFoodPoints(inv, Items.CHICKEN, 2, 1.2);
        count += getFoodPoints(inv, Items.MUTTON, 2, 1.2);
        count += getFoodPoints(inv, Items.PORKCHOP, 3, 1.8);
        count += getFoodPoints(inv, Items.RABBIT, 3, 1.8);
        count += getFoodPoints(inv, Items.SALMON, 1, .4);
        count += getFoodPoints(inv, Items.COD, 2, .4);
        count += getFoodPoints(inv, Items.COOKED_COD, 5, 6);
        count += getFoodPoints(inv, Items.TROPICAL_FISH, 1, .2);
        count += getFoodPoints(inv, Items.ROTTEN_FLESH, 4, .8);
        count += getFoodPoints(inv, Items.SPIDER_EYE, 2, 3.2);
        count += getFoodPoints(inv, Items.COOKED_BEEF, 8, 12.8);

        if (count == 0) {
            return lang("none", player.createCommandSourceStack());
        } else {
            return ((int) count) + "" + ChatFormatting.GOLD + "p";
        }
    }

    /**
     * Returns a colored bar based on the length
     */
    public String getBar(double length) {
        StringBuilder out = new StringBuilder();

        if (length >= 16) {
            out.append(ChatFormatting.GREEN);
        } else if (length >= 8) {
            out.append(ChatFormatting.GOLD);
        } else {
            out.append(ChatFormatting.RED);
        }

        for (int i = 0; i < length; i++) {
            out.append('|');
        }

        return out.toString();
    }

    /**
     * Sort clans by active
     */
    public void sortClansByActive(List<Clan> clans, boolean asc) {
        clans.sort((c1, c2) -> {
            int o = 1;
            if (!asc) {
                o = -1;
            }

            return Long.compare(c1.getLastUsed(), c2.getLastUsed()) * o;
        });
    }

    /**
     * Sort clans by founded date
     */
    public void sortClansByFounded(List<Clan> clans, boolean asc) {
        clans.sort((c1, c2) -> {
            int o = 1;
            if (!asc) {
                o = -1;
            }

            return Long.compare(c1.getFounded(), c2.getFounded()) * o;
        });
    }

    /**
     * Sort clans by kdr
     */
    public void sortClansByKDR(List<Clan> clans, boolean asc) {
        clans.sort((c1, c2) -> {
            int o = 1;
            if (!asc) {
                o = -1;
            }

            return Float.compare(c1.getTotalKDR(), c2.getTotalKDR()) * o;
        });
    }

    /**
     * Sort clans by size
     */
    public void sortClansBySize(List<Clan> clans, boolean asc) {
        clans.sort((c1, c2) -> {
            int o = 1;
            if (!asc) {
                o = -1;
            }

            return Integer.compare(c1.getSize(), c2.getSize()) * o;
        });
    }

    /**
     * Sort clans by name
     */
    public void sortClansByName(List<Clan> clans, boolean asc) {
        clans.sort((c1, c2) -> {
            int o = 1;
            if (!asc) {
                o = -1;
            }

            return c1.getName().toString().compareTo(c2.getName().toString()) * o;
        });
    }

    /**
     * Sort clans by KDR
     */
    public void sortClansByKDR(List<Clan> clans) {
        clans.sort((c1, c2) -> {
            Float o1 = c1.getTotalKDR();
            Float o2 = c2.getTotalKDR();

            return o2.compareTo(o1);
        });
    }

    /**
     * Sort clans by KDR
     */
    public void sortClansBySize(List<Clan> clans) {
        clans.sort((c1, c2) -> {
            Integer o1 = c1.getMembers().size();
            Integer o2 = c2.getMembers().size();

            return o2.compareTo(o1);
        });
    }

    /**
     * Sort clan players by KDR
     */
    public void sortClanPlayersByKDR(List<ClanPlayer> cps) {
        cps.sort((c1, c2) -> {
            Float o1 = c1.getKDR();
            Float o2 = c2.getKDR();

            return o2.compareTo(o1);
        });
    }

    /**
     * Sort clan players by last seen days
     */
    public void sortClanPlayersByLastSeen(List<ClanPlayer> cps) {
        cps.sort((c1, c2) -> {
            Double o1 = c1.getLastSeenDays();
            Double o2 = c2.getLastSeenDays();

            return o1.compareTo(o2);
        });
    }

    public long getMinutesBeforeRejoin(@NotNull ClanPlayer cp, @NotNull Clan clan) {
        SettingsManager settings = plugin.getSettingsManager();
        if (settings.is(ENABLE_REJOIN_COOLDOWN)) {
            Long resign = cp.getResignTime(clan.getTag());
            if (resign != null) {
                long timePassed = Instant.ofEpochMilli(resign).until(Instant.now(), ChronoUnit.MINUTES);
                int cooldown = settings.getInt(REJOIN_COOLDOWN);
                if (timePassed < cooldown) {
                    return cooldown - timePassed;
                }
            }
        }
        return 0;
    }

    /**
     * Purchase member fee set
     */
    public boolean purchaseMemberFeeSet(Player player) {
        if (!plugin.getSettingsManager().is(ECONOMY_PURCHASE_MEMBER_FEE_SET)) {
            return true;
        }

        return true;
    }

    /**
     * Purchase clan creation
     */
    public boolean purchaseCreation(Player player) {
        if (!plugin.getSettingsManager().is(ECONOMY_PURCHASE_CLAN_CREATE)) {
            return true;
        }

        return true;
    }

    /**
     * Purchase invite
     */
    public boolean purchaseInvite(Player player) {
        if (!plugin.getSettingsManager().is(ECONOMY_PURCHASE_CLAN_INVITE)) {
            return true;
        }

        return true;
    }

    /**
     * Purchase Home Teleport
     */
    public boolean purchaseHomeTeleport(Player player) {
        if (!plugin.getSettingsManager().is(ECONOMY_PURCHASE_HOME_TELEPORT)) {
            return true;
        }

        return true;
    }

    /**
     * Purchase Home Teleport Set
     */
    public boolean purchaseHomeTeleportSet(Player player) {
        if (!plugin.getSettingsManager().is(ECONOMY_PURCHASE_HOME_TELEPORT_SET)) {
            return true;
        }

        return true;
    }

    /**
     * Purchase Reset Kdr
     */
    public boolean purchaseResetKdr(Player player) {
        if (!plugin.getSettingsManager().is(ECONOMY_PURCHASE_RESET_KDR)) {
            return true;
        }

        return true;
    }

    /**
     * Purchase Home Regroup
     */
    public boolean purchaseHomeRegroup(Player player) {
        ClanPlayer cp = plugin.getClanManager().getClanPlayer(player);
        if (cp == null) {
            return false;
        }

        if (!plugin.getSettingsManager().is(ECONOMY_PURCHASE_HOME_REGROUP)) {
            return true;
        }

        return false;
    }

    /**
     * Purchase clan verification
     */
    public boolean purchaseVerification(Player player) {
        if (!plugin.getSettingsManager().is(ECONOMY_PURCHASE_CLAN_VERIFY)) {
            return true;
        }

        return true;
    }

    /**
     * Processes a global chat command
     */
    @Deprecated
    public boolean processGlobalChat(Player player, String msg) {
        ClanPlayer cp = plugin.getClanManager().getClanPlayer(player.getUUID());

        if (cp == null) {
            return false;
        }

        String[] split = msg.split(" ");

        if (split.length == 0) {
            return false;
        }

        String command = split[0];

        if (command.equals(lang("on", player.createCommandSourceStack()))) {
            cp.setGlobalChat(true);
            plugin.getStorageManager().updateClanPlayer(cp);
            ChatBlock.sendMessage(player.createCommandSourceStack(), AQUA + "You have enabled global chat");
        } else if (command.equals(lang("off", player.createCommandSourceStack()))) {
            cp.setGlobalChat(false);
            plugin.getStorageManager().updateClanPlayer(cp);
            ChatBlock.sendMessage(player.createCommandSourceStack(), AQUA + "You have disabled global chat");
        } else {
            return true;
        }

        return false;
    }

    /**
     * Gibt eine sortierte Liste der besten Spieler zurück.
     * @param limit Maximale Anzahl an Spielern.
     * @return Eine Liste von ClanPlayer Objekten.
     */
    public List<ClanPlayer> getTopPlayers(int limit) {
        // Wir holen uns alle bekannten Clan-Spieler
        List<ClanPlayer> allPlayers = new ArrayList<>(clanPlayers.values());

        // Sortierung nach KDR (absteigend)
        allPlayers.sort((cp1, cp2) -> Double.compare(cp2.getKDR(), cp1.getKDR()));

        // Begrenzung auf das gewünschte Limit
        if (allPlayers.size() > limit) {
            return allPlayers.subList(0, limit);
        }

        return allPlayers;
    }
}
