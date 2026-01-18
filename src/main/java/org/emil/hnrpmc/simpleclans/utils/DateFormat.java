package org.emil.hnrpmc.simpleclans.utils;

import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.DATE_TIME_PATTERN;

public class DateFormat {

    private static final SimpleClans plugin = SimpleClans.getInstance();
    private static SimpleDateFormat format;

    static {
        String pattern = plugin.getSettingsManager().getString(DATE_TIME_PATTERN);
        try {
            format = new SimpleDateFormat(pattern);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warn(String.format("%s is not a valid pattern!", (pattern)));
            format = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
        }
    }

    public static String formatDateTime(long date) {
        return format.format(new Date(date));
    }

}
