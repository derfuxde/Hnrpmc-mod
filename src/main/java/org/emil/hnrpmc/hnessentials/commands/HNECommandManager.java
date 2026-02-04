package org.emil.hnrpmc.hnessentials.commands;

import co.aikar.commands.BaseCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.commands.PlayerData.registerAdminGui;
import org.emil.hnrpmc.hnessentials.commands.blocklogger.LoggerCommands;
import org.emil.hnrpmc.hnessentials.commands.common.commonCommands;
import org.emil.hnrpmc.hnessentials.commands.home.*;
import org.emil.hnrpmc.hnessentials.commands.tpa.TpaCommands;
import org.emil.hnrpmc.hnessentials.commands.tpa.TpoCommand;
import org.emil.hnrpmc.hnessentials.commands.warps.WarpCommand;
import org.emil.hnrpmc.hnessentials.commands.warps.delWarpCommand;
import org.emil.hnrpmc.hnessentials.commands.warps.setWarpCommand;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.emil.hnrpmc.world.WorldProtCommands;

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
        commands.add(new homesCommand(plugin));
        commands.add(new delHomeCommand(plugin));
        commands.add(new SetHomeCommand(plugin));
        commands.add(new HomeCommands(plugin));
        commands.add(new registerAdminGui(plugin));
        commands.add(new SpawnCommand(plugin));
        commands.add(new TpoCommand(plugin));
        commands.add(new setWarpCommand(plugin));
        commands.add(new WarpCommand(plugin));
        commands.add(new delWarpCommand(plugin));
        commands.add(new commonCommands(plugin));
        commands.add(new LoggerCommands(plugin));
        commands.add(new WorldProtCommands(plugin));

        for (ClanSBaseCommand command : commands) {
            List<String> sprimarycommands = command.primarycommand();
            for (String sprimarycommand : sprimarycommands) {
                String commandname = sprimarycommand != null && !Objects.equals(sprimarycommand, "") ? sprimarycommand : "essentials";
                String commandname2 = sprimarycommand != null && !Objects.equals(sprimarycommand, "") ? "hnrpmc:" +sprimarycommand : "hnrpmc:essentials";
                CommandNode<CommandSourceStack> rootNode =
                        command.register(dispatcher,  commandname);
                CommandNode<CommandSourceStack> rootNode2 =
                        command.register(dispatcher,  commandname2);

                if (allcommandstarts.contains(commandname)) {
                    allcommandstarts.add(commandname);
                }

                for (int i = 1; i < aliases.size(); i++) {
                    dispatcher.register(Commands.literal(aliases.get(i)).redirect(rootNode));
                    dispatcher.register(Commands.literal(aliases.get(i)).redirect(rootNode2));
                }
            }

        }

        plugin.getLogger().info("here all commands {}", allcommandstarts);
    }

    @SubscribeEvent
    public static void onPlayerCommand(CommandEvent event) {
        String command = event.getParseResults().getReader().getString();
        CommandSourceStack source = event.getParseResults().getContext().getSource();

        if (allcommandstarts.contains(command.split(" ")[0])) {
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                if (command.split(" ")[0].equals("restart")) {
                    return;
                }
                event.setCanceled(true);
                source.sendFailure(Component.literal("Dieser Command kann nur auf einem Client ausgeführt werden"));
                return;
            }
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

