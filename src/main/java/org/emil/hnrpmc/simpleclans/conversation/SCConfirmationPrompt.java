package org.emil.hnrpmc.simpleclans.conversation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public abstract class SCConfirmationPrompt {
    protected final SimpleClans plugin;
    protected final ServerPlayer player;
    protected final SCConversation conversation;

    public SCConfirmationPrompt(SimpleClans plugin, ServerPlayer player, SCConversation conversation) {
        this.plugin = plugin;
        this.player = player;
        this.conversation = conversation;

        // Zeige die Bestätigungsfrage an (z.B. "Bist du sicher? (ja/nein)")
        player.sendSystemMessage(Component.literal(lang(getPromptTextKey(), player))
                .withStyle(ChatFormatting.YELLOW));
    }

    public void handleInput(String input) {
        String confirmKey = lang("confirmation.yes", player); // Standardmäßig "yes" oder "ja"
        String declineKey = lang("confirmation.no", player);   // Standardmäßig "no" oder "nein"

        if (input.equalsIgnoreCase(confirmKey) || input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("ja")) {
            ClanPlayer cp = plugin.getClanManager().getClanPlayer(player.getUUID());
            if (cp != null && cp.getClan() != null) {
                onConfirm(cp, cp.getClan());
            } else {
                conversation.abandon();
            }
        } else if (input.equalsIgnoreCase(declineKey) || input.equalsIgnoreCase("no") || input.equalsIgnoreCase("nein")) {
            player.sendSystemMessage(Component.literal(lang(getDeclineTextKey(), player))
                    .withStyle(ChatFormatting.GRAY));
            conversation.abandon();
        }
    }

    protected abstract void onConfirm(ClanPlayer sender, Clan clan);
    protected abstract String getPromptTextKey();
    protected abstract String getDeclineTextKey();
}