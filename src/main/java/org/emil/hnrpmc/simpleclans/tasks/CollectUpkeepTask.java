package org.emil.hnrpmc.simpleclans.tasks;

import org.emil.hnrpmc.simpleclans.EconomyResponse;
import org.emil.hnrpmc.simpleclans.Helper;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.events.ClanBalanceUpdateEvent;
import org.emil.hnrpmc.simpleclans.loggers.BankOperator;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;

import java.text.MessageFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public class CollectUpkeepTask {
    private final SimpleClans plugin;
    private final SettingsManager settingsManager;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public CollectUpkeepTask() {
        this.plugin = SimpleClans.getInstance();
        this.settingsManager = plugin.getSettingsManager();
    }

    public void start() {
        int hour = settingsManager.getInt(TASKS_COLLECT_UPKEEP_HOUR);
        int minute = settingsManager.getInt(TASKS_COLLECT_UPKEEP_MINUTE);

        // Helper liefert Sekunden
        long initialDelay = Helper.getDelayTo(hour, minute);
        long period = 86400; // 24 Stunden in Sekunden

        scheduler.scheduleAtFixedRate(this::run, initialDelay, period, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    public void run() {
        // Wir wechseln auf den Main-Thread, da disband() und BB-Nachrichten nicht thread-sicher sind
        plugin.getServer().execute(() -> {
            plugin.getClanManager().getClans().forEach((clan) -> {
                if (settingsManager.is(ECONOMY_UPKEEP_REQUIRES_MEMBER_FEE) && !clan.isMemberFeeEnabled()) {
                    return;
                }

                double upkeep = settingsManager.getDouble(ECONOMY_UPKEEP);
                if (settingsManager.is(ECONOMY_MULTIPLY_UPKEEP_BY_CLAN_SIZE)) {
                    upkeep = upkeep * clan.getSize();
                }
            });
        });
    }
}