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
import static net.minecraft.ChatFormatting.RED;

@SuppressWarnings("unused")
public class RivableCondition extends AbstractCommandCondition{
    public RivableCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context) throws InvalidCommandArgument {
        Clan clan = Conditions.assertClanMember(clanManager, context.getIssuer());
        if (clan.isUnrivable()) {
            throw new ConditionFailedException(RED + lang("your.clan.cannot.create.rivals", (ServerPlayer) context.getIssuer()));
        }
    }

    @Override
    public @NotNull String getId() {
        return "rivable";
    }
}
