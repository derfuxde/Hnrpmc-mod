package org.emil.hnrpmc.simpleclans.commands.data;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.emil.hnrpmc.simpleclans.*;
import org.emil.hnrpmc.simpleclans.utils.VanishUtils;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.awt.Color.WHITE;
import static net.minecraft.ChatFormatting.AQUA;
import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;
import static org.emil.hnrpmc.simpleclans.managers.SettingsManager.ConfigField.*;

public class ClanCoords extends Sendable {

    private final Player player;
    private final Clan clan;

    public ClanCoords(@NotNull SimpleClans plugin, @NotNull Player player, @NotNull Clan clan) {
        super(plugin, player.createCommandSourceStack());
        this.player = player;
        this.clan = clan;
    }

    private void populateRows() {
        Map<Integer, List<String>> rows = new TreeMap<>();
        for (ClanPlayer cpm : VanishUtils.getNonVanished((ServerPlayer) player, clan)) {
            Player p = cpm.toPlayer();

            if (p != null) {
                String name = (cpm.isLeader() ? sm.getColored(PAGE_LEADER_COLOR) : (cpm.isTrusted() ?
                        sm.getColored(PAGE_TRUSTED_COLOR) : sm.getColored(PAGE_UNTRUSTED_COLOR))) + cpm.getName();
                Vec3 loc = p.position();
                int distance = (int) Math.ceil(loc.distanceTo(player.position()));
                String coords = loc.get(Direction.Axis.X) + " " + loc.get(Direction.Axis.Y) + " " + loc.get(Direction.Axis.Z);
                String world = player.getServer().getWorldData() == null ? "-" : player.getServer().getWorldData().getLevelName();

                List<String> cols = new ArrayList<>();
                cols.add("  " + name);
                cols.add(AQUA + "" + distance);
                cols.add(WHITE + "" + coords);
                cols.add(world);
                rows.put(distance, cols);
            }
        }
        for (List<String> col : rows.values()) {
            chatBlock.addRow(col.get(0), col.get(1), col.get(2), col.get(3));
        }
    }

    private void configureAndSendHeader() {
        chatBlock.setFlexibility(true, false, false, false);
        chatBlock.setAlignment("l", "c", "c", "c");

        ChatBlock.sendBlank(player.createCommandSourceStack());
        ChatBlock.saySingle(player.createCommandSourceStack(), sm.getColored(PAGE_CLAN_NAME_COLOR) + clan.getName() + subColor + " " +
                lang("coords", player) + " " + headColor + Helper.generatePageSeparator(sm.getString(PAGE_SEPARATOR)));
        ChatBlock.sendBlank(player.createCommandSourceStack());

        chatBlock.addRow("  " + headColor + lang("name", player), lang("distance", player),
                lang("coords.upper", player), lang("world", player));
    }

    @Override
    public void send() {
        configureAndSendHeader();
        populateRows();

        sendBlock();
    }
}
