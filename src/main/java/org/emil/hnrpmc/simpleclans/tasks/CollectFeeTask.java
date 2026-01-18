package org.emil.hnrpmc.simpleclans.tasks;

import com.mojang.authlib.GameProfile;
import org.emil.hnrpmc.simpleclans.*;
import org.emil.hnrpmc.simpleclans.loggers.BankOperator;
import org.emil.hnrpmc.simpleclans.managers.PermissionsManager;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.events.ClanBalanceUpdateEvent.Cause;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.TASKS_COLLECT_FEE_HOUR;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.TASKS_COLLECT_FEE_MINUTE;

/**
 *
 * @author roinujnosde
 */
public class CollectFeeTask {
	private final SimpleClans plugin;

    private final SettingsManager sm;
    private final ScheduledExecutorService scheduler;
    
	public CollectFeeTask() {
		plugin = SimpleClans.getInstance();
        sm = plugin.getSettingsManager();
        scheduler = plugin.getScheduler();
	}
	
    /**
     * Starts the repetitive task
     */
    public void start() {
        // Holen der konfigurierten Zeit aus den Settings (z.B. 00:00 Uhr)
        int hour = sm.getInt(TASKS_COLLECT_FEE_HOUR);
        int minute = sm.getInt(TASKS_COLLECT_FEE_MINUTE);

        long initialDelay = Helper.getDelayTo(hour, minute);
        long period = 86400; // 24 Stunden in Sekunden

        scheduler.scheduleAtFixedRate(this::run, initialDelay, period, TimeUnit.SECONDS);
    }
    
    /**
     * (used internally)
     */
    public void run() {
        PermissionsManager pm = plugin.getPermissionsManager();
        for (Clan clan : plugin.getClanManager().getClans()) {
            final double memberFee = clan.getMemberFee();
            if (!clan.isMemberFeeEnabled() || memberFee <= 0) {
                continue;
            }

            for (ClanPlayer cp : clan.getFeePayers()) {
                GameProfile player = SimpleClans.getInstance().getServer().getProfileCache().get(cp.getUniqueId()).get();
                boolean success = pm.chargePlayer(player.getId(), memberFee, null);
                if (success) {
                    plugin.getStorageManager().updateClan(clan);
                } else {
                    clan.removePlayerFromClan(cp.getUniqueId());
                    clan.addBb(lang("bb.fee.player.kicked", cp.getUniqueId()));
                }
            }
        } 
    }
}
