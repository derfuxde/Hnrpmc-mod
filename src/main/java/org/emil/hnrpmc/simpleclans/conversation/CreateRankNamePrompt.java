package org.emil.hnrpmc.simpleclans.conversation;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.conversation.dings.ClanStringPrompt;
import org.emil.hnrpmc.simpleclans.events.CreateRankEvent;
import org.emil.hnrpmc.simpleclans.events.PreCreateRankEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class CreateRankNamePrompt extends ClanStringPrompt {
    @Override
    public @NotNull String getPromptText(@NotNull SCConversation context) {
        Player forWhom = context.getForWhom().getPlayer();
        return lang("insert.rank.name", forWhom, lang("cancel", forWhom));
    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull SCConversation context, @Nullable String input) {
        SimpleClans plugin = (SimpleClans) context.getPlugin();
        Player player = context.getForWhom().getPlayer();
        Clan clan = (Clan) context.getSessionData("clan");
        if (clan == null || plugin == null) return END_OF_CONVERSATION;
        if (input == null) return this;

        String rank = input.toLowerCase().replace(" ", "_");
        PreCreateRankEvent event = new PreCreateRankEvent(player, clan, rank);
        NeoForge.EVENT_BUS.post(event);
        rank = event.getRankName();

        if (event.isCanceled()) {
            return null;
        }

        if (clan.hasRank(rank)) {
            return new MessagePromptImpl(ChatFormatting.RED + lang("rank.already.exists", player), this);
        }

        clan.createRank(rank);
        NeoForge.EVENT_BUS.post(new CreateRankEvent(player, clan, clan.getRank(rank)));
        plugin.getStorageManager().updateClan(clan, true);
        return new MessagePromptImpl(ChatFormatting.AQUA + lang("rank.created", player));
    }
}
