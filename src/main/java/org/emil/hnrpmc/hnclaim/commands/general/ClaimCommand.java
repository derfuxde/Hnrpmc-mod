package org.emil.hnrpmc.hnclaim.commands.general;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.emil.hnrpmc.hnclaim.Claim;
import org.emil.hnrpmc.hnclaim.HNClaims;
import org.emil.hnrpmc.hnclaim.claimperms;
import org.emil.hnrpmc.hnclaim.managers.ClaimManager;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanSBaseCommand;
import org.emil.hnrpmc.simpleclans.commands.clan.Suggestions;
import org.emil.hnrpmc.simpleclans.overlay.ClanScoreboard;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public final class ClaimCommand extends ClanSBaseCommand {

    private final HNClaims plugin;
    private final ClaimManager claimManager;

    public ClaimCommand(SimpleClans SC, HNClaims plugin) {
        super(SC);
        this.plugin = plugin;
        this.claimManager = plugin.getClaimManager();
    }

    @Override
    public @Nullable String primarycommand() {
        return "claim";
    }

    @Override
    public RootCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String rootLiteral) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("claim")
                .requires(src -> src.getEntity() instanceof ServerPlayer)
                // /claim (Erstellt neuen Claim)
                .executes(ctx -> executeClaim(ctx.getSource()))
                // /claim list
                .then(Commands.literal("list")
                        .executes(ctx -> executeList(ctx.getSource())))
                .then(Commands.literal("giveItem")
                        .executes(ctx -> executeClaimItem(ctx.getSource())))
                .then(Commands.literal("ban")
                        .then(Commands.argument("claim", StringArgumentType.string())
                                .suggests(Suggestions.claimsfromplayer(plugin))
                                .then(Commands.argument("player", StringArgumentType.word())
                                    .suggests(Suggestions.allPlayers(SimpleClans.getInstance()))
                                    .executes(ctx -> executeBan(ctx, false)))))
                .then(Commands.literal("unban")
                        .then(Commands.argument("claim", StringArgumentType.string())
                                .suggests(Suggestions.claimsfromplayer(plugin))
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .suggests(Suggestions.banedPlayers(plugin))
                                        .executes(ctx -> executeBan(ctx, true)))))

                // /claim info
                .then(Commands.literal("info")
                        .executes(ctx -> executeInfo(ctx.getSource())))
                // /claim delete
                .then(Commands.literal("delete")
                        .executes(ctx -> executeDelete(ctx.getSource())))
                // /claim setperm <claim> add/remove <permission>
                .then(Commands.literal("setperm")
                        .then(Commands.argument("claim", StringArgumentType.string())
                                .suggests(Suggestions.claimsfromplayer(plugin))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("permission", StringArgumentType.word())
                                                .suggests(Suggestions.allclaimperms(plugin))
                                                .executes(ctx -> executeSetPerm(ctx, StringArgumentType.getString(ctx, "permission"), true))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("permission", StringArgumentType.word())
                                                .suggests(Suggestions.allclaimperms(plugin))
                                                .executes(ctx -> executeSetPerm(ctx, StringArgumentType.getString(ctx, "permission"), false))))))
                .then(Commands.literal("placeholder")
                        .then(Commands.argument("Player", EntityArgument.player())
                                .then(Commands.argument("msg", StringArgumentType.greedyString())
                                        .executes(this::executePlaceholder))))
                // /claim playerperm <player> <permission>
                .then(Commands.literal("playerperm")
                        .then(Commands.argument("Player/Clan", StringArgumentType.word())
                                .suggests(Suggestions.getPlayerandClansNotOwn(plugin))
                                .then(Commands.argument("claim", StringArgumentType.string())
                                        .suggests(Suggestions.claimsfromplayer(plugin))
                                        .then(Commands.literal("add")
                                                .then(Commands.argument("permission", StringArgumentType.word())
                                                        .suggests(Suggestions.allclaimperms(plugin))
                                                        .executes(ctx -> executePlayerPerm(ctx, StringArgumentType.getString(ctx, "permission"), true))))
                                        .then(Commands.literal("remove")
                                                .then(Commands.argument("permission", StringArgumentType.word())
                                                        .suggests(Suggestions.allclaimperms(plugin))
                                                        .executes(ctx -> executePlayerPerm(ctx, StringArgumentType.getString(ctx, "permission"), false)))))));

        dispatcher.register(command);
        return null;
    }

    // --- LOGIK METHODEN ---

    private int executeClaim(CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        if (player == null) return 0;
        String name = player.getScoreboardName() + "_claim-" + claimManager.getClaims().size();
        claimManager.createClaim(player, name);
        ChatBlock.sendMessage(src, ChatFormatting.GREEN + "Claim '" + name + "' erstellt!");
        return 1;
    }

    private int executeList(CommandSourceStack src) {
        String allClaims = claimManager.getClaims().stream()
                .map(claim -> claim.getName() + " " + claim.getCenter())
                .collect(Collectors.joining(", "));
        ChatBlock.sendMessage(src, ChatFormatting.AQUA + "Alle Claims: " + ChatFormatting.WHITE + allClaims);
        return 1;
    }

    private int executeClaimItem(CommandSourceStack src) {
        ItemStack wand = new ItemStack(Items.STICK);
        wand.set(DataComponents.CUSTOM_NAME, Component.literal("Claim-Tool").withStyle(ChatFormatting.GOLD));
        wand.set(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag() {{ putBoolean("hnclaim_tool", true); }}));
        src.getPlayer().getInventory().add(wand);
        return 1;
    }

    private int executeInfo(CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        Claim claim = claimManager.getClaimbyPlayerPos(player);
        if (claim == null) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Du stehst in keinem Claim.");
            return 0;
        }
        ChatBlock.sendMessage(src, ChatFormatting.GOLD + "=== Claim Info ===");
        ChatBlock.sendMessage(src, "Name: " + claim.getName());
        ChatBlock.sendMessage(src, "Besitzer: " + plugin.getServer().getProfileCache().get(claim.getownerUUID()).get().getName());
        return 1;
    }

    private int executeDelete(CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        Claim claim = claimManager.getClaimbyPlayerPos(player);
        if (claim != null && claim.getownerUUID().equals(player.getUUID())) {
            claimManager.getClaims().remove(claim.getName());
            claimManager.removeClaim(claim.getName());
            plugin.getStorageManager().deleteClaim(claim);
            ChatBlock.sendMessage(src, ChatFormatting.DARK_RED + "Claim gelöscht.");
            return 1;
        }
        ChatBlock.sendMessage(src, ChatFormatting.RED + "Nur der Besitzer kann das.");
        return 0;
    }

    private int executeBan(CommandContext<CommandSourceStack> ctx, boolean unban) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        String targetname = StringArgumentType.getString(ctx, "player");
        if (!plugin.getServer().getProfileCache().get(targetname).isPresent()) {
            player.sendSystemMessage(Component.literal("§cSpieler " + targetname + " konnte nicht gefunden werden"));
            return 0;
        }
        UUID target = plugin.getServer().getProfileCache().get(targetname).get().getId();
        String claimname = StringArgumentType.getString(ctx, "claim");
        if (Objects.equals(claimname, "alle")) {
            for (Claim claim : claimManager.getClaims().stream().filter(cc -> cc.getownerUUID().equals(player.getUUID())).toList()) {
                if (claim != null && !claim.getownerUUID().equals(player.getUUID())) {
                    ChatBlock.sendMessage(src, ChatFormatting.RED + "Nur der Besitzer kann das.");
                    return 0;
                } else if (claim == null) {
                    ChatBlock.sendMessage(src, ChatFormatting.RED + "Claim konnte nicht gefunden werden.");
                    return 0;
                }

                if (!player.getUUID().equals(target)) {
                    if (unban) {
                        claim.removeBanPlayers(target);
                        return 1;
                    } else {
                        claim.addBanPlayers(target);
                        return 1;
                    }
                } else {
                    ChatBlock.sendMessage(src, ChatFormatting.RED + "Du kannst dich nicht aus deinem eigenen Claim bannen.");
                    return 0;
                }
            }
        }
        Claim claim = claimManager.getClaimByName(claimname);
        if (claim != null && !claim.getownerUUID().equals(player.getUUID())) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Nur der Besitzer kann das.");
            return 0;
        } else if (claim == null) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Claim konnte nicht gefunden werden.");
            return 0;
        }

        if (!player.getUUID().equals(target)) {
            if (unban) {
                claim.removeBanPlayers(target);
                return 1;
            } else {
                claim.addBanPlayers(target);
                return 1;
            }
        } else {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Du kannst dich nicht aus deinem eigenen Claim bannen.");
            return 0;
        }
    }

    private int executePlaceholder(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer sp = ctx.getSource().getPlayer();

            String msg = StringArgumentType.getString(ctx, "msg");
            ServerPlayer player = EntityArgument.getPlayer(ctx, "Player");

            if (sp == null) return 0;

            sp.sendSystemMessage(Component.literal(ClanScoreboard.formatplaceholder(SimpleClans.getInstance(), msg, player)));

            return 1;
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private int executeSetPerm(CommandContext<CommandSourceStack> ctx, String permName, boolean add) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = ctx.getSource().getPlayer();
        int changedCount = 0;
        String selcedclaim = StringArgumentType.getString(ctx, "claim");
        if (Objects.equals(selcedclaim, "alle")) {
            List<Claim> claimlist = plugin.getClaimManager().getClaims().stream().filter(claim -> claim.getownerUUID().equals(ctx.getSource().getPlayer().getUUID())).toList();
            for (Claim claim : plugin.getClaimManager().getClaims()) {
                plugin.getLogger().debug("ist jetzt bei claim {}, und i", claim.getName());
                if (!player.getUUID().equals(claim.getownerUUID())) continue;
                if (claim == null) continue;

                try {
                    claimperms perm = claimperms.valueOf(permName.toUpperCase());
                    if (claim.getPerms().contains(perm) == add) {
                        //String returnmsg = add ? "Die Berechtigung " + permName + " wurde bereits zu " + claim.getName() + " hinzugefügt" : "Keine Berechtigung " + permName + " in " + claim.getName() + " gefunden";
                        //ChatBlock.sendMessage(src, ChatFormatting.RED + returnmsg);
                        continue;
                    } else {
                        if (add) {
                            claim.addPerms(perm);
                            plugin.getStorageManager().updateClaim(claim, true);
                        } else {
                            claim.removePerms(perm);
                            plugin.getStorageManager().updateClaim(claim, true);
                        }
                        changedCount++;
                    }
                } catch (Exception e) {
                    ChatBlock.sendMessage(src, ChatFormatting.RED + "Unbekannte Berechtigung!");
                }
            }
            String ending  = add ? " hinzugefügt." : " entfernt.";
            ChatBlock.sendMessage(src, ChatFormatting.GREEN + "Globale Berechtigung " + permName + ending);
            return 1;
        } else {
            Claim claim = claimManager.getClaim(selcedclaim);
            if (claim == null) return 0;

            try {
                claimperms perm = claimperms.valueOf(permName.toUpperCase());
                if (claim.getPerms().contains(perm) == add) {
                    String returnmsg = add ? "Die Berechtigung " + permName + " wurde bereits zu " + claim.getName() + " hinzugefügt" : "Keine Berechtigung " + permName + " in " + claim.getName() + " gefunden";
                    ChatBlock.sendMessage(src, ChatFormatting.RED + returnmsg);
                    return 0;
                } else {
                    if (add) {
                        claim.addPerms(perm);
                        plugin.getStorageManager().updateClaim(claim, true);
                    } else {
                        claim.removePerms(perm);
                        plugin.getStorageManager().updateClaim(claim, true);
                    }

                    changedCount++;
                }

                plugin.getStorageManager().updateAllClaim();

                String action = add ? "hinzugefügt" : "entfernt";
                if (changedCount > 0) {
                    ChatBlock.sendMessage(src, ChatFormatting.GREEN + "Berechtigung " + ChatFormatting.YELLOW + permName +
                            ChatFormatting.GREEN + " wurde " + ChatFormatting.GREEN + " in " + changedCount + " Claim(s) " + action + ".");
                } else {
                    ChatBlock.sendMessage(src, ChatFormatting.YELLOW + "Es wurden keine Änderungen vorgenommen.");
                }
            } catch (Exception e) {
                ChatBlock.sendMessage(src, ChatFormatting.RED + "Unbekannte Berechtigung!");
            }
            return 1;
        }
    }

    private int executePlayerPerm(CommandContext<CommandSourceStack> ctx, String permName, boolean add) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer owner = src.getPlayer();

        // 1. Argumente auslesen
        String selectedClaimName = StringArgumentType.getString(ctx, "claim");
        String inputTarget = StringArgumentType.getString(ctx, "Player/Clan");

        // 2. Permission validieren
        claimperms perm;
        try {
            perm = claimperms.valueOf(permName.toUpperCase());
        } catch (IllegalArgumentException e) {
            ChatBlock.sendMessage(src, ChatFormatting.RED + "Unbekannte Berechtigung: " + permName);
            return 0;
        }

        // 3. Ziel-ID bestimmen (Unterscheidung zwischen Clan und Spieler)
        String targetId;
        String displayName;

        if (inputTarget.startsWith(".")) {
            // FALL A: Es ist ein Clan
            targetId = inputTarget; // z.B. ".MeinClan"
            displayName = "Clan " + ChatFormatting.GOLD + inputTarget;
        } else {
            // FALL B: Es ist ein Spieler
            var profile = plugin.getServer().getProfileCache().get(inputTarget);
            if (profile.isEmpty()) {
                ChatBlock.sendMessage(src, ChatFormatting.RED + "Spieler '" + inputTarget + "' wurde nicht gefunden!");
                return 0;
            }
            targetId = profile.get().getId().toString(); // Die UUID als String
            displayName = "Spieler " + ChatFormatting.WHITE + inputTarget;
        }

        // 4. Welche Claims sind betroffen?
        List<Claim> targets;
        if (selectedClaimName.equals("*") || selectedClaimName.equalsIgnoreCase("alle")) {
            targets = plugin.getClaimManager().getClaims().stream()
                    .filter(c -> c.getownerUUID().equals(owner.getUUID()))
                    .toList();
        } else {
            Claim single = plugin.getClaimManager().getClaim(selectedClaimName);
            if (single == null || !single.getownerUUID().equals(owner.getUUID())) {
                ChatBlock.sendMessage(src, ChatFormatting.RED + "Claim nicht gefunden oder nicht dein Eigentum!");
                return 0;
            }
            targets = List.of(single);
        }

        if (targets.isEmpty()) {
            ChatBlock.sendMessage(src, ChatFormatting.YELLOW + "Du besitzt keine Claims.");
            return 0;
        }

        // 5. Die eigentliche Änderung durchführen
        int changedCount = 0;
        for (Claim claim : targets) {
            // Wir prüfen die Overrides für die targetId (egal ob Clan-String oder Spieler-UUID)
            boolean alreadyHasIt = false;
            if (displayName.startsWith("Clan ")) {
                List<claimperms> clanperms = claim.getClaimPerms(targetId);
                if (clanperms != null){
                    alreadyHasIt = clanperms.contains(perm);
                }
            } else {
                List<claimperms> playerperms = claim.getPlayerPerms(targetId, plugin);
                if (playerperms != null) {
                    alreadyHasIt = playerperms.contains(perm);
                }
            }

            if (add && !alreadyHasIt) {
                claim.addoverridePerms(targetId, perm);
                plugin.getStorageManager().updateClaim(claim, true);
                changedCount++;
            } else if (!add && alreadyHasIt) {
                claim.removeoverridePerms(targetId, perm);
                plugin.getStorageManager().updateClaim(claim, true);
                changedCount++;
            }
        }

        // 6. Feedback an den Spieler
        String action = add ? "hinzugefügt" : "entfernt";
        if (changedCount > 0) {
            ChatBlock.sendMessage(src, ChatFormatting.GREEN + "Berechtigung " + ChatFormatting.YELLOW + permName +
                    ChatFormatting.GREEN + " wurde für " + displayName + ChatFormatting.GREEN + " in " + changedCount + " Claim(s) " + action + ".");
        } else {
            ChatBlock.sendMessage(src, ChatFormatting.YELLOW + "Es wurden keine Änderungen vorgenommen.");
        }

        return 1;
    }
}