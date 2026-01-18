package org.emil.hnrpmc.simpleclans.commands.completions;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.util.concurrent.CompletableFuture;

public class ClansCompletion extends AbstractSyncCompletion {

    public ClansCompletion(SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "clans";
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context,
                                                        SuggestionsBuilder builder) {
        var tags = clanManager.getClans().stream().map(c -> c.getTag()).toList();
        return suggest(tags, builder);

    }
}
