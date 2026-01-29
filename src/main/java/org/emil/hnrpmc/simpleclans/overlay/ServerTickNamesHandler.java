package org.emil.hnrpmc.simpleclans.overlay;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.hnessentials.network.AdminUpdateDataPayload;
import org.emil.hnrpmc.hnessentials.network.ClientPacketHandler;
import org.emil.hnrpmc.hnessentials.network.OpenAdminScreenPayload;
import org.emil.hnrpmc.hnessentials.network.ServerPacketHandler;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.config.ClanConfig;
import org.emil.hnrpmc.simpleclans.hooks.discord.DiscordHook;

import java.util.List;

@EventBusSubscriber(modid = Hnrpmc.MODID)
public final class ServerTickNamesHandler {

    private static int counter = 0;

    private static SimpleClans plugin;

    private static DiscordHook discordHook;



    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        // Die aktuellen Kosten, die durch die Kombination entstehen würden
        long cost = event.getCost();

        event.setCost(cost/2);

        if (cost >= 40) {
            event.setCost(39);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        if (plugin == null) {
            plugin = SimpleClans.getInstance();
        }

        counter++;
        if (counter < 20) return; // ~5 seconds
        counter = 0;

        //PerPlayerSidebar.TabListUpdate(event.getServer());

        int seccounter = 0;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ClanConfig.Board config = null;
            ClanConfig.Tablist tab_config = null;
            var root = ClanConfig.get(player.server);
            List<ClanConfig.Board> boards = root.boards();
            List<ClanConfig.Tablist> tablists = root.tablists();

            for (ClanConfig.Board o : boards) {
                String cons = ClanScoreboard.formatplaceholder(plugin, o.conditions(), player);
                if (ClanScoreboard.checkconditions(cons, player)) {
                    config = o;
                    break; // Wichtig: Nimm das erste passende Board
                }
            }

            if (tablists == null) {
                tablists = List.of(
                        new ClanConfig.Tablist("tab", "true",
                                        """
                                        &0&m                                                &7
                                        &r&3&lSurvival
                                        &r&7&l>> Willkommen&3 &l%playername%&7&l! &7&l<<
                                        &r&7Aktive Spieler: &f%server_players%
                                        
                                        """,
                                        """
                                        %hnph_ani_time%
                                        
                                        &0&m                                                &7
                                        """
                        )
            );}

            for (ClanConfig.Tablist o : tablists) {
                String cons = ClanScoreboard.formatplaceholder(plugin, o.conditions(), player);
                if (ClanScoreboard.checkconditions(cons, player)) {
                    tab_config = o;
                    break;
                }
            }

            if (config == null) return;

            List<String> configLines = config.lines();
            PerPlayerSidebar.update(plugin, player, config.title(), configLines);
            NameDisplayService.update(plugin, player);
            seccounter++;

            String tab_configLines = tab_config.footer();
            PerPlayerSidebar.TabListUpdate(plugin, player, tab_config.header(), tab_configLines);
            NameDisplayService.update(plugin, player);
            seccounter++;
        }
        seccounter = 0;
    }

    @SubscribeEvent
    public static void onPlayerJoin(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (plugin == null) {
            plugin = SimpleClans.getInstance();
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            PerPlayerSidebar.forceUpdate(player);
            //PerPlayerSidebar.TabListUpdate(plugin, player);

            updatePlayerScoreboard(player);
        }
    }

    private static void updatePlayerScoreboard(ServerPlayer player) {
        if (plugin == null) {
            plugin = SimpleClans.getInstance();
        }
        var root = ClanConfig.get(player.server);
        List<ClanConfig.Board> boards = root.boards();
        ClanConfig.Board config = null;

        for (ClanConfig.Board o : boards) {
            String cons = ClanScoreboard.formatplaceholder(plugin, o.conditions(), player);
            if (ClanScoreboard.checkconditions(cons, player)) {
                config = o;
                break;
            }
        }

        if (config != null) {
            PerPlayerSidebar.update(plugin, player, config.title(), config.lines());
        }
    }
}