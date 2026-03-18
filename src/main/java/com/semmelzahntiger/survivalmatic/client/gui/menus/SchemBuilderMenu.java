package com.semmelzahntiger.survivalmatic.client.gui.menus;

import com.semmelzahntiger.survivalmatic.common.registries.ModRegistry;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SchemBuilderMenu extends AbstractContainerMenu {
    private final GlobalPos pos;
    public SchemBuilderMenu(int id, Inventory playerInventory, GlobalPos pos) {
        super(ModRegistry.SCHEM_BUILDER_MENU.get(), id);
        this.pos = pos;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9, // The slot index inside the player's inventory
                        8 + col * 18,      // X coordinate
                        84 + row * 18      // Y coordinate
                ));
            }
        }

        // 2. Add the Player's Hotbar (1 row of 9)
        // Standard starting Y coordinate for the hotbar is usually 142
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(
                    playerInventory,
                    col,               // Hotbar indices are 0-8
                    8 + col * 18,      // X coordinate
                    142                // Y coordinate
            ));
        }
    }
    public SchemBuilderMenu(int pContainerId, Inventory pInventory, FriendlyByteBuf pBuffer) {
        super(ModRegistry.SCHEM_BUILDER_MENU.get(), pContainerId);
        pos = pBuffer.readGlobalPos();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
