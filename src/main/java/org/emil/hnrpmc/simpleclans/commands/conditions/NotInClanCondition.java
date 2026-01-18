package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.*;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanPlayerInput;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.debug;
import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static net.minecraft.ChatFormatting.RED;

@SuppressWarnings("unused")
public class NotInClanCondition extends AbstractParameterCondition<ClanPlayerInput> {

    public NotInClanCondition(@NotNull SimpleClans plugin) {
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
        ClanPlayer clanPlayer = value.getClanPlayer();
        debug(String.format("NotInClanCondition -> %s %s", clanPlayer.getName(), clanPlayer.getUniqueId()));
        if (clanPlayer.getClan() != null) {
            throw new ConditionFailedException(RED + lang("the.player.is.already.member.of.another.clan",
                    (ServerPlayer) execContext.getIssuer()));
        }
    }

    @Override
    public @NotNull String getId() {
        return "not_in_clan";
    }
}
