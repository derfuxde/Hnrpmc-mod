package org.emil.hnrpmc.hnclaim.managers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.emil.hnrpmc.hnclaim.Claim;
import org.emil.hnrpmc.hnclaim.HNClaims;
import org.emil.hnrpmc.hnclaim.Helper;
import org.emil.hnrpmc.hnclaim.storage.DBCore;
import org.emil.hnrpmc.hnclaim.storage.MySQLCore;
import org.emil.hnrpmc.hnclaim.storage.SQLiteCore;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.Date;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

/**
 * @author phaed
 */
public final class StorageManager {

    private final HNClaims plugin;
    private DBCore core;
    //private final HashMap<String, ChatBlock> chatBlocks = new HashMap<>();
    private final Set<Claim> modifiedClaimss = new HashSet<>();

    /**
     *
     */
    public StorageManager(DBCore dbcore) {
        plugin = HNClaims.getInstance();
        this.core = dbcore;
        initiateDB();
        updateDatabase();
        importFromDatabase();
    }

    /**
     * Initiates the db
     */
    public void initiateDB() {
        SettingsManager settings = SimpleClans.getInstance().getSettingsManager();
        if (settings.is(MYSQL_ENABLE)) {
            core = new MySQLCore(settings.getString(MYSQL_HOST), settings.getString(MYSQL_DATABASE), settings.getInt(MYSQL_PORT), settings.getString(MYSQL_USERNAME), settings.getString(MYSQL_PASSWORD));

            if (core.checkConnection()) {
                plugin.getLogger().info(lang("mysql.connection.successful"));

                if (!core.existsTable(getPrefixedTable("claims"))) {
                    plugin.getLogger().info("Creating table: " + getPrefixedTable("claims"));

                    String query = "CREATE TABLE IF NOT EXISTS `" + getPrefixedTable("claims") + "` ("
                            + " `id` bigint(20) NOT NULL auto_increment,"
                            + " `name` varchar(64) NOT NULL,"
                            + " `ownerUUID` varchar(36) NOT NULL,"
                            + " `corner1` varchar(255) NOT NULL,"
                            + " `corner2` varchar(255) NOT NULL,"
                            + " `corner3` varchar(255) NOT NULL,"
                            + " `corner4` varchar(255) NOT NULL,"
                            + " `perms` text,"
                            + " `permsoverride` text,"
                            + " `center` varchar(255) NOT NULL,"
                            + " `banPlayers` varchar(255),"
                            + " `dimension` varchar(64) NOT NULL,"
                            + " `founded` bigint NOT NULL,"
                            + " `last_used` bigint,"
                            + " PRIMARY KEY (`id`),"
                            + " UNIQUE KEY `uq_hnclaims_1` (`name`));";
                    core.execute(query);
                }
            } else {
                plugin.getServer().sendSystemMessage(Component.literal("[SimpleClans] " + ChatFormatting.RED + SimpleClans.lang("mysql.connection.failed")));
            }
        } else {
            core = new SQLiteCore(plugin.getServer());

            if (core.checkConnection()) {

                plugin.getLogger().info(lang("sqlite.connection.successful"));

                if (!core.existsTable(getPrefixedTable("claims"))) {
                    plugin.getLogger().info("Creating table: " + getPrefixedTable("claims"));

                    String query = "CREATE TABLE IF NOT EXISTS `" + getPrefixedTable("claims") + "` ("
                            + " `id` bigint(20),"
                            + " `name` varchar(64) NOT NULL,"
                            + " `ownerUUID` varchar(36) NOT NULL,"
                            + " `corner1` varchar(255) NOT NULL,"
                            + " `corner2` varchar(255) NOT NULL,"
                            + " `corner3` varchar(255) NOT NULL,"
                            + " `corner4` varchar(255) NOT NULL,"
                            + " `perms` text,"
                            + " `permsoverride` text,"
                            + " `center` varchar(255) NOT NULL,"
                            + " `banPlayers` varchar(255),"
                            + " `dimension` varchar(64) NOT NULL,"
                            + " `founded` bigint NOT NULL,"
                            + " `last_used` bigint NOT NULL,"
                            + " PRIMARY KEY (`id`),"
                            + " UNIQUE (`name`));";
                    core.execute(query);
                }

            } else {
                plugin.getServer().sendSystemMessage(Component.literal("[SimpleClans] " + ChatFormatting.RED + SimpleClans.lang("sqlite.connection.failed")));
            }
        }
    }

    /**
     * Closes DB connection
     */
    public void closeConnection() {
        core.close();
    }

    /**
     * Import all data from database to memory
     */
    public void importFromDatabase() {
        plugin.getClaimManager().cleanData();

        List<Claim> claims = retrieveClaims();

        for (Claim claim : claims) {
            plugin.getClaimManager().importClan(claim);
            plugin.getLogger().debug("claim {} regestriert", claim.getName());
        }

        if (!claims.isEmpty()) {
            plugin.getLogger().info(MessageFormat.format(lang("clans"), claims.size()));
        }
    }

    /**
     * Retrieves all simple clans from the database
     *
     */
    public List<Claim> retrieveClaims() {
        List<Claim> out = new ArrayList<>();

        String query = "SELECT * FROM `" + getPrefixedTable("claims") + "`;";
        ResultSet res = core.select(query);

        if (res != null) {
            try {
                while (res.next()) {
                    try {
                        String name = res.getString("name");
                        String ownerUUID = res.getString("ownerUUID");
                        String corner1 = res.getString("corner1");
                        String corner2 = res.getString("corner2");
                        String corner3 = res.getString("corner3");
                        String corner4 = res.getString("corner4");
                        String perms = res.getString("perms");
                        String permsoverride = res.getString("permsoverride");
                        String center = res.getString("center");
                        String dimension = res.getString("dimension");
                        String banPlayers = res.getString("banPlayers");
                        long founded = res.getLong("founded");
                        long last_used = res.getLong("last_used");

                        if (founded == 0) {
                            founded = (new Date()).getTime();
                        }

                        if (last_used == 0) {
                            last_used = (new Date()).getTime();
                        }

                        Claim claim = new Claim();
                        claim.setName(name);
                        claim.setOwnerUUID(UUID.fromString(ownerUUID));
                        claim.setCorner1(corner1);
                        claim.setCorner2(corner2);
                        claim.setCorner3(corner3);
                        claim.setCorner4(corner4);
                        claim.setPerms(perms);
                        claim.setoverridePerms(permsoverride);
                        claim.setCenter(center);
                        claim.setDimension(dimension);
                        claim.setCreateDate(founded);
                        claim.setLast_used(last_used);
                        claim.setStringBanPlayers(banPlayers);

                        out.add(claim);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (SQLException ex) {
                plugin.getLogger().error(String.format("An Error occurred: %s", ex.getErrorCode()));
                plugin.getLogger().error(null, ex);
            }
        }

        return out;
    }

    /**
     * Retrieves one Clan from the database
     * Used for BungeeCord Reload ClanPlayer and your Clan
     */
    public @Nullable Claim retrieveOneClan(String tagClan) {
        Claim out = null;

        String query = "SELECT * FROM  `" + getPrefixedTable("clans") + "` WHERE `tag` = '" + tagClan + "';";
        ResultSet res = core.select(query);

        if (res != null) {
            try {
                while (res.next()) {
                    try {
                        String name = res.getString("name");
                        String ownerUUID = res.getString("ownerUUID");
                        String corner1 = res.getString("corner1");
                        String corner2 = res.getString("corner2");
                        String corner3 = res.getString("corner3");
                        String corner4 = res.getString("corner4");
                        String perms = res.getString("perms");
                        String permsoverride = res.getString("permsoverride");
                        String center = res.getString("center");
                        String dimension = res.getString("dimension");
                        String banPlayers = res.getString("banPlayers");
                        long founded = res.getLong("founded");
                        long last_used = res.getLong("last_used");

                        if (founded == 0) {
                            founded = (new Date()).getTime();
                        }

                        if (last_used == 0) {
                            last_used = (new Date()).getTime();
                        }

                        Claim claim = new Claim();
                        claim.setName(name);
                        claim.setOwnerUUID(UUID.fromString(ownerUUID));
                        claim.setCorner1(corner1);
                        claim.setCorner2(corner2);
                        claim.setCorner3(corner3);
                        claim.setCorner4(corner4);
                        claim.setPerms(perms);
                        claim.setoverridePerms(permsoverride);
                        claim.setCenter(center);
                        claim.setDimension(dimension);
                        claim.setCreateDate(founded);
                        claim.setLast_used(last_used);
                        claim.setStringBanPlayers(banPlayers);

                        out = claim;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (SQLException ex) {
                plugin.getLogger().error(String.format("An Error occurred: %s", ex.getErrorCode()));
                plugin.getLogger().error(null, ex);
            }
        }

        return out;
    }


    /**
     * Insert a clan into the database
     *
     */
    public void insertClan(Claim claim) {
        //plugin.getProxyManager().sendUpdate(claim);

        String query = "INSERT INTO `" + getPrefixedTable("claims") + "` (`name`, `ownerUUID`, `corner1`, `corner2`, `corner3`, `corner4`, `perms`," +
                " `permsoverride`, `center`, `dimension`, `banPlayers`, `founded`, `last_used`) ";
        claim.getoverrideStringPerms();
        claim.getStringPerms();
        String values = "VALUES ( '"
                + Helper.escapeQuotes(claim.getName())+ "','"
                + Helper.escapeQuotes(claim.getownerUUID().toString()) +"','"
                + Helper.escapeQuotes(claim.getCorner1()) + "','"
                + Helper.escapeQuotes(claim.getCorner2()) + "','"
                + Helper.escapeQuotes(claim.getCorner3()) + "','"
                + Helper.escapeQuotes(claim.getCorner4()) + "','"
                + Helper.escapeQuotes(claim.getStringPerms().toString()) + "','"
                + Helper.escapeQuotes(claim.getoverrideStringPerms()) + "','"
                + Helper.escapeQuotes(claim.getCenter()) + "','"
                + Helper.escapeQuotes(claim.getDimension()) + "','"
                + Helper.escapeQuotes(claim.getBanPlayers() != null ? claim.getBanPlayers().toString() : null) + "','"
                + claim.getCreateDate() + "','"
                + claim.getLast_used() + "');";
        core.executeUpdate(query + values);
    }

    /**
     * Update a clan to the database asynchronously
     *
     */
    @Deprecated
    public void updateClanAsync(final Claim claim) {
        new Thread(() -> {
            updateClaim(claim);
        }, "SimpleClans-AsyncUpdate-" + claim.getName()).start();
    }

    /**
     * Update a clan to the database
     *
     */
    public void updateClaim(Claim claim) {
        updateClaim(claim, true);
    }

    public void updateAllClaim() {
        for (Claim claim : plugin.getClaimManager().getClaims()) {
            updateClaim(claim, false);
            if (Objects.equals(claim.getName(), "Dev_claim-8")) {
                plugin.getLogger().debug("Dev-8 ist da");
            }
        }
    }

    /**
     * Update a clan to the database
     *
     * @param claim clan to update
     *
     * @param updateLastUsed should the clan's last used time be updated as well?
     */
    public void updateClaim(Claim claim, boolean updateLastUsed) {
        if (updateLastUsed) {
            claim.setLast_used(new Date().getTime());
        }
        plugin.getProxyManager().sendUpdate(claim);
        if (SimpleClans.getInstance().getSettingsManager().is(PERFORMANCE_SAVE_PERIODICALLY)) {
            modifiedClaimss.add(claim);
            return;
        }
        try (PreparedStatement st = prepareUpdateClaimStatement(core.getGConnection())) {
            setValues(st, claim);
            st.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().error(String.format("Error updating Clan %s", claim.getName()), ex);
        }
    }

    private PreparedStatement prepareUpdateClaimStatement(Connection connection) throws SQLException {
        String sql = "UPDATE `" + getPrefixedTable("claims") + "` SET name = ?, ownerUUID = ?, corner1 = ?, corner2 = ?, corner3 = ?, " +
                "corner4 = ?, perms = ?, permsoverride = ?, `center` = ?, dimension = ?, banPlayers = ?, founded = ?, " +
                "last_used = ? WHERE name = ?;";
        return connection.prepareStatement(sql);
    }


    private void setValues(PreparedStatement statement, Claim claim) throws SQLException {

        // 1 name
        statement.setString(1, claim.getName());

        // 2 ownerUUID
        statement.setString(2, claim.getownerUUID().toString());

        // 3–6 corners
        statement.setString(3, claim.getCorner1());
        statement.setString(4, claim.getCorner2());
        statement.setString(5, claim.getCorner3());
        statement.setString(6, claim.getCorner4());

        // 7 perms (Enum List -> String)
        String perms = String.join(", ", claim.getStringPerms());
        statement.setString(7, perms);

        // 8 permsoverride (Map -> String)
        statement.setString(8, claim.getoverrideStringPerms());

        // 9 center
        statement.setString(9, claim.getCenter());

        // 10 dimension
        statement.setString(10, claim.getDimension());

        // 11 banned players (UUID list -> String)
        String banned = String.join(", ", claim.getStringBanPlayers());
        statement.setString(11, banned);

        // 12 founded
        statement.setLong(12, claim.getCreateDate());

        // 13 last_used
        statement.setLong(13, claim.getLast_used());

        // 14 SET name
        statement.setString(14, claim.getName());
    }


    /**
     * Delete a clan from the database
     */
    public void deleteClaim(Claim claim) {
        String query = "DELETE FROM `" + getPrefixedTable("claims") + "` WHERE name = '" + claim.getName() + "';";
        core.executeUpdate(query);
    }

    /**
     * Callback that returns some data
     *
     * @author roinujnosde
     *
     */
    public interface DataCallback<T> {
        /**
         * Notifies when the result is ready
         *
         */
        void onResultReady(T data);
    }

    /**
     * Updates the database to the latest version
     *
     */
    private void updateDatabase() {
        String table = getPrefixedTable("claims");
        String query;

        /*
         * Initial claim fields
         */
        if (!core.existsColumn(table, "name")) {
            core.execute("ALTER TABLE `" + table + "` ADD COLUMN `name` VARCHAR(255);");
        }

        if (!core.existsColumn(table, "ownerUUID")) {
            core.execute("ALTER TABLE `" + table + "` ADD COLUMN `ownerUUID` VARCHAR(36);");
        }

        /*
         * Corners
         */
        if (!core.existsColumn(table, "corner1")) {
            core.execute("ALTER TABLE `" + table + "` ADD COLUMN `corner1` TEXT;");
        }
        if (!core.existsColumn(table, "corner2")) {
            core.execute("ALTER TABLE `" + table + "` ADD COLUMN `corner2` TEXT;");
        }
        if (!core.existsColumn(table, "corner3")) {
            core.execute("ALTER TABLE `" + table + "` ADD COLUMN `corner3` TEXT;");
        }
        if (!core.existsColumn(table, "corner4")) {
            core.execute("ALTER TABLE `" + table + "` ADD COLUMN `corner4` TEXT;");
        }

        /*
         * Permissions
         */
        if (!core.existsColumn(table, "perms")) {
            core.execute("ALTER TABLE `" + table + "` ADD COLUMN `perms` TEXT;");
        }

        if (!core.existsColumn(table, "permsoverride")) {
            core.execute("ALTER TABLE `" + table + "` ADD COLUMN `permsoverride` TEXT;");
        }

        /*
         * Position & world
         */
        if (!core.existsColumn(table, "center")) {
            core.execute("ALTER TABLE `" + table + "` ADD COLUMN `center` TEXT;");
        }

        if (!core.existsColumn(table, "dimension")) {
            core.execute("ALTER TABLE `" + table + "` ADD COLUMN `dimension` VARCHAR(255);");
        }

        /*
         * Ban list
         */
        if (!core.existsColumn(table, "banPlayers")) {
            core.execute("ALTER TABLE `" + table + "` ADD COLUMN `banPlayers` TEXT;");
        }

        /*
         * Timestamps
         */
        if (!core.existsColumn(table, "founded")) {
            core.execute("ALTER TABLE `" + table + "` ADD COLUMN `founded` BIGINT;");
        }

        if (!core.existsColumn(table, "last_used")) {
            core.execute("ALTER TABLE `" + table + "` ADD COLUMN `last_used` BIGINT;");
        }

        /*
         * Optional: index for faster lookups
         */
        if (SimpleClans.getInstance().getSettingsManager().is(MYSQL_ENABLE)) {
            core.execute(
                    "CREATE INDEX IF NOT EXISTS `idx_claim_name_dimension` " +
                            "ON `" + table + "` (`name`, `dimension`);"
            );
        }
    }

    /**
     * Updates the database to the latest version
     *
     */

    private void logSuccess(int current, int total, String playerName, UUID uuid) {
        plugin.getLogger().info(String.format("[%d / %d] Success: %s; UUID: %s", current, total, playerName, uuid));
    }

    private void logFailure(int current, int total, String playerName, Exception ex) {
        plugin.getLogger().warn( String.format("[%d / %d] Failed [ERROR]: %s; UUID: ???", current, total, playerName), ex);
    }

    private void logMigrationStart() {
        plugin.getLogger().warn("Starting Migration to UUID Players!");
        plugin.getLogger().warn( "==================== ATTENTION DON'T STOP BUKKIT! ====================");
        plugin.getLogger().warn( "==================== ATTENTION DON'T STOP BUKKIT! ====================");
        plugin.getLogger().warn( "==================== ATTENTION DON'T STOP BUKKIT! ====================");
    }

    private void logMigrationEnd(int totalPlayers) {
        plugin.getLogger().warn( "==================== END OF MIGRATION ====================");
        plugin.getLogger().warn( "==================== END OF MIGRATION ====================");
        plugin.getLogger().warn( "==================== END OF MIGRATION ====================");

        if (totalPlayers > 0) {
            plugin.getLogger().info(MessageFormat.format(lang("clan.players"), totalPlayers));
        }
    }

    private String getPrefixedTable(String name) {
        return "hn_" + name;
    }

    /**
     * Saves modified Clans and ClanPlayers to the database
     * @since 2.10.2
     *
     * <p>
     * author: RoinujNosde
     * </p>
     */
    public void saveModified() {
        try (PreparedStatement pst = prepareUpdateClaimStatement(core.getGConnection())) {
            //removing disbanded clans
            modifiedClaimss.retainAll(plugin.getClaimManager().getClaims());
            for (Claim claim : modifiedClaimss) {
                setValues(pst, claim);
                pst.addBatch();
            }
            pst.executeBatch();

            modifiedClaimss.clear();
        } catch (SQLException ex) {
            plugin.getLogger().error("Error saving modified Clans:", ex);
        }
    }
}
