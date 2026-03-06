package com.semmelzahntiger.survivalmatic.common.schem;

import com.google.common.primitives.Bytes;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.semmelzahntiger.survivalmatic.Survivalmatic;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Todo: Documentation
 */
public class SchematicReader {
    @OnlyIn(Dist.CLIENT)
    public static ClientInstance getClientSchematicInstance(File file, MinecraftServer server) throws IOException {
        CompoundTag schematicData = NbtIo.readCompressed(file);
        int version = schematicData.getInt("Version");
        // Version 2 Parser
        if(version == 2) {
            CompoundTag metaData = schematicData.getCompound("Metadata");
            BlockPos weOffset = new BlockPos(
                    metaData.getInt("WEOffsetX"),
                    metaData.getInt("WEOffsetY"),
                    metaData.getInt("WEOffsetZ")
            );
            short width = schematicData.getShort("Width");
            short height = schematicData.getShort("Height");
            short length = schematicData.getShort("Length");

            CompoundTag paletteTag = schematicData.getCompound("Palette");
            // byte array from the schematic
            byte[] blockDataBytes = schematicData.getByteArray("BlockData");
            HolderLookup<Block> blockRegistry = server.registryAccess().lookupOrThrow(Registries.BLOCK);
            // Holds BlockState for each ID in the Schematic (Inversed CompoundTag)
            HashMap<Integer, String> paletteMap = new HashMap<>();
            for (String key : paletteTag.getAllKeys()) {
                paletteMap.put(paletteTag.getInt(key), key);
            }
            return new ClientInstance(width, height, length, weOffset, blockDataBytes, paletteMap);
        }
        else if (version == 3) {
            CompoundTag blockTag = schematicData.getCompound("Blocks");
            CompoundTag palette = blockTag.getCompound("Palette");
            byte[] blockDataBytes = blockTag.getByteArray("Data");
            short length = schematicData.getShort("Length");
            short height = schematicData.getShort("Height");
            short width = schematicData.getShort("Width");
            int[] offsets = schematicData.getIntArray("Offset");
            BlockPos offset = new BlockPos(offsets[0], offsets[1], offsets[2]);

            HashMap<Integer, String> paletteMap = new HashMap<>();
            for (String key : palette.getAllKeys()) {
                paletteMap.put(palette.getInt(key), key);
            }
            return new ClientInstance(length, height, width, offset, blockDataBytes, paletteMap);
        }
        else {
            return null;
        }
    }
    @OnlyIn(Dist.CLIENT)
    public record ClientInstance(short length, short height, short width, BlockPos offset, byte[] blockData, HashMap<Integer, String> palleteMap) {}

    public static class ServerSchematicInstance {
        public final int width;
        public final int height;
        public final int length;
        public final Map<Item, Integer> requiredItems;

        private final int[] blockData;
        private final Map<Integer, BlockState> palette;

        public ServerSchematicInstance(int width, int height, int length, Map<Item, Integer> requiredItems, int[] blockData, Map<Integer, BlockState> palette) {
            this.width = width;
            this.height = height;
            this.length = length;
            this.requiredItems = Map.copyOf(requiredItems);
            this.blockData = blockData;
            this.palette = Map.copyOf(palette);
        }
        public BlockState getBlockState(int x, int y, int z) {
            // return Air if out of bounds
            if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= length) {
                return Blocks.AIR.defaultBlockState();
            }
            int index = (y * width * length) + (z * width) + x;
            int paletteId = blockData[index];
            return palette.getOrDefault(paletteId, Blocks.AIR.defaultBlockState());
        }
        public int[] getBlockData() {
            return blockData;
        }
        public Map<Item, Integer> getRequiredItems() {
            return requiredItems;
        }
        public Map<Item, Integer> getRequiredItemsCopy() {
            return Map.copyOf(requiredItems);
        }
        public static ServerSchematicInstance createFromBuilder(SchematicReconstructor.SchematicBuilder builder, MinecraftServer server) {
            HashMap<Item, Integer> requiredItems = new HashMap<>();
            HashMap<Integer, Item> paletteItems = new HashMap<>();
            List<Byte> blockData = builder.getBytes();
            HashMap<Integer, String> palette = builder.getPalette();
            HashMap<Integer, BlockState> paletteBlockStates = new HashMap<>();
            for (Map.Entry<Integer, String> integerStringEntry : palette.entrySet()) {
                int key = integerStringEntry.getKey();
                try {
                    BlockState result = BlockStateParser.parseForBlock(
                            server.registryAccess().lookupOrThrow(Registries.BLOCK), palette.get(key), false).blockState();
                    if(result != Blocks.AIR.defaultBlockState()) {
                        paletteBlockStates.put(key, result);
                        Item item = result.getBlock().asItem();
                        paletteItems.put(key, item);
                    }
                }
                catch (Exception e) {
                    Survivalmatic.LOGGER.error("Error while parsing block state", e);
                }
            }

            int totalSize = builder.getWidth() * builder.getHeight() * builder.getLength();
            int[] blockDataIntegers = new int[totalSize];
            // Calculate Integer based off the schematics byte array
            int byteIndex = 0;
            for (int i = 0; i < totalSize; i++) {
                boolean lastByte;
                int value = 0;
                int offset = 0;
                byte currentByte;
                do {
                    currentByte = blockData.get(byteIndex++);
                    value |= (currentByte & 0x7f) << offset;
                    offset += 7;
                    lastByte = (currentByte & 0x80) == 0;
                } while (!lastByte);

                blockDataIntegers[i] = value;
                Item item = paletteItems.get(value);
                if (item != null && item != Items.AIR) {
                    requiredItems.merge(item, 1, Integer::sum);
                }
            }
            return new ServerSchematicInstance(builder.getWidth(), builder.getHeight(), builder.getLength(),
                    requiredItems, blockDataIntegers, paletteBlockStates);
        }
    }
}
