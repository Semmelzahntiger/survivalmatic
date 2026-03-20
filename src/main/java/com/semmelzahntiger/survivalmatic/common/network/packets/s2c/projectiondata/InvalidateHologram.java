package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.projectiondata;

import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class InvalidateHologram implements IPacketPayload {
    @Override
    public void encode(FriendlyByteBuf buf) {

    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {

    }
}
