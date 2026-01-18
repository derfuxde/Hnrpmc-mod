package org.emil.hnrpmc.simpleclans.ui.frames;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.RankPermission;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.ui.InventoryDrawer;
import org.emil.hnrpmc.simpleclans.ui.SCComponent;
import org.emil.hnrpmc.simpleclans.ui.SCComponentImpl;
import org.emil.hnrpmc.simpleclans.ui.SCFrame;
import org.emil.hnrpmc.simpleclans.utils.Paginator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public final class CoordsFrame extends SCFrame {

    private final List<ClanPlayer> allMembers;
    private final Paginator paginator;
    private final SimpleClans plugin = SimpleClans.getInstance();

    public CoordsFrame(@NotNull ServerPlayer viewer, SCFrame parent, @NotNull Clan subject) {
        super(parent, viewer);

        this.allMembers = new ArrayList<>();
        for (ClanPlayer cp : subject.getMembers()) {
            if (cp == null) continue;

            var p = cp.toPlayer();
            if (!(p instanceof ServerPlayer sp)) continue;

            // Vanish-Filter: nur anzeigen, wenn viewer den Spieler sehen darf
            if (plugin.getVanishService() != null && plugin.getVanishService().isVanished(viewer, sp)) continue;

            allMembers.add(cp);
        }

        allMembers.sort(Comparator.comparing(ClanPlayer::isLeader).reversed());

        this.paginator = new Paginator(getSize() - 9, allMembers);
    }

    @Override
    public void createComponents() {
        // Panels (deine Components-Klasse muss NeoForge-tauglich sein, ansonsten ersetze durch eigene Icons)
        for (int slot = 0; slot < 9; slot++) {
            if (slot == 2 || slot == 6 || slot == 7) continue;
            add(Components.getPanelComponent(slot));
        }

        add(Components.getBackComponent(getParent(), 2, (ServerPlayer) getViewer()));
        add(Components.getPreviousPageComponent(6, this::previousPage, paginator, (ServerPlayer) getViewer()));
        add(Components.getNextPageComponent(7, this::nextPage, paginator, (ServerPlayer) getViewer()));

        int slot = 9;

        ServerPlayer viewer = (ServerPlayer) getViewer();

        for (int i = paginator.getMinIndex(); paginator.isValidIndex(i); i++) {
            ClanPlayer cp = allMembers.get(i);

            if (!(cp.toPlayer() instanceof ServerPlayer target)) continue;

            double dist = viewer.position().distanceTo(target.position());
            int distance = (int) Math.ceil(dist);

            int x = target.blockPosition().getX();
            int y = target.blockPosition().getY();
            int z = target.blockPosition().getZ();
            String dim = target.level().dimension().location().toString();

            SCComponent c = new SCComponentImpl(
                    lang("gui.playerdetails.player.title", viewer, cp.getName()),
                    List.of(
                            lang("gui.coords.player.lore.distance", viewer, distance),
                            lang("gui.coords.player.lore.coords", viewer, x, y, z),
                            lang("gui.coords.player.lore.world", viewer, dim)
                    ),
                    Items.PLAYER_HEAD,
                    slot
            );

            setOwningPlayerHead(c.getItem(), target.getGameProfile());

            c.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () ->
                    InventoryDrawer.open(getViewer(), new PlayerDetailsFrame(viewer, this, SimpleClans.getInstance().getServer().getProfileCache().get(target.getUUID()).get()))
            );

            c.setLorePermission(RankPermission.COORDS);
            add(c);
            slot++;
        }
    }

    private void previousPage() {
        if (paginator.previousPage()) InventoryDrawer.open(getViewer(), this);
    }

    private void nextPage() {
        if (paginator.nextPage()) InventoryDrawer.open(getViewer(), this);
    }

    @Override
    @NotNull
    public String getTitle() {
        return lang("gui.coords.title", (ServerPlayer) getViewer());
    }

    @Override
    public int getSize() {
        return 6 * 9;
    }

    private static void setOwningPlayerHead(@NotNull ItemStack stack, @NotNull GameProfile profile) {
        // 1.20.5+ / 1.21+: PlayerHead nutzt PROFILE DataComponent
        stack.set(DataComponents.PROFILE, new ResolvableProfile(profile));

        // Optional: Wenn du Name/Lore nochmal setzen willst:
        // stack.set(DataComponents.CUSTOM_NAME, Component.literal("..."));
        // stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("..."))));
    }
}
