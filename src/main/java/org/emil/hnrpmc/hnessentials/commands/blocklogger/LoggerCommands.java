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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.emil.hnrpmc.hnessentials.HNessentials;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.clan.Suggestions;
import org.emil.hnrpmc.simpleclans.commands.conditions.Conditions;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoggerCommands extends ClanSBaseCommand {

    private final HNessentials plugin;

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final long CONFIRM_TTL_MS = 60_000L;
    private static final SecureRandom RNG = new SecureRandom();
    private static final Map<UUID, PendingRollback> PENDING = new HashMap<>();

    public LoggerCommands(HNessentials plugin) {
        super(SimpleClans.getInstance());
        this.plugin = plugin;
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
                .then(Lookup())
                .then(Rollback());
    }

    private LiteralArgumentBuilder<CommandSourceStack> Lookup() {
        return Commands.literal("lookup")
                .executes(ctx -> executeLookup(ctx, ""))
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .suggests(Suggestions.lookupsugests(plugin))
                        .executes(ctx -> executeLookup(ctx, StringArgumentType.getString(ctx, "args")))
                );
    }

    private LiteralArgumentBuilder<CommandSourceStack> Rollback() {
        return Commands.literal("rollback")
                .executes(ctx -> executeRollback(ctx, ""))
                .then(Commands.literal("confirm")
                        .then(Commands.argument("token", StringArgumentType.word())
                                .executes(this::executeRollbackConfirm)
                        )
                )
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .suggests(Suggestions.lookupsugests(plugin))
                        .executes(ctx -> executeRollback(ctx, StringArgumentType.getString(ctx, "args")))
                );
    }

    private int executeLookup(CommandContext<CommandSourceStack> ctx, String args) throws CommandSyntaxException {
        ParsedArgs parsed = ParsedArgs.parse(args);

        int page = Math.max(1, parsed.getInt("page", 1));
        int limit = Math.max(1, parsed.getInt("limit", DEFAULT_PAGE_SIZE));
        int offset = (page - 1) * limit;

        long timeWindowMs = parseTimeWindow(parsed.getAny("t", "time", "since"));
        int radius = parsed.getIntAny(0, "r", "radius");

        String user = parsed.getAny("u", "user", "spieler", "Spieler");
        Set<String> exclude = parsed.getCsvSetAny("e", "exclude");
        Set<String> actions = parsed.getCsvSetAny("a", "action");
        Set<String> obj = parsed.getCsvSetAny("obj", "object");

        BlockPos center = ctx.getSource().getPlayerOrException().blockPosition();
        ServerLevel level = ctx.getSource().getLevel();

        LogQueryHandler.LookupFilter filter = new LogQueryHandler.LookupFilter(
                center,
                level.dimension(),
                timeWindowMs,
                radius,
                user,
                exclude,
                actions,
                obj
        );

        CompletableFuture
                .supplyAsync(() -> LogQueryHandler.lookup(filter, limit, offset))
                .thenAccept(result -> ctx.getSource().getServer().execute(() -> {
                    if (result.rows().isEmpty()) {
                        ctx.getSource().sendSystemMessage(Component.literal("§cKeine Einträge gefunden."));
                        return;
                    }

                    int shownFrom = offset + 1;
                    int shownTo = offset + result.rows().size();

                    ctx.getSource().sendSystemMessage(Component.literal("§3----- §fLogger Lookup §3-----"));
                    ctx.getSource().sendSystemMessage(Component.literal("§3Seite §f" + page + " §7(" + shownFrom + "-" + shownTo + " von " + result.total() + ")"));

                    for (LogQueryHandler.LogRow row : result.rows()) {
                        String rel = formatTimeRelative(row.timestamp());
                        String actionWord = row.action().equalsIgnoreCase("BREAK") ? "entfernt" :
                                row.action().equalsIgnoreCase("PLACE") ? "platziert" : row.action().toLowerCase(Locale.ROOT);

                        MutableComponent msg = Component.literal("§7vor " + rel + " - §f" + row.player() + " §b" + actionWord + " §f" + row.object() + "§b.");
                        String tp = "/tp " + row.x() + " " + row.y() + " " + row.z();
                        MutableComponent coord = Component.literal(" §8(" + row.x() + "/" + row.y() + "/" + row.z() + ")")
                                .withStyle(Style.EMPTY
                                        .withColor(0xAAAAAA)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tp))
                                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                                net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                                Component.literal("§eKlicke zum Teleportieren")
                                        ))
                                );

                        ctx.getSource().sendSystemMessage(msg.append(coord));
                    }

                    String base = "/co lookup " + rebuildArgsWithoutKeys(args, Set.of("page")).trim();
                    if (!base.endsWith(" ") && !base.endsWith("lookup")) base += " ";

                    String prevCmd = base + "page:" + Math.max(1, page - 1);
                    String nextCmd = base + "page:" + (page + 1);

                    MutableComponent nav = Component.literal("§8[")
                            .append(clickable("§b<< Zurück", prevCmd, "Vorherige Seite"))
                            .append(Component.literal("§8] §8["))
                            .append(clickable("§bWeiter >>", nextCmd, "Nächste Seite"))
                            .append(Component.literal("§8]"));

                    ctx.getSource().sendSystemMessage(nav);
                }));

        return 1;
    }

    private int executeRollback(CommandContext<CommandSourceStack> ctx, String args) throws CommandSyntaxException {
        ParsedArgs parsed = ParsedArgs.parse(args);

        long timeWindowMs = parseTimeWindow(parsed.getAny("t", "time", "since"));
        int radius = parsed.getIntAny(0, "r", "radius");

        String user = parsed.getAny("u", "user", "spieler", "Spieler");
        Set<String> exclude = parsed.getCsvSetAny("e", "exclude");
        Set<String> actions = parsed.getCsvSetAny("a", "action");
        Set<String> obj = parsed.getCsvSetAny("obj", "object");

        boolean force = parsed.getBooleanAny(false, "force", "apply");
        int limit = Math.min(2000, parsed.getIntAny(500, "limit", "max"));

        if (actions.isEmpty()) actions = Set.of("break", "place");

        BlockPos center = ctx.getSource().getPlayerOrException().blockPosition();
        ServerLevel level = ctx.getSource().getLevel();

        LogQueryHandler.LookupFilter filter = new LogQueryHandler.LookupFilter(
                center,
                level.dimension(),
                timeWindowMs,
                radius,
                user,
                exclude,
                actions,
                obj
        );

        UUID executor = ctx.getSource().getPlayerOrException().getUUID();

        CompletableFuture
                .supplyAsync(() -> LogQueryHandler.rollbackPreview(filter, limit))
                .thenAccept(rows -> ctx.getSource().getServer().execute(() -> {
                    if (rows.isEmpty()) {
                        ctx.getSource().sendSystemMessage(Component.literal("§cKeine Einträge zum Rollback gefunden."));
                        return;
                    }

                    if (force) {
                        int changed = applyRollbackOnServerThread(level, rows);
                        ctx.getSource().sendSystemMessage(Component.literal("§aRollback ausgeführt. Änderungen: §f" + changed));
                        return;
                    }

                    String token = genToken();
                    PENDING.put(executor, new PendingRollback(token, System.currentTimeMillis(), filter, limit));

                    ctx.getSource().sendSystemMessage(Component.literal("§6Rollback Vorschau"));
                    ctx.getSource().sendSystemMessage(Component.literal("§7Treffer: §f" + rows.size() + " §7(max: " + limit + ")"));

                    int show = Math.min(8, rows.size());
                    for (int i = 0; i < show; i++) {
                        LogQueryHandler.LogRow r = rows.get(i);
                        ctx.getSource().sendSystemMessage(Component.literal(
                                "§7vor " + formatTimeRelative(r.timestamp()) + " §8- §f" + r.player() + " §3" + r.action() +
                                        " §f" + r.object() + " §8(" + r.x() + "/" + r.y() + "/" + r.z() + ")"
                        ));
                    }
                    if (rows.size() > show) {
                        ctx.getSource().sendSystemMessage(Component.literal("§8… +" + (rows.size() - show) + " weitere"));
                    }

                    String confirmCmd = "/co rollback confirm " + token;
                    ctx.getSource().sendSystemMessage(clickable("§cKlicke zum Bestätigen: " + confirmCmd, confirmCmd, "Rollback ausführen (60s gültig)"));
                }));

        return 1;
    }

    private int executeRollbackConfirm(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String token = StringArgumentType.getString(ctx, "token");
        UUID executor = ctx.getSource().getPlayerOrException().getUUID();

        PendingRollback pending = PENDING.get(executor);
        if (pending == null || !pending.token.equals(token)) {
            ctx.getSource().sendSystemMessage(Component.literal("§cKein passender Rollback-Request gefunden."));
            return 0;
        }
        if (System.currentTimeMillis() - pending.createdAtMs > CONFIRM_TTL_MS) {
            PENDING.remove(executor);
            ctx.getSource().sendSystemMessage(Component.literal("§cRollback-Token abgelaufen. Bitte erneut /co rollback ausführen."));
            return 0;
        }

        ServerLevel level = ctx.getSource().getLevel();

        CompletableFuture
                .supplyAsync(() -> LogQueryHandler.rollbackPreview(pending.filter, pending.limit))
                .thenAccept(rows -> ctx.getSource().getServer().execute(() -> {
                    int changed = applyRollbackOnServerThread(level, rows);
                    PENDING.remove(executor);
                    ctx.getSource().sendSystemMessage(Component.literal("§aRollback ausgeführt. Änderungen: §f" + changed));
                }));

        return 1;
    }

    private static int applyRollbackOnServerThread(ServerLevel level, List<LogQueryHandler.LogRow> rows) {
        int changed = 0;

        List<LogQueryHandler.LogRow> places = new ArrayList<>();
        List<LogQueryHandler.LogRow> breaks = new ArrayList<>();
        List<LogQueryHandler.LogRow> kills = new ArrayList<>();

        for (LogQueryHandler.LogRow row : rows) {
            String act = row.action();
            if ("PLACE".equalsIgnoreCase(act)) places.add(row);
            else if ("BREAK".equalsIgnoreCase(act)) breaks.add(row);
            else if ("EXPLOSION_BREAK".equalsIgnoreCase(act)) breaks.add(row);
            else if ("DESTROY".equalsIgnoreCase(act)) breaks.add(row);
            else if ("KILL".equalsIgnoreCase(act)) kills.add(row);
        }

        for (LogQueryHandler.LogRow row : places) {
            BlockPos pos = new BlockPos(row.x(), row.y(), row.z());
            if (level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)) changed++;
        }

        for (LogQueryHandler.LogRow row : breaks) {
            BlockPos pos = new BlockPos(row.x(), row.y(), row.z());
            Block block = parseBlock(row.object());
            if (block == null) continue;
            if (level.setBlock(pos, block.defaultBlockState(), 3)) changed++;
        }

        int revived = 0;
        for (LogQueryHandler.LogRow row : kills) {
            Optional<EntityType<?>> entityType = EntityType.byString(row.object());

            if (entityType.isPresent()) {
                Vec3 pos = new Vec3(row.x() + 0.5, row.y(), row.z() + 0.5);

                Entity entity = entityType.get().create(level);

                if (entity != null) {
                    entity.moveTo(pos.x, pos.y, pos.z, 0, 0);

                    if (level.addFreshEntity(entity)) {
                        revived++;
                    }
                }
            }
        }

        return changed;
    }

    private static Block parseBlock(String object) {
        if (object == null) return null;
        String s = object.trim();

        Matcher m1 = Pattern.compile("Block\\{([a-z0-9_\\-]+):([a-z0-9_\\-/\\.]+)}", Pattern.CASE_INSENSITIVE).matcher(s);
        if (m1.find()) {
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(m1.group(1).toLowerCase(Locale.ROOT), m1.group(2).toLowerCase(Locale.ROOT));
            return BuiltInRegistries.BLOCK.getOptional(rl).orElse(null);
        }

        Matcher m2 = Pattern.compile("([a-z0-9_\\-]+):([a-z0-9_\\-/\\.]+)", Pattern.CASE_INSENSITIVE).matcher(s);
        if (m2.matches()) {
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(m2.group(1).toLowerCase(Locale.ROOT), m2.group(2).toLowerCase(Locale.ROOT));
            return BuiltInRegistries.BLOCK.getOptional(rl).orElse(null);
        }

        return null;
    }

    private static long parseTimeWindow(String candidate) {
        if (candidate == null || candidate.isBlank()) return 0;
        return parseTimeToMillis(candidate.trim());
    }

    public static long parseTimeToMillis(String timeStr) {
        if (timeStr == null) return 0;

        String s = timeStr.trim().toLowerCase(Locale.ROOT);
        Matcher m = Pattern.compile("^(\\d+)(s|m|h|d|w)$").matcher(s);
        if (!m.matches()) return 0;

        long amount = Long.parseLong(m.group(1));
        return switch (m.group(2)) {
            case "s" -> amount * 1_000L;
            case "m" -> amount * 60_000L;
            case "h" -> amount * 3_600_000L;
            case "d" -> amount * 86_400_000L;
            case "w" -> amount * 604_800_000L;
            default -> 0;
        };
    }

    private static String formatTimeRelative(long timestamp) {
        long diffMs = Math.max(0, System.currentTimeMillis() - timestamp);

        double seconds = diffMs / 1000.0;
        double minutes = seconds / 60.0;
        double hours = minutes / 60.0;
        double days = hours / 24.0;

        if (days >= 1.0) return String.format(Locale.ROOT, "%.2fd", days);
        if (hours >= 1.0) return String.format(Locale.ROOT, "%.2fh", hours);
        if (minutes >= 1.0) return Math.round(minutes) + "m";
        return Math.round(seconds) + "s";
    }

    private static MutableComponent clickable(String text, String suggestCmd, String hover) {
        return Component.literal(text).withStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestCmd))
                .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                        net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                        Component.literal("§e" + hover)
                )));
    }

    private static String genToken() {
        byte[] b = new byte[4];
        RNG.nextBytes(b);
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    private static String rebuildArgsWithoutKeys(String raw, Set<String> keysLower) {
        if (raw == null || raw.isBlank()) return "";
        String[] parts = raw.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            String k = p.split(":", 2)[0].toLowerCase(Locale.ROOT);
            if (keysLower.contains(k)) continue;
            sb.append(p).append(" ");
        }
        return sb.toString();
    }

    private record PendingRollback(String token, long createdAtMs, LogQueryHandler.LookupFilter filter, int limit) {}

    private static final class ParsedArgs {
        private final Map<String, String> map;

        private ParsedArgs(Map<String, String> map) { this.map = map; }

        static ParsedArgs parse(String raw) {
            Map<String, String> out = new HashMap<>();
            if (raw == null || raw.isBlank()) return new ParsedArgs(out);

            for (String p : raw.trim().split("\\s+")) {
                String[] kv = p.split(":", 2);
                if (kv.length == 2) out.put(kv[0], kv[1]);
            }
            return new ParsedArgs(out);
        }

        String getAny(String... keys) {
            for (String k : keys) {
                String v = map.get(k);
                if (v != null && !v.isBlank()) return v;
            }
            return null;
        }

        int getInt(String key, int def) {
            String v = map.get(key);
            if (v == null) return def;
            try { return Integer.parseInt(v); } catch (Exception e) { return def; }
        }

        int getIntAny(int def, String... keys) {
            String v = getAny(keys);
            if (v == null) return def;
            try { return Integer.parseInt(v); } catch (Exception e) { return def; }
        }

        boolean getBooleanAny(boolean def, String... keys) {
            String v = getAny(keys);
            if (v == null) return def;
            String s = v.toLowerCase(Locale.ROOT);
            return s.equals("true") || s.equals("1") || s.equals("yes") || s.equals("y");
        }

        Set<String> getCsvSetAny(String... keys) {
            String v = getAny(keys);
            if (v == null || v.isBlank()) return Set.of();
            Set<String> out = new HashSet<>();
            for (String p : v.split(",")) {
                String s = p.trim();
                if (!s.isEmpty()) out.add(s.toLowerCase(Locale.ROOT));
            }
            return out;
        }
    }
}
