package com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schemtransfer;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.network.ModNetwork;
import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.schemtransfer.TransferHeaderResponsePacket;
import com.semmelzahntiger.survivalmatic.common.schem.ServerSchematicHandler;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class TransferHeaderPacket implements IPacketPayload {
    private final UUID requestUUID;
    private final String schematicName;
    private final String fileExtension;
    private final int byteCount;
    private final GlobalPos blockEntityPosition;

    public TransferHeaderPacket(UUID requestUUID, String schematicName, String fileExtension, int byteCount, GlobalPos blockEntityPosition) {
        this.requestUUID = requestUUID;
        this.schematicName = schematicName;
        this.fileExtension = fileExtension;
        this.byteCount = byteCount;
        this.blockEntityPosition = blockEntityPosition;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(requestUUID);
        buf.writeUtf(schematicName);
        buf.writeUtf(fileExtension);
        buf.writeInt(byteCount);
        buf.writeGlobalPos(blockEntityPosition);
    }

    @Override
    public void handleOnMainThread(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        MinecraftServer server = player.getServer();
        if (server != null) {
            ServerSchematicHandler.getInstance().ifPresentOrElse(handler -> {
                handler.incomingInitialRequest(this.requestUUID, this.schematicName,this.fileExtension, this.byteCount, this.blockEntityPosition, server, player.getUUID());
            }, () -> ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TransferHeaderResponsePacket(this.requestUUID, false)));
        }
        else {
            ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TransferHeaderResponsePacket(this.requestUUID, false));
        }
    }

    public static TransferHeaderPacket decode(FriendlyByteBuf buf) {
        UUID requestUUID = buf.readUUID();
        String schematicName = buf.readUtf();
        String fileExtension = buf.readUtf();
        int byteCount = buf.readInt();
        GlobalPos blockEntityPosition = buf.readGlobalPos();
        return new TransferHeaderPacket(requestUUID, schematicName,fileExtension, byteCount, blockEntityPosition);
    }
}
