package com.semmelzahntiger.survivalmatic.client.gui.menus;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.network.ModNetwork;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderDumpBufferPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderMaterialBufferPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderSchematicDataPacket;
import com.semmelzahntiger.survivalmatic.common.registries.ModRegistry;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SchemBuilderMenu extends AbstractContainerMenu {
    public final ContainerLevelAccess access;
    public final GlobalPos pos;
    public final ContainerData data;
    public final Inventory playerInventory;
    public final IItemHandler itemHandler;

    public int trackedDumpBufferVersion = -1;
    public int trackedMaterialBufferVersion = -1;
    public int trackedSchematicDataVersion = -1;

    public int ticksUntilUpdate = 0;
    public int tickInterval = 20;
    public SchemBuilderMenu(int id, Inventory playerInventory, ContainerLevelAccess containerAccess, IItemHandler builderInventory, GlobalPos pos, ContainerData containerData) {
        super(ModRegistry.SCHEM_BUILDER_MENU.get(), id);
        this.pos = pos;
        this.access = containerAccess;
        this.data = containerData;
        this.playerInventory = playerInventory;
        this.itemHandler = builderInventory;
        for(int col = 0; col < 9; col++) {
            this.addSlot(new Slot(
                    playerInventory,
                    col, 89 + col * 18, 173
            ));
            for (int row = 0; row < 3; row++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + 9 + row * 9,
                        89 + col * 18,
                        115 + row * 18
                ));
            }
        }
        this.addSlot(new SlotItemHandler(builderInventory,0,58, 138));
        this.addSlot(new SlotItemHandler(builderInventory,1,58, 156));
        this.addDataSlots(containerData);
    }
    public SchemBuilderMenu(int pContainerId, Inventory playerInventory, FriendlyByteBuf pBuffer) {
        this(pContainerId, playerInventory,ContainerLevelAccess.NULL, new ItemStackHandler(2), pBuffer.readGlobalPos(), new SimpleContainerData(9));
    }
    // Todo: Implement quick moving
    @Override
    public ItemStack quickMoveStack(Player player, int i) {
       return ItemStack.EMPTY;
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
    protected SchemBuilderMaterialBufferPacket constructMaterialBufferPacket(SchemBuilderBlockEntity schemBuilderBlockEntity) {
        Map<Item,Integer> itemsInBuffer = schemBuilderBlockEntity.getMaterialBuffer();
        Map<Item,Integer> requiredItems = schemBuilderBlockEntity.getCurrentRequiredItems();
        Map<Item, Tuple<Integer, Integer>> items = new HashMap<>();
        requiredItems.forEach(((item,amount) -> {
            items.put(item, new Tuple<>(itemsInBuffer.getOrDefault(item, 0), amount));
        }));
        return new SchemBuilderMaterialBufferPacket(items,schemBuilderBlockEntity.getLevel().registryAccess().registry(ForgeRegistries.ITEMS.getRegistryKey()).get());
    }
    protected SchemBuilderDumpBufferPacket constructDumpBufferPacket(SchemBuilderBlockEntity schemBuilderBlockEntity) {
        Map<Item,Integer> itemsInBuffer = schemBuilderBlockEntity.getDumpBuffer();
        return new SchemBuilderDumpBufferPacket(itemsInBuffer, schemBuilderBlockEntity.getLevel().registryAccess().registry(ForgeRegistries.ITEMS.getRegistryKey()).get());
    }
    protected SchemBuilderSchematicDataPacket constructSchematicDataPacket(SchemBuilderBlockEntity schemBuilderBlockEntity) {
        SchematicReader.SchematicInstance schematicInstance = schemBuilderBlockEntity.getSchematicInstance();
        if(schematicInstance == null) {
            return new SchemBuilderSchematicDataPacket("No Schematic Selected", 0,0,0);
        }
        return new SchemBuilderSchematicDataPacket(schematicInstance.name(), schematicInstance.width(), schematicInstance.height(), schematicInstance.length());
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if(this.playerInventory.player instanceof ServerPlayer serverPlayer) {
            access.execute((level, blockpos) -> getBlockEntity(level, blockpos).ifPresent(be -> {
                if(ticksUntilUpdate > 0) {
                    ticksUntilUpdate--;
                }
                int dumpBufferVersion = be.getDumpBufferVersionCounter();
                int materialBufferVersion = be.getMaterialBufferVersionCounter();
                boolean resetTicks = false;
                if(trackedMaterialBufferVersion != materialBufferVersion && ticksUntilUpdate <= 0) {
                    ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), constructMaterialBufferPacket(be));
                    trackedMaterialBufferVersion = materialBufferVersion;
                    resetTicks = true;
                }
                if(trackedDumpBufferVersion != dumpBufferVersion && ticksUntilUpdate <= 0) {
                    ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), constructDumpBufferPacket(be));
                    trackedDumpBufferVersion = dumpBufferVersion;
                    resetTicks = true;
                }
                if (resetTicks) {
                    ticksUntilUpdate = tickInterval;
                }
                int schematicDataVersion = be.getSchematicInstanceVersionCounter();
                if(schematicDataVersion != materialBufferVersion) {
                    ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), constructSchematicDataPacket(be));
                    trackedSchematicDataVersion = schematicDataVersion;
                }
            }));
        }
    }
}
