package org.emil.hnrpmc.simpleclans.migrations;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;

public abstract class ConfigMigration implements Migration {

    protected final SettingsManager settings;
    // Wir ersetzen FileConfiguration durch das NightConfig Äquivalent
    protected final CommentedFileConfig config;

    public ConfigMigration(SettingsManager settingsManager) {
        this.settings = settingsManager;
        // Wir gehen davon aus, dass dein SettingsManager ein NightConfig-Objekt hält
        this.config = (CommentedFileConfig) settingsManager.getConfig();

        migrate();
        // NightConfig speichert Änderungen oft automatisch oder über save()
        config.save();
    }
}