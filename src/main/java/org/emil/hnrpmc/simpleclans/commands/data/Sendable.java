package org.emil.hnrpmc.simpleclans.commands.data;

import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.managers.ClanManager;
import org.emil.hnrpmc.simpleclans.managers.SettingsManager;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public abstract class Sendable {

    protected final SimpleClans plugin;
    protected final SettingsManager sm;
    protected final ClanManager cm;
    protected final CommandSourceStack sender;
    protected final ChatBlock chatBlock = new ChatBlock();
    protected final String headColor;
    protected final String subColor;


    public Sendable(@NotNull SimpleClans plugin, @NotNull CommandSourceStack sender) {
        this.plugin = plugin;
        sm = plugin.getSettingsManager();
        cm = plugin.getClanManager();
        this.sender = sender;
        headColor = sm.getColored(PAGE_HEADINGS_COLOR);
        subColor = sm.getColored(PAGE_SUBTITLE_COLOR);
    }

    protected void sendBlock() {
        SettingsManager sm = plugin.getSettingsManager();
        boolean more = chatBlock.sendBlock(sender, sm.getInt(PAGE_SIZE));

        if (more) {
            plugin.getStorageManager().addChatBlock(sender, chatBlock);
            ChatBlock.sendBlank(sender);
            ChatBlock.sendMessage(sender, sm.getColored(PAGE_HEADINGS_COLOR) + lang("view.next.page", sender,
                    sm.getString(COMMANDS_MORE)));
        }
        ChatBlock.sendBlank(sender);
    }

    public abstract void send();
}
