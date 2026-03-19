package com.semmelzahntiger.survivalmatic.client.gui.menus;

import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.registries.ModRegistry;
import com.semmelzahntiger.survivalmatic.common.schem.ServerSchematicHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Optional;

public class SchemBuilderMenu extends AbstractContainerMenu {
    public final ContainerLevelAccess access;
    public final GlobalPos pos;
    public SchemBuilderMenu(int id, Inventory playerInventory, ContainerLevelAccess containerAccess, IItemHandler builderInventory, GlobalPos pos) {
        super(ModRegistry.SCHEM_BUILDER_MENU.get(), id);
        this.pos = pos;
        this.access = containerAccess;
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
        this(pContainerId, playerInventory,ContainerLevelAccess.NULL,new ItemStackHandler(2), pBuffer.readGlobalPos());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModRegistry.SCHEM_BUILDER.get());
    }

    @Override
    public boolean clickMenuButton(Player pPlayer, int pId) {
        switch (pId) {
            case 0 -> {
                access.execute((level, blockpos) -> {
                    getBlockEntity(level, blockpos).ifPresent(SchemBuilderBlockEntity::startBuilding);
                });
                return true;
            }
            case 1 -> {
                access.execute((level, blockpos) -> {
                    getBlockEntity(level, blockpos).ifPresent(SchemBuilderBlockEntity::pauseBuilding);
                });
                return true;
            }
            case 2 -> {
                access.execute((level, blockpos) -> {
                    getBlockEntity(level, blockpos).ifPresent(SchemBuilderBlockEntity::resumeBuilding);
                });
                return true;
            }
            case 3 -> {
                access.execute((level, blockpos) -> {
                    getBlockEntity(level, blockpos).ifPresent(SchemBuilderBlockEntity::cancelBuilding);
                });
                return true;
            }
            case 4 -> {
                access.execute((level, blockpos) -> {
                    getBlockEntity(level, blockpos).ifPresent(SchemBuilderBlockEntity::clearDumpBuffer);
                });
                return true;
            }
        }
        return super.clickMenuButton(pPlayer, pId);
    }

    protected static Optional<SchemBuilderBlockEntity> getBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof SchemBuilderBlockEntity schemBuilder) {
            return Optional.of(schemBuilder);
        }
        return Optional.empty();
    }
}
