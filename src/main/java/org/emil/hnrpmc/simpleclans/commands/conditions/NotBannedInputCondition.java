package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.*;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanPlayerInput;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static net.minecraft.ChatFormatting.RED;

@SuppressWarnings("unused")
public class NotBannedInputCondition extends AbstractParameterCondition<ClanPlayerInput> {

    public NotBannedInputCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Class<ClanPlayerInput> getType() {
        return ClanPlayerInput.class;
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context,
                                  CommandExecutionContext execContext, ClanPlayerInput value)
            throws InvalidCommandArgument {
        UUID uniqueId = value.getClanPlayer().getUniqueId();
        if (settingsManager.isBanned(uniqueId)) {
            throw new ConditionFailedException(RED + lang("this.player.is.banned.from.using.clan.commands",
                    (ServerPlayer) execContext.getIssuer()));
        }
    }

    @Override
    public @NotNull String getId() {
        return "not_banned";
    }
}
