package org.emil.hnrpmc.hnclaim.storage;

import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import org.emil.hnrpmc.hnclaim.HNClaims;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * @author phaed, portiert auf NeoForge
 */
public interface DBCore {

    HNClaims plugin = HNClaims.getInstance();
    // NeoForge nutzt Log4j Logger
    Logger log = plugin.getLogger();

    Path worldRoot = HNClaims.getInstance().getServer().getWorldPath(LevelResource.ROOT);


    /**
     * @return connection
     */
    static Connection getConnection() {
        return SQLiteCore.getConnection();
    }

    /**
     * @return whether connection can be established
     */
    default boolean checkConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Close connection
     */
    void close();

    /**
     * Execute a select statement
     */
    default @Nullable ResultSet select(String query) {
        try {
            return getConnection().createStatement().executeQuery(query);
        } catch (SQLException ex) {
            log.error("Error executing query: {}" , query , ex);
        }
        return null;
    }

    /**
     * Execute a statement
     */
    default boolean execute(String query) {
        try {
            return getConnection().createStatement().execute(query);
        } catch (SQLException ex) {
            log.error("Error executing query: {}" , query , ex);
            return false;
        }
    }

    /**
     * Check whether a table exists
     */
    default boolean existsTable(String table) {
        try {
            ResultSet tables = getConnection().getMetaData().getTables(null, null, table, null);
            return tables.next();
        } catch (SQLException ex) {
            log.error("Error checking if table {} exists", table, ex);
            return false;
        }
    }

    default Connection getGConnection() {
        return getConnection();
    }

    /**
     * Check whether a column exists
     */
    default boolean existsColumn(String table, String column) {
        try {
            Connection conn = getConnection();
            if (conn == null) return false; // Verhindert die NullPointerException!

            ResultSet col = conn.getMetaData().getColumns(null, null, table, column);
            return col.next();
        } catch (Exception ex) {
            log.error("Error checking if column {} exists in table {}", column, table, ex);
            return false;
        }
    }

    /**
     * Führt ein Update aus (wahlweise asynchron)
     */
    default void executeUpdate(String query) {
        // Exception zur Rückverfolgung bei asynchronen Fehlern
        final Exception callerStack = new Exception("Asynchronous caller stack trace");

        Runnable task = () -> {
            try {
                Connection conn = getConnection();
                if (conn != null) {
                    conn.createStatement().executeUpdate(query);
                }
            } catch (SQLException ex) {
                log.error("Error executing query: {}" + query + ex);
                // In NeoForge gibt es keinen "Primary Thread" Check wie in Bukkit via Bukkit-Klasse.
                // Wir prüfen stattdessen, ob wir auf dem Minecraft-Server-Thread sind.
                if (plugin.getServer() != null && !plugin.getServer().isSameThread()) {
                    log.error("Async context stack trace:" + callerStack);
                }
            }
        };

        if (plugin.getSettingsManager().is(ConfigField.PERFORMANCE_USE_THREADS)) {
            // Der Ersatz für Bukkit's Async Task
            CompletableFuture.runAsync(task);
        } else {
            task.run();
        }
    }
}