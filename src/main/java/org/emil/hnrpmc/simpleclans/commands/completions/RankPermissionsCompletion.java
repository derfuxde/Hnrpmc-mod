package org.emil.hnrpmc.simpleclans.commands.completions;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.util.concurrent.CompletableFuture;

public class RankPermissionsCompletion extends AbstractSyncCompletion {

    public RankPermissionsCompletion(SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "rank_permissions";
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context,
                                                        SuggestionsBuilder builder) {
        var perms = java.util.Arrays.asList(org.emil.hnrpmc.simpleclans.Helper.fromPermissionArray());
        return suggest(perms, builder);

    }
}
