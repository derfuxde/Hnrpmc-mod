package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.CLAN_MIN_SIZE_TO_SET_ALLY;
import static net.minecraft.ChatFormatting.RED;

@SuppressWarnings("unused")
public class MinimumToAllyCondition extends AbstractCommandCondition {
    public MinimumToAllyCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context) throws InvalidCommandArgument {
        Clan clan = Conditions.assertClanMember(clanManager, context.getIssuer());
        if (clan.getSize() < settingsManager.getInt(CLAN_MIN_SIZE_TO_SET_ALLY)) {
            throw new ConditionFailedException(RED +
                    lang("minimum.to.make.alliance", (ServerPlayer) context.getIssuer(), settingsManager.getInt(CLAN_MIN_SIZE_TO_SET_ALLY)));
        }
    }

    @Override
    public @NotNull String getId() {
        return "minimum_to_ally";
    }
}
