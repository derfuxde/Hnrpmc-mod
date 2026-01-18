package org.emil.hnrpmc.simpleclans.ui;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.Nullable;

public abstract class MyMenu extends ChestMenu {


    public MyMenu(MenuType<?> type, int containerId, Inventory playerInventory, Container container, int rows) {
        super(type, containerId, playerInventory, container, rows);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // 1. Prüfen, ob der Klick überhaupt in einem gültigen Slot landete
        if (slotId < 0) {
            super.clicked(slotId, button, clickType, player);
            return;
        }

        // 2. Bestimme die ClickAction (PRIMARY oder SECONDARY)
        ClickAction action = (button == 1) ? ClickAction.SECONDARY : ClickAction.PRIMARY;
        if (clickType == ClickType.THROW) {
            action = (button == 0) ? ClickAction.PRIMARY : ClickAction.SECONDARY;
        }

        // 3. Deinen Controller benachrichtigen (WICHTIG!)
        if (player instanceof ServerPlayer serverPlayer) {
            // Hier rufen wir deine Logik auf
            InventoryController.handleInternalClick(serverPlayer, slotId, clickType, action);
        }

        // 4. ITEM-ENTNAHME VERHINDERN:
        // Wir rufen NICHT super.clicked auf, wenn es einer UNSERER Slots ist.
        // Stattdessen schicken wir ein Inventar-Update an den Client, damit
        // das Item visuell wieder zurück in den Slot springt.
        this.sendAllDataToRemote();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}