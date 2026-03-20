package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.client.schem.ClientPayloadManager;
import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TransferResultPacket implements IPacketPayload {
    private final UUID requestUUID;
    private final boolean accepted;
    public TransferResultPacket(UUID requestUUID, boolean accepted) {
        this.requestUUID = requestUUID;
        this.accepted = accepted;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(requestUUID);
        buf.writeBoolean(accepted);
    }

    @Override
    public void handleOnMainThread(Supplier<NetworkEvent.Context> ctx) {
        ClientPayloadManager.INSTANCE.computePayloadResponse(requestUUID, accepted);
    }

    public static TransferResultPacket decode(FriendlyByteBuf in) {
        return new TransferResultPacket(in.readUUID(), in.readBoolean());
    }
}
