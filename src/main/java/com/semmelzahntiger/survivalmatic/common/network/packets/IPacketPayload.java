package com.semmelzahntiger.survivalmatic.common.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface IPacketPayload {
    void encode(FriendlyByteBuf buf);
    void handle(Supplier<NetworkEvent.Context> ctx);
    static void handle(IPacketPayload packet, Supplier<NetworkEvent.Context> ctx) {
        packet.handle(ctx);
    }
}
