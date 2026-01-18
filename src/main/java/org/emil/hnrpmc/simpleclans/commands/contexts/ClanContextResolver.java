package org.emil.hnrpmc.simpleclans.commands.contexts;

import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class ClanContextResolver extends AbstractIssuerOnlyContextResolver<Clan> {
    public ClanContextResolver(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Clan getContext(CommandExecutionContext c) throws InvalidCommandArgument {
        return Contexts.assertClanMember(clanManager, c.getIssuer());
    }

    @Override
    public Class<Clan> getType() {
        return Clan.class;
    }
}
