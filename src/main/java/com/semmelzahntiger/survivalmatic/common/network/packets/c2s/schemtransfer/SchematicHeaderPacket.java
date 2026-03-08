package com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer;

import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SchematicHeaderPacket implements IPacketPayload {
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
    public BlockPos getBlockEntityPosition() {
        return blockEntityPosition;
    }
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(requestUUID);
        buffer.writeInt(this.paletteSize);
        buffer.writeInt(this.byteCount);
        buffer.writeUtf(this.schematicName);
        buffer.writeShort(this.dimensions[0]);
        buffer.writeShort(this.dimensions[1]);
        buffer.writeShort(this.dimensions[2]);
        buffer.writeBlockPos(this.blockEntityPosition);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {

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
}
