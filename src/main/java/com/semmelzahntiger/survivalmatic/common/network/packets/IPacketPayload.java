package com.semmelzahntiger.survivalmatic.common.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
public interface IPacketPayload {
    void encode(FriendlyByteBuf buf);
    void handleOnMainThread(Supplier<NetworkEvent.Context> ctx);
    default void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleOnMainThread(ctx);
        });
        ctx.get().setPacketHandled(true);
    }
}
