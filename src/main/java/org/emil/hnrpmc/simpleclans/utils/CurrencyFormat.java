package org.emil.hnrpmc.simpleclans.utils;

import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.PermissionsManager;

import java.text.NumberFormat;

public class CurrencyFormat {

    private static final SimpleClans plugin = SimpleClans.getInstance();
    private static final NumberFormat fallbackFormat = NumberFormat.getCurrencyInstance();

    public static String format(double value) {
        PermissionsManager permissionsManager = plugin.getPermissionsManager();
        if (permissionsManager.hasEconomy()) {
            return permissionsManager.format(value);
        }
        return fallbackFormat.format(value);
    }

}
