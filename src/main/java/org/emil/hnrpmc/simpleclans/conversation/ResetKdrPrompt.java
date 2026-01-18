package org.emil.hnrpmc.simpleclans.conversation;

import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.common.NeoForge;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.events.PlayerResetKdrEvent;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class ResetKdrPrompt extends ConfirmationPrompt {

    private final ClanManager cm;

    public ResetKdrPrompt(ClanManager cm) {
        this.cm = cm;
    }

    @Override
    protected Prompt confirm(ClanPlayer sender, Clan clan) {
        PlayerResetKdrEvent event = new PlayerResetKdrEvent(sender);
        NeoForge.EVENT_BUS.post(event);
        if (!event.isCanceled() && cm.purchaseResetKdr(sender.toPlayer())) {
            cm.resetKdr(sender);
            return new MessagePromptImpl(ChatFormatting.RED + lang("you.have.reseted.your.kdr", sender.getUniqueId()));
        } else {
            return Prompt.END_OF_CONVERSATION;
        }
    }

    @Override
    protected String getPromptTextKey() {
        return "resetkdr.confirmation";
    }

    @Override
    protected String getDeclineTextKey() {
        return "resetkdr.request.cancelled";
    }

}
