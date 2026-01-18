package org.emil.hnrpmc.simpleclans.commands.completions;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;

public interface SCCompletion extends SuggestionProvider<CommandSourceStack> {
    String getId();
}
