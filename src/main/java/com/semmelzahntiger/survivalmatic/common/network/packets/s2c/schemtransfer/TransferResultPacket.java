package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer;

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
    public static TransferResultPacket decode(FriendlyByteBuf in) {
        return new TransferResultPacket(in.readUUID(), in.readBoolean());
    }
    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {

    }
}
