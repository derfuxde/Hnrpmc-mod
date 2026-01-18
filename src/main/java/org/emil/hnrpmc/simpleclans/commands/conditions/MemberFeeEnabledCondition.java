package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.ECONOMY_MEMBER_FEE_ENABLED;

public class MemberFeeEnabledCondition extends AbstractCommandCondition {
    public MemberFeeEnabledCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context) throws InvalidCommandArgument {
        if (!settingsManager.is(ECONOMY_MEMBER_FEE_ENABLED)) {
            throw new ConditionFailedException(ChatFormatting.RED + lang("disabled.command",
                    (ServerPlayer) context.getIssuer().getIssuer()));
        }
    }

    @Override
    public @NotNull String getId() {
        return "member_fee_enabled";
    }
}
