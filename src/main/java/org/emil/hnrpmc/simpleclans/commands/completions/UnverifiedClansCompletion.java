package org.emil.hnrpmc.simpleclans.commands.completions;

import org.emil.hnrpmc.simpleclans.Clan;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.util.concurrent.CompletableFuture;

public class UnverifiedClansCompletion extends AbstractSyncCompletion {

    public UnverifiedClansCompletion(SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "unverified_clans";
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context,
                                                        SuggestionsBuilder builder) {
        var tags = clanManager.getClans().stream()
                .filter(c -> !c.isVerified())
                .map(Clan::getTag)
                .toList();
        return suggest(tags, builder);

    }
}
