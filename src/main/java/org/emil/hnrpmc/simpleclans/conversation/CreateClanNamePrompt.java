package org.emil.hnrpmc.simpleclans.conversation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.Rank;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.conversation.dings.ClanStringPrompt;
import org.emil.hnrpmc.simpleclans.events.PreCreateClanEvent;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.emil.hnrpmc.simpleclans.SimpleClans.getInstance;
import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.chat.ChatHandler.settingsManager;
import static org.emil.hnrpmc.simpleclans.conversation.CreateClanTagPrompt.TAG_KEY;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public class CreateClanNamePrompt extends ClanStringPrompt {
    public static final String NAME_KEY = "name";

    @Override
    public @NotNull String getPromptText(@NotNull SCConversation context) {
        if (context.getSessionData(NAME_KEY) != null) {
            return "";
        }
        return lang("insert.clan.name", context.getForWhom().getPlayer());
    }

    @Override
    public boolean blocksForInput(@NotNull SCConversation context) {
        return context.getSessionData(NAME_KEY) == null;
    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull SCConversation context, @Nullable String clanName) {
        SimpleClans plugin = (SimpleClans) context.getPlugin();
        ServerPlayer player = context.getForWhom().getPlayer();
        clanName = clanName != null ? clanName : (String) context.getSessionData(NAME_KEY);
        context.setSessionData(NAME_KEY, null);
        if (plugin == null || clanName == null) return this;

        Prompt errorPrompt = validateName(plugin, player, clanName);
        if (errorPrompt != null) return errorPrompt;

        String finalClanName = clanName;
        SimpleClans.getInstance().getServer().execute(() -> {
            String tag = (String) context.getSessionData(TAG_KEY);
            //noinspection ConstantConditions
            PreCreateClanEvent event = new PreCreateClanEvent(player, tag, finalClanName);
            NeoForge.EVENT_BUS.post(event);
            if (!event.isCanceled()) {
                processClanCreation(plugin, (ServerPlayer) player, tag, finalClanName);
            }
        });

        return END_OF_CONVERSATION;
    }

    private void processClanCreation(@NotNull SimpleClans plugin, @NotNull ServerPlayer player, @NotNull String tag,
                                     @NotNull String name) {
        if (plugin.getClanManager().purchaseCreation(player)) {
            plugin.getClanManager().createClan(player, tag, name);

            Clan clan = plugin.getClanManager().getClan(tag);
            clan.addBb(player.getName().getString(), lang("clan.created", player.getUUID(), name));
            plugin.getStorageManager().updateClan(clan);
            // Nutze getStarterRanks(), da diese Methode bereits Rank-Objekte zurückgibt

            if (plugin.getSettingsManager().is(REQUIRE_VERIFICATION)) {
                // Wenn er die Permission hat, gilt er als "automatisch verifiziert"
                boolean canVerify = plugin.getPermissionsManager().has(player, "simpleclans.mod.verify");

                if (!canVerify) {
                    ChatBlock.sendMessage(player.createCommandSourceStack(), lang("get.your.clan.verified.to.access.advanced.features", player));
                }
            }
        }
    }

    @Nullable
    private Prompt validateName(@NotNull SimpleClans plugin, @NotNull ServerPlayer player, @NotNull String input) {
        boolean bypass = plugin.getPermissionsManager().has(player, "simpleclans.mod.bypass");
        if (!bypass) {
            if (ChatUtils.stripColors(input).length() > plugin.getSettingsManager().getInt(CLAN_MAX_LENGTH)) {
                return new MessagePromptImpl(ChatFormatting.RED +
                        lang("your.clan.name.cannot.be.longer.than.characters", player,
                                plugin.getSettingsManager().getInt(CLAN_MAX_LENGTH)), this);
            }
            if (ChatUtils.stripColors(input).length() <= plugin.getSettingsManager().getInt(CLAN_MIN_LENGTH)) {
                return new MessagePromptImpl(ChatFormatting.RED +
                        lang("your.clan.name.must.be.longer.than.characters", player,
                                plugin.getSettingsManager().getInt(CLAN_MIN_LENGTH)), this);
            }
        }
        if (input.contains("&")) {
            return new MessagePromptImpl(ChatFormatting.RED +
                    lang("your.clan.name.cannot.contain.color.codes", player), this);
        }

        return null;
    }
}
