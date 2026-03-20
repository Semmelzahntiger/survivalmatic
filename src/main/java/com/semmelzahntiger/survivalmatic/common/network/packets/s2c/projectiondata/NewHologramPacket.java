package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.projectiondata;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class NewHologramPacket implements IPacketPayload {
    public final UUID hologramUUID;
    public final GlobalPos pos;
    public final int width;
    public final int height;
    public final int length;
    public boolean failed = false;
    public final Map<Integer, BlockState> blocks;


    public NewHologramPacket (UUID hologramUUID, GlobalPos pos,int width, int height, int length, Map<Integer, BlockState> blocks) {
        this.hologramUUID = hologramUUID;
        this.pos = pos;
        this.width = width;
        this.height = height;
        this.length = length;
        this.blocks = blocks;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(hologramUUID);
        buf.writeGlobalPos(pos);
        buf.writeInt(width);
        buf.writeInt(height);
        buf.writeInt(length);
        buf.writeInt(blocks.size());
        blocks.forEach((flatPos, state) -> {
            buf.writeInt(flatPos);
            buf.writeInt(Block.getId(state));
        });
    }

    @Override
    public void handleOnMainThread(Supplier<NetworkEvent.Context> ctx) {

    }

    public static NewHologramPacket decode(FriendlyByteBuf buf) {
        UUID hologramUUID = buf.readUUID();
        GlobalPos pos = buf.readGlobalPos();
        int width = buf.readInt();
        int height = buf.readInt();
        int length = buf.readInt();
        int size = buf.readInt();
        HashMap<Integer, BlockState> blocks = new HashMap<>();
        for (int i = 0; i < size; i++) {
            int flatPos = buf.readInt();
            BlockState state = Block.stateById(buf.readInt());
            blocks.put(flatPos, state);
        }
        return new NewHologramPacket(hologramUUID,pos,width, height, length, blocks);
    }

}
