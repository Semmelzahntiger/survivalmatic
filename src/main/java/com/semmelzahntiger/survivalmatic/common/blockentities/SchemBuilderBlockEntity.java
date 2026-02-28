package com.semmelzahntiger.survivalmatic.common.blockentities;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SchemBuilderBlockEntity extends BlockEntity {
    // Todo: Implement Config Settings for Batch Sizes
    private static final int ITEM_SCAN_BATCH_SIZE = 40;
    private static final int BUILD_BATCH_SIZE = 40;

    public enum SCHEM_BUILDER_STATE {
        IDLE,
        SCANNING,
        PREPARING,
        BUILDING,
        DUMPING
    }
    private SCHEM_BUILDER_STATE currentState = SCHEM_BUILDER_STATE.IDLE;
    private List<MaterialSource> materialSources = new ArrayList<>();

    private Scan currentScan = null;
    private final HashMap<Item, Integer> materialBuffer = new HashMap<>();
    private HashMap<Item, Integer> missingItems = new HashMap<>();
    private HashMap<Item, Integer> dumpBuffer = new HashMap<>();

    public SchematicReader.SchematicInstance instance = null;
    private boolean schematicLocked = false;

    private boolean hasRequiredItems = false;

    private boolean preserveExcavation = false;


    public SchemBuilderBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }
    public void setSchematic(String schematicName) {
        if(!this.getLevel().isClientSide()) {
            try {
                MinecraftServer server = this.getLevel().getServer();
                Path path = Path.of(server.getServerDirectory().getPath() + "/schematics/" + schematicName + ".schem");
                if(Files.exists(path)) {
                    File file = new File(path.toUri());
                    SchematicReader.getSchematicInstance(file , server);
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
    public void tick() {

        // Todo: Synchronize Data with Client
        //PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.getBlockPos())).send();
    }
    // Read and Prepare Scan
    public void beginScanning(boolean extractScan) {
        validateSources();
        // Convert MaterialSources to ScanInstances for batched scanning
        List<SourceScanInstance> scanInstances = materialSources.stream().map(source -> new SourceScanInstance(source.slotCount, source.pos)).toList();
        Queue<SourceScanInstance> materialSourceQueue = new LinkedList<>(scanInstances);
        // If a buildscan is run, schematic cant be changed until cancellation
        if(extractScan) {
            schematicLocked = true;
        }
        // Only scan for missing items in Buffer
        calculateMissingItems();
        // Instantiate new Scan
        currentScan = new Scan(materialSourceQueue, missingItems, extractScan);
        currentScan.beginScan();
        currentState = extractScan ? SCHEM_BUILDER_STATE.PREPARING : SCHEM_BUILDER_STATE.SCANNING;
    }
    public void startBuilding() {

    }
    public void cancel() {

    }
    // Todo: Only recalculate if MaterialBuffer has changed
    private void calculateMissingItems() {
        missingItems = new HashMap<>(instance.requiredItems);
        materialBuffer.forEach((item, amount) -> {
            if(missingItems.containsKey(item)) {
                missingItems.put(item, missingItems.get(item) - amount);
            }
        });
    }
    private boolean isMaterialBufferSufficient() {
        return isScanAccumulatedSufficient(materialBuffer);
    }
    private boolean isScanAccumulatedSufficient(Map<Item, Integer> accumulatedItems) {
        boolean sufficient = true;
        for (Map.Entry<Item, Integer> itemIntegerEntry : instance.requiredItems.entrySet()) {
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

        boolean isScanning = false;
        boolean finishedScanning = false;

        private Scan(Queue<SourceScanInstance> scanQueue, Map<Item, Integer> requiredItems, boolean isExtractingScan) {
            this.scanQueue = scanQueue;
            this.missingItems = new HashMap<>(requiredItems);
            this.isExtractingScan = isExtractingScan;
        }

        public void beginScan() {
            isScanning = true;
        }
        public void tickScan() {
            if(isScanning) {
                proceedScan();
            }
        }
        private void finishScan() {
            isScanning = false;
            finishedScanning = true;
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
                // If Block has no IItemHandler capability, remove Source
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
    private class BuildInstance {
        private int trackedWidth;
        private int trackedLength;
        private int trackedHeight;
        final SchematicReader.SchematicInstance instance;

        private BuildInstance(SchematicReader.SchematicInstance instance) {
            this.instance = instance;
        }
        public void build(ServerLevel level) {
            int remainingBatchCount = BUILD_BATCH_SIZE;
            batch:
            for (int y = trackedHeight; y < instance.height; y++) {
                trackedHeight = y;
                for (int x = trackedWidth; x < instance.width; x++) {
                    trackedWidth = x;
                    for (int z = trackedLength; z < instance.length; z++) {
                        trackedLength = z;
                        level.setBlock(new BlockPos(x,y,z), instance.getBlockState(x,y,z),0);
                        // If batchCount is exhausted, continue building in the next tick
                        if(--remainingBatchCount == 0) break batch;
                    }
                }
            }
            // Todo: Signal when finished building
        }
    }
}
