package org.emil.hnrpmc.simpleclans.commands.data;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.ChatBlock;
import org.emil.hnrpmc.simpleclans.Helper;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.PAGE_SEPARATOR;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.SERVER_NAME;

public class MostKilled extends Sendable {

    private final ServerPlayer player;

    public MostKilled(@NotNull SimpleClans plugin, @NotNull ServerPlayer player) {
        // Sendable nutzt den CommandSourceStack für die Ausgabe
        super(plugin, player.createCommandSourceStack());
        this.player = player;
    }

    @Override
    public void send() {
        // Asynchroner Datenabruf
        plugin.getStorageManager().getMostKilled(data -> {
            // Zurück auf den Hauptthread für Chat/UI Operationen
            player.getServer().execute(() -> {
                if (data.isEmpty()) {
                    ChatBlock.sendMessage(player.createCommandSourceStack(),
                            ChatFormatting.RED + lang("nokillsfound", player));
                    return;
                }

                sendHeader();

                Map<String, Integer> killsPerPlayer = Helper.sortByValue(data);

                for (Map.Entry<String, Integer> attackerVictim : killsPerPlayer.entrySet()) {
                    addLine(attackerVictim);
                }

                sendBlock();
            });
        });
    }

    private void addLine(Map.Entry<String, Integer> attackerVictim) {
        String[] split = attackerVictim.getKey().split(" ");

        if (split.length < 2) {
            return;
        }

        int count = attackerVictim.getValue();
        String attacker = split[0];
        String victim = split[1];

        // Verwendung von ChatFormatting für Farben
        chatBlock.addRow("  " + ChatFormatting.WHITE + victim,
                ChatFormatting.AQUA + String.valueOf(count),
                ChatFormatting.YELLOW + attacker);
    }

    private void sendHeader() {
        chatBlock.setFlexibility(true, false, false);
        chatBlock.setAlignment("l", "c", "l");
        chatBlock.addRow("  " + headColor + lang("victim", player),
                headColor + lang("killcount", player),
                headColor + lang("attacker", player));

        ChatBlock.saySingle(player.createCommandSourceStack(),
                sm.getColored(SERVER_NAME) + subColor + " " + lang("mostkilled", player) + " " +
                        headColor + Helper.generatePageSeparator(sm.getString(PAGE_SEPARATOR)));

        ChatBlock.sendBlank(player.createCommandSourceStack());
    }
}