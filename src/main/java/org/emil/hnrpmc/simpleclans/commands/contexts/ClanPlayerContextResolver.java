package org.emil.hnrpmc.simpleclans.commands.contexts;

import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class ClanPlayerContextResolver extends AbstractIssuerOnlyContextResolver<ClanPlayer> {
    public ClanPlayerContextResolver(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public ClanPlayer getContext(CommandExecutionContext context) throws InvalidCommandArgument {
        Player player = Contexts.assertPlayer(context.getIssuer());
        return clanManager.getCreateClanPlayer(player.getUUID());
    }

    @Override
    public Class<ClanPlayer> getType() {
        return ClanPlayer.class;
    }
}
