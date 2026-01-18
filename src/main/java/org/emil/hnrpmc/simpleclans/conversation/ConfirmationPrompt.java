package org.emil.hnrpmc.simpleclans.conversation;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.conversation.dings.ClanStringPrompt;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

abstract public class ConfirmationPrompt extends ClanStringPrompt {

    protected abstract Prompt confirm(ClanPlayer sender, Clan clan);

    protected Prompt decline(ClanPlayer sender) {
        return new MessagePromptImpl(ChatFormatting.RED + lang(getDeclineTextKey(), sender.getUniqueId()));
    }

    protected abstract String getPromptTextKey();

    protected abstract String getDeclineTextKey();

    @NotNull
    @Override
    public String getPromptText(@NotNull SCConversation context) {
        Player player = context.getForWhom().getPlayer();
        List<String> options = Arrays.asList(lang("yes", player), lang("cancel", player));

        return ChatFormatting.RED + lang(getPromptTextKey(), player, options);
    }

    @Override
    public Prompt acceptInput(@NotNull SCConversation cc, @Nullable String input) {
        final SimpleClans plugin = (SimpleClans) cc.getPlugin();

        Player player = cc.getForWhom().getPlayer();
        String yes = lang("yes", player);
        ClanManager cm = Objects.requireNonNull(plugin).getClanManager();
        ClanPlayer cp = cm.getCreateClanPlayer(player.getUUID());
        Clan clan = cp.getClan();
        if (clan == null) {
            return END_OF_CONVERSATION;
        }

        return yes.equalsIgnoreCase(input) ? confirm(cp, clan) : decline(cp);
    }
}
