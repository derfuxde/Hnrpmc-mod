package org.emil.hnrpmc.simpleclans.commands.data;

import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.Helper;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.utils.KDRFormat;
import org.emil.hnrpmc.simpleclans.utils.RankingNumberResolver;
import org.emil.hnrpmc.simpleclans.utils.VanishUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;
import static net.minecraft.ChatFormatting.*;

public class Leaderboard extends Sendable {

    private final RankingNumberResolver<ClanPlayer, BigDecimal> rankingResolver;
    private final List<ClanPlayer> clanPlayers;

    public Leaderboard(@NotNull SimpleClans plugin, @NotNull CommandSourceStack sender) {
        super(plugin, sender);
        clanPlayers = cm.getAllClanPlayers();
        rankingResolver = new RankingNumberResolver<>(clanPlayers,
                c -> KDRFormat.toBigDecimal(c.getKDR()), false, sm.getRankingType());
    }

    @Override
    public void send() {
        configureAndSendHeader();
        addLines();
        sendBlock();
    }

    private void addLines() {
        for (ClanPlayer cp : clanPlayers) {
            boolean online = !VanishUtils.isVanished(cp);

            String name = (cp.isLeader() ? sm.getColored(PAGE_LEADER_COLOR) : (cp.isTrusted() ? sm.getColored(PAGE_TRUSTED_COLOR) :
                    sm.getColored(PAGE_UNTRUSTED_COLOR))) + cp.getName();
            String lastSeen = online ? GREEN + lang("online", sender) : WHITE + cp.getLastSeenDaysString(sender);

            String clanTag = WHITE + lang("free.agent", sender);

            if (cp.getClan() != null) {
                clanTag = cp.getClan().getColorTag();
            }

            chatBlock.addRow("  " + rankingResolver.getRankingNumber(cp),
                    name, YELLOW + "" + KDRFormat.format(cp.getKDR()), WHITE + clanTag, lastSeen);
        }
    }

    private void configureAndSendHeader() {
        ChatBlock.sendBlank(sender);
        ChatBlock.saySingle(sender, sm.getColored(SERVER_NAME) + subColor + " " + lang("leaderboard.command", sender)
                + " " + headColor + Helper.generatePageSeparator(sm.getString(PAGE_SEPARATOR)));
        ChatBlock.sendBlank(sender);
        ChatBlock.sendMessage(sender, headColor + lang("total.clan.players.0", sender, subColor +
                clanPlayers.size()));
        ChatBlock.sendBlank(sender);

        chatBlock.setAlignment("c", "l", "c", "c", "c", "c");
        chatBlock.addRow("  " + headColor + lang("rank", sender), lang("player", sender),
                lang("kdr", sender), lang("clan", sender), lang("seen", sender));
    }
}
