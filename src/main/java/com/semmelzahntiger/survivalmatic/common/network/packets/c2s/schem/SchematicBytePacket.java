package com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schem;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class SchematicBytePacket {
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
    public static void encode(SchematicBytePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.requestUUID);
        buffer.writeBoolean(packet.finalBytePacket);
        buffer.writeByteArray(packet.data);
    }
    public static SchematicBytePacket decode(FriendlyByteBuf buffer) {
        UUID requestUUID = buffer.readUUID();
        boolean finalBytePacket = buffer.readBoolean();
        byte[] data = buffer.readByteArray();
        return new SchematicBytePacket(requestUUID, data, finalBytePacket);
    }
    public static void handle(SchematicBytePacket packet, NetworkEvent.Context ctx) {

    }
}
