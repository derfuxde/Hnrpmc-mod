package org.emil.hnrpmc.simpleclans.commands.completions;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.util.concurrent.CompletableFuture;

public class ClanMembersCompletion extends AbstractSyncCompletion {

    public ClanMembersCompletion(SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "clan_members";
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context,
                                                        SuggestionsBuilder builder) {
        var source = context.getSource();
        net.minecraft.server.level.ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception ex) {
            return builder.buildFuture();
        }
        var clan = clanManager.getClanByPlayerUniqueId(player.getUUID());
        if (clan == null) {
            return builder.buildFuture();
        }
        var names = clan.getMembers().stream().map(cp -> cp.getName()).toList();
        return suggest(names, builder);

    }
}
