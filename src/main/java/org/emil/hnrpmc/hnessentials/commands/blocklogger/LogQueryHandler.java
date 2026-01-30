package org.emil.hnrpmc.hnessentials.commands.blocklogger;

import net.minecraft.core.BlockPos;
import org.emil.hnrpmc.hnessentials.managers.DatabaseManager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogQueryHandler {

    public static List<DatabaseManager.LogEntry> executeStringQuery(String input, BlockPos playerPos) {
        String timeStr = getValue(input, "tme"); // oder "time"
        String playerFilter = getValue(input, "Spieler");
        String radiusStr = getValue(input, "radius");
        String actionFilter = getValue(input, "aktion");

        int radius = (radiusStr != null) ? Integer.parseInt(radiusStr) : 10; // Default 10 Blöcke
        long hoursAgo = parseTimeToHours(timeStr);

        return DatabaseManager.getFilteredLogs(
                playerPos,
                radius,
                playerFilter,
                actionFilter,
                hoursAgo
        );
    }

    private static String getValue(String input, String key) {
        Pattern pattern = Pattern.compile(key + ":\\s*(\\S+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static long parseTimeToHours(String timeStr) {
        if (timeStr == null) return 0;
        try {
            long val = Long.parseLong(timeStr.replaceAll("[^0-9]", ""));
            if (timeStr.endsWith("d")) return val * 24;
            if (timeStr.endsWith("h")) return val;
            if (timeStr.endsWith("m")) return Math.max(1, val / 60);
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }
}