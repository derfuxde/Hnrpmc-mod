package org.emil.hnrpmc.simpleclans.commands.data;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.*;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.emil.hnrpmc.simpleclans.utils.VanishUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;
import static net.minecraft.ChatFormatting.*;

public class ClanRoster extends Sendable {

    private final Clan clan;

    public ClanRoster(@NotNull SimpleClans plugin, @NotNull CommandSourceStack sender, @NotNull Clan clan) {
        super(plugin, sender);
        this.clan = clan;
    }

    @Override
    public void send() {
        configureAndSendHeader();
        addLeaders();
        addMembers();

        sendBlock();
    }

    private void addMembers() {
        List<ClanPlayer> members = clan.getNonLeaders();
        plugin.getClanManager().sortClanPlayersByLastSeen(members);
        for (ClanPlayer cp : members) {
            String name = (cp.isTrusted() ? sm.getColored(PAGE_TRUSTED_COLOR) : sm.getColored(PAGE_UNTRUSTED_COLOR)) + cp.getName();
            String lastSeen = VanishUtils.isVanished(sender.getPlayer(), cp) ? WHITE + cp.getLastSeenDaysString(sender) : GREEN + lang("online", sender);

            chatBlock.addRow("  " + name, YELLOW + ChatUtils.parseColors(cp.getRankDisplayName()) + RESET, lastSeen);
        }
    }

    private void addLeaders() {
        List<ClanPlayer> leaders = clan.getLeaders();
        plugin.getClanManager().sortClanPlayersByLastSeen(leaders);
        for (ClanPlayer cp : leaders) {
            String name = sm.getColored(PAGE_LEADER_COLOR) + cp.getName();
            String lastSeen = VanishUtils.isVanished(sender.getPlayer(), cp) ? WHITE + cp.getLastSeenDaysString(sender) :
                    GREEN + lang("online", sender);

            chatBlock.addRow("  " + name, YELLOW + ChatUtils.parseColors(cp.getRankDisplayName()) + RESET, lastSeen);
        }
    }

    private void configureAndSendHeader() {
        ChatBlock.sendBlank(sender);
        ChatBlock.saySingle(sender, sm.getColored(PAGE_CLAN_NAME_COLOR) + clan.getName() + subColor + " " +
                lang("roster", sender) + " " + headColor + Helper.generatePageSeparator(sm.getString(PAGE_SEPARATOR)));
        ChatBlock.sendBlank(sender);
        ChatBlock.sendMessage(sender, headColor + lang("legend", sender) + " " + sm.getColored(PAGE_LEADER_COLOR) +
                lang("leader", sender) + headColor + ", " + sm.getColored(PAGE_TRUSTED_COLOR) + lang("trusted", sender) +
                headColor + ", " + sm.getColored(PAGE_UNTRUSTED_COLOR) + lang("untrusted", sender));
        ChatBlock.sendBlank(sender);

        chatBlock.setFlexibility(false, true, false, true);
        chatBlock.addRow("  " + headColor + lang("player", sender), lang("rank", sender),
                lang("seen", sender));
    }

}
