package com.semmelzahntiger.survivalmatic.common.blockentities;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.capabilities.SchemBuilderItemCap;
import com.semmelzahntiger.survivalmatic.common.registries.ModRegistry;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SchemBuilderBlockEntity extends BlockEntity implements ContainerData {
    public static final int BUILD_BATCH_SIZE = 120;

    public enum STATE {
        IDLE(0, Component.translatable("survivalmatic.schembuilder.idle")),
        RECEIVING_DATA(1,Component.translatable("survivalmatic.schembuilder.receiving_data")),
        SCHEMATIC_LOCKED(2,Component.translatable("survivalmatic.schembuilder.schematic_locked")),
        READY(3,Component.translatable("survivalmatic.schembuilder.ready")),
        BUILDING(4,Component.translatable("survivalmatic.schembuilder.building")),
        DUMPING(5,Component.translatable("survivalmatic.schembuilder.dumping")),;
        private final int id;
        private final Component stateMessage;
        private static final Map<Integer, STATE> idToState = new HashMap<>();
        static {
            for (STATE state : values()) {
                idToState.put(state.id, state);
            }
        }
        STATE(int id, Component stateMessageq) {
            this.id = id;
            this.stateMessage = stateMessageq;
        }
        public int getId() {
            return id;
        }
        public Component getStateMessage() {
            return stateMessage;
        }
        public static STATE getByID(int id) {
            return idToState.get(id);
        }
    }
    private STATE state = STATE.IDLE;
    private final Map<Item,Integer> materialBuffer = new HashMap<>();
    private int materialBufferVersionCounter = 0;

    private final LinkedHashMap<Item, Integer> dumpBuffer = new LinkedHashMap<>();
    private int dumpBufferVersionCounter = 0;

    private SchematicReader.SchematicInstance serverSchematicInstance = null;

    private BuildInstance buildInstance = null;
    private boolean placesAir = false;
    private boolean preserveExcavation = false;
    private Vec3i offset = new Vec3i(1,0,1);

    public IItemHandler itemHandler;
    LazyOptional<IItemHandler> inventoryHandlerLazyOptional;

    public ItemStack inputSlot = ItemStack.EMPTY;
    public ItemStack outputSlot = ItemStack.EMPTY;

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
    public int get(int i) {
        return switch (i) {
            // state sync
            case 0 -> state.getId();
            // build Instance Sync
            case 1 -> {
                if(buildInstance != null) {
                    yield buildInstance.currentIndex;
                }
                yield 0;
            }
            case 2 -> {
                if(buildInstance != null) {
                    yield buildInstance.INDEX_GUARD;
                }
                yield 0;
            }
            case 3 -> {
                if(buildInstance != null) {
                    yield buildInstance.isPaused ? 1 : 0;
                }
                yield 0;
            }
            // Active Sync
            case 4 -> placesAir ? 1 : 0;
            case 5 -> preserveExcavation ? 1 : 0;
            default -> 0;
        };
    }

    @Override
    public void set(int i, int i1) {
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryHandlerLazyOptional.invalidate();
    }
    public ItemStack getInputSlot() {
        return inputSlot;
    }
    public void setInputSlot(ItemStack inputSlot) {
        this.inputSlot = inputSlot;
    }
    public ItemStack getOutputSlot() {
        return outputSlot;
    }
    public void setOutputSlot(ItemStack outputSlot) {
        this.outputSlot = outputSlot;
    }
    public Map<Item,Integer> getMaterialBuffer() {
        return materialBuffer;
    }
    public int getMaterialBufferVersionCounter() {
        return materialBufferVersionCounter;
    }
    public void incrementMaterialBufferVersionCounter() {
        materialBufferVersionCounter++;
    }
    public Map<Item,Integer> getDumpBuffer() {
        return dumpBuffer;
    }
    public int getDumpBufferVersionCounter() {
        return dumpBufferVersionCounter;
    }
    public void incrementDumpBufferVersionCounter() {
        dumpBufferVersionCounter++;
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
    public boolean placesAir() {
        return placesAir;
    }
    public void setPreserveExcavation(boolean preserveExcavation) {
        this.preserveExcavation = preserveExcavation;
    }
    public boolean preservesExcavation() {
        return preserveExcavation;
    }
    public void setOffset(Vec3i offset) {
        this.offset = offset;
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
    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T Entity) {
        SchemBuilderBlockEntity schemBuilderBlockEntity = (SchemBuilderBlockEntity) Entity;
        schemBuilderBlockEntity.internalTick(level, pos, state);
    }

    private void internalTick(Level level, BlockPos pos, BlockState blockState) {
        if(level.isClientSide()) {
            return;
        }
        this.state = switch (state) {
            case IDLE, READY, RECEIVING_DATA -> this.state;
            case SCHEMATIC_LOCKED -> {
                if(serverSchematicInstance != null) {
                    boolean enoughResource = true;
//                    for (Map.Entry<Item, Integer> entry : serverSchematicInstance.requiredItems().entrySet()) {
//                        Item item = entry.getKey();
//                        int minAmount = entry.getValue();
//                        if(!materialBuffer.containsKey(item) || (materialBuffer.containsKey(item) && !(materialBuffer.get(item) > minAmount))) {
//                            enoughResource = false;
//                            break;
//                        }
//                    }
                    tickInputSlot();
                    materialBuffer.putAll(serverSchematicInstance.requiredItems());
                    incrementMaterialBufferVersionCounter();
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
            case DUMPING -> {
                tickOutputSlot();
                Survivalmatic.LOGGER.info(getDumpBuffer().toString());
                yield dumpBuffer.isEmpty() ? STATE.IDLE : STATE.DUMPING;
            }
        };
    }
    private void tickInputSlot() {
        ItemStack inputSlotItem = getInputSlot();
        if(state != STATE.SCHEMATIC_LOCKED || inputSlotItem.isEmpty()) return;
        Map<Item, Integer> storedItems = getMaterialBuffer();
        Map<Item, Integer> requiredItems = getSchematicInstance().requiredItems();

        Item item = getInputSlot().getItem();
        int count = inputSlotItem.getCount();

        if(!requiredItems.containsKey(item)) {
           tryTransferSlot();
        }
        else {
            int maxStorable = requiredItems.get(item);
            int storedAmount = storedItems.getOrDefault(item, 0);
            int remainder = count + storedAmount - maxStorable;
            if(remainder > 0) {
                storedItems.put(item, maxStorable);
                inputSlotItem.setCount(remainder);
                tryTransferSlot();
            }
            else {
                storedItems.put(item, count);
                setInputSlot(ItemStack.EMPTY);
            }
        }
    }
    private void tickOutputSlot() {
        ItemStack outputSlotItem = getOutputSlot();
        LinkedHashMap<Item,Integer> dumpBuffer = (LinkedHashMap<Item, Integer>) getDumpBuffer();
        if(state != STATE.DUMPING && !dumpBuffer.isEmpty() && outputSlotItem.isEmpty()) {
            Iterator<Map.Entry<Item,Integer>> iterator = dumpBuffer.entrySet().iterator();
            Map.Entry<Item,Integer> item = iterator.next();
            ItemStack newItem = new ItemStack(item.getKey());
            int storedAmount = item.getValue();
            int maxCount = newItem.getMaxStackSize();
            int outputted;
            if(storedAmount > maxCount) {
                item.setValue(storedAmount - maxCount);
                outputted = maxCount;
            }
            else {
                iterator.remove();
                outputted = storedAmount;
            }
            ItemStack outputtedItem = new ItemStack(item.getKey());
            outputtedItem.setCount(outputted);
            setOutputSlot(outputtedItem);
        }
    }
    private void tryTransferSlot() {
        ItemStack inputSlotItem = getInputSlot();
        ItemStack outputSlotItem = getOutputSlot();
        if(inputSlotItem.isEmpty()) {
            return;
        }
        Item item = inputSlotItem.getItem();
        int count = inputSlotItem.getCount();

        if(outputSlotItem.isEmpty()) {
            setOutputSlot(inputSlotItem);
            setInputSlot(ItemStack.EMPTY);
        }
        else if (outputSlotItem.getItem() == item) {
            int maxInStack = outputSlotItem.getMaxStackSize();
            int addable = maxInStack - outputSlotItem.getCount();
            if(addable >= count) {
                setInputSlot(ItemStack.EMPTY);
                outputSlotItem.grow(count);
            }
            else {
                inputSlotItem.shrink(addable);
                outputSlotItem.grow(addable);
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
    public void cancel() {
        if(state == STATE.BUILDING && buildInstance != null) {
            buildInstance.setCanceled();
        }
        else if(state == STATE.SCHEMATIC_LOCKED || state == STATE.READY) {
            dump();
        }
    }
    private void dump() {
        dumpBuffer.putAll(materialBuffer);
        incrementDumpBufferVersionCounter();
        materialBuffer.clear();
        incrementMaterialBufferVersionCounter();
    }
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        ListTag materialBufferTag = new ListTag();
        ListTag dumpBufferTag = new ListTag();
        materialBuffer.forEach((item, amount) -> {
            CompoundTag itemIDTag = new CompoundTag();
            int id = Item.getId(item);
            itemIDTag.putInt("id", id);
            itemIDTag.putInt("amount", amount);
            materialBufferTag.add(itemIDTag);
        });
        tag.put("materialBuffer", materialBufferTag);
        dumpBuffer.forEach((item, amount) -> {
            CompoundTag itemIDTag = new CompoundTag();
            int id = Item.getId(item);
            itemIDTag.putInt("id", id);
            itemIDTag.putInt("amount", amount);
        });
        tag.put("dumpBuffer", dumpBufferTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        ListTag materialBufferTag = nbt.getList("materialBuffer", ListTag.TAG_LIST);
        materialBuffer.clear();
        materialBufferTag.forEach((tag) -> {
            CompoundTag itemIDTag = (CompoundTag) tag;
            Item item = Item.byId(itemIDTag.getInt("id"));
            int amount = itemIDTag.getInt("amount");
            materialBuffer.put(item, amount);
        });
        ListTag dumpBufferTag = nbt.getList("dumpBuffer", ListTag.TAG_LIST);
        dumpBuffer.clear();
        dumpBufferTag.forEach((tag) -> {
            CompoundTag itemIDTag = (CompoundTag) tag;
            Item item = Item.byId(itemIDTag.getInt("id"));
            int amount = itemIDTag.getInt("amount");
            dumpBuffer.put(item, amount);
        });
    }
    private class BuildInstance {
        private final int INDEX_GUARD;
        private int currentIndex = 0;
        final SchematicReader.SchematicInstance instance;

        private boolean isCanceled = false;
        private boolean isPaused = false;

        private BuildInstance(SchematicReader.SchematicInstance instance) {
            this.instance = instance;
            INDEX_GUARD = instance.height() * instance.width() * instance.length();
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
        // Todo: logic for invalid replacements like bedrock
        public void tickBuild(Level level) {
            Map<Item, Integer> cache = new HashMap<>();
            if(isCanceled) {
                return;
            }
            int remainingBatchCount = BUILD_BATCH_SIZE;
            for (int i = currentIndex; i < INDEX_GUARD; i++) {
                currentIndex++;
                remainingBatchCount--;
                BlockPos pos = instance.getBlockPosFrom1D(i)
                        .offset(offset)
                        .offset(getBlockPos());
                BlockState state = instance.getBlockStateFrom1D(i);
                Item item = state.getBlock().asItem();
                if(materialBuffer.containsKey(item)) {
                    int stored  = materialBuffer.get(item);
                    if(stored <= 1) {
                        materialBuffer.remove(item);
                    }
                    else {
                        materialBuffer.put(item, materialBuffer.get(item) - 1);
                    }
                }
                if(preservesExcavation()) {
                    BlockState excavatedState = level.getBlockState(pos);
                    if(excavatedState != Blocks.AIR.defaultBlockState()) {
                        Item excavatedItem = excavatedState.getBlock().asItem();
                        cache.put(excavatedItem, cache.getOrDefault(excavatedItem, 0) + 1);
                    }
                }
                if(!(state == Blocks.AIR.defaultBlockState() && !placesAir())) {
                            level.setBlockAndUpdate(pos, state);
                }
                if(remainingBatchCount == 0) {
                    break;
                }
            }
            cache.forEach((curItem, count) -> {
                dumpBuffer.merge(curItem, 1, Integer::sum);
            });
            incrementDumpBufferVersionCounter();
        }
        public boolean isFinished() {
            return currentIndex >= INDEX_GUARD;
        }
    }


}
