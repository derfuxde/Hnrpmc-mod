package org.emil.hnrpmc.simpleclans.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.Hnrpmod;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class ClanSBaseCommand {

    protected final Hnrpmod plugin;

    protected ClanSBaseCommand(Hnrpmod plugin) {
        this.plugin = plugin;
    }

     @Nullable
    public abstract List<String> primarycommand();

    /**
     * Pflicht-Hook: wird nach ACF-Registrierung aufgerufen
     */
    public abstract RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral);
}
