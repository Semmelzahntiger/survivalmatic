package com.semmelzahntiger.survivalmatic.common.schem;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.semmelzahntiger.survivalmatic.Survivalmatic;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SchematicReader {
    /**
     * Reads a Schematic file from Disk and converts it into a {@link StreamableSchematicInstance StreamableSchematicInstance} intended to be sent to the Server, thus is only available on the Client
     * @param file Schematic File
     * @return {@link Optional} of the {@link StreamableSchematicInstance StreamableSchematicInstance}, if file cannot be read, return empty
     */
    @OnlyIn(Dist.CLIENT)
    public static Optional<StreamableSchematicInstance> getStreamableInstance(File file) {
        try(FileInputStream stream = new FileInputStream(file)) {
            String name = file.getName();
            String extension = FilenameUtils.getExtension(name);
            byte[] bytes = stream.readAllBytes();
            return Optional.of(new StreamableSchematicInstance(name,extension, bytes));
        }
        catch(IOException e) {
            Survivalmatic.LOGGER.error("Couldn't read schematic from Disk", e);
            return Optional.empty();
        }
        catch(Exception e) {
            Survivalmatic.LOGGER.error("Error occurred while trying to create StreamableInstance", e);
            return Optional.empty();
        }
    }

    /**
     * Asynchronous Version of {@link SchematicReader#getStreamableInstance(File)}
     * @return Completable Future containing the Optional of {@link StreamableSchematicInstance StreamableSchematicInstance}
     */
    @OnlyIn (Dist.CLIENT)
    public static CompletableFuture<Optional<StreamableSchematicInstance>> getStreamableInstanceAsync(File file) {
        return CompletableFuture.supplyAsync(() -> getStreamableInstance(file));
    }
    /**
     * Only reads the Schematic as a {@link ByteArrayInputStream}
     * @param file schematic file
     * @return Optional of {@link ByteArrayInputStream}
     */
    public static Optional<ByteArrayInputStream> readFileAsInputStream(File file) {
        try(FileInputStream stream = new FileInputStream(file)) {
            byte[] bytes = stream.readAllBytes();
            return Optional.of(new ByteArrayInputStream(bytes));
        }
        catch (FileNotFoundException e) {
            Survivalmatic.LOGGER.error("File not found", e);
        }
        catch (IOException e) {
            Survivalmatic.LOGGER.error("Error reading file", e);
        }
        return Optional.empty();
    }

    /**
     * Takes a {@link ByteArrayInputStream} (e.g. from {@link SchematicReader#readFileAsInputStream(File)} and attempts to convert it into a {@link SchematicInstance SchematicInstance}
     * @param stream ByteArray from the Schematic
     * @param registry RegistryAccess from the Server in order to retrieve {@link BlockState BlockStates} and the required {@link Item Items} for the Schematic
     * @return Optional of {@link SchematicInstance SchematicInstance}
     */
    public static Optional<SchematicInstance> readSchematicFromBuffer(String instanceName, ByteArrayInputStream stream, String fileExtension, HolderLookup.Provider registry) {
        if(fileExtension.equals("schem")) {
            Survivalmatic.LOGGER.info("Reading .schem File");
            return readSchemFile(instanceName,stream, registry);
        }
        else if(fileExtension.equals("nbt")) {
            Survivalmatic.LOGGER.info("Reading .nbt File");
            return readNbtFile(instanceName,stream, registry);
        }
        else {
            Survivalmatic.LOGGER.error(".{} invalid. Only .schem and .nbt are currently supported", fileExtension);
            return Optional.empty();
        }
    }
    private static Optional<SchematicInstance> readSchemFile(String name,ByteArrayInputStream stream, HolderLookup.Provider registry) {
        try {
            CompoundTag schematicRead = NbtIo.readCompressed(stream);
            CompoundTag schematicData = schematicRead.getCompound("Schematic");
            int version = schematicData.getInt("Version");
            BlockPos schematicOffset;
            short width;
            short height;
            short length;
            byte[] blockDataBytes;
            HashMap<Integer, String> paletteMap = new HashMap<>();
            HashMap<Integer, BlockState> blockStateMap = new HashMap<>();
            HashMap<Integer, Item> itemMap = new HashMap<>();
            HashMap<Item, Integer> requiredItems = new HashMap<>();
            CompoundTag paletteTag;
            // Schematic Version 2 Parser
            if (version == 2) {
                CompoundTag metaData = schematicData.getCompound("Metadata");
                schematicOffset = new BlockPos(
                        metaData.getInt("WEOffsetX"),
                        metaData.getInt("WEOffsetY"),
                        metaData.getInt("WEOffsetZ")
                );
                width = schematicData.getShort("Width");
                height = schematicData.getShort("Height");
                length = schematicData.getShort("Length");
                paletteTag = schematicData.getCompound("Palette");
                blockDataBytes = schematicData.getByteArray("BlockData");
                // Holds BlockState for each ID in the Schematic (Inversed CompoundTag)
            }
            // Schematic Version 3 Parser
            else if (version == 3) {
                CompoundTag blockTag = schematicData.getCompound("Blocks");
                paletteTag = blockTag.getCompound("Palette");
                blockDataBytes = blockTag.getByteArray("Data");
                length = schematicData.getShort("Length");
                height = schematicData.getShort("Height");
                width = schematicData.getShort("Width");
                int[] offsets = schematicData.getIntArray("Offset");
                schematicOffset = new BlockPos(offsets[0], offsets[1], offsets[2]);
            }
            else {
                Survivalmatic.LOGGER.error("No compatible version found");
                return Optional.empty();
            }

            for (String key : paletteTag.getAllKeys()) {
                paletteMap.put(paletteTag.getInt(key), key);
            }
            for (Map.Entry<Integer, String> entry : paletteMap.entrySet()) {
                BlockState result;
                try {
                    result = BlockStateParser.parseForBlock(registry.lookupOrThrow(Registries.BLOCK), entry.getValue(), false).blockState();
                    itemMap.put(entry.getKey(), result.getBlock().asItem());
                }
                catch (CommandSyntaxException e) {
                    result = Blocks.AIR.defaultBlockState();
                }
                blockStateMap.put(entry.getKey(), result);
            }
            int totalSize = width * height * length;
            int[] blockDataIntegers = new int[totalSize];
            int byteIndex = 0;
            for (int i = 0; i < totalSize; i++) {
                boolean lastByte;
                int value = 0;
                int offset = 0;
                byte currentByte;
                do {
                    currentByte = blockDataBytes[byteIndex++];
                    value |= (currentByte & 0x7f) << offset;
                    offset += 7;
                    lastByte = (currentByte & 0x80) == 0;
                } while (!lastByte);

                blockDataIntegers[i] = value;
                Item item = itemMap.get(value);
                if (item != null && item != Items.AIR) {
                    requiredItems.merge(item, 1, Integer::sum);
                }
            }
            Survivalmatic.LOGGER.info("Read schematic successfully");
            return Optional.of(new SchematicInstance(name,width, height, length, blockDataIntegers, schematicOffset, blockStateMap, requiredItems));
        }
        catch (IOException exception) {
            Survivalmatic.LOGGER.error("Failed to read schematic from ByteArrayInputStream", exception);
            return Optional.empty();
        }
    }
    private static Optional<SchematicInstance> readNbtFile(String name, ByteArrayInputStream stream, HolderLookup.Provider registry) {
        try {
            HashMap<Integer, BlockState> blockStateMap = new HashMap<>();
            HashMap<Integer, Item> itemMap = new HashMap<>();
            HashMap<Item, Integer> requiredItems = new HashMap<>();
            CompoundTag schematic = NbtIo.readCompressed(stream);
            int[] dimensions = schematic.getIntArray("size");
            int width = dimensions[0];
            int height = dimensions[1];
            int length = dimensions[2];
            ListTag blocks = schematic.getList("blocks",Tag.TAG_COMPOUND);
            int[] flattenedBlockPos = new int[blocks.size()];
            ListTag palette = schematic.getList("palette",Tag.TAG_COMPOUND);
            for (int i = 0; i < palette.size(); i++) {
                BlockState blockState = NbtUtils.readBlockState(registry.lookupOrThrow(Registries.BLOCK), palette.getCompound(i));
                blockStateMap.put(i, blockState);
                Item item = blockState.getBlock().asItem();
                if(item != Items.AIR) {
                    itemMap.put(i, blockState.getBlock().asItem());
                }
            }
            for (Tag block : blocks) {
                CompoundTag compoundTag = (CompoundTag) block;
                int[] pos = compoundTag.getIntArray("pos");
                int stateID = compoundTag.getInt("state");
                int index = to1D(pos[0],pos[1], pos[2],width,length);
                flattenedBlockPos[index] = stateID;
                requiredItems.put(itemMap.get(stateID), requiredItems.getOrDefault(itemMap.get(stateID), 0) + 1);
            }
            return Optional.of(new SchematicInstance(name, width, height, length, flattenedBlockPos, new BlockPos(0,0,0), blockStateMap, requiredItems));
        }
        catch (Exception e) {
            Survivalmatic.LOGGER.error("Failed to read schematic", e);
        }
        return Optional.empty();
    }
    private static int to1D(int x, int y, int z, int width, int length) {
        return (y * width * length) + (z * width) + x;
    }

    /**
     * Asynchronous Version of {@link SchematicReader#readSchematicFromBuffer(String name,ByteArrayInputStream, String, HolderLookup.Provider)}
     * @return {@link CompletableFuture} containing the Optional of {@link SchematicInstance SchematicInstance}
     */
    public static CompletableFuture<Optional<SchematicInstance>> getSchematicFromBufferAsync(String name,ByteArrayInputStream stream, String fileExtension, HolderLookup.Provider registry) {
        return CompletableFuture.supplyAsync(() -> readSchematicFromBuffer(name, stream, fileExtension, registry));
    }

    /**
     * @apiNote Currently Serves no purpose, in the future I intend to allow users to upload Schematics to the Server and share them with all players
     * Asynchronously reads Schematic from File
     * @param file Schematic File
     * @param registry RegistryAccess
     * @return {@link CompletableFuture} with the Optional {@link SchematicInstance SchematicInstance}
     */
    public static CompletableFuture<Optional<SchematicInstance>> getSchematicFromFileAsync(File file, HolderLookup.Provider registry) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<ByteArrayInputStream> stream = readFileAsInputStream(file);
            return stream.isPresent() ? readSchematicFromBuffer(file.getName(),stream.get(),FilenameUtils.getExtension(file.getName()), registry) : Optional.empty();
        });
    }

    /**
     * <p>Client side representation of a schematic, does not contain or expose any concrete information about the schematic</p>
     * <p>Intended to be streamed to the server via the {@link com.semmelzahntiger.survivalmatic.client.schem.ClientPayloadManager ClientPayloadManager}</p>
     */
    @OnlyIn(Dist.CLIENT)
    public static class StreamableSchematicInstance {
        public final String name;
        public final String extension;
        public final int dataSize;
        public final byte[] data;
        public StreamableSchematicInstance(String name, String extension, byte[] data) {
            this.name = name;
            this.extension = extension;
            this.dataSize = data.length;
            this.data = data;
        }
    }
    /**
     * Common/Server Side representation of a schematic, contains the actual information from the Schematic.
     * Either instantiated from {@link SchematicReader#getSchematicFromFileAsync(File, HolderLookup.Provider)} or built from {@link ServerSchematicHandler ServerSchematicHandler}
     * @param name Internal Name for the Schematic
     * @param width Width of Schematic
     * @param height Height of Schematic
     * @param length Length of Schematic
     * @param blockData Integer array converted from the Schematics Byte Array
     * @param offset Offset of the Schematic
     * @param palette All BlockStates in the Schematic with their respective blockData ID
     * @param requiredItems All required Items for the Schematic
     */
    public record SchematicInstance(String name, int width, int height, int length, int[] blockData, BlockPos offset,
                                    Map<Integer, BlockState> palette, Map<Item, Integer> requiredItems) {
        public BlockState getBlockState(int x, int y, int z) {
                if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= length) {
                    return Blocks.AIR.defaultBlockState();
                }
                int index = to1D(x,y,z,width,length);
                int paletteId = blockData[index];
                return palette.getOrDefault(paletteId, Blocks.AIR.defaultBlockState());
            }
        public Map<Item, Integer> getRequiredItemsCopy() {
                return Map.copyOf(requiredItems);
            }
        }
}
