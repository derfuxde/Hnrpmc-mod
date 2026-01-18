package org.emil.hnrpmc.simpleclans.tasks;

import org.emil.hnrpmc.simpleclans.SimpleClans;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.PERFORMANCE_SAVE_INTERVAL;

/**
 * Portiert auf NeoForge unter Verwendung von ScheduledExecutorService.
 */
public class SaveDataTask {
    private final SimpleClans plugin;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public SaveDataTask() {
        this.plugin = SimpleClans.getInstance();
    }

    /**
     * Startet den repetitiven Task (Ersatz für runTaskTimerAsynchronously)
     */
    public void start() {
        // Intervall in Minuten aus den Einstellungen laden
        long interval = plugin.getSettingsManager().getMinutes(PERFORMANCE_SAVE_INTERVAL);

        // Da die Minuten in Ticks umgerechnet wurden (Bukkit),
        // nutzen wir hier direkt TimeUnit.MINUTES für bessere Lesbarkeit.
        scheduler.scheduleAtFixedRate(this::run, interval, interval, TimeUnit.MINUTES);
    }

    /**
     * Stoppt den Task sauber
     */
    public void stop() {
        scheduler.shutdown();
    }

    public void run() {
        try {
            // Führt das Speichern asynchron aus
            plugin.getStorageManager().saveModified();
        } catch (Exception e) {
            plugin.getLogger().error("Fehler beim automatischen Speichern der Clan-Daten!" + e);
        }
    }
}