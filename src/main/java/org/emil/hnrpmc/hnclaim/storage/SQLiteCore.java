package org.emil.hnrpmc.hnclaim.storage;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.emil.hnrpmc.hnclaim.HNClaims;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class SQLiteCore implements DBCore {
    private static Connection connection;

    private static File dbFile = null;
    private final String jdbcUrl;

    public SQLiteCore(MinecraftServer server) {
        Path worldRoot = server.getWorldPath(LevelResource.ROOT);
        Path dbPath = worldRoot.resolve("clans").resolve("HNClaims.db");

        try {
            Files.createDirectories(dbPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Konnte DB-Ordner nicht erstellen: " + dbPath.getParent(), e);
        }

        dbFile = dbPath.toFile();
        this.jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    public boolean columnExists(String tableName, String columnName) {
        String sql = "PRAGMA table_info(" + tableName + ")";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                if (columnName.equalsIgnoreCase(name)) return true;
            }
        } catch (SQLException e) {
            HNClaims.LOGGER.error("columnExists fehlgeschlagen für {}.{}", tableName, columnName, e);
        }
        return false;
    }


    public static Connection getConnection() {
        try {
            // Dieser Befehl zwingt den ClassLoader, die SQLite-Klasse zu suchen
            Class.forName("org.sqlite.JDBC");

            if (connection == null || connection.isClosed()) {
                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                connection = DriverManager.getConnection(url);
            }
        } catch (ClassNotFoundException e) {
            HNClaims.LOGGER.error("KRITISCH: SQLite-Treiber wurde nicht im Mod-Paket gefunden!");
        } catch (SQLException e) {
            HNClaims.LOGGER.error("SQL-Fehler beim Verbindungsaufbau!", e);
        }
        return connection;
    }


    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            HNClaims.LOGGER.error("Fehler beim Schließen der Datenbankverbindung:", e);
        }
    }
}