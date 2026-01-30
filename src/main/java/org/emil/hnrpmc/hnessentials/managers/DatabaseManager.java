package org.emil.hnrpmc.hnessentials.managers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.Hnrpmod;
import org.emil.hnrpmc.hnessentials.HNessentials;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private static String URL;
    private Hnrpmod plugin;
    private static File dbFile = null;

    public DatabaseManager(HNessentials plugin) {
        this.plugin = plugin;
        MinecraftServer server = plugin.getServer();
        Path worldRoot = server.getWorldPath(LevelResource.ROOT);
        Path dbPath = worldRoot.resolve("clans").resolve("logger.db");

        try {
            Files.createDirectories(dbPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Konnte DB-Ordner nicht erstellen: " + dbPath.getParent(), e);
        }

        dbFile = dbPath.toFile();
        URL = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        init();
    }

    public static void init() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_name TEXT, player_uuid TEXT, action TEXT, " +
                    "object_name TEXT, x INTEGER, y INTEGER, z INTEGER, " +
                    "dimension TEXT, timestamp LONG)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Speichert eine Aktion asynchron in der Datenbank.
     * @param name Name des Spielers
     * @param uuid UUID des Spielers
     * @param action Die Art der Aktion (z.B. BREAK)
     * @param objectName Was wurde beeinflusst (z.B. minecraft:stone)
     * @param pos Die Position
     * @param dim Die Dimension
     */
    public static void logAction(String name, UUID uuid, String action, String objectName, BlockPos pos, String dim) {
        // Wir nutzen CompletableFuture, damit der Server-Thread nicht auf die Festplatte warten muss
        CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO logs(player_name, player_uuid, action, object_name, x, y, z, dimension, timestamp) VALUES(?,?,?,?,?,?,?,?,?)";

            try (Connection conn = DriverManager.getConnection(URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, name);
                pstmt.setString(2, uuid.toString());
                pstmt.setString(3, action);
                pstmt.setString(4, objectName);
                pstmt.setInt(5, pos.getX());
                pstmt.setInt(6, pos.getY());
                pstmt.setInt(7, pos.getZ());
                pstmt.setString(8, dim);
                pstmt.setLong(9, System.currentTimeMillis());

                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[Logger] Fehler beim Speichern in die DB:");
                e.printStackTrace();
            }
        });
    }

    // --- GETTER FUNKTIONEN ---

    /**
     * Die "Alles-Filter-Methode".
     * Erlaubt die Suche nach Radius, Spieler, Aktion und Zeit gleichzeitig.
     */
    public static List<LogEntry> getFilteredLogs(BlockPos center, int radius, String playerName, String actionType, long hoursAgo) {
        List<LogEntry> results = new ArrayList<>();

        // Basis-Query mit Radius (Bounding Box für Performance)
        StringBuilder sql = new StringBuilder("SELECT * FROM logs WHERE " +
                "x BETWEEN ? AND ? AND y BETWEEN ? AND ? AND z BETWEEN ? AND ?");

        if (playerName != null) sql.append(" AND player_name = ?");
        if (actionType != null) sql.append(" AND action = ?");
        if (hoursAgo > 0) sql.append(" AND timestamp > ?");

        sql.append(" ORDER BY timestamp DESC LIMIT 100"); // Neueste zuerst, max 100

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Radius Parameter
            pstmt.setInt(1, center.getX() - radius);
            pstmt.setInt(2, center.getX() + radius);
            pstmt.setInt(3, center.getY() - radius);
            pstmt.setInt(4, center.getY() + radius);
            pstmt.setInt(5, center.getZ() - radius);
            pstmt.setInt(6, center.getZ() + radius);

            int index = 7;
            if (playerName != null) pstmt.setString(index++, playerName);
            if (actionType != null) pstmt.setString(index++, actionType);
            if (hoursAgo > 0) pstmt.setLong(index++, System.currentTimeMillis() - (hoursAgo * 3600000L));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(new LogEntry(
                        rs.getString("player_name"),
                        rs.getString("action"),
                        rs.getString("object_name"),
                        new BlockPos(rs.getInt("x"), rs.getInt("y"), rs.getInt("z")),
                        rs.getLong("timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Spezial-Getter: Nur Aktionen eines bestimmten Spielers weltweit
     */
    public static List<String> getLogsByPlayer(String name) {
        return getRawStrings("SELECT * FROM logs WHERE player_name = ? ORDER BY timestamp DESC", name);
    }

    /**
     * Spezial-Getter: Alles, was an einer exakten Position passiert ist (z.B. wer hat diese Truhe geklaut?)
     */
    public static List<String> getLogsAtPosition(BlockPos pos) {
        List<String> results = new ArrayList<>();
        String sql = "SELECT * FROM logs WHERE x = ? AND y = ? AND z = ? ORDER BY timestamp DESC";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pos.getX());
            pstmt.setInt(2, pos.getY());
            pstmt.setInt(3, pos.getZ());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(formatRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return results;
    }

    // --- HILFSMETHODEN ---

    private static List<String> getRawStrings(String sql, String param) {
        List<String> results = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, param);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(formatRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return results;
    }

    private static String formatRow(ResultSet rs) throws SQLException {
        long minutesAgo = (System.currentTimeMillis() - rs.getLong("timestamp")) / 60000;
        return String.format("§7[Vor %d min] §b%s §f%s §e%s §7at %d, %d, %d",
                minutesAgo,
                rs.getString("player_name"),
                rs.getString("action"),
                rs.getString("object_name"),
                rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
    }

    // Hilfsklasse für strukturierte Daten
    public record LogEntry(String player, String action, String object, BlockPos pos, long time) {}
}