package org.emil.hnrpmc.simpleclans.overlay;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.utils.ChatUtils;

import java.util.*;

public final class PerPlayerSidebar {

    private static final int MAX_LINES = 15;

    private static final Map<UUID, State> STATES = new HashMap<>();

    private record State(String objectiveId, Map<Integer, String> lastLines, String lastTitle) {}

    public static void update(SimpleClans plugin, ServerPlayer p, String title, List<String> lines) {
        State st = STATES.computeIfAbsent(p.getUUID(), id -> new State(makeObjectiveId(id), new HashMap<>(), null));

        ensureObjective(p, st, title);

        int n = Math.min(lines.size(), MAX_LINES);

        // Scoreboard: höhere Zahl = weiter oben
        int score = n;

        for (int i = 0; i < n; i++) {
            String entry = entryKey(st.objectiveId(), i);
            String text = ClanScoreboard.formatplaceholder(plugin, lines.get(i), p);
            text = plugin.getSettingsManager().parseConditionalMessage(p, text);

            String prev = st.lastLines().get(i);
            if (!Objects.equals(prev, text)) {
                // SetScore: entry bekommt score im objective
                p.connection.send(new ClientboundSetScorePacket(
                        entry,
                        st.objectiveId(),
                        score,
                        Optional.of(Component.literal(text)),
                        Optional.empty()
                ));

                st.lastLines().put(i, text);
            }
            score--;
        }

        // Entferne alte restliche Zeilen
        for (int i = n; i < MAX_LINES; i++) {
            if (st.lastLines().containsKey(i)) {
                String entry = entryKey(st.objectiveId(), i);
                p.connection.send(new ClientboundResetScorePacket(entry, st.objectiveId()));
                st.lastLines().remove(i);
            }
        }
    }

    public static void TabListUpdate(SimpleClans plugin, ServerPlayer player, String header,  String footer) {
        String formatedheader = ChatUtils.parseColors(ClanScoreboard.formatplaceholder(SimpleClans.getInstance(), header, player));
        String formatedfooter = ChatUtils.parseColors(ClanScoreboard.formatplaceholder(SimpleClans.getInstance(), footer, player));
        player.setTabListHeaderFooter(Component.literal(plugin.getSettingsManager().parseConditionalMessage(player, formatedheader)),Component.literal(plugin.getSettingsManager().parseConditionalMessage(player, formatedfooter)) );
    }
    String tablistgead = """
                    
                    &0&m                                                &7
                    &r&3&lSurvival
                    &r&7&l>> Willkommen&3 &l%playername%&7&l! &7&l<<
                    &r&7Aktive Spieler: &f%server_players%
                    
                    """;

    String tablistfoot = """
                    %ani_time%
                    
                    &0&m                                                &7
                    """;
    public static void forceUpdate(ServerPlayer p) {
        STATES.remove(p.getUUID());
    }

    private static void ensureObjective(ServerPlayer p, State st, String title) {
        // Wenn der Titel gleich ist, müssen wir nichts tun
        if (Objects.equals(st.lastTitle(), title)) return;

        Scoreboard dummy = new Scoreboard();
        Objective obj = new Objective(
                dummy,
                st.objectiveId(),
                ObjectiveCriteria.DUMMY,
                Component.literal(title),
                ObjectiveCriteria.RenderType.HEARTS,
                true,
                null
        );

        // Wenn lastTitle null ist, wurde es für diesen Spieler noch nie erstellt
        if (st.lastTitle() == null) {
            // CREATE (0)
            p.connection.send(new ClientboundSetObjectivePacket(obj, 0));
            // DISPLAY (Sidebar)
            p.connection.send(new ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, obj));
        } else {
            p.connection.send(new ClientboundSetObjectivePacket(obj, 2));
        }

        STATES.put(p.getUUID(), new State(st.objectiveId(), st.lastLines(), title));
    }

    private static String makeObjectiveId(UUID uuid) {
        String h = Integer.toHexString(uuid.hashCode());
        h = (h.length() > 14) ? h.substring(0, 14) : String.format("%1$14s", h).replace(' ', '0');
        return "ui" + h;
    }

    private static String entryKey(String objId, int index) {
        return "l" + String.format("%02d", index) + objId.substring(0, 4);
    }
}
