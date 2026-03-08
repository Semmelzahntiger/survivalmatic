package com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer;

import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SchematicBytePacket implements IPacketPayload {
    public final UUID requestUUID;
    public final boolean finalBytePacket;
    public final byte[] data;
    public SchematicBytePacket(UUID requestUUID,byte[] data, boolean finalBytePacket) {
        this.requestUUID = requestUUID;
        this.data = data;
        this.finalBytePacket = finalBytePacket;
    }
    public UUID getRequestUUID() {
        return requestUUID;
    }
    public byte[] getData() {
        return data;
    }
    public boolean isFinalBytePacket() {
        return finalBytePacket;
    }
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.requestUUID);
        buffer.writeBoolean(this.finalBytePacket);
        buffer.writeByteArray(this.data);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {

    }

    public static SchematicBytePacket decode(FriendlyByteBuf buffer) {
        UUID requestUUID = buffer.readUUID();
        boolean finalBytePacket = buffer.readBoolean();
        byte[] data = buffer.readByteArray();
        return new SchematicBytePacket(requestUUID, data, finalBytePacket);
    }
}
