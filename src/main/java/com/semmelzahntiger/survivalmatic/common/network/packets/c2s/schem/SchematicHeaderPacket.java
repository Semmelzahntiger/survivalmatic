package com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class SchematicHeaderPacket {
    public final UUID requestUUID;
    public final int paletteSize;
    public final int byteCount;
    public final String schematicName;
    public final short[] dimensions;
    public final BlockPos blockEntityPosition;

    public SchematicHeaderPacket(UUID requestUUID, int paletteSize, int byteCount, String schematicName, short[] dimensions, BlockPos blockEntityPosition) {
        this.requestUUID = requestUUID;
        this.paletteSize = paletteSize;
        this.byteCount = byteCount;
        this.schematicName = schematicName;
        this.dimensions = dimensions;
        this.blockEntityPosition = blockEntityPosition;
    }
    public int getPaletteSize() {
        return paletteSize;
    }
    public int getByteCount() {
        return byteCount;
    }
    public String getSchematicName() {
        return schematicName;
    }
    public short[] getDimensions() {
        return dimensions;
    }
    public UUID getRequestUUID() {
        return requestUUID;
    }
    public BlockPos getBlockEntityPosition() {
        return blockEntityPosition;
    }
    public static void encode(SchematicHeaderPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.requestUUID);
        buffer.writeInt(packet.paletteSize);
        buffer.writeInt(packet.byteCount);
        buffer.writeUtf(packet.schematicName);
        buffer.writeShort(packet.dimensions[0]);
        buffer.writeShort(packet.dimensions[1]);
        buffer.writeShort(packet.dimensions[2]);
        buffer.writeBlockPos(packet.blockEntityPosition);
    }
    public static SchematicHeaderPacket decode(FriendlyByteBuf buffer) {
        UUID requestUUID = buffer.readUUID();
        int paletteSize = buffer.readInt();
        int byteCount = buffer.readInt();
        String schematicName = buffer.readUtf();
        short[] dimensions = new short[] {
                buffer.readShort(),
                buffer.readShort(),
                buffer.readShort(),
        };
        BlockPos blockEntityPosition = new BlockPos(buffer.readBlockPos());
        return new SchematicHeaderPacket(requestUUID, paletteSize, byteCount, schematicName, dimensions, blockEntityPosition);
    }
    public static void handle(SchematicHeaderPacket packet, NetworkEvent.Context ctx) {

    }
}
