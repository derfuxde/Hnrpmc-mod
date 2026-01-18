package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

@SuppressWarnings("unused")
public class CanChangeFeeCondition extends AbstractCommandCondition {

    private static final float HOUR_IN_MINUTES = 60;

    public CanChangeFeeCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context) throws InvalidCommandArgument {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime collectTime = getCollectTime();
        long interval = now.until(collectTime, MINUTES);
        if (interval <= settingsManager.getInt(ECONOMY_MEMBER_FEE_LAST_MINUTE_CHANGE_INTERVAL) * HOUR_IN_MINUTES) {
            CommandIssuer issuer = context.getIssuer();
            String error = lang("cannot.change.member.fee.now", (ServerPlayer) issuer);
            if (interval <= 60) {
                error += lang("cannot.change.member.fee.minutes", (ServerPlayer) issuer, interval);
            } else {
                error += lang("cannot.change.member.fee.hours", (ServerPlayer) issuer, Math.ceil(interval / HOUR_IN_MINUTES));
            }
            throw new ConditionFailedException(error);
        }
    }

    private LocalDateTime getCollectTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime collectTime = now.withHour(settingsManager.getInt(TASKS_COLLECT_FEE_HOUR))
                .withMinute(settingsManager.getInt(TASKS_COLLECT_FEE_MINUTE));
        if (collectTime.isBefore(now)) {
            collectTime = collectTime.plusDays(1);
        }
        return collectTime;
    }

    @Override
    public @NotNull String getId() {
        return "change_fee";
    }
}
