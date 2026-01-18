package org.emil.hnrpmc.simpleclans.commands.completions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractCompletion implements SCCompletion {

    protected final SimpleClans plugin;
    protected final ClanManager clanManager;

    protected AbstractCompletion(SimpleClans plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
    }

    protected static CompletableFuture<Suggestions> suggest(Collection<String> values,
                                                           SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (String v : values) {
            if (v == null) continue;
            if (remaining.isEmpty() || v.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(v);
            }
        }
        return builder.buildFuture();
    }

    @Override
    public abstract String getId();

    @Override
    public abstract CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context,
                                                                 SuggestionsBuilder builder);
}
