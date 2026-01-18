package org.emil.hnrpmc.simpleclans.commands.data;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.Helper;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.PAGE_CLAN_NAME_COLOR;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.PAGE_SEPARATOR;

public class Kills extends Sendable {

    private final ServerPlayer player;
    private final String polled;

    public Kills(@NotNull SimpleClans plugin, @NotNull ServerPlayer player, @NotNull String polled) {
        // Sendable nutzt in NeoForge direkt den CommandSourceStack des Players
        super(plugin, player.createCommandSourceStack());
        this.player = player;
        this.polled = polled;
    }

    @Override
    public void send() {
        // Wir holen die Daten asynchron vom Storage
        plugin.getStorageManager().getKillsPerPlayer(polled, data -> {
            // Um UI/Chat-Operationen sicher auszuführen, springen wir zurück auf den Main-Thread des Servers
            player.getServer().execute(() -> {
                if (data.isEmpty()) {
                    ChatBlock.sendMessage(player.createCommandSourceStack(), ChatFormatting.RED + lang("nokillsfound", player));
                    return;
                }
                configureAndSendHeader();
                addLines(data);
                sendBlock();
            });
        });
    }

    private void addLines(Map<String, Integer> data) {
        Map<String, Integer> killsPerPlayer = Helper.sortByValue(data);

        for (Map.Entry<String, Integer> playerKills : killsPerPlayer.entrySet()) {
            int count = playerKills.getValue();
            // In NeoForge nutzen wir ChatFormatting statt ChatColor
            chatBlock.addRow("  " + playerKills.getKey(), ChatFormatting.AQUA + String.valueOf(count));
        }
    }

    private void configureAndSendHeader() {
        chatBlock.setFlexibility(true, false);
        chatBlock.setAlignment("l", "c");
        chatBlock.addRow("  " + headColor + lang("victim", player), lang("killcount", player));

        // Header senden
        ChatBlock.saySingle(player.createCommandSourceStack(),
                sm.getColored(PAGE_CLAN_NAME_COLOR) + polled + subColor
                        + " " + lang("kills", player) + " " + headColor +
                        Helper.generatePageSeparator(sm.getString(PAGE_SEPARATOR)));

        ChatBlock.sendBlank(player.createCommandSourceStack());
    }
}