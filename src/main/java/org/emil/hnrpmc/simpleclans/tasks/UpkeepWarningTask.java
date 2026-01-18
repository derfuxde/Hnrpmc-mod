package org.emil.hnrpmc.simpleclans.tasks;

import org.emil.hnrpmc.simpleclans.Helper;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;

import java.text.MessageFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

/**
 * Portiert auf NeoForge unter Verwendung von ScheduledExecutorService
 */
public class UpkeepWarningTask {
    private final SimpleClans plugin;
    private final SettingsManager sm;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public UpkeepWarningTask() {
        plugin = SimpleClans.getInstance();
        sm = plugin.getSettingsManager();
    }

    /**
     * Startet den repetitiven Task
     */
    public void start() {
        int hour = sm.getInt(TASKS_COLLECT_UPKEEP_WARNING_HOUR);
        int minute = sm.getInt(TASKS_COLLECT_UPKEEP_WARNING_MINUTE);

        // Helper.getDelayTo liefert Sekunden
        long initialDelay = Helper.getDelayTo(hour, minute);
        long period = 86400; // 24 Stunden in Sekunden

        scheduler.scheduleAtFixedRate(this::run, initialDelay, period, TimeUnit.SECONDS);
    }

    /**
     * Stoppt den Scheduler (sollte beim Server-Stop aufgerufen werden)
     */
    public void stop() {
        scheduler.shutdown();
    }

    /**
     * Die eigentliche Logik
     */
    public void run() {
        // Wir nutzen server.execute(), um sicherzustellen, dass die Clan-Operationen
        // thread-sicher auf dem Main-Thread ausgeführt werden
        plugin.getServer().execute(() -> {
            plugin.getClanManager().getClans().forEach((clan) -> {
                if (sm.is(ECONOMY_UPKEEP_REQUIRES_MEMBER_FEE) && !clan.isMemberFeeEnabled()) {
                    return;
                }

                double upkeep = sm.getDouble(ECONOMY_UPKEEP);

                if (sm.is(ECONOMY_MULTIPLY_UPKEEP_BY_CLAN_SIZE)) {
                    upkeep = upkeep * clan.getSize();
                }
            });
        });
    }
}