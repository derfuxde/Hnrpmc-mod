package org.emil.hnrpmc.simpleclans;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.emil.hnrpmc.simpleclans.events.*;
import org.emil.hnrpmc.simpleclans.hooks.placeholder.Placeholder;
import org.emil.hnrpmc.simpleclans.loggers.BankLog;
import org.emil.hnrpmc.simpleclans.loggers.BankLogger;
import org.emil.hnrpmc.simpleclans.loggers.BankOperator;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.emil.hnrpmc.simpleclans.utils.CurrencyFormat;
import org.emil.hnrpmc.simpleclans.utils.DateFormat;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.Level;


import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.emil.hnrpmc.simpleclans.EconomyResponse.CANCELLED;
import static org.emil.hnrpmc.simpleclans.EconomyResponse.SUCCESS;
import static org.emil.hnrpmc.simpleclans.SimpleClans.*;
import static org.emil.hnrpmc.simpleclans.chat.ChatHandler.plugin;
import static org.emil.hnrpmc.simpleclans.chat.ChatHandler.settingsManager;
import static org.emil.hnrpmc.simpleclans.loggers.BankLogger.Operation.SET;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

/**
 * @author phaed
 */
public class Clan implements Serializable, Comparable<Clan> {

    private static final long serialVersionUID = 1L;
    private static final String WARRING_KEY = "warring";
    private boolean verified;
    private String tag;
    private String colorTag;
    private String ClanColor;
    private String name;
    private String description;
    private double balance;
    private double fee;
    private boolean friendlyFire;
    private long founded;
    private long lastUsed;
    private String capeUrl;
    private List<String> allies = new ArrayList<>();
    private List<String> rivals = new ArrayList<>();
    private List<String> bb = new ArrayList<>();
    private final List<ClanPlayer> members = new ArrayList<>();
    private Flags flags = new Flags(null);
    private boolean feeEnabled;
    private List<Rank> ranks = new ArrayList<>();
    private @Nullable String defaultRank = null;
    private @Nullable ItemStack banner;

    /**
     *
     */
    public Clan() {
        this.capeUrl = "";
        this.tag = "";
    }

    public Clan(String tag, String name, boolean verified) {
        this.tag = Helper.cleanTag(tag);
        this.colorTag = ChatUtils.parseColors(tag);
        this.name = name;
        this.founded = (new Date()).getTime();
        this.lastUsed = (new Date()).getTime();
        this.verified = verified;
        this.capeUrl = "";
        this.ClanColor = "§b";
        if (SimpleClans.getInstance().getSettingsManager().is(CLAN_FF_ON_BY_DEFAULT)) {
            friendlyFire = true;
        }

    }

    @Override
    public int hashCode() {
        return getTag().hashCode() >> 13;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Clan)) {
            return false;
        }

        Clan other = (Clan) obj;
        return other.getTag().equals(this.getTag());
    }

    @Override
    public int compareTo(Clan other) {
        return this.getTag().compareToIgnoreCase(other.getTag());
    }

    @Override
    public String toString() {
        return tag;
    }

    /**
     * Returns the clan's name
     *
     * @return the name
     */
    public Object getName() {
        return name;
    }

    public String getStringName() {
        return name;
    }

    public String getClanColor() {
        return ClanColor;
    }

    public void setClanColor(String color) {
        ClanColor = color;
    }

    /**
     * Returns the clan's balance
     *
     * @return the balance
     */
    @Placeholder("balance")
    public double getBalance() {
        return balance;
    }

    /**
     * Returns the clan's balance formatted
     *
     * @return the balance formatted
     */
    @Placeholder("balance_formatted")
    public String getBalanceFormatted() {
        return CurrencyFormat.format(balance);
    }

    /**
     * (used internally)
     *
     * @param balance the balance to set
     */
    private void setBalance(double balance) {
        setBalance(BankOperator.INTERNAL, ClanBalanceUpdateEvent.Cause.INTERNAL, SET, balance);
    }

    public EconomyResponse setBalance(@NotNull BankOperator operator, @NotNull ClanBalanceUpdateEvent.Cause cause,
                                      @NotNull BankLogger.Operation operation, double balance) {
        EconomyResponse response = SUCCESS;

        ClanBalanceUpdateEvent event = new ClanBalanceUpdateEvent(operator, this, getBalance(), balance, cause);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            response = CANCELLED;
        }

        this.balance = event.getNewBalance();
        if (cause != ClanBalanceUpdateEvent.Cause.LOADING) {
            if (operation == SET) {
                SimpleClans.getInstance().getBankLogger().log(new BankLog(operator, this, response, SET, cause, balance));
            }
            SimpleClans.getInstance().getStorageManager().updateClan(this);
        }
        return response;
    }

    /**
     * (used internally)
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the clan's description
     *
     * @return the description or null if it doesn't have one
     */
    public String getDescription() {
        return description;
    }

    /**
     * (used internally)
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the clan's fee
     */
    public void setMemberFee(double fee) {
        if (fee < 0) {
            fee = 0;
        }
        this.fee = fee;
    }

    /**
     * Returns the clan's fee
     *
     * @return the fee
     */
    public double getMemberFee() {
        return fee;
    }

    /**
     * Returns the clan's tag clean (no colors)
     *
     * @return the tag
     */
    @Placeholder("tag")
    public String getTag() {
        return tag;
    }

    /**
     * (used internally)
     *
     * @param tag the tag to set
     */
    public void setTag(String tag) {
        this.tag = tag;
    }
    /**
     * Returns the first color in the clan's tag
     *
     * @return the color code or an empty string if there is no color
     */
    @Placeholder("color")
    public String getColor() {
        if (colorTag.startsWith("\u00a7x")) { // Hexadecimal Code
            return colorTag.substring(0, 14);
        } else if (colorTag.charAt(0) == '\u00a7') { // Regular Code
            return colorTag.substring(0, 2);
        } else { // No Code
            return "";
        }
    }

    /**
     * Returns the last used date in milliseconds
     *
     * @return the lastUsed
     */
    public long getLastUsed() {
        return lastUsed;
    }

    /**
     * Updates last used date to today (does not update clan on db)
     */
    public void updateLastUsed() {
        setLastUsed((new Date()).getTime());
    }

    /**
     * Returns the number of days the clan has been inactive
     */
    public int getInactiveDays() {
        Timestamp now = new Timestamp((new Date()).getTime());
        return (int) Math.floor(Dates.differenceInDays(new Timestamp(getLastUsed()), now));
    }

    /**
     * Returns the max number of days the clan can be inactive
     * A {@literal <=} 0 means it won't be purged
     */
    public int getMaxInactiveDays() {
        if (isPermanent()) {
            return -1;
        }

        int verifiedClanInactiveDays = SimpleClans.getInstance().getSettingsManager().getInt(PURGE_INACTIVE_CLAN_DAYS);
        int unverifiedClanInactiveDays = SimpleClans.getInstance().getSettingsManager().getInt(PURGE_UNVERIFIED_CLAN_DAYS);

        return isVerified() ? verifiedClanInactiveDays : unverifiedClanInactiveDays;
    }

    /**
     * (used internally)
     *
     * @param lastUsed the lastUsed to set
     */
    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    /**
     * Check whether this clan allows friendly fire
     *
     * @return the friendlyFire
     */
    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    /**
     * Sets the friendly fire status of this clan (does not update clan on db)
     *
     * @param friendlyFire the friendlyFire to set
     */
    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    /**
     * Check if the player is a member of this clan
     *
     * @param player the Player
     * @return confirmation
     */
    public boolean isMember(Player player) {
        return isMember(player.getUUID());
    }

    /**
     * Check if the player is a member of this clan
     *
     * @param uuid the Player's UUID
     * @return confirmation
     */
    public boolean isMember(UUID uuid) {
        for (ClanPlayer cp : members) {
            if (cp.getUniqueId().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public boolean isMember(String playerName) {
        return isMember(SimpleClans.getInstance().getServer().getProfileCache().get(playerName).get().getId());
    }

    /**
     * Returns a list with the contents of the bulletin board
     *
     * @return the bb
     */
    public List<String> getBb() {
        return Collections.unmodifiableList(bb);
    }

    /**
     * Return a list of all the allies' tags clean (no colors)
     *
     * @return the allies
     */
    @Placeholder(value = "allies_count", resolver = "list_size")
    public List<String> getAllies() {
        return Collections.unmodifiableList(allies);
    }

    private void addAlly(String tag) {
        allies.add(tag);
    }

    private boolean removeAlly(String ally) {
        if (!allies.contains(ally)) {
            return false;
        }

        allies.remove(ally);
        return true;
    }

    /**
     * The founded date in milliseconds
     *
     * @return the founded
     */
    public long getFounded() {
        return founded;
    }

    /**
     * The string representation of the founded date
     */
    public String getFoundedString() {
        return DateFormat.formatDateTime(founded);
    }

    /**
     * (used internally)
     *
     * @param founded the founded to set
     */
    public void setFounded(long founded) {
        this.founded = founded;
    }

    /**
     * Returns the color tag for this clan
     *
     * @return the colorTag
     */
    public String getColorTag() {
        return colorTag;
    }

    /**
     * (used internally)
     *
     * @param colorTag the colorTag to set
     */
    public void setColorTag(String colorTag) {
        this.colorTag = ChatUtils.parseColors(colorTag);
    }

    /**
     * Adds a bulletin board message without announcer
     */
    public void addBb(String msg) {
        addBbWithoutSaving(msg);
        SimpleClans.getInstance().getStorageManager().updateClan(this);
    }

    public void setBb(List<String> bb) {
        this.bb = new ArrayList<>(bb);
    }

    /**
     * Adds a bulletin board message without saving it to the database
     */
    public void addBbWithoutSaving(String msg) {
        while (bb.size() > SimpleClans.getInstance().getSettingsManager().getInt(BB_SIZE)) {
            bb.remove(0);
        }

        bb.add(System.currentTimeMillis() + "_" + msg);
    }

    /**
     * Adds a bulletin board message without announcer and saves it to the database
     *
     * @param updateLastUsed should the clan's last used time be updated as well?
     */
    public void addBb(String msg, boolean updateLastUsed) {
        addBbWithoutSaving(msg);
        SimpleClans.getInstance().getStorageManager().updateClan(this, updateLastUsed);
    }

    /**
     * Clears the bulletin board
     */
    public void clearBb() {
        bb.clear();
        SimpleClans.getInstance().getStorageManager().updateClan(this);
    }

    /**
     * (used internally)
     */
    public void importMember(ClanPlayer cp) {
        if (!members.contains(cp)) {
            members.add(cp);
        }
    }

    /**
     * (used internally)
     */
    public void removeMember(UUID uuid) {
        members.removeIf(cp -> cp.getUniqueId().equals(uuid));
    }

    /**
     * Get total clan size
     */
    public int getSize() {
        return members.size();
    }

    /**
     * Returns a list of all rival tags clean (no colors)
     *
     * @return the rivals
     */
    public List<String> getRivals() {
        return Collections.unmodifiableList(rivals);
    }

    private void addRival(String tag) {
        rivals.add(tag);
    }

    private boolean removeRival(String rival) {
        return rivals.remove(rival);
    }

    /**
     * Check if the tag is a rival
     */
    public boolean isRival(String tag) {
        return rivals.contains(tag);
    }

    /**
     * Check if the tag is an ally
     */
    public boolean isAlly(String tag) {
        return allies.contains(tag);
    }

    /**
     * Tells you if the clan is verified, always returns true if no verification
     * is required
     */
    public boolean isVerified() {
        return !SimpleClans.getInstance().getSettingsManager().is(REQUIRE_VERIFICATION) || verified;

    }

    /**
     * (used internally)
     *
     * @param verified the verified to set
     */
    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isPermanent() {
        return flags.getBoolean("permanent", false);
    }

    public void setPermanent(boolean permanent) {
        flags.put("permanent", permanent);
    }

    /**
     * Returns the cape url for this clan
     *
     * @return the capeUrl
     */
    @Deprecated
    public String getCapeUrl() {
        return capeUrl;
    }

    /**
     * (used internally)
     *
     * @param capeUrl the capeUrl to set
     */
    @Deprecated
    public void setCapeUrl(String capeUrl) {
        this.capeUrl = capeUrl;
    }

    /**
     * (used internally)
     *
     * @return the packedBb
     */
    public String getPackedBb() {
        return String.join("|", bb);
    }

    /**
     * (used internally)
     *
     * @param packedBb the packedBb to set
     */
    public void setPackedBb(String packedBb) {
        if (packedBb == null || packedBb.isEmpty()) {
            this.allies = new ArrayList<>();
            return;
        }

        String[] split = packedBb.split("\\|");
        allies = Helper.fromArrayToList(packedBb.split("[|]"));
    }

    /**
     * (used internally)
     *
     * @return the packedAllies
     */
    public String getPackedAllies() {
        return String.join("|", allies);
    }

    /**
     * (used internally)
     *
     * @param packedAllies the packedAllies to set
     */
    public void setPackedAllies(String packedAllies) {
        // Falls der Wert null oder leer ist, brechen wir ab oder setzen eine leere Liste
        if (packedAllies == null || packedAllies.isEmpty()) {
            this.allies = new ArrayList<>(); // oder was auch immer dein Feld-Typ ist
            return;
        }

        // Erst wenn wir sicher sind, dass es nicht null ist, splitten wir
        String[] split = packedAllies.split("\\|");
        allies = Helper.fromArrayToList(packedAllies.split("[|]"));
    }

    /**
     * (used internally)
     *
     * @return the packedRivals
     */
    public String getPackedRivals() {
        return String.join("|", rivals);
    }

    /**
     * (used internally)
     *
     * @param packedRivals the packedRivals to set
     */

    public void setPackedRivals(String packedRivals) {
        // Falls der Wert null oder leer ist, brechen wir ab oder setzen eine leere Liste
        if (packedRivals == null || packedRivals.isEmpty()) {
            this.allies = new ArrayList<>(); // oder was auch immer dein Feld-Typ ist
            return;
        }

        // Erst wenn wir sicher sind, dass es nicht null ist, splitten wir
        String[] split = packedRivals.split("\\|");
        allies = Helper.fromArrayToList(packedRivals.split("[|]"));
    }

    /**
     * Returns a separator delimited string with all the ally clan's colored
     * tags
     */
    public String getAllyString(String sep, @Nullable CommandSourceStack viewer) {
        String coloredAllies = getAllies().stream().
                map(allyTag -> SimpleClans.getInstance().getClanManager().getClan(allyTag)).
                filter(Objects::nonNull).
                map(Clan::getColorTag).
                collect(Collectors.joining(sep));

        return coloredAllies.isEmpty() ? lang("none", viewer) : coloredAllies;
    }

    /**
     * @deprecated use {@link Clan#getAllyString(String, CommandSourceStack)}
     */
    @Deprecated
    public String getAllyString(String sep) {
        return getAllyString(sep, null);
    }

    /**
     * Returns a separator delimited string with all the rival clan's colored
     * tags
     */
    public String getRivalString(String sep, @Nullable CommandSourceStack viewer) {
        String coloredRivals = getRivals().stream().
                map(rivalTag -> SimpleClans.getInstance().getClanManager().getClan(rivalTag)).
                filter(Objects::nonNull).
                map(rival -> isWarring(rival) ? ChatFormatting.DARK_RED + "[" + rival.getTag() + "]" : rival.getColorTag()).
                map(ChatUtils::parseColors).
                collect(Collectors.joining(sep));

        return coloredRivals.isEmpty() ? lang("none", viewer) : coloredRivals;
    }

    /**
     * @deprecated use {@link Clan#getRivalString(String, CommandSourceStack)}
     */
    @Deprecated
    public String getRivalString(String sep) {
        return getRivalString(sep, null);
    }

    /**
     * Returns a separator delimited string with all the leaders
     *
     * @return the formatted leaders string
     */
    public String getLeadersString(String prefix, String sep) {
        return members.stream().filter(ClanPlayer::isLeader).map(ClanPlayer::getName).
                collect(Collectors.joining(sep, prefix, ""));
    }

    /**
     * Check if a player is a leader of a clan
     *
     * @return the leaders
     */
    public boolean isLeader(Player player) {
        return isLeader(player.getUUID());
    }


    /**
     * Check if a player is a leader of a clan
     *
     * @return the leaders
     */
    public boolean isLeader(UUID playerUniqueId) {
        if (isMember(playerUniqueId)) {
            ClanPlayer cp = SimpleClans.getInstance().getClanManager().getClanPlayer(playerUniqueId);

            return cp != null && cp.isLeader();
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    public boolean isLeader(String playerName) {
        return isLeader(SimpleClans.getInstance().getServer().getProfileCache().get(playerName).get().getId());
    }

    /**
     * Get all members that must pay the fee (that excludes leaders and players with the permission to bypass it)
     *
     * @return the fee payers
     */
    public Set<ClanPlayer> getFeePayers() {
        return getMembers().stream().filter(cp -> !cp.isLeader()).filter(cp -> {
            Rank rank = getRank(cp.getRankId());
            return rank == null || !rank.getPermissions().contains(RankPermission.FEE_BYPASS.toString());
        }).collect(Collectors.toSet());
    }

    /**
     * Get all members (leaders, and non-leaders) in the clan
     *
     * @return the members
     */
    public List<ClanPlayer> getMembers() {
        return new ArrayList<>(members);
    }

    /**
     * Get all online members (leaders, and non-leaders) in the clan
     *
     * @return the members
     */
    @Placeholder(value = "onlinemembers_count", resolver = "list_size", config = "filter_vanished")
    public List<ClanPlayer> getOnlineMembers() {
        return members.stream().filter(cp -> cp.toPlayer() != null).collect(Collectors.toList());
    }

    /**
     * Get all leaders in the clan
     *
     * @return the leaders
     */
    @Placeholder(value = "leader_size", resolver = "list_size")
    public List<ClanPlayer> getLeaders() {
        return members.stream().filter(ClanPlayer::isLeader).collect(Collectors.toList());
    }

    /**
     * Get all non-leader players in the clan
     *
     * @return non leaders
     */
    public List<ClanPlayer> getNonLeaders() {
        return members.stream().filter(cp -> !cp.isLeader()).collect(Collectors.toList());
    }

    /**
     * Get all clan's members
     *
     * @deprecated use {@link Clan#getMembers()}
     */
    @Deprecated
    public List<ClanPlayer> getAllMembers() {
        return getMembers();
    }

    /**
     * Get all the ally clan's members
     */
    public Set<ClanPlayer> getAllAllyMembers() {
        Set<ClanPlayer> out = new HashSet<>();

        for (String tag : allies) {
            Clan ally = SimpleClans.getInstance().getClanManager().getClan(tag);

            if (ally != null) {
                out.addAll(ally.getMembers());
            }
        }

        return out;
    }

    /**
     * Gets the clan's total KDR
     */
    @Placeholder(value = "total_kdr", resolver = "kdr")
    @Placeholder(value = "topclans_position", resolver = "ranking_position")
    public float getTotalKDR() {
        if (members.isEmpty()) {
            return 0;
        }
        double totalWeightedKills = 0;
        int totalDeaths = 0;

        for (ClanPlayer cp : members) {
            totalWeightedKills += cp.getWeightedKills();
            totalDeaths += cp.getDeaths();
        }

        if (totalDeaths == 0) {
            totalDeaths = 1;
        }

        return ((float) totalWeightedKills) / ((float) totalDeaths);
    }

    /**
     * Gets the clan's total KDR
     */
    @Placeholder("total_deaths")
    public int getTotalDeaths() {
        int totalDeaths = 0;

        if (members.isEmpty()) {
            return totalDeaths;
        }

        for (ClanPlayer cp : members) {
            totalDeaths += cp.getDeaths();
        }

        return totalDeaths;
    }

    /**
     * Gets average weighted kills for the clan
     */
    @Placeholder("average_wk")
    public int getAverageWK() {
        int total = 0;

        if (members.isEmpty()) {
            return total;
        }

        for (ClanPlayer cp : members) {
            total += cp.getWeightedKills();
        }

        return total / getSize();
    }

    @Placeholder("total_kills")
    public int getTotalKills() {
        return getTotalCivilian() + getTotalNeutral() + getTotalRival() + getTotalAlly();
    }

    /**
     * Gets total rival kills for the clan
     */
    @Placeholder("total_rival")
    public int getTotalRival() {
        int total = 0;

        for (ClanPlayer cp : getMembers()) {
            total += cp.getRivalKills();
        }

        return total;
    }

    /**
     * Gets total neutral kills for the clan
     */
    @Placeholder("total_neutral")
    public int getTotalNeutral() {
        int total = 0;

        for (ClanPlayer cp : getMembers()) {
            total += cp.getNeutralKills();
        }

        return total;
    }

    /**
     * Gets total civilian kills for the clan
     */
    @Placeholder("total_civilian")
    public int getTotalCivilian() {
        int total = 0;

        for (ClanPlayer cp : getMembers()) {
            total += cp.getCivilianKills();
        }

        return total;
    }

    @Placeholder("total_ally")
    public int getTotalAlly() {
        int total = 0;

        for (ClanPlayer cp : getMembers()) {
            total += cp.getAllyKills();
        }

        return total;
    }

    /**
     * Check whether the clan has crossed the rival limit
     */
    public boolean reachedRivalLimit() {
        int rivalCount = rivals.size();
        int clanCount = SimpleClans.getInstance().getClanManager().getRivableClanCount() - 1;
        double rivalPercent = SimpleClans.getInstance().getSettingsManager().getInt(RIVAL_LIMIT_PERCENT);

        double limit = ((double) clanCount) * (rivalPercent / ((double) 100));

        return rivalCount > limit;
    }

    /**
     * Add a new player to the clan
     */
    public void addPlayerToClan(ClanPlayer cp) {
        cp.removePastClan(getColorTag());
        cp.setClan(this);
        cp.setLeader(false);
        cp.setJoinDate(new Date().getTime());
        cp.setTrusted(SimpleClans.getInstance().getSettingsManager().is(CLAN_TRUST_MEMBERS_BY_DEFAULT));
        if (defaultRank != null) {
            cp.setRank(defaultRank);
        }

        importMember(cp);

        SimpleClans.getInstance().getStorageManager().updateClanPlayer(cp);
        SimpleClans.getInstance().getStorageManager().updateClan(this);

        // add clan permission
        SimpleClans.getInstance().getPermissionsManager().addClanPermissions(cp);
        SimpleClans.getInstance().getPermissionsManager().addPlayerPermissions(cp);

        Player player = SimpleClans.getInstance().getServer().getPlayerList().getPlayer(cp.getUniqueId());

        if (player != null) {
            SimpleClans.getInstance().getClanManager().updateDisplayName(player);
        }
        NeoForge.EVENT_BUS.post(new PlayerJoinedClanEvent(this, cp));
    }

    @SuppressWarnings("deprecation")
    public void removePlayerFromClan(String playerName) {
        removePlayerFromClan(SimpleClans.getInstance().getServer().getProfileCache().get(playerName).get().getId());
    }

    /**
     * Remove a player from a clan
     */
    public void removePlayerFromClan(UUID playerUniqueId) {
        ClanPlayer cp = SimpleClans.getInstance().getClanManager().getClanPlayer(playerUniqueId);
        if (cp == null || !isMember(playerUniqueId)) {
            return;
        }

        // remove clan group-permission
        SimpleClans.getInstance().getPermissionsManager().removeClanPermissions(cp.getClan());

        // remove permissions
        SimpleClans.getInstance().getPermissionsManager().removeClanPlayerPermissions(cp);

        cp.setClan(null);
        cp.addPastClan(getColorTag() + (cp.isLeader() ? ChatFormatting.DARK_RED + "*" : ""));
        cp.setLeader(false);
        cp.setTrusted(false);
        cp.setJoinDate(0);
        cp.setRank(null);
        removeMember(playerUniqueId);

        SimpleClans.getInstance().getStorageManager().updateClanPlayer(cp);
        SimpleClans.getInstance().getStorageManager().updateClan(this);

        Player matched = SimpleClans.getInstance().getServer().getPlayerList().getPlayer(playerUniqueId);

        if (matched != null) {
            SimpleClans.getInstance().getClanManager().updateDisplayName(matched);
        }
        NeoForge.EVENT_BUS.post(new PlayerKickedClanEvent(this, cp));
    }

    @SuppressWarnings("deprecation")
    public void promote(String playerName) {
        promote(SimpleClans.getInstance().getServer().getProfileCache().get(playerName).get().getId());
    }

    /**
     * Promote a member to a leader of a clan
     */
    public void promote(UUID playerUniqueId) {
        ClanPlayer cp = SimpleClans.getInstance().getClanManager().getCreateClanPlayer(playerUniqueId);

        cp.setLeader(true);
        cp.setTrusted(true);

        SimpleClans.getInstance().getStorageManager().updateClanPlayer(cp);
        SimpleClans.getInstance().getStorageManager().updateClan(this);

        // add clan permission
        SimpleClans.getInstance().getPermissionsManager().addClanPermissions(cp);
        NeoForge.EVENT_BUS.post(new PlayerPromoteEvent(this, cp));
    }

    @SuppressWarnings("deprecation")
    public void demote(String playerName) {
        demote(SimpleClans.getInstance().getServer().getProfileCache().get(playerName).get().getId());
    }

    /**
     * Demote a leader back to a member of a clan
     */
    public void demote(UUID playerUniqueId) {
        ClanPlayer cp = SimpleClans.getInstance().getClanManager().getCreateClanPlayer(playerUniqueId);

        cp.setLeader(false);

        SimpleClans.getInstance().getStorageManager().updateClanPlayer(cp);
        SimpleClans.getInstance().getStorageManager().updateClan(this);

        // add clan permission
        SimpleClans.getInstance().getPermissionsManager().addClanPermissions(cp);
        NeoForge.EVENT_BUS.post(new PlayerDemoteEvent(this, cp));
    }

    /**
     * Add an ally to a clan, and the clan to the ally
     */
    public void addAlly(Clan ally) {
        removeRival(ally.getTag());
        addAlly(ally.getTag());

        ally.removeRival(getTag());
        ally.addAlly(getTag());

        SimpleClans.getInstance().getStorageManager().updateClan(this);
        SimpleClans.getInstance().getStorageManager().updateClan(ally);
        NeoForge.EVENT_BUS.post(new AllyClanAddEvent(this, ally));
    }

    /**
     * Remove an ally from the clan, and the clan from the ally
     */
    public void removeAlly(Clan ally) {
        removeAlly(ally.getTag());
        ally.removeAlly(getTag());

        SimpleClans.getInstance().getStorageManager().updateClan(this);
        SimpleClans.getInstance().getStorageManager().updateClan(ally);
        NeoForge.EVENT_BUS.post(new AllyClanRemoveEvent(this, ally));
    }

    /**
     * Add a rival to the clan, and the clan to the rival
     */
    public void addRival(Clan rival) {
        removeAlly(rival.getTag());
        addRival(rival.getTag());

        rival.removeAlly(getTag());
        rival.addRival(getTag());

        SimpleClans.getInstance().getStorageManager().updateClan(this);
        SimpleClans.getInstance().getStorageManager().updateClan(rival);
        NeoForge.EVENT_BUS.post(new RivalClanAddEvent(this, rival));
    }

    /**
     * Removes a rival from the clan, the clan from the rival
     */
    public void removeRival(Clan rival) {
        removeRival(rival.getTag());
        rival.removeRival(getTag());

        SimpleClans.getInstance().getStorageManager().updateClan(this);
        SimpleClans.getInstance().getStorageManager().updateClan(rival);
        NeoForge.EVENT_BUS.post(new RivalClanRemoveEvent(this, rival));
    }

    /**
     * Verify a clan
     */
    public void verifyClan() {
        setVerified(true);
        SimpleClans.getInstance().getStorageManager().updateClan(this);
    }

    /**
     * Check whether any clan member is online
     */
    @Placeholder("is_anyonline")
    public boolean isAnyOnline() {
        return members.stream().anyMatch(cp -> cp.toPlayer() != null);
    }

    /**
     * Checks if there are enough leaders online to vote
     *
     * @param cp the one to demote
     * @return true if there are
     */
    public boolean enoughLeadersOnlineToDemote(ClanPlayer cp) {
        List<ClanPlayer> online = getOnlineLeaders();
        online.remove(cp);

        double minimum = SimpleClans.getInstance().getSettingsManager().getPercent(CLAN_PERCENTAGE_ONLINE_TO_DEMOTE);
        // all leaders minus the one being demoted
        double totalLeaders = getLeaders().size() - 1;
        double onlineLeaders = online.size();


        return ((onlineLeaders / totalLeaders) * 100) >= minimum;
    }

    /**
     * Gets the online leaders
     *
     * @return the online leaders
     */
    public List<ClanPlayer> getOnlineLeaders() {
        return getOnlineMembers().stream().filter(ClanPlayer::isLeader).collect(Collectors.toList());
    }

    /**
     * Check whether all leaders of a clan are online
     */
    public boolean allLeadersOnline() {
        List<ClanPlayer> leaders = getLeaders();

        for (ClanPlayer leader : leaders) {
            if (leader.toPlayer() == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check whether all leaders, except for the one passed in, are online
     */
    @Deprecated
    public boolean allOtherLeadersOnline(String playerName) {
        List<ClanPlayer> leaders = getLeaders();

        for (ClanPlayer leader : leaders) {
            if (leader.getName().equalsIgnoreCase(playerName)) {
                continue;
            }

            if (leader.toPlayer() == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check whether all leaders, except for the one passed in, are online
     */
    public boolean allOtherLeadersOnline(UUID playerUniqueId) {
        List<ClanPlayer> leaders = getLeaders();

        for (ClanPlayer leader : leaders) {
            if (leader.getUniqueId().equals(playerUniqueId)) {
                continue;
            }

            if (leader.toPlayer() == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Change a clan's tag
     */
    public void changeClanTag(String tag) {
        setColorTag(tag);
        SimpleClans.getInstance().getStorageManager().updateClan(this);
    }

    /**
     * Announce message to a whole clan
     */
    public void clanAnnounce(String playerName, String msg) {
        String message = SimpleClans.getInstance().getSettingsManager().getColored(CLANCHAT_ANNOUNCEMENT_COLOR) + msg;

        for (ClanPlayer cp : getMembers()) {
            ChatBlock.sendMessage(cp, message);
        }

        SimpleClans.getInstance().getServer().sendSystemMessage(Component.literal(ChatFormatting.AQUA + "[" + lang("clan.announce") + ChatFormatting.AQUA + "] " + ChatFormatting.AQUA + "[" + Helper.getColorName(playerName) + ChatFormatting.WHITE + "] " + message));
    }

    /**
     * Announce message to all the leaders of a clan
     */
    public void leaderAnnounce(String msg) {
        String message = SimpleClans.getInstance().getSettingsManager().getColored(CLANCHAT_ANNOUNCEMENT_COLOR) + msg;

        List<ClanPlayer> leaders = getLeaders();

        for (ClanPlayer cp : leaders) {
            ChatBlock.sendMessage(cp, message);
        }
        SimpleClans.getInstance().getServer().sendSystemMessage(Component.literal(ChatFormatting.AQUA + "[" + lang("leader.announce") + ChatFormatting.AQUA + "] " + ChatFormatting.WHITE + message));
    }

    /**
     * Add a new bb message and announce it to all online members of a clan
     */
    public void addBb(String announcerName, String msg) {
        if (isVerified()) {
            addBb(msg);
            clanAnnounce(announcerName, SimpleClans.getInstance().getSettingsManager().getColored(BB_PREFIX) + ChatUtils.parseColors(msg));
        }
    }

    /**
     * Add a new bb message and announce it to all online members of a clan
     */
    public void addBb(String announcerName, String msg, boolean updateLastUsed) {
        if (isVerified()) {
            addBb(msg, updateLastUsed);
            clanAnnounce(announcerName, SimpleClans.getInstance().getSettingsManager().getColored(BB_PREFIX) + ChatUtils.parseColors(msg));
        }
    }

    /**
     * Displays bb to a player
     */
    public void displayBb(Player player) {
        displayBb(player, -1);
    }

    /**
     * Displays bb to a player
     *
     * @param maxSize amount of lines to display
     */
    public void displayBb(Player player, int maxSize) {
        if (!isVerified()) {
            return;
        }

        SettingsManager settings = SimpleClans.getInstance().getSettingsManager();

        ChatBlock.sendBlank(player.createCommandSourceStack());
        plugin.getLogger().info("lade board");
        ChatBlock.saySingle(player.createCommandSourceStack(), lang("bulletin.board.header", player, player.getName().getString()));

        List<String> localBb;
        if (maxSize == -1) {
            localBb = bb;
            maxSize = settings.getInt(BB_SIZE);
        } else {
            localBb = new ArrayList<>(bb);
        }

        while (localBb.size() > maxSize) {
            localBb.remove(0);
        }

        for (String msg : localBb) {
            if (!sendBbTime(player, msg)) {
                String bbPrefix = settings.getColored(BB_PREFIX);
                plugin.getLogger().info("got bb {}", bbPrefix + ChatUtils.parseColors(msg));
                ChatBlock.sendMessage(player.createCommandSourceStack(), bbPrefix + ChatUtils.parseColors(msg));
            }
        }

        ChatBlock.sendBlank(player.createCommandSourceStack());
    }

    /**
     * Sends a bb message with the timestamp in a hover message, if the bb message is timestamped
     *
     * @param msg the bb message
     * @return true if sent
     */
    @SuppressWarnings("deprecation")
    private boolean sendBbTime(Player player, String msg) {
        try {
            int index = msg.indexOf("_");
            if (index < 1) {
                return false;
            }

            String bbPrefix = SimpleClans.getInstance().getSettingsManager().getColored(BB_PREFIX);

            long time = (System.currentTimeMillis() - Long.parseLong(msg.substring(0, index))) / 1000L;
            msg = ChatUtils.parseColors(bbPrefix + msg.substring(++index));

            //BaseComponent[] baseComponent = TextComponent.fromLegacyText(msg);
            //Component textMessage = new Component(baseComponent);
            return true;
        } catch (Exception rock) {
            return false;
        }
    }

    /**
     * Disbands the clan
     *
     * @param sender   who is trying to disband
     * @param announce should it be announced?
     * @param force    should it be force disbanded?
     */
    public void disband(@Nullable CommandSourceStack sender, boolean announce, boolean force) {
        Collection<ClanPlayer> clanPlayers = SimpleClans.getInstance().getClanManager().getAllClanPlayers();
        List<Clan> clans = SimpleClans.getInstance().getClanManager().getClans();

        if (isPermanent() && !force) {
            ChatBlock.sendMessage(sender, ChatFormatting.RED + lang("cannot.disband.permanent", sender));
            return;
        }

        if (announce) {
            if (SimpleClans.getInstance().getSettingsManager().is(DISABLE_MESSAGES) && sender != null) {
                clanAnnounce(sender.getPlayer().getName().getString(), ChatFormatting.AQUA + lang("clan.has.been.disbanded", (ServerPlayer) null, getName().toString()));
            } else {
                SimpleClans.getInstance().getClanManager().serverAnnounce(ChatFormatting.AQUA + lang("clan.has.been.disbanded", (ServerPlayer) null, getName().toString()));
            }
        }

        SimpleClans.getInstance().getPermissionsManager().removeClanPermissions(this);
        for (ClanPlayer cp : clanPlayers) {
            if (cp.getTag().equals(getTag())) {
                cp.setClan(null);
                cp.setJoinDate(0);
                cp.setRank(null);
                if (isVerified()) {
                    cp.addPastClan(getColorTag() + (cp.isLeader() ? ChatFormatting.DARK_RED + "*" : ""));
                }

                cp.setLeader(false);
            }
        }

        NeoForge.EVENT_BUS.post(new DisbandClanEvent(sender, this));
        clans.remove(this);

        for (Clan c : clans) {
            String disbanded = lang("clan.disbanded");

            if (c.removeWarringClan(this)) {
                c.addBb(disbanded, lang("you.are.no.longer.at.war", (ServerPlayer) null, getStringName(), getColorTag()));
            }

            if (c.removeRival(getTag())) {
                c.addBb(disbanded, lang("has.been.disbanded.rivalry.ended", (ServerPlayer) null, getName().toString()));
            }

            if (c.removeAlly(getTag())) {
                c.addBb(disbanded, lang("has.been.disbanded.alliance.ended", (ServerPlayer) null, getName().toString()));
            }
        }

        SimpleClans.getInstance().getRequestManager().removeRequest(getTag());

        SimpleClans.getInstance().runTaskLater(() -> {
            SimpleClans.getInstance().getClanManager().removeClan(getTag());
            SimpleClans.getInstance().getStorageManager().deleteClan(this);
        }, 1);
    }

    public void disband() {
        disband(null, true, false);
    }

    /**
     * Whether this clan can be rivaled
     */
    @Placeholder("is_unrivable")
    public boolean isUnrivable() {
        return SimpleClans.getInstance().getSettingsManager().isUnrivable(getTag());
    }

    /**
     * Returns whether this clan is warring with another clan
     *
     * @param tag the tag of the clan we are at war with
     */
    public boolean isWarring(String tag) {
        return flags.getStringList(WARRING_KEY).contains(tag);
    }

    /**
     * Returns whether this clan is warring with another clan
     *
     * @param clan the clan we are testing against
     */
    public boolean isWarring(Clan clan) {
        return isWarring(clan.getTag());
    }

    /**
     * Add a clan to be at war with
     */
    public void addWarringClan(@Nullable ClanPlayer requestPlayer, Clan targetClan) {
        List<String> warring = flags.getStringList(WARRING_KEY);
        if (!warring.contains(targetClan.getTag())) {
            warring.add(targetClan.getTag());
            flags.put(WARRING_KEY, warring);
            if (requestPlayer != null) {
                addBb(requestPlayer.getName(), lang("you.are.at.war",
                        (ServerPlayer) null, getStringName(), targetClan.getColorTag()));
            }
            SimpleClans.getInstance().getStorageManager().updateClan(this);
        }
    }

    public void addWarringClan(Clan targetClan) {
        addWarringClan(null, targetClan);
    }

    /**
     * Remove a warring clan
     */
    public boolean removeWarringClan(Clan clan) {
        List<String> warring = flags.getStringList(WARRING_KEY);
        if (warring.remove(clan.getTag())) {
            flags.put(WARRING_KEY, warring);
            SimpleClans.getInstance().getStorageManager().updateClan(this);
            return true;
        }

        return false;
    }

    /**
     * Return a collection of all the warring clans
     *
     * @return the clan list
     */
    public List<Clan> getWarringClans() {
        return flags.getStringList(WARRING_KEY).stream().map(tag -> SimpleClans.getInstance().getClanManager()
                .getClan(tag)).collect(Collectors.toList());
    }

    /**
     * Return the list of flags and their data as a json string
     *
     * @return the flags
     */
    public String getFlags() {
        return flags.toJSONString();
    }

    /**
     * Read the list of flags in from a json string
     *
     * @param flagString the flags to set
     */
    public void setFlags(String flagString) {
        flags = new Flags(flagString);
    }

    public void validateWarring() {
        List<String> warring = flags.getStringList(WARRING_KEY);
        Iterator<String> iterator = warring.iterator();
        while (iterator.hasNext()) {
            String clanTag = iterator.next();
            Clan clan = SimpleClans.getInstance().getClanManager().getClan(clanTag);
            if (clan == null) {
                iterator.remove();
            }
        }
        flags.put(WARRING_KEY, warring);
    }

    public void setHomeLocation(@Nullable ServerLevel level, @Nullable Vec3 pos, float yaw, float pitch) {
        if (pos != null && level != null) {
            flags.put("homeX", pos.x);
            flags.put("homeY", pos.y);
            flags.put("homeZ", pos.z);
            flags.put("homePitch", (double) pitch);
            flags.put("homeYaw", (double) yaw);

            // In NeoForge speichert man die Welt als ResourceLocation (z.B. "minecraft:overworld")
            flags.put("homeWorld", level.dimension().location().toString());
        } else {
            flags.put("homeX", 0.0);
            flags.put("homeY", 0.0);
            flags.put("homeZ", 0.0);
            flags.put("homePitch", 0.0);
            flags.put("homeYaw", 0.0);
            flags.put("homeWorld", "");
        }

        String serverName = SimpleClans.getInstance().getProxyManager().getServerName();
        flags.put("homeServer", serverName);

        SimpleClans.getInstance().getStorageManager().updateClan(this);
    }

    public List<UUID> getMemberUUIDs() {
        List<UUID> uuids = new ArrayList<>(List.of());
        for (ClanPlayer cp : members){
            uuids.add(cp.getUniqueId());
        }
        return  uuids;
    }

    public record HomeLocation(ServerLevel level, Vec3 pos, float yaw, float pitch) {}

    public @Nullable HomeLocation getHomeLocation() {
        String homeWorld = (String) flags.getString("homeWorld");

        if (homeWorld == null || homeWorld.isEmpty()) {
            return null;
        }

        // 1. Welt (Dimension) finden
        // In NeoForge/Minecraft sind Welten ResourceKeys (z.B. minecraft:overworld)
        ResourceLocation worldLoc = ResourceLocation.parse(homeWorld);
        ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, worldLoc);

        ServerLevel level = SimpleClans.getInstance().getServer().getLevel(worldKey);
        if (level == null) {
            return null;
        }

        // 2. Koordinaten aus den Flags parsen
        double x = ((Number) flags.getOrDefault("homeX", 0.0)).doubleValue();
        double y = ((Number) flags.getOrDefault("homeY", 0.0)).doubleValue();
        double z = ((Number) flags.getOrDefault("homeZ", 0.0)).doubleValue();
        float yaw = ((Number) flags.getOrDefault("homeYaw", 0.0f)).floatValue();
        float pitch = ((Number) flags.getOrDefault("homePitch", 0.0f)).floatValue();

        return new HomeLocation(level, new Vec3(x, y, z), yaw, pitch);
    }

    public String getTagLabel(boolean isLeader) {
        SettingsManager sm = SimpleClans.getInstance().getSettingsManager();
        String bracketColor = isLeader ? sm.getColored(TAG_BRACKET_LEADER_COLOR) : sm.getColored(TAG_BRACKET_COLOR);
        String bracketDefaultColor = sm.getColored(TAG_DEFAULT_COLOR);
        String bracketLeft = sm.getColored(TAG_BRACKET_LEFT);
        String bracketRight = sm.getColored(TAG_BRACKET_RIGHT);
        String tagSeparatorColor = isLeader ? sm.getColored(TAG_SEPARATOR_LEADER_COLOR) : sm.getColored(TAG_SEPARATOR_COLOR);
        String tagSeparator = sm.getString(TAG_SEPARATOR_CHAR);

        return bracketColor + bracketLeft + bracketDefaultColor + getColorTag() + bracketColor + bracketRight + tagSeparatorColor + tagSeparator;
    }

    /**
     * Checks if the fee is enabled
     *
     * @return true if enabled
     */
    public boolean isMemberFeeEnabled() {
        return feeEnabled;
    }

    /**
     * Enables or disables the fee
     */
    public void setMemberFeeEnabled(boolean enable) {
        feeEnabled = enable;
    }

    /**
     * @return the allowWithdraw
     */
    @Placeholder("allow_withdraw")
    public boolean isAllowWithdraw() {
        return flags.getBoolean("allowWithdraw", false);
    }

    /**
     * @param allowWithdraw the allowWithdraw to set
     */
    public void setAllowWithdraw(boolean allowWithdraw) {
        flags.put("allowWithdraw", allowWithdraw);
    }

    /**
     * @return the allowDeposit
     */
    @Placeholder("allow_deposit")
    public boolean isAllowDeposit() {
        return flags.getBoolean("allowDeposit", true);
    }

    /**
     * @param allowDeposit the allowDeposit to set
     */
    public void setAllowDeposit(boolean allowDeposit) {
        flags.put("allowDeposit", allowDeposit);
    }

    /**
     * Checks if the clan has the specified rank
     *
     * @param name the rank
     */
    public boolean hasRank(@Nullable String name) {
        return getRank(name) != null;
    }

    /**
     * Creates a rank
     */
    public void createRank(String name) {
        Rank rank = new Rank(name);
        ranks.add(rank);
    }

    /**
     * Returns the clan's ranks
     *
     * @return the ranks
     */
    public List<Rank> getRanks() {
        return ranks;
    }

    /**
     * Sets the clan's ranks
     */
    public void setRanks(@Nullable List<Rank> ranks) {
        if (ranks == null) {
            ranks = new ArrayList<>();
        }
        this.ranks = ranks;
    }

    /**
     * Deletes a rank with the specified name
     */
    public void deleteRank(String name) {
        Rank r = getRank(name);
        if (r != null) {
            ranks.remove(r);

            getMembers().forEach(cp -> {
                if (Objects.equals(cp.getRankId(), r.getName())) {
                    cp.setRank("");
                    SimpleClans.getInstance().getStorageManager().updateClanPlayer(cp);
                }
            });
        }
    }

    /**
     * Gets a rank with the specified name or null if not found
     *
     * @param name the rank name
     * @return a rank or null
     */
    public @Nullable Rank getRank(@Nullable String name) {
        if (name != null) {
            for (Rank r : ranks) {
                if (r.getName().equals(name)) {
                    return r;
                }
            }
        }
        return null;
    }

    /**
     * Sets the default rank for this clan.
     *
     * @param name The name of the rank to be set as default
     */
    public void setDefaultRank(@Nullable String name) {
        // I don't know how this could happen, but if it somehow does, here's a check for it
        if (!hasRank(name)) {
            defaultRank = null;
        } else {
            defaultRank = name;
        }
    }

    /**
     * Gets the default rank for this clan.
     *
     * @return The default rank or null if there is no default
     */
    public @Nullable String getDefaultRank() {
        return defaultRank;
    }

    public void setBanner(@Nullable ItemStack banner) {
        if (banner == null || banner.isEmpty()) {
            this.banner = null;
            return;
        }

        // 1. Kopie erstellen (In NeoForge .copy() statt .clone())
        ItemStack bannerCopy = banner.copy();

        // 2. Anzahl auf 1 setzen
        bannerCopy.setCount(1);

        // 3. Flags verstecken (In 1.20.5+ nutzt Minecraft DataComponents)
        // Das Äquivalent zu HIDE_POTION_EFFECTS für Banner ist das Verstecken
        // der "dye_color" oder spezifischer Komponenten.
        // Um alles zu verstecken (wie ItemFlags), nutzt man das HIDE_TOOLTIP Component.
        //bannerCopy.set(DataComponentType<DataComponents.HIDE_TOOLTIP>, true);

        // 4. Lore, DisplayName und andere Daten löschen
        // In NeoForge entfernt man einfach die entsprechenden Komponenten
        bannerCopy.remove(DataComponents.LORE);
        bannerCopy.remove(DataComponents.CUSTOM_NAME);
        bannerCopy.remove(DataComponents.REPAIR_COST); // Optional: Reparaturbosten entfernen

        this.banner = bannerCopy;
    }

    public @Nullable ItemStack getBanner() {
        if (banner != null) {
            return banner.copy();
        }
        return null;
    }
}
