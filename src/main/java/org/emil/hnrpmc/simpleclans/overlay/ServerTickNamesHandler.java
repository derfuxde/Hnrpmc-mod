package org.emil.hnrpmc.simpleclans.overlay;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.server.level.ServerPlayer;

import org.emil.hnrpmc.Hnrpmc;
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
        // By using ServerTickEvent.Post, we ensure the code only runs ONCE per tick
        // after the server has finished its logic.

        if (plugin == null) {
            plugin = SimpleClans.getInstance();
        }

        counter++;
        if (counter < 100) return; // ~5 seconds
        counter = 0;

        int seccounter = 0;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ClanConfig.Board config = null;
            var root = ClanConfig.get(player.server);
            List<ClanConfig.Board> boards = root.boards();

            for (ClanConfig.Board o : boards) {
                String cons = ClanScoreboard.formatplaceholder(plugin, o.conditions(), player);
                if (ClanScoreboard.checkconditions(cons, player)) {
                    config = o;
                    break; // Wichtig: Nimm das erste passende Board
                }
            }

            if (config == null) return;

            List<String> configLines = config.lines();
            PerPlayerSidebar.update(plugin, player, config.title(), configLines);
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
            // Wir setzen den Status für diesen Spieler zurück, damit PerPlayerSidebar
            // weiß, dass das Objective neu erstellt werden muss (CREATE-Paket)
            PerPlayerSidebar.forceUpdate(player);

            // Sofortige Anzeige triggern, nicht auf den 100-Tick-Counter warten
            updatePlayerScoreboard(player);
        }
    }

    // Lagere die Update-Logik in eine eigene Methode aus, damit du sie
    // sowohl vom Tick als auch vom Join aufrufen kannst
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