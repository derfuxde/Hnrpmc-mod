package org.emil.hnrpmc.simpleclans.commands.data;

import net.minecraft.commands.CommandSourceStack;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.Helper;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.PAGE_SEPARATOR;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.SERVER_NAME;
import static net.minecraft.ChatFormatting.AQUA;
import static net.minecraft.ChatFormatting.DARK_GRAY;

public class Alliances extends Sendable {

    public Alliances(@NotNull SimpleClans plugin, @NotNull CommandSourceStack sender) {
        super(plugin, sender);
    }

    @Override
    public void send() {
        List<Clan> clans = cm.getClans();
        cm.sortClansByKDR(clans);
        sendHeader();

        for (Clan clan : clans) {
            if (!clan.isVerified()) {
                continue;
            }

            chatBlock.addRow("  " + AQUA + clan.getName(), clan.getAllyString(DARK_GRAY + ", ", sender));
        }

        sendBlock();
    }

    private void sendHeader() {
        ChatBlock.sendBlank(sender);
        ChatBlock.saySingle(sender, sm.getColored(SERVER_NAME) + subColor + " " +
                lang("alliances", sender) + " " + headColor +
                Helper.generatePageSeparator(sm.getString(PAGE_SEPARATOR)));
        ChatBlock.sendBlank(sender);

        chatBlock.setAlignment("l", "l");
        chatBlock.addRow("  " + headColor + lang("clan", sender), lang("allies", sender));
    }
}
