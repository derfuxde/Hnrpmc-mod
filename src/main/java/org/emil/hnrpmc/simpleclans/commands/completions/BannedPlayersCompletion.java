package org.emil.hnrpmc.simpleclans.commands.completions;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.util.concurrent.CompletableFuture;

public class BannedPlayersCompletion extends AbstractSyncCompletion {

    public BannedPlayersCompletion(SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "banned_players";
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context,
                                                        SuggestionsBuilder builder) {
        var settings = plugin.getSettingsManager();
        var values = settings.getStringList(org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.BANNED_PLAYERS);
        return suggest(values, builder);

    }
}
