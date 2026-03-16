package com.semmelzahntiger.survivalmatic.client.gui.menus;

import com.semmelzahntiger.survivalmatic.common.registries.Registry;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class SchemBuilderMenu extends AbstractContainerMenu {
    private final GlobalPos pos;
    public SchemBuilderMenu(int id, Inventory playerInventory, GlobalPos pos) {
        super(Registry.SCHEM_BUILDER_MENU.get(), id);
        this.pos = pos;
    }
    public SchemBuilderMenu(int pContainerId, Inventory pInventory, FriendlyByteBuf pBuffer) {
        super(Registry.SCHEM_BUILDER_MENU.get(), pContainerId);
        pos = pBuffer.readGlobalPos();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }
}
