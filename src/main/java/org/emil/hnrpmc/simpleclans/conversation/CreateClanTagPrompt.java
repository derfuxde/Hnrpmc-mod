package org.emil.hnrpmc.simpleclans.conversation;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.conversation.dings.ClanStringPrompt;
import org.emil.hnrpmc.simpleclans.conversation.dings.Convosable;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class CreateClanTagPrompt extends ClanStringPrompt {
    public static final String TAG_KEY = "tag";

    @Override
    public @NotNull String getPromptText(@NotNull SCConversation context) {
        UUID forWhom = context.getForWhom().getUUID();
        if (context.getSessionData(TAG_KEY) != null) {
            return "";
        }
        return lang("insert.clan.tag", forWhom, lang("cancel", forWhom));
    }

    @Override
    public boolean blocksForInput(@NotNull SCConversation context) {
        return context.getSessionData(TAG_KEY) == null;
    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull SCConversation context, @Nullable String input) {
        Convosable player = context.getForWhom();
        SimpleClans plugin = (SimpleClans) context.getPlugin();
        input = input != null ? input : (String) context.getSessionData(TAG_KEY);
        context.setSessionData(TAG_KEY, null);
        if (input == null || plugin == null) return this;

        Prompt errorPrompt = validateTag(plugin, player, input);
        if (errorPrompt != null) return errorPrompt;
        context.setSessionData(TAG_KEY, input);
        return new CreateClanNamePrompt();
    }

    @Nullable
    private Prompt validateTag(SimpleClans plugin, Convosable player, @NotNull String clanTag) {
        String cleanTag = ChatUtils.stripColors(clanTag);
        if (plugin.getClanManager().isClan(cleanTag)) {
            return new MessagePromptImpl(ChatFormatting.RED +
                    lang("clan.with.this.tag.already.exists", player.getUUID()), this);
        }

        Optional<String> validationError = plugin.getTagValidator().validate(player.getPlayer(), clanTag);
        return validationError.map(error -> new MessagePromptImpl(error, this)).orElse(null);
    }
}
