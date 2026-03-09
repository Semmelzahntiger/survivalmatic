package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer;

import com.semmelzahntiger.survivalmatic.client.schem.ClientPayloadManager;
import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TransferHeaderResponsePacket implements IPacketPayload {
    public final UUID requestUUID;
    public final boolean success;

    public TransferHeaderResponsePacket(UUID requestUUID, boolean success) {
        this.requestUUID = requestUUID;
        this.success = success;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(requestUUID);
        buf.writeBoolean(success);
    }
    public static TransferHeaderResponsePacket decode(FriendlyByteBuf buf) {
        return new TransferHeaderResponsePacket(buf.readUUID(), buf.readBoolean());
    }
    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ClientPayloadManager.INSTANCE.computeRequestResponse(this.requestUUID, this.success);
    }
}
