package com.semmelzahntiger.survivalmatic.common.blockentities;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.registries.Registry;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
// Why am I realizing after pulling an all-nighter that I DIDNT cook with this.
// What even is that sorry ass approach???
// Forge/MC provides literally everything I need for this and I decide to do this?
// I'm literally gonna commit this so if maybe someone sees this they know what kind of bullshit ive been cooking. Actually embarrassing.
public class SchemBuilderBlockEntity extends BlockEntity {
    // Todo: Implement Config Settings for Batch Sizes
    private static final int ITEM_SCAN_BATCH_SIZE = 40;
    private static final int BUILD_BATCH_SIZE = 40;

    /**
     * <p><ul>
     *     <li>{@code IDLE} = Machine's default state</li>
     *     <li>{@code IDLE_LOCKED} Machine commited to a schematic, triggered when a PREPARE scan is done</li>
     *     <li>{@code SCANNING} Read Only scan how many Materials are missing</li>
     *     <li>{@code PREPARATION_SCAN} Scans all Sources and retrieves missing materials if available</li>
     *     <li>{@code READY} Latest Preparation Scan fulfilled all item requirements for the build, ready to build schematic</li>
     *     <li>{@code BUILDING} Currently Building structure</li>
     *     <li>{@code DUMPING} State assigned after completing or cancelling build, machine currently holds extractable Items, automatically unassigned if no dumpable items are available or player decided to delete all dumpable items</li>
     * </ul></p>
     * <p>
     *
     */
    public enum SCHEM_BUILDER_STATE {
        IDLE(0),
        IDLE_LOCKED(1),
        SCANNING(2),
        PREPARATION_SCAN(3),
        READY(4),
        BUILDING(5),
        DUMPING(6);
        final int stateID;
        public int getStateID() {
            return this.stateID;
        }
        SCHEM_BUILDER_STATE(int stateID) {
            this.stateID = stateID;
        }
    }

    /**
     * Direction of adjacent {@link IItemHandler} to dump Dumpables into
     */
    Direction dumpDirection = Direction.WEST;

    private SCHEM_BUILDER_STATE currentState = SCHEM_BUILDER_STATE.IDLE;
    private boolean isComitted = false;
    /**
     * {@link MaterialSource MaterialSource}'s to scan and retrieve Items from
     */
    private final List<MaterialSource> materialSources = new ArrayList<>();
     // Virtualized Items
    private final HashMap<Item, Integer> materialBuffer = new HashMap<>();
    private HashMap<Item, Integer> missingItems = new HashMap<>();
    private final HashMap<Item, Integer> dumpBuffer = new HashMap<>();

    private SchematicReader.SchematicInstance schematicInstance = null;
    private Scan currentScanInstance = null;
    private BuildInstance buildInstance = null;
    // If blocks removed by building schematic should be preserved or deleted
    private boolean preserveExcavation = false;

    // Todo: Later on, implement NON-AIR Placement
    private boolean ignoresAirInSchematic = false;


    public SchemBuilderBlockEntity( BlockPos pPos, BlockState pBlockState) {
        super(Registry.SCHEM_BUILDER_BE.get(), pPos, pBlockState);
    }
    public void setSchematic(String schematicName) {
        if(!this.getLevel().isClientSide()) {
            try {
                MinecraftServer server = this.getLevel().getServer();
                Path path = Path.of(server.getServerDirectory().getPath() + "/schematics/" + schematicName + ".schem");
                if(Files.exists(path)) {
                    File file = new File(path.toUri());
                    schematicInstance = SchematicReader.getSchematicInstance(file , server);
                }
            } catch (IOException e) {
                Survivalmatic.LOGGER.error("Schematic '{}' not found or invalid", schematicName);
            }
        }
    }
    public void addSource(BlockPos pos, IItemHandler source) {
        materialSources.add(new MaterialSource(pos, source));
    }
    public void removeSource(BlockPos pos, IItemHandler source) {
        materialSources.remove(new MaterialSource(pos, source));
    }
    /**
     * Evaluate if the MaterialSources are still valid
     * If only slotcount changed, revalidate MaterialSource
     */
    public void validateSources() {
        List<MaterialSource> newSources = new ArrayList<>();
        materialSources.removeIf(source -> {
            BlockEntity be = this.level.getBlockEntity(source.getPos());
            if(be == null) return true;
            IItemHandler handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
            if(handler == null) return true;
            // Revalidate MaterialSource if the block still has an IItemHandler Capability
            newSources.add(new MaterialSource(source.getPos(), handler));
            return handler.getSlots() != source.getSlotCount();
        });
        materialSources.addAll(newSources);
    }
    public static <T extends BlockEntity> void ticker(Level level, BlockPos pos, BlockState state, T entity) {
        SchemBuilderBlockEntity castEntity = (SchemBuilderBlockEntity) entity;
        castEntity.tick(level, pos, state);
    }
    public void tick(Level level, BlockPos pos, BlockState state) {
        currentState = switch (currentState) {
            case IDLE, IDLE_LOCKED, READY -> currentState;
            case SCANNING, PREPARATION_SCAN -> {
                currentScanInstance.tickScan();
                if(currentScanInstance.finishedScanning) {
                    boolean isExtracting = currentScanInstance.isExtractingScan();
                    if(isExtracting) {
                        currentScanInstance.getAccumulatedItems().forEach((item, amount) -> materialBuffer.merge(item, amount, Integer::sum));
                        currentScanInstance = null;
                        if(isMaterialBufferSufficient()) {
                            yield SCHEM_BUILDER_STATE.READY;
                        }
                    }
                    currentScanInstance = null;
                   yield isExtracting || isComitted ? SCHEM_BUILDER_STATE.IDLE_LOCKED : SCHEM_BUILDER_STATE.IDLE;
                }
                yield currentState;
            }
            // Todo: Finish build logic
            case BUILDING -> {
                if(buildInstance.isCanceled()) {
                    buildInstance = null;
                    yield SCHEM_BUILDER_STATE.DUMPING;
                }
                if(buildInstance.isBuildFinished()) {
                    yield SCHEM_BUILDER_STATE.DUMPING;
                }
                buildInstance.tickBuild((ServerLevel) this.getLevel());
                yield currentState;
            }
            case DUMPING -> {
                dump();
                if(dumpBuffer.isEmpty()) {
                    yield SCHEM_BUILDER_STATE.IDLE;
                }
                yield currentState;
            }
        };
        syncDataWithClients();
    }

    /**
     * <p>Todo: Finish Doc</p>
     * <p>Will be called from the Interface with the SCAN and PREPARE Button </p>
     * @param extractScan differentiates if it was a SCAN or PREPARE Button
     */
    public void beginScanning(boolean extractScan) {
        if(currentState != SCHEM_BUILDER_STATE.IDLE && currentState != SCHEM_BUILDER_STATE.IDLE_LOCKED) {
            return;
        }
        validateSources();
        // Convert MaterialSources to ScanInstances for batched scanning
        List<SourceScanInstance> scanInstances = materialSources.stream().map(source -> new SourceScanInstance(source.slotCount, source.pos)).toList();
        Queue<SourceScanInstance> materialSourceQueue = new LinkedList<>(scanInstances);
        // Only scan for missing items in Buffer
        calculateMissingItems();
        // Create new scan and set state for tick behavior
        currentScanInstance = new Scan(materialSourceQueue, missingItems, extractScan);
        if(extractScan) {
            isComitted = true;
            currentState = SCHEM_BUILDER_STATE.PREPARATION_SCAN;
        }
        else {
            currentState = SCHEM_BUILDER_STATE.SCANNING;
        }
    }
    public void startBuilding() {
        if(currentState == SCHEM_BUILDER_STATE.READY) {
            buildInstance = new BuildInstance(schematicInstance);
        }
    }

    public void cancelBuild() {
        if(currentState == SCHEM_BUILDER_STATE.IDLE || currentState == SCHEM_BUILDER_STATE.DUMPING) {
            return;
        }
        isComitted = false;
        if(currentState == SCHEM_BUILDER_STATE.BUILDING) {
            buildInstance.setCanceled();
            dumpBuffer.putAll(materialBuffer);
            materialBuffer.clear();
        }
        currentState = SCHEM_BUILDER_STATE.DUMPING;
    }
    public void clearDumpBuffer() {
        dumpBuffer.clear();
    }
    public void dump() {
        BlockEntity dumpEntity = this.getLevel().getBlockEntity(this.getBlockPos().relative(dumpDirection));
        if(dumpEntity != null) {
            dumpEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                // Todo: Implement Dump Logic

            });
        }
    }
    public void setPreserveExcavation(boolean value) {
        this.preserveExcavation = value;
    }
    public void setDumpDirection(Direction value) {
        this.dumpDirection = value;
    }
    // Todo: Only recalculate if MaterialBuffer has changed
    private void calculateMissingItems() {
        missingItems = new HashMap<>(schematicInstance.requiredItems);
        materialBuffer.forEach((item, amount) -> {
            if(missingItems.containsKey(item)) {
                missingItems.put(item, missingItems.get(item) - amount);
            }
        });
    }
    private boolean isMaterialBufferSufficient() {
        return isScanAccumulatedSufficient(materialBuffer);
    }

    /**
     * @deprecated Will be merged with {@code isMaterialBufferSufficient()}
     */
    private boolean isScanAccumulatedSufficient(Map<Item, Integer> accumulatedItems) {
        boolean sufficient = true;
        for (Map.Entry<Item, Integer> itemIntegerEntry : schematicInstance.requiredItems.entrySet()) {
            Item item = itemIntegerEntry.getKey();
            int amount = itemIntegerEntry.getValue();
            if(accumulatedItems.containsKey(item) ) {
                sufficient = amount <= accumulatedItems.get(item) && sufficient;
            }
            else {
                sufficient = false;
            }
        }
        return sufficient;
    }
    public void syncDataWithClients() {
        int currentStateID = currentState.getStateID();
        // Todo: Finish Client Sync Logic
    }

    private static class MaterialSource {
        private final BlockPos pos;
        private final int slotCount;
        private final IItemHandler source;

        public MaterialSource(BlockPos pos, IItemHandler source) {
            this.pos = pos;
            this.slotCount = source.getSlots();
            this.source = source;
        }
        public BlockPos getPos() {
            return pos;
        }
        public int getSlotCount() {
            return slotCount;
        }
        public IItemHandler getSource() {
            return source;
        }
    }
    private class Scan {
        private final Queue<SourceScanInstance> scanQueue;
        // Only for final check if all items are present
        // Copy of required Items to track missing amount
        private final Map<Item, Integer> missingItems;
        private final Map<Item, Integer> accumulatedItems = new HashMap<>();
        private final boolean isExtractingScan;

        boolean finishedScanning = false;

        private Scan(Queue<SourceScanInstance> scanQueue, Map<Item, Integer> requiredItems, boolean isExtractingScan) {
            this.scanQueue = scanQueue;
            this.missingItems = new HashMap<>(requiredItems);
            this.isExtractingScan = isExtractingScan;
        }

        public void tickScan() {
            proceedScan();
        }
        private void finishScan() {
            finishedScanning = true;
        }
        public boolean isExtractingScan() {
            return isExtractingScan;
        }
        public Map<Item,Integer> getAccumulatedItems() {
            return accumulatedItems;
        }
        public void proceedScan() {
            // Scan for Items in defined batch size
            for (int i = ITEM_SCAN_BATCH_SIZE; i > 0; i--) {
                SourceScanInstance scanInstance = scanQueue.peek();
                // If no sources are available to query anymore, finish Scan
                if(scanInstance == null) {
                    finishScan();
                    break;
                }
                // Get IItemHandler from Source
                IItemHandler source = scanInstance.getSource();
                // If Block has no IItemHandler capability, remove Source and skip
                if(source == null) {
                    scanQueue.poll();
                    continue;
                }
                // Accumulate/Extract Relevant items found in this Batch
                else {
                    try {
                        ItemStack item = source.getStackInSlot(scanInstance.scanIndex);
                        if(!item.isEmpty() && missingItems.containsKey(item.getItem())) {
                            int itemAmount = item.getCount();
                            // Extraction Branch
                            if(isExtractingScan) {
                                int amountRequired = missingItems.get(item.getItem());
                                ItemStack actualItem = source.extractItem(scanInstance.scanIndex, amountRequired, false);
                                // Lower Required amount by amount extracted
                                missingItems.put(item.getItem(), missingItems.get(item.getItem()) - actualItem.getCount());
                                missingItems.entrySet().removeIf(entry -> entry.getValue() <= 0);

                                accumulatedItems.put(item.getItem(), accumulatedItems.getOrDefault(item.getItem(), 0) + actualItem.getCount());
                            }
                            // Scan Branch
                            else {
                                accumulatedItems.put(item.getItem(), accumulatedItems.getOrDefault(item.getItem(), 0) + itemAmount);
                            }
                        }
                        scanInstance.scanIndex++;
                    }
                    catch(IndexOutOfBoundsException e) {
                        Survivalmatic.LOGGER.error("Unexpected Index while retrieving Item, skipping ItemSource");
                        scanQueue.poll();
                    }
                }
                // If last Slot was reached in the current batch scan, remove source
                if(scanInstance.scanIndex >= scanInstance.slotCount) {
                    scanQueue.poll();
                }
            }
        }
    }
    private class SourceScanInstance {
        private final int slotCount;
        private int scanIndex = 0;
        private final BlockPos pos;
        public SourceScanInstance(int slotCount, BlockPos pos) {
            this.slotCount = slotCount;
            this.pos = pos;
        }
        public SourceScanInstance(MaterialSource source) {
            this.slotCount = source.slotCount;
            this.pos = source.getPos();
        }
        public IItemHandler getSource() {
            BlockEntity be = getLevel().getBlockEntity(pos);
            if(be == null) {
                return null;
            }
            return be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        }

    }

    /**
     * Todo: Finish BuildInstance logic and documentation
     */
    private class BuildInstance {
        private int trackedWidth;
        private int trackedLength;
        private int trackedHeight;

        private final int LAST_INDEX;
        private int currentIndex = 1;

        final SchematicReader.SchematicInstance instance;

        private boolean isCanceled = false;


        private BuildInstance(SchematicReader.SchematicInstance instance) {
            this.instance = instance;
            LAST_INDEX = instance.height * instance.width * instance.length;
        }
        public void setCanceled() {
            isCanceled = true;
        }
        public boolean isCanceled() {
            return isCanceled;
        }
        // Todo: Finish, Review, Improve
        public void tickBuild(ServerLevel level) {
            if(isCanceled) {
                return;
            }
            int remainingBatchCount = BUILD_BATCH_SIZE;
            batch:
            for (int y = trackedHeight; y < instance.height; y++) {
                trackedHeight = y;
                for (int x = trackedWidth; x < instance.width; x++) {
                    trackedWidth = x;
                    for (int z = trackedLength; z < instance.length; z++) {
                        // If batch is exhausted, continue in the next tick, or if build is finished terminate as well (in case the tick is triggered even if it shouldn't)
                        if(remainingBatchCount == 0 || isBuildFinished()) break batch;
                        trackedLength = z;
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState toBePlaced = instance.getBlockState(x, y, z);
                        Item item = toBePlaced.getBlock().asItem();

                        // A build can only start if all materials are present, so its assumed the only missing item in here is air which will be ignored
                        if(materialBuffer.containsKey(item)) {
                            materialBuffer.put(item, materialBuffer.get(item) - 1);
                        }

                        if(preserveExcavation) {
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
        public boolean isBuildFinished() {
            return currentIndex >= LAST_INDEX;
        }
    }
}
