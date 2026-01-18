package org.emil.hnrpmc.simpleclans.commands.completions;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.util.concurrent.CompletableFuture;

public class DisallowedTagsCompletion extends AbstractSyncCompletion {

    public DisallowedTagsCompletion(SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "disallowed_tags";
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context,
                                                        SuggestionsBuilder builder) {
        var settings = plugin.getSettingsManager();
        var values = settings.getStringList(org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.DISALLOWED_TAGS);
        return suggest(values, builder);

    }
}
