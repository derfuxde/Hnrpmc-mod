package org.emil.hnrpmc.simpleclans.ui.frames;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.ui.InventoryDrawer;
import org.emil.hnrpmc.simpleclans.ui.SCComponent;
import org.emil.hnrpmc.simpleclans.ui.SCComponentImpl;
import org.emil.hnrpmc.simpleclans.ui.SCFrame;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class ConfirmationFrame extends SCFrame {

    private final Runnable listener;

    public ConfirmationFrame(@Nullable SCFrame parent, @NotNull Player viewer, @Nullable Runnable listener) {
        super(parent, (ServerPlayer) viewer);
        this.listener = listener;
    }

    @Override
    public @NotNull String getTitle() {
        return lang("gui.confirmation.title", getViewer());
    }

    @Override
    public int getSize() {
        return 3 * 9;
    }

    @Override
    public void createComponents() {
        SCComponent confirm = new SCComponentImpl.Builder(Items.LIME_WOOL.getDefaultInstance())
                .withDisplayName(lang("gui.confirmation.confirm", getViewer())).withSlot(12).build();
        confirm.setListener(ClickType.PICKUP, ClickAction.PRIMARY, listener);
        add(confirm);

        SCComponent returnC = new SCComponentImpl.Builder(Items.RED_WOOL.getDefaultInstance())
                .withDisplayName(lang("gui.confirmation.return", getViewer())).withSlot(14).build();
        returnC.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> InventoryDrawer.open(getViewer(), getParent()));
        add(returnC);
    }
}
