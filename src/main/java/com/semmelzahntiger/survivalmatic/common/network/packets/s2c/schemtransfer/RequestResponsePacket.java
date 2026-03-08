package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer;

import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class RequestResponsePacket implements IPacketPayload {
    private final boolean accepted;
    public RequestResponsePacket(boolean accepted) {
        this.accepted = accepted;
    }
    public static RequestResponsePacket decode(FriendlyByteBuf in) {
        return new RequestResponsePacket(in.readBoolean());
    }
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(accepted);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {

    }
}
