package com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.network.ModNetwork;
import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer.TransferHeaderResponsePacket;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer.TransferResultPacket;
import com.semmelzahntiger.survivalmatic.common.schem.ServerSchematicHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class TransferPayloadPacket implements IPacketPayload {
    private final UUID requestUUID;
    private final int batchSize;
    private final byte[] payload;
    private final boolean finalPacket;

    public TransferPayloadPacket(UUID requestUUID, int batchSize, byte[] payload, boolean finalPacket) {
        this.requestUUID = requestUUID;
        this.batchSize = batchSize;
        this.payload = payload;
        this.finalPacket = finalPacket;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(requestUUID);
        buf.writeInt(batchSize);
        buf.writeBoolean(this.finalPacket);
        buf.writeByteArray(this.payload);
    }
    public static TransferPayloadPacket decode(FriendlyByteBuf buf) {
        UUID requestUUID = buf.readUUID();
        int batchSize = buf.readInt();
        boolean finalPacket = buf.readBoolean();
        byte[] payload = buf.readByteArray(2097152);
        return new TransferPayloadPacket(requestUUID, batchSize, payload, finalPacket);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            MinecraftServer server = player.getServer();
            if (server != null) {
                ServerSchematicHandler.getInstance().ifPresentOrElse(handler -> {
                    handler.incomingByteBatch(this.requestUUID, this.payload,this.finalPacket, server, player.getUUID());
                }, () -> ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TransferResultPacket(this.requestUUID, false)));
            }
            else {
                ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TransferResultPacket(this.requestUUID, false));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
