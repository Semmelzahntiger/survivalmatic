package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.client.data.SchemBuilderInterfaceData;
import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class SchemBuilderDumpBufferPacket implements IPacketPayload {
    public final Map<Item,Integer> items;
    private final Registry<Item> registry;
    public SchemBuilderDumpBufferPacket(Map<Item,Integer> items, Registry<Item> registry) {
        this.items = items;
        this.registry = registry;
    }
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(items.size());
        items.forEach((item, integer) -> {
            buf.writeId(registry, item);
            buf.writeInt(integer);
        });
    }
    public static SchemBuilderDumpBufferPacket decode(FriendlyByteBuf buf) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if(connection != null) {
            RegistryAccess access = connection.registryAccess();
            Optional<Registry<Item>> registry = access.registry(ForgeRegistries.ITEMS.getRegistryKey());
            if(registry.isPresent()) {
                Registry<Item> itemRegistry = registry.get();
                int size = buf.readInt();
                Map<Item,Integer> items = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    items.put(buf.readById(itemRegistry), buf.readInt());
                }
                return new SchemBuilderDumpBufferPacket(items, itemRegistry);
            }
            else {
                Survivalmatic.LOGGER.info("Couldn't find Item Registry, returning empty SchemBuilderDumpBufferPacket");
            }
        }
        else {
            Survivalmatic.LOGGER.error("Couldn't fetch RegistryAccess from Server as Connection is null, returning empty SchemBuilderItemBufferPacket");
        }
        return new SchemBuilderDumpBufferPacket(Map.of(), null);
    }

    @Override
    public void handleOnMainThread(Supplier<NetworkEvent.Context> ctx) {
        SchemBuilderInterfaceData.getINSTANCE().computeDumpBufferPacket(this);
    }
}
