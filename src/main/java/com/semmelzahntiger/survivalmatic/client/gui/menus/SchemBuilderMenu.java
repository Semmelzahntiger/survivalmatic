package com.semmelzahntiger.survivalmatic.client.gui.menus;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.network.ModNetwork;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderItemBufferPacket;
import com.semmelzahntiger.survivalmatic.common.registries.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class SchemBuilderMenu extends AbstractContainerMenu {
    public final ContainerLevelAccess access;
    public final GlobalPos pos;
    public final ContainerData data;
    public final Inventory playerInventory;
    public int trackedDumpBufferVersion = -1;
    public int trackedMaterialBufferVersion = -1;
    public SchemBuilderMenu(int id, Inventory playerInventory, ContainerLevelAccess containerAccess, IItemHandler builderInventory, GlobalPos pos, ContainerData containerData) {
        super(ModRegistry.SCHEM_BUILDER_MENU.get(), id);
        this.pos = pos;
        this.access = containerAccess;
        this.data = containerData;
        this.playerInventory = playerInventory;
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
        this.addDataSlots(containerData);
    }
    public SchemBuilderMenu(int pContainerId, Inventory playerInventory, FriendlyByteBuf pBuffer) {
        this(pContainerId, playerInventory,ContainerLevelAccess.NULL, new ItemStackHandler(2), pBuffer.readGlobalPos(), new SimpleContainerData(6));
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
            // Build
            case 0 -> {
                access.execute((level, blockpos) -> {
                    getBlockEntity(level, blockpos).ifPresent(SchemBuilderBlockEntity::startBuilding);
                });
                return true;
            }
            // Pause
            case 1 -> {
                access.execute((level, blockpos) -> {
                    getBlockEntity(level, blockpos).ifPresent(SchemBuilderBlockEntity::pauseBuilding);
                });
                return true;
            }
            // Resume
            case 2 -> {
                access.execute((level, blockpos) -> {
                    getBlockEntity(level, blockpos).ifPresent(SchemBuilderBlockEntity::resumeBuilding);
                });
                return true;
            }
            // Cancel
            case 3 -> {
                access.execute((level, blockpos) -> {
                    getBlockEntity(level, blockpos).ifPresent(SchemBuilderBlockEntity::cancel);
                });
                return true;
            }
            // Clear DumpBuffer
            case 4 -> {
                access.execute((level, blockpos) -> {
                    getBlockEntity(level, blockpos).ifPresent(SchemBuilderBlockEntity::clearDumpBuffer);
                });
                return true;
            }
            // Toggle Air placement
            case 5 -> {
                access.execute((level, blockpos) -> {
                    getBlockEntity(level, blockpos).ifPresent(be -> be.setPlacesAir(!be.placesAir()));
                });
                return true;
            }
            // Toggle Excavation Preservation
            case 6 -> {
                access.execute((level, blockpos) -> {
                    Survivalmatic.LOGGER.info("Debug toggle");
                    getBlockEntity(level, blockpos).ifPresent(be -> {
                        be.setPreserveExcavation(!be.preservesExcavation());
                        Survivalmatic.LOGGER.info(Boolean.toString(be.preservesExcavation()));
                    });
                });
                return true;
            }
            // Toggle Hologram
            case 7 -> {
                access.execute((level, blockpos) -> {
//                    getBlockEntity(level, blockpos).ifPresent(be -> be.setPreserveExcavation(!be.preservesExcavation()));
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

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if(this.playerInventory.player instanceof ServerPlayer serverPlayer) {
            access.execute((level, blockpos) -> {
                getBlockEntity(level, blockpos).ifPresent(be -> {
                    int dumpVer = be.getDumpBufferVersionCounter();
                    int matVer = be.getMaterialBufferVersionCounter();
                    if(trackedDumpBufferVersion != dumpVer || trackedMaterialBufferVersion != matVer) {
                        ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                                new SchemBuilderItemBufferPacket(
                                        be.getMaterialBuffer(),
                                        be.getDumpBuffer(),
                                        be.getLevel().registryAccess().registry(ForgeRegistries.ITEMS.getRegistryKey()).get()
                                ));
                    }
                    else {
                        trackedDumpBufferVersion = dumpVer;
                        trackedMaterialBufferVersion = matVer;
                    }
                });
            });
        }
    }
}
