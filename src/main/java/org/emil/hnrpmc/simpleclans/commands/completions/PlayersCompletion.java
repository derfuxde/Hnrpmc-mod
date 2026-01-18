package org.emil.hnrpmc.simpleclans.commands.completions;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.util.concurrent.CompletableFuture;

public class PlayersCompletion extends AbstractAsyncCompletion {

    public PlayersCompletion(SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "players";
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context,
                                                        SuggestionsBuilder builder) {
        var names = clanManager.getAllClanPlayers().stream().map(cp -> cp.getName()).toList();
        return suggest(names, builder);

    }
}
