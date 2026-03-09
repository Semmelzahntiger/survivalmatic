package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer;

import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SchematicTransferRequestResponsePacket implements IPacketPayload {
    private final UUID requestUUID;
    private final boolean accepted;
    public SchematicTransferRequestResponsePacket(UUID requestUUID, boolean accepted) {
        this.requestUUID = requestUUID;
        this.accepted = accepted;
    }
    public static SchematicTransferRequestResponsePacket decode(FriendlyByteBuf in) {
        return new SchematicTransferRequestResponsePacket(in.readUUID(), in.readBoolean());
    }
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(requestUUID);
        buf.writeBoolean(accepted);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {

    }
}
