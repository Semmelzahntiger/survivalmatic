package com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.client.data.SchemBuilderInterfaceData;
import com.semmelzahntiger.survivalmatic.common.network.packets.IPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class SchemBuilderMaterialBufferPacket implements IPacketPayload {
    public final Map<Item, Tuple<Integer, Integer>> items;
    private final Registry<Item> registry;
    public SchemBuilderMaterialBufferPacket(Map<Item, Tuple<Integer, Integer>> items, Registry<Item> registry) {
        this.items = items;
        this.registry = registry;
    }
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(items.size());
        for (Map.Entry<Item, Tuple<Integer, Integer>> entry : items.entrySet()) {
            Tuple<Integer, Integer> value = entry.getValue();
            buf.writeId(registry, entry.getKey());
            buf.writeInt(value.getA());
            buf.writeInt(value.getB());
        }
    }
    public static SchemBuilderMaterialBufferPacket decode(FriendlyByteBuf buf) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if( connection != null ) {
            RegistryAccess access = connection.registryAccess();
            Optional<Registry<Item>> itemRegistry = access.registry(ForgeRegistries.ITEMS.getRegistryKey());
            if( itemRegistry.isPresent() ) {
                Registry<Item> registry = itemRegistry.get();
                Map<Item, Tuple<Integer, Integer>> items = new HashMap<>();
                int size = buf.readInt();
                for (int i = 0; i < size; i++) {
                    Item item = buf.readById(registry);
                    items.put(item, new Tuple<>(buf.readInt(), buf.readInt()));
                }
                return new SchemBuilderMaterialBufferPacket(items, registry);
            }
            else {
                Survivalmatic.LOGGER.error("Couldn't fetch Item registry, returning empty Packet");

            }
        }
        else {
            Survivalmatic.LOGGER.error("Connection is null, returning empty Packet");
        }
        return new SchemBuilderMaterialBufferPacket(Map.of(), null);
    }

    @Override
    public void handleOnMainThread(Supplier<NetworkEvent.Context> ctx) {
        SchemBuilderInterfaceData.getINSTANCE().computeMaterialBufferPacket(this);
    }
}
