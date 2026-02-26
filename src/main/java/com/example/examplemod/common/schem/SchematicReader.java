package com.example.examplemod.common.schem;

import io.netty.handler.codec.bytes.ByteArrayDecoder;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SchematicReader {
    public static SchematicInstance getSchematicInstance(File file) throws IOException {
        CompoundTag schematicData = NbtIo.readCompressed(file);

        int version = schematicData.getInt("Version");
        CompoundTag metaData = schematicData.getCompound("Metadata");
        int relativeOffsetX = metaData.getInt("WEOffsetX");
        int relativeOffsetY = metaData.getInt("WEOffsetY");
        int relativeOffsetZ = metaData.getInt("WEOffsetZ");

        int[] worldOffsets = schematicData.getIntArray("Offset");
        byte[] BlockData = schematicData.getByteArray("BlockData");


        short width = schematicData.getShort("Width");
        short height = schematicData.getShort("Height");
        short length = schematicData.getShort("Length");
        int paletteSize = schematicData.getInt("PaletteMax");
        CompoundTag palette = schematicData.getCompound("Palette");




    }
    private int[] getIntsFromBytes(byte[] blockDataBytes, int width, int height, int length) {
        int totalSize = width * height * length;
        int[] convertedBytes = new int[totalSize];
        int byteIndex = 0;
        for (int i = 0; i < convertedBytes.length; i++) {
            boolean lastByte;
            int value = 0;
            int offset = 0;
            byte currentByte;
            do {
                currentByte = blockDataBytes[byteIndex++];
                value |= (currentByte & 0x7f) << offset;
                offset += 7;
                lastByte = (currentByte & 0x80) == 0;
            } while(!lastByte);
            convertedBytes[i] = value;
        }
        return convertedBytes;
    }
    private HashMap<Integer, Integer> frequencyDistOfPalette() {
        HashMap<Integer, Integer> frequencyDistOfPalette = new HashMap<>();
    }

    public static class SchematicInstance {
        private int width;
        private int height;
        private int depth;

        private int paletteSize;
        private HashMap<Integer, String> palette = new HashMap<>();
        private HashMap<String, Integer> blockStateCount = new HashMap<>();
        private int version;

        private static Vec3 determinePosition(int width, int height, int depth, int index) {

        }

    }
}
