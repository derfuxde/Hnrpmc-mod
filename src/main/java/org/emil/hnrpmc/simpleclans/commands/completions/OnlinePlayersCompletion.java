package org.emil.hnrpmc.simpleclans.commands.completions;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.util.concurrent.CompletableFuture;

public class OnlinePlayersCompletion extends AbstractSyncCompletion {

    public OnlinePlayersCompletion(SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "online_players";
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context,
                                                        SuggestionsBuilder builder) {
        var source = context.getSource();
        var server = source.getServer();
        var names = server.getPlayerList().getPlayers().stream()
                .map(p -> p.getGameProfile().getName())
                .toList();
        return suggest(names, builder);

    }
}
