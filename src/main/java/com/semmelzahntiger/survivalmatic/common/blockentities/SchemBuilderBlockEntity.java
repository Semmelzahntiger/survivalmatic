package com.semmelzahntiger.survivalmatic.common.blockentities;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.client.gui.menus.SchemBuilderMenu;
import com.semmelzahntiger.survivalmatic.common.capabilities.SchemBuilderItemCap;
import com.semmelzahntiger.survivalmatic.common.network.ModNetwork;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderItemBufferPacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderStatePacket;
import com.semmelzahntiger.survivalmatic.common.registries.ModRegistry;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SchemBuilderBlockEntity extends BlockEntity {
    public static final int BUILD_BATCH_SIZE = 40;

    public enum STATE {
        IDLE(0),
        RECEIVING_DATA(1),
        SCHEMATIC_LOCKED(2),
        READY(3),
        BUILDING(4),
        DUMPING(5);
        private final int id;
        STATE(int id) {
            this.id = id;
        }
        public int getId() {
            return id;
        }
    }
    private STATE state = STATE.IDLE;
    private final HashMap<Item,Integer> materialBuffer = new HashMap<>();

    private final HashMap<Item, Integer> dumpBuffer = new HashMap<>();
    private boolean dumpBufferChanged = false;

    private SchematicReader.SchematicInstance serverSchematicInstance = null;
    private Map<Item, Integer> requiredItems = new HashMap<>();

    private BuildInstance buildInstance = null;
    private boolean placesAir = false;
    private boolean preserveExcavation = false;
    private Vec3 offset = Vec3.ZERO;

    private String displayMessage = "Select Schematic...";

    private int ticksSinceLastSync = 0;

    public IItemHandler itemHandler;
    LazyOptional<IItemHandler> inventoryHandlerLazyOptional;

    public SchemBuilderBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModRegistry.SCHEM_BUILDER_BE.get(), pPos, pBlockState);
        itemHandler = new SchemBuilderItemCap(this);
        inventoryHandlerLazyOptional = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryHandlerLazyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryHandlerLazyOptional.invalidate();
    }
    public HashMap<Item,Integer> getMaterialBuffer() {
        return materialBuffer;
    }
    public HashMap<Item,Integer> getDumpBuffer() {
        return dumpBuffer;
    }
    public STATE getState() {
        return state;
    }
    private void setState(STATE state) {
        this.state = state;
    }
    public boolean newIncomingRequest() {
        if(state != STATE.IDLE) {
            return false;
        }
        setState(STATE.RECEIVING_DATA);
        return true;
    }
    public void setPlacesAir(boolean placesAir) {
        this.placesAir = placesAir;
    }
    public boolean isPlacesAir() {
        return placesAir;
    }
    public void setPreserveExcavation(boolean preserveExcavation) {
        this.preserveExcavation = preserveExcavation;
    }
    public boolean preservesExcavation() {
        return preserveExcavation;
    }
    private void dumpBufferChanged() {
        dumpBufferChanged = true;
    }
    public boolean hasDumpBufferChanged() {
        return dumpBufferChanged;
    }
    public void confirmDumpBufferChanged() {
        dumpBufferChanged = false;
    }
    public void setOffset(Vec3 offset) {
        this.offset = offset;
    }
    public int cancel() {
        if(state == STATE.BUILDING && buildInstance != null) {
            buildInstance.setCanceled();
            return 0;
        }
        else if(state == STATE.SCHEMATIC_LOCKED || state == STATE.READY) {
            dump();
            return 0;
        }
        else {
            return 1;
        }
    }
    public void clearDumpBuffer() {
        dumpBuffer.clear();
    }
    public SchematicReader.SchematicInstance getSchematicInstance() {
        return serverSchematicInstance;
    }
    public void setSchematicInstance(SchematicReader.SchematicInstance serverSchematicInstance) {
        state = STATE.SCHEMATIC_LOCKED;
        this.serverSchematicInstance = serverSchematicInstance;
        Survivalmatic.LOGGER.info("SchemBuilder @{}/{} received Schematic {}", this.getBlockPos(), this.getLevel().dimension(), serverSchematicInstance.name());
    }
    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }
    public String getDisplayMessage() {
        return displayMessage;
    }
    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T Entity) {
        SchemBuilderBlockEntity schemBuilderBlockEntity = (SchemBuilderBlockEntity) Entity;
        schemBuilderBlockEntity.internalTick(level, pos, state);
    }
    // Todo: Refactor Tick Method
    private void internalTick(Level level, BlockPos pos, BlockState blockState) {
        if(level.isClientSide()) {
            return;
        }
        this.state = switch (state) {
            case IDLE, READY, RECEIVING_DATA -> this.state;
            case SCHEMATIC_LOCKED -> {
                if(serverSchematicInstance != null) {
                    boolean enoughResource = true;
                    for (Map.Entry<Item, Integer> entry : serverSchematicInstance.requiredItems().entrySet()) {
                        Item item = entry.getKey();
                        int minAmount = entry.getValue();
                        if(!materialBuffer.containsKey(item) || (materialBuffer.containsKey(item) && !(materialBuffer.get(item) > minAmount))) {
                            enoughResource = false;
                            break;
                        }
                    }
                    yield enoughResource ? STATE.READY : STATE.SCHEMATIC_LOCKED;
                }
                yield STATE.IDLE;
            }
            case BUILDING -> {
                if(buildInstance == null) {
                    yield STATE.IDLE;
                }
                if(buildInstance.isCanceled() || buildInstance.isFinished()) {
                    dump();
                    buildInstance = null;
                    yield STATE.DUMPING;
                }
                if(!buildInstance.isPaused()) {
                    buildInstance.tickBuild(level);
                }
                yield STATE.BUILDING;
            }
            case DUMPING -> dumpBuffer.isEmpty() ? STATE.IDLE : STATE.DUMPING;
        };

        if(ticksSinceLastSync >= 10) {
            Survivalmatic.LOGGER.info(state.toString() + level.isClientSide);
          syncWithClients();
          ticksSinceLastSync = 0;
        }
        else {
            ticksSinceLastSync++;
        }
    }
    private void syncWithClients() {
        switch (state) {
            case SCHEMATIC_LOCKED, DUMPING, READY -> {
                try {
                    Registry<Item> registry = this.getLevel().getServer().registryAccess().registry(ForgeRegistries.ITEMS.getRegistryKey()).get();
                    SchemBuilderItemBufferPacket itemBufferPacket = new SchemBuilderItemBufferPacket(materialBuffer, dumpBuffer, registry);
                    SchemBuilderStatePacket statePacket = new SchemBuilderStatePacket(state.getId(), getDisplayMessage());
                    List<ServerPlayer> players = this.getLevel().getServer().getPlayerList().getPlayers().stream().filter(player -> {
                        GlobalPos globalPos = GlobalPos.of(this.getLevel().dimension(), this.getBlockPos());
                        return player.containerMenu instanceof SchemBuilderMenu menu && Objects.equals(menu.pos, globalPos);
                    }).toList();
                    players.forEach(player -> {
                        ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), itemBufferPacket);
                        ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), statePacket);
                    });
                }
                catch(Exception e) {
                    Survivalmatic.LOGGER.info("Could not sync data with Clients" + e.getMessage());
                }
            }
        }
    }
    public void startBuilding() {
        if(state != STATE.READY) {
            return;
        }
        buildInstance = new BuildInstance(serverSchematicInstance);
        state = STATE.BUILDING;
    }
    public void pauseBuilding() {
        if(state != STATE.BUILDING) {
            return;
        }
        buildInstance.pause();
    }
    public void resumeBuilding() {
        if(state != STATE.BUILDING) {
            return;
        }
        buildInstance.resume();
    }
    public void cancelBuilding() {
        if(state != STATE.BUILDING) {
            return;
        }
        buildInstance.setCanceled();
    }
    private void dump() {
        dumpBuffer.putAll(materialBuffer);
        dumpBufferChanged();
        materialBuffer.clear();
    }
    private class BuildInstance {
        private int trackedWidth;
        private int trackedHeight;
        private int trackedLength;

        private final int LAST_INDEX;
        private int currentIndex = 1;
        final SchematicReader.SchematicInstance instance;

        private boolean isCanceled = false;
        private boolean isPaused = false;

        private BuildInstance(SchematicReader.SchematicInstance instance) {
            this.instance = instance;
            LAST_INDEX = instance.height() * instance.width() * instance.length();
        }
        public void setCanceled() {
            isCanceled = true;
        }
        public void pause() {
            isPaused = true;
        }
        public void resume() {
            isPaused = false;
        }
        public boolean isCanceled() {
            return isCanceled;
        }
        public boolean isPaused() {
            return isPaused;
        }
        public void tickBuild(Level level) {
            if(isCanceled) {
                return;
            }
            int remainingBatchCount = BUILD_BATCH_SIZE;
            batch:
            for (int y = trackedHeight; y < instance.height(); y++) {
                trackedHeight = y;
                for (int x = trackedWidth; x < instance.width(); x++) {
                    trackedWidth = x;
                    for (int z = trackedLength; z < instance.length(); z++) {
                        // If batch is exhausted, continue in the next tick, or if build is finished terminate as well (in case the tick is triggered even if it shouldn't)
                        if(remainingBatchCount == 0 || isFinished()) break batch;
                        trackedLength = z;
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState toBePlaced = instance.getBlockState(x, y, z);
                        Item item = toBePlaced.getBlock().asItem();
                        // A build can only start if all materials are present, so its assumed the only missing item in here is air which will be ignored
                        if(materialBuffer.containsKey(item)) {
                            materialBuffer.put(item, materialBuffer.get(item) - 1);
                        }
                        if(preservesExcavation()) {
                            BlockState excavatedState = level.getBlockState(pos);
                            Item excavatedItem = excavatedState.getBlock().asItem();
                            dumpBuffer.put(excavatedItem, dumpBuffer.getOrDefault(excavatedItem, 0) + 1);
                        }
                        level.setBlock(pos, toBePlaced,0);
                        remainingBatchCount--;
                        currentIndex++;
                    }
                }
            }
        }
        public boolean isFinished() {
            return currentIndex >= LAST_INDEX;
        }
    }
}
