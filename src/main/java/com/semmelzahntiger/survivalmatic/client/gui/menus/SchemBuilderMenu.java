package com.semmelzahntiger.survivalmatic.client.gui.menus;

import com.semmelzahntiger.survivalmatic.common.registries.ModRegistry;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SchemBuilderMenu extends AbstractContainerMenu {
    public final GlobalPos pos;
    public SchemBuilderMenu(int id, Inventory playerInventory, IItemHandler builderInventory, GlobalPos pos) {
        super(ModRegistry.SCHEM_BUILDER_MENU.get(), id);
        this.pos = pos;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18
                ));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(
                    playerInventory,
                    col,
                    8 + col * 18,
                    142
            ));
        }
    }
    public SchemBuilderMenu(int pContainerId, Inventory playerInventory, FriendlyByteBuf pBuffer) {
        this(pContainerId, playerInventory,new ItemStackHandler(2), pBuffer.readGlobalPos());
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
