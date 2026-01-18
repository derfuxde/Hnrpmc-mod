package org.emil.hnrpmc.hnessentials.commands;

import co.aikar.commands.BaseCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.commands.home.HomeCommands;
import org.emil.hnrpmc.hnessentials.commands.tpa.TpaCommands;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;

import java.util.*;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.COMMANDS_CLAN;

@EventBusSubscriber(modid = "hnrpmc")
public final class HNECommandManager {

    private static HNessentials plugin;

    public static void init(HNessentials pluginInstance) {
        plugin = pluginInstance;
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        registercommands(event.getDispatcher());
    }

    static List<String> allcommandstarts = new ArrayList<>();
    public static void registercommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        plugin = HNessentials.getInstance();
        if (plugin == null) return;

        SettingsManager sm = SimpleClans.getInstance().getSettingsManager();

        List<String> aliases = parseAliases("essentials", "essentials");
        //String primarycommand = aliases.get(0);

        List<ClanSBaseCommand> commands = new ArrayList<>();
        commands.add(new TpaCommands(plugin));
        commands.add(new HomeCommands(plugin));

        for (ClanSBaseCommand command : commands) {
            String sprimarycommand = command.primarycommand();
            String commandname = sprimarycommand != null && !Objects.equals(sprimarycommand, "") ? "hnrpmc:" +sprimarycommand : "hnrpmc:essentials";
            CommandNode<CommandSourceStack> rootNode =
                    command.register(dispatcher,  commandname);

            if (allcommandstarts.contains(commandname)) {
                allcommandstarts.add(commandname);
            }

            for (int i = 1; i < aliases.size(); i++) {
                dispatcher.register(Commands.literal(aliases.get(i)).redirect(rootNode));
            }
        }

        plugin.getLogger().info("here all commands {}", allcommandstarts);
    }

    @SubscribeEvent
    public static void onPlayerCommand(CommandEvent event) {
        String command = event.getParseResults().getReader().getString();
        CommandSourceStack source = event.getParseResults().getContext().getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (allcommandstarts.contains(command.split(" ")[0])) {
            refreshPlayerCommands(source.getPlayer());
        }
    }

    public static void refreshPlayerCommands(ServerPlayer player) {
        if (player != null && player.connection != null) {
            plugin.getLogger().info("command reloading for {}", player.getName());
            player.getServer().getCommands().sendCommands(player);
        }else {
            plugin.getLogger().info("failed to load for {}", player.getName());
        }
    }

    private static List<String> parseAliases(String configured, String fallback) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (configured != null) {
            String cleaned = configured.replace(" ", "");
            for (String s : cleaned.split("\\|")) {
                String v = s.trim().toLowerCase(Locale.ROOT);
                if (!v.isEmpty()) set.add(v);
            }
        }
        String fb = fallback.trim().toLowerCase(Locale.ROOT);
        if (set.isEmpty()) set.add(fb);
        else set.add(fb);
        return new ArrayList<>(set);
    }
}

