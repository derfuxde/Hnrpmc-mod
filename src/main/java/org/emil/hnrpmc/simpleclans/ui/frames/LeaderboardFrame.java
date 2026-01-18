package org.emil.hnrpmc.simpleclans.ui.frames;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.ui.InventoryDrawer;
import org.emil.hnrpmc.simpleclans.ui.SCComponent;
import org.emil.hnrpmc.simpleclans.ui.SCComponentImpl;
import org.emil.hnrpmc.simpleclans.ui.SCFrame;
import org.emil.hnrpmc.simpleclans.utils.KDRFormat;
import org.emil.hnrpmc.simpleclans.utils.Paginator;
import org.emil.hnrpmc.simpleclans.utils.RankingNumberResolver;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class LeaderboardFrame extends SCFrame {

    private final Paginator paginator;
    private final List<ClanPlayer> clanPlayers;
    private final RankingNumberResolver<ClanPlayer, BigDecimal> rankingResolver;

    public LeaderboardFrame(Player viewer, SCFrame parent) {
        super(parent, (ServerPlayer) viewer);

        SimpleClans plugin = SimpleClans.getInstance();
        clanPlayers = plugin.getClanManager().getAllClanPlayers();

        rankingResolver = new RankingNumberResolver<>(clanPlayers, c -> KDRFormat.toBigDecimal(c.getKDR()), false,
                plugin.getSettingsManager().getRankingType());
        paginator = new Paginator(getSize() - 9, this.clanPlayers);
    }

    @Override
    public void createComponents() {
        for (int slot = 0; slot < 9; slot++) {
            if (slot == 2 || slot == 6 || slot == 7)
                continue;
            add(Components.getPanelComponent(slot));
        }
        add(Components.getBackComponent(getParent(), 2, getViewer()));

        add(Components.getPreviousPageComponent(6, this::previousPage, paginator, getViewer()));
        add(Components.getNextPageComponent(7, this::nextPage, paginator, getViewer()));

        int slot = 9;
        for (int i = paginator.getMinIndex(); paginator.isValidIndex(i); i++) {
            ClanPlayer cp = clanPlayers.get(i);
            SCComponent c = new SCComponentImpl(
                    lang("gui.leaderboard.player.title", getViewer(),
                            rankingResolver.getRankingNumber(cp), cp.getName()),
                    Arrays.asList(
                            cp.getClan() == null ? lang("gui.playerdetails.player.lore.noclan", getViewer())
                                    : lang("gui.playerdetails.player.lore.clan", getViewer(),
                                    cp.getClan().getColorTag(), cp.getClan().getName()),
                            lang("gui.playerdetails.player.lore.kdr", getViewer(), KDRFormat.format(cp.getKDR())),
                            lang("gui.playerdetails.player.lore.last.seen", getViewer(), cp.getLastSeenString(getViewer()))),
                    Items.PLAYER_HEAD, slot);
            GameProfile offlinePlayer = SimpleClans.getInstance().getServer().getProfileCache().get(cp.getUniqueId()).get();
            Components.setOwningPlayer(c.getItem(), offlinePlayer);
            c.setListener(ClickType.PICKUP, ClickAction.PRIMARY,
                    () -> InventoryDrawer.open(getViewer(), new PlayerDetailsFrame(getViewer(), this, offlinePlayer)));
            c.setLorePermission("simpleclans.anyone.leaderboard");
            add(c);
            slot++;
        }
    }

    private void previousPage() {
        if (paginator.previousPage()) {
            updateFrame();
        }
    }

    private void nextPage() {
        if (paginator.nextPage()) {
            updateFrame();
        }
    }

    private void updateFrame() {
        InventoryDrawer.open(getViewer(), this);
    }

    @Override
    public @NotNull String getTitle() {
        return lang("gui.leaderboard.title", getViewer(), clanPlayers.size());
    }

    @Override
    public int getSize() {
        return 6 * 9;
    }

}
