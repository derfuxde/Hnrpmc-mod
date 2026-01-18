package org.emil.hnrpmc.simpleclans.conversation;

import net.minecraft.ChatFormatting;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

/**
 * @author roinujnosde
 */
public class ResignPrompt extends ConfirmationPrompt {

    @Override
    protected Prompt confirm(ClanPlayer sender, Clan clan) {
        if (clan.isPermanent() || !sender.isLeader() || clan.getLeaders().size() > 1) {
            clan.addBb(sender.getName(), lang("0.has.resigned", sender.getUniqueId()));
            sender.addResignTime(clan.getTag());
            clan.removePlayerFromClan(sender.getUniqueId());

            return new MessagePromptImpl(ChatFormatting.AQUA + lang("resign.success", sender.getUniqueId()));
        } else if (sender.isLeader() && clan.getLeaders().size() == 1) {
            clan.disband(sender.toPlayer().createCommandSourceStack(), true, false);
            return new MessagePromptImpl(ChatFormatting.RED + lang("clan.has.been.disbanded", sender.getUniqueId(), clan.getName()));
        } else {
            return new MessagePromptImpl(ChatFormatting.RED + lang("last.leader.cannot.resign.you.must.appoint.another.leader.or.disband.the.clan", sender.getUniqueId()));
        }
    }

    @Override
    protected String getPromptTextKey() {
        return "resign.confirmation";
    }

    @Override
    protected String getDeclineTextKey() {
        return "resign.request.cancelled";
    }
}
