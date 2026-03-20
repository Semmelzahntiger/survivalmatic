package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.projectiondata;

import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class HologramUpdatePacket implements IPacketPayload {
    public final UUID hologramUUID;
    public final GlobalPos updatedPos;

    public HologramUpdatePacket(UUID hologramUUID, GlobalPos updatedPos) {
        this.hologramUUID = hologramUUID;
        this.updatedPos = updatedPos;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(hologramUUID);
        buf.writeGlobalPos(updatedPos);
    }

    @Override
    public void handleOnMainThread(Supplier<NetworkEvent.Context> ctx) {

    }

    public static HologramUpdatePacket decode(FriendlyByteBuf buf) {
        return new HologramUpdatePacket(buf.readUUID(), buf.readGlobalPos());
    }

}
