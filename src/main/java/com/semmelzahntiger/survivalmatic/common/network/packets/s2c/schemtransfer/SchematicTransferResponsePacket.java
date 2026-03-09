package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer;

import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SchematicTransferResponsePacket implements IPacketPayload {
    private final UUID requestUUID;
    private final boolean success;

    public SchematicTransferResponsePacket(UUID requestUUID, boolean success) {
        this.requestUUID = requestUUID;
        this.success = success;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(requestUUID);
        buf.writeBoolean(success);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {

    }
    public static SchematicTransferResponsePacket decode(FriendlyByteBuf buf) {
        return new SchematicTransferResponsePacket(buf.readUUID(), buf.readBoolean());
    }
}
