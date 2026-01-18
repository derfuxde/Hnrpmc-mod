package org.emil.hnrpmc.simpleclans.conversation;

import net.minecraft.ChatFormatting;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.conversation.ConfirmationPrompt;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class DisbandPrompt extends ConfirmationPrompt {

    @Override
    protected Prompt confirm(ClanPlayer sender, Clan clan) {
        if (clan.isPermanent()) {
            return new MessagePromptImpl(ChatFormatting.RED + lang("cannot.disband.permanent", sender.getUniqueId()));
        }

        clan.disband(sender.toPlayer().createCommandSourceStack(), true, false);
        return new MessagePromptImpl(ChatFormatting.RED + lang("clan.has.been.disbanded", sender.toPlayer(), sender.toPlayer(), clan.getName()));
    }

    @Override
    protected String getPromptTextKey() {
        return "disband.confirmation";
    }

    @Override
    protected String getDeclineTextKey() {
        return "disband.request.cancelled";
    }

}
