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
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.CLAN_MIN_SIZE_TO_SET_RIVAL;
import static net.minecraft.ChatFormatting.RED;

@SuppressWarnings("unused")
public class MinimumToRivalCondition extends AbstractCommandCondition {
    public MinimumToRivalCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context) throws InvalidCommandArgument {
        CommandIssuer issuer = context.getIssuer();
        Clan clan = Conditions.assertClanMember(clanManager, issuer);
        if (clan.getSize() < settingsManager.getInt(CLAN_MIN_SIZE_TO_SET_RIVAL)) {
            throw new ConditionFailedException(RED + lang("min.players.rivalries", (ServerPlayer) issuer,
                    settingsManager.getInt(CLAN_MIN_SIZE_TO_SET_RIVAL)));
        }
    }

    @Override
    public @NotNull String getId() {
        return "minimum_to_rival";
    }
}
