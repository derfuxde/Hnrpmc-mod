package org.emil.hnrpmc.hnessentials.commands.blocklogger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.emil.hnrpmc.Hnrpmod;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.hnessentials.commands.CommandHelper;
import org.emil.hnrpmc.hnessentials.managers.DatabaseManager;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.clan.Suggestions;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.*;

public class LoggerCommands extends ClanSBaseCommand {

    private final HNessentials plugin;

    private final CommandHelper commandHelper;

    public LoggerCommands(HNessentials plugin) {
        super(SimpleClans.getInstance());
        this.plugin = plugin;
        this.commandHelper = plugin.getCommandHelper();
    }

    @Override
    public @Nullable List<String> primarycommand() {
        return List.of("co");
    }

    @Override
    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        dispatcher.register(root(dispatcher, rootLiteral));
        return dispatcher.getRoot();
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        return Commands.literal(root)
                .requires(ctx -> Conditions.permission(ctx.getPlayer(), "essentials.admin.log"))
                .then(Lookup());
    }

    private LiteralArgumentBuilder<CommandSourceStack> Lookup() {
        return Commands.literal("lookup")
                .executes(this::executeLookup)
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .suggests(Suggestions.lookupsugests(plugin))
                        .executes(this::executeLookup)
                );
    }

    private int executeLookup(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String input = StringArgumentType.getString(ctx, "args");
        String time = getValue(input, "tme");
        long longtime = parseTimeToMillis(time);
        String spieler = getValue(input, "Spieler");
        String radius = getValue(input, "radius");

        int page = 1;
        String actualQuery = input;

        String[] parts = input.split(" ");
        if (parts.length > 1 && parts[parts.length - 1].matches("\\d+")) {
            page = Integer.parseInt(parts[parts.length - 1]);
            actualQuery = input.substring(0, input.lastIndexOf(" "));
        }

        final int finalPage = page;
        final String finalQuery = actualQuery;
        BlockPos pos = ctx.getSource().getPlayerOrException().blockPosition();

        CompletableFuture.supplyAsync(() -> LogQueryHandler.executeStringQuery(input, pos))
                .thenAccept(results -> {
                    if (results.isEmpty()) {
                        ctx.getSource().sendSystemMessage(Component.literal("§cKeine Einträge gefunden."));
                        return;
                    }


                    ctx.getSource().sendSystemMessage(Component.literal("§6--- Suchergebnisse ---"));

                    for (DatabaseManager.LogEntry entry : results) {
                        String timeRaw = formatTimeRelative(entry.time());
                        String timePrefix = CPStyle.LOOKUP_TIME.replace("{0}", timeRaw);
                        String actionWord = entry.action().equalsIgnoreCase("BREAK") ? "entfernt" : "platziert";

                        // 1. Erstelle den Haupttext (Zeit, Spieler, Aktion)
                        MutableComponent message = Component.literal(timePrefix)
                                .append(Component.literal(entry.player()).withStyle(s -> s.withColor(0xFFFFFF))) // Weiß
                                .append(Component.literal(" " + actionWord + " ").withStyle(s -> s.withColor(0x55FFFF))) // Dunkeltürkis
                                .append(Component.literal(entry.object()).withStyle(s -> s.withColor(0xFFFFFF))) // Weiß
                                .append(Component.literal(".").withStyle(s -> s.withColor(0x55FFFF))); // Punkt in Türkis

                        // 2. Erstelle die anklickbaren Koordinaten
                        String coordString = String.format(" (%d/%d/%d)",
                                entry.pos().getX(), entry.pos().getY(), entry.pos().getZ());

                        // Der Befehl, der vorgeschlagen werden soll (z.B. Teleport)
                        String tpCommand = String.format("/tp %d %d %d",
                                entry.pos().getX(), entry.pos().getY(), entry.pos().getZ());

                        MutableComponent coordComponent = Component.literal(coordString)
                                .withStyle(Style.EMPTY
                                        .withColor(0xAAAAAA) // Grau
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tpCommand))
                                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                                net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                                Component.literal("§eKlicke zum Teleportieren")
                                        ))
                                );

                        // 3. Alles zusammenfügen und senden
                        ctx.getSource().sendSystemMessage(message.append(coordComponent));
                    }
                });

        return 1;
    }

    /**
     * Formatiert die Millisekunden in eine kompakte Zeitangabe (z.B. 1.5h, 10m).
     */
    private static String formatTimeRelative(long timestamp) {
        long diffMs = System.currentTimeMillis() - timestamp;

        // Umrechnungen
        double seconds = diffMs / 1000.0;
        double minutes = seconds / 60.0;
        double hours = minutes / 60.0;
        double days = hours / 24.0;

        // Rückgabe je nach Größe (mit einer Nachkommastelle, falls sinnvoll)
        if (days >= 1.0) {
            return String.format("%.2fd", days); // z.B. 1.25d
        } else if (hours >= 1.0) {
            return String.format("%.2fh", hours); // z.B. 2.50h
        } else if (minutes >= 1.0) {
            return Math.round(minutes) + "m";     // z.B. 10m
        } else {
            return Math.round(seconds) + "s";     // z.B. 15s
        }
    }

    public class CPStyle {
        // Original-Stile aus deiner de.yml
        public static final String LOOKUP_HEADER = "§3----- §fLogger Ergebnisse §3-----";
        public static final String LOOKUP_PAGE = "§3Seite {0}";
        public static final String LOOKUP_ROWS_FOUND = "§3{0} {Reihe|Reihen} gefunden.";

        // Aktions-Templates
        public static final String LOOKUP_BLOCK = "§f{0} §3{1} §f{2}."; // {0}=Spieler, {1}=platziert/entfernt, {2}=Block
        public static final String LOOKUP_TIME = "§7vor {0} - ";

        // Plural-Hilfe (simuliert die {Reihe|Reihen} Logik)
        public static String getPlural(int count, String single, String plural) {
            return count == 1 ? single : plural;
        }
    }

    private static String formatTime(long timestamp) {
        long diff = (System.currentTimeMillis() - timestamp) / 60000;
        if (diff < 60) return diff + "m";
        if (diff < 1440) return (diff / 60) + "h";
        return (diff / 1440) + "d";
    }

    private static String getValue(String input, String key) {
        Pattern pattern = Pattern.compile(key + ":\\s*(\\S+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static long parseTimeToMillis(String timeStr) {
        if (timeStr == null) return 0;

        try {
            long amount = Long.parseLong(timeStr.substring(0, timeStr.length() - 1));
            char unit = timeStr.charAt(timeStr.length() - 1);

            return switch (unit) {
                case 'm' -> amount * 60_000L;          // Minuten
                case 'h' -> amount * 3_600_000L;       // Stunden
                case 'd' -> amount * 86_400_000L;      // Tage
                default -> amount * 1000L;             // Sekunden als Fallback
            };
        } catch (Exception e) {
            return 0;
        }
    }
}
