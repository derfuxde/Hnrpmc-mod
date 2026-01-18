package org.emil.hnrpmc.hnessentials.commands.tpa;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BannerItem;
import net.neoforged.neoforge.common.NeoForge;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.Tpa;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.clan.Suggestions;
import org.emil.hnrpmc.simpleclans.managers.*;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public final class TpaCommands extends ClanSBaseCommand {

    private final HNessentials plugin;
    private StorageManager storage;

    public TpaCommands(HNessentials plugin) {
        super(SimpleClans.getInstance());
        this.plugin = plugin;
    }

    @Override
    public @Nullable String primarycommand() {
        return "";
    }

    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(tpa("tpa"));
        dispatcher.register(tpahere("tpahere"));
        dispatcher.register(tpaccept("tpaccept"));

        dispatcher.register(tpa("hnrpmc:tpa"));
        dispatcher.register(tpahere("hnrpmc:tpahere"));
        dispatcher.register(tpaccept("hnrpmc:tpaccept"));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return tpa("tpa");
    }

    private LiteralArgumentBuilder<CommandSourceStack> tpa(String command) {
        return Commands.literal(command)
                .then(Commands.argument("Spieler", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer requester = ctx.getSource().getPlayerOrException();
                            ServerPlayer receiver = EntityArgument.getPlayer(ctx, "Spieler");

                            if (requester == receiver) {
                                ctx.getSource().sendFailure(Component.literal("§cDu kannst dich nicht zu dir selbst teleportieren!"));
                                return 0;
                            }

                            Tpa tpaRequest = new Tpa(requester, receiver, false);
                            plugin.getTpaRequester().addRequest(tpaRequest);
                            Component part1 = Component.literal("§a/tpaccept ")
                                    .withStyle(style -> style
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + requester.getName().getString()))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("/tpaccept")))
                                    );

                            Component part2 = Component.literal("§7Nutze ")
                                    .append(part1)
                                    .append("§7zum Annehmen.");

                            receiver.sendSystemMessage(Component.literal(formatMessage("§7{} §7möchte sich zu dir teleportieren.", requester.getName().getString())));
                            receiver.sendSystemMessage(part2);//Component.literal("§7Nutze §2/tpaccept§7 zum Annehmen."));

                            requester.sendSystemMessage(Component.literal(formatMessage("§7Eine Teleportierungsanfragen wurde an {} gesendet.", receiver.getName().getString())));
                            //"Eine Tpa wurde an {} gesendet."§7 §6 §7
                            // Optional: Timer zum Löschen nach 60s
                            return 1;
                        }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> tpahere(String command) {
        return Commands.literal(command)
                .then(Commands.argument("Spieler", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer requester = ctx.getSource().getPlayerOrException();
                            ServerPlayer receiver = EntityArgument.getPlayer(ctx, "Spieler");

                            if (requester == receiver) {
                                ctx.getSource().sendFailure(Component.literal("§cDu kannst dich nicht zu dir selbst teleportieren!"));
                                return 0;
                            }

                            Tpa tpaRequest = new Tpa(requester, receiver, true);
                            plugin.getTpaRequester().addRequest(tpaRequest);
                            Component part1 = Component.literal("§a/tpaccept ")
                                    .withStyle(style -> style
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + requester.getName().getString()))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("/tpaccept")))
                                    );

                            Component part2 = Component.literal("§7Nutze ")
                                    .append(part1)
                                    .append("§7zum Annehmen.");

                            receiver.sendSystemMessage(Component.literal(formatMessage("§7{} §7möchte das du dich zu ihm teleportierst.", requester.getName().getString())));
                            receiver.sendSystemMessage(part2);//Component.literal("§7Nutze §2/tpaccept§7 zum Annehmen."));

                            requester.sendSystemMessage(Component.literal(formatMessage("§7Eine Teleportierungsanfragen wurde an {} gesendet.", receiver.getName().getString())));
                            return 1;
                        }));
    }


    private LiteralArgumentBuilder<CommandSourceStack> tpaccept(String command) {
        return Commands.literal(command)
                .then(Commands.argument("Spieler", EntityArgument.player())
                        .suggests(Suggestions.getRequests(plugin))
                        .executes(ctx -> acceptLogic(ctx, EntityArgument.getPlayer(ctx, "Spieler"))))
                .executes(ctx -> acceptLogic(ctx, null)); // Ohne ID -> Neueste Anfrage
    }

    private int acceptLogic(CommandContext<CommandSourceStack> ctx, @Nullable Player id) {
        ServerPlayer receiver = ctx.getSource().getPlayer();
        List<Tpa> requests = plugin.getTpaRequester().getRequests(receiver.getUUID());

        if (requests.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("§cDu hast keine Teleportierungsanfragen."));
            return 0;
        }

        Tpa targetRequest;
        if (id != null) {
            targetRequest = requests.stream()
                    .filter(r -> r.getRequester().getUUID().equals(id.getUUID()))
                    .findFirst()
                    .orElse(null);
        } else {
            targetRequest = requests.get(requests.size() - 1);
        }

        ServerPlayer requester = targetRequest.getRequester();
        if (requester != null && !requester.hasDisconnected()) {
            if (!targetRequest.isHere()) {
                requester.teleportTo(receiver.serverLevel(), receiver.getX(), receiver.getY(), receiver.getZ(), receiver.getYRot(), receiver.getXRot());
                receiver.sendSystemMessage(Component.literal(formatMessage("§7Du hast die Teleportierungsanfrage von {} §aangenommen§7.", requester.getName().getString())));
            } else {
                receiver.teleportTo(receiver.serverLevel(), targetRequest.getSendPos().x, targetRequest.getSendPos().y, targetRequest.getSendPos().z, receiver.getYRot(), receiver.getXRot());
                receiver.sendSystemMessage(Component.literal(formatMessage("§7Du hast die Teleportierungsanfrage von {} §aangenommen§7.", requester.getName().getString())));
            }
        }

        plugin.getTpaRequester().removeRequest(targetRequest);
        return 1;
    }

    private String formatMessage(String msg, Object... args) {
        if (!msg.contains("{}")) return msg;
        for (Object arg : args) {
            msg = msg.replaceFirst(java.util.regex.Pattern.quote("{}"), String.valueOf(arg));
        }
        return msg;
    }
}
