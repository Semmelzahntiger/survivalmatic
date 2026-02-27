package com.example.examplemod.common.schem;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchematicReader {
    public static SchematicInstance getSchematicInstance(File file, MinecraftServer server) throws IOException {
        CompoundTag schematicData = NbtIo.readCompressed(file);

        int version = schematicData.getInt("Version");
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
        Map<Integer, BlockState> paletteMap = new HashMap<>();
        // Required item type for each ID
        Map<Integer, Item> paletteItems = new HashMap<>();

        for (String stateString : paletteTag.getAllKeys()) {
            int paletteID = paletteTag.getInt(stateString);
            BlockState state;
            // Default back to Air if state can't be parsed
            try {
                BlockStateParser.BlockResult result = BlockStateParser.parseForBlock(blockRegistry, stateString, false);
                state = result.blockState();
            } catch (CommandSyntaxException e) {
                state = Blocks.AIR.defaultBlockState();
            }
            paletteMap.put(paletteID, state);
            paletteItems.put(paletteID, state.getBlock().asItem());
        }
        int totalSize = width * height * length;
        int[] blockDataIntegers = new int[totalSize];
        // Required No. of Items
        Map<Item, Integer> requiredItems = new HashMap<>();

        // Calculate Integer based off the schematics byte array
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
            Item item = paletteItems.get(value);
            if (item != null && item != Items.AIR) {
                requiredItems.merge(item, 1, Integer::sum);
            }
        }

        return new SchematicInstance(width, height, length, version, weOffset, requiredItems, blockDataIntegers, paletteMap);
    }
    public static class SchematicInstance {
        public final int width;
        public final int height;
        public final int length;
        public final int version;
        public final BlockPos weOffset;
        public final Map<Item, Integer> requiredItems;

        private final int[] blockData;
        private final Map<Integer, BlockState> palette;

        public SchematicInstance(int width, int height, int length, int version, BlockPos weOffset, Map<Item, Integer> requiredItems, int[] blockData, Map<Integer, BlockState> palette) {
            this.width = width;
            this.height = height;
            this.length = length;
            this.version = version;
            this.weOffset = weOffset;
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
    }
}
