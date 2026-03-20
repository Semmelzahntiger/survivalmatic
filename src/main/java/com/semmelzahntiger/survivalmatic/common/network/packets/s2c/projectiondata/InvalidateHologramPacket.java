package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.projectiondata;

import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class InvalidateHologramPacket implements IPacketPayload {
    public final UUID hologramUUID;

    public InvalidateHologramPacket(UUID hologramUUID) {
        this.hologramUUID = hologramUUID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(hologramUUID);
    }

    @Override
    public void handleOnMainThread(Supplier<NetworkEvent.Context> ctx) {

    }

    public static InvalidateHologramPacket decode(FriendlyByteBuf buf) {
        return new InvalidateHologramPacket(buf.readUUID());
    }

}
