package org.emil.hnrpmc.simpleclans.commands.completions;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class LocalesCompletion extends AbstractSyncCompletion {

    public LocalesCompletion(SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "locales";
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context,
                                                        SuggestionsBuilder builder) {
        var values = org.emil.hnrpmc.simpleclans.language.LanguageResource.getAvailableLocales();
        return suggest(Collections.singleton(values.toString()), builder);

    }
}
