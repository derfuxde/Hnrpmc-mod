package org.emil.hnrpmc.hnessentials.commands.blocklogger;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.emil.hnrpmc.hnessentials.managers.DatabaseManager;

import java.sql.*;
import java.util.*;

public final class LogQueryHandler {

    public record LogRow(long id, String player, String action, String object, int x, int y, int z, String dimension, long timestamp) {}
    public record LookupResult(List<LogRow> rows, int total) {}

    public record LookupFilter(
            BlockPos center,
            ResourceKey<Level> dimension,
            long timeWindowMs,
            int radius,
            String user,
            Set<String> excludeUsers,
            Set<String> actions,
            Set<String> objContains
    ) {}

    private LogQueryHandler() {}

    public static LookupResult lookup(LookupFilter filter, int limit, int offset) {
        String where = buildWhere(filter);
        List<Object> params = new ArrayList<>();
        buildParams(filter, params);

        String countSql = "SELECT COUNT(*) FROM logs " + where;
        String pageSql = "SELECT id, player_name, action, object_name, x, y, z, dimension, timestamp " +
                "FROM logs " + where + " ORDER BY timestamp DESC LIMIT ? OFFSET ?";

        int total = 0;
        List<LogRow> rows = new ArrayList<>();

        try (Connection con = DatabaseManager.openConnection()) {

            try (PreparedStatement ps = con.prepareStatement(countSql)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) total = rs.getInt(1);
                }
            }

            List<Object> pageParams = new ArrayList<>(params);
            pageParams.add(limit);
            pageParams.add(offset);

            try (PreparedStatement ps = con.prepareStatement(pageSql)) {
                bind(ps, pageParams);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new LogRow(
                                rs.getLong("id"),
                                rs.getString("player_name"),
                                rs.getString("action"),
                                rs.getString("object_name"),
                                rs.getInt("x"),
                                rs.getInt("y"),
                                rs.getInt("z"),
                                rs.getString("dimension"),
                                rs.getLong("timestamp")
                        ));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new LookupResult(rows, total);
    }

    public static List<LogRow> rollbackPreview(LookupFilter filter, int limit) {
        String where = buildWhere(filter);
        List<Object> params = new ArrayList<>();
        buildParams(filter, params);
        params.add(limit);

        String sql = "SELECT id, player_name, action, object_name, x, y, z, dimension, timestamp " +
                "FROM logs " + where + " ORDER BY timestamp DESC LIMIT ?";

        List<LogRow> rows = new ArrayList<>();

        try (Connection con = DatabaseManager.openConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            bind(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new LogRow(
                            rs.getLong("id"),
                            rs.getString("player_name"),
                            rs.getString("action"),
                            rs.getString("object_name"),
                            rs.getInt("x"),
                            rs.getInt("y"),
                            rs.getInt("z"),
                            rs.getString("dimension"),
                            rs.getLong("timestamp")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rows;
    }

    private static String buildWhere(LookupFilter filter) {
        List<String> clauses = new ArrayList<>();

        if (filter.dimension() != null) clauses.add("dimension = ?");
        if (filter.timeWindowMs() > 0) clauses.add("timestamp >= ?");

        if (filter.radius() > 0) {
            clauses.add("( (x - ?) * (x - ?) + (y - ?) * (y - ?) + (z - ?) * (z - ?) ) <= (? * ?)");
        }

        if (filter.user() != null && !filter.user().isBlank()) clauses.add("LOWER(player_name) = LOWER(?)");

        if (filter.excludeUsers() != null && !filter.excludeUsers().isEmpty()) {
            StringBuilder sb = new StringBuilder("LOWER(player_name) NOT IN (");
            for (int i = 0; i < filter.excludeUsers().size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("?");
            }
            sb.append(")");
            clauses.add(sb.toString());
        }

        if (filter.actions() != null && !filter.actions().isEmpty()) {
            StringBuilder sb = new StringBuilder("LOWER(action) IN (");
            for (int i = 0; i < filter.actions().size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("?");
            }
            sb.append(")");
            clauses.add(sb.toString());
        }

        if (filter.objContains() != null && !filter.objContains().isEmpty()) {
            StringBuilder sb = new StringBuilder("(");
            int i = 0;
            for (String ignored : filter.objContains()) {
                if (i++ > 0) sb.append(" OR ");
                sb.append("LOWER(object_name) LIKE ?");
            }
            sb.append(")");
            clauses.add(sb.toString());
        }

        if (clauses.isEmpty()) return "";
        return "WHERE " + String.join(" AND ", clauses);
    }

    private static void buildParams(LookupFilter filter, List<Object> out) {
        if (filter.dimension() != null) out.add(filter.dimension().location().toString());
        if (filter.timeWindowMs() > 0) out.add(System.currentTimeMillis() - filter.timeWindowMs());

        if (filter.radius() > 0) {
            BlockPos c = filter.center();
            out.add(c.getX()); out.add(c.getX());
            out.add(c.getY()); out.add(c.getY());
            out.add(c.getZ()); out.add(c.getZ());
            out.add(filter.radius()); out.add(filter.radius());
        }

        if (filter.user() != null && !filter.user().isBlank()) out.add(filter.user());

        if (filter.excludeUsers() != null && !filter.excludeUsers().isEmpty()) {
            for (String u : filter.excludeUsers()) out.add(u.toLowerCase(Locale.ROOT));
        }

        if (filter.actions() != null && !filter.actions().isEmpty()) {
            for (String a : filter.actions()) out.add(a.toLowerCase(Locale.ROOT));
        }

        if (filter.objContains() != null && !filter.objContains().isEmpty()) {
            for (String s : filter.objContains()) out.add("%" + s.toLowerCase(Locale.ROOT) + "%");
        }
    }

    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
    }
}
