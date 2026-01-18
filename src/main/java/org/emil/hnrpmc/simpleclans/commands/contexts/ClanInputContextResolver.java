package org.emil.hnrpmc.simpleclans.commands.contexts;

import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanInput;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

@SuppressWarnings("unused")
public class ClanInputContextResolver extends AbstractInputOnlyContextResolver<ClanInput> {
    public ClanInputContextResolver(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public ClanInput getContext(CommandExecutionContext context) throws InvalidCommandArgument {
        String arg = context.popFirstArg();
        Clan clan = clanManager.getClan(arg);
        if (clan == null) {
            throw new InvalidCommandArgument(ChatFormatting.RED + lang("the.clan.does.not.exist", (ServerPlayer) context.getIssuer()),
                    false);
        }
        return new ClanInput(clan);
    }

    @Override
    public Class<ClanInput> getType() {
        return ClanInput.class;
    }
}
