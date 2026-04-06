package com.semmelzahntiger.survivalmatic.client.data;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderItemBufferPacket;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class SchemBuilderInterfaceData {
    public Map<Item, Integer> materialBufferItems = new HashMap<>();
    public Map<Item, Integer> dumpBufferItems = new HashMap<>();
    public int tracker = 0;
    private static final SchemBuilderInterfaceData INSTANCE = new SchemBuilderInterfaceData();

    public static SchemBuilderInterfaceData getINSTANCE() {
        return INSTANCE;
    }
    public void computeItemPacket(SchemBuilderItemBufferPacket packet) {
        materialBufferItems = packet.materialBufferItems;
        dumpBufferItems = packet.dumpBufferItems;
        tracker++;
        Survivalmatic.LOGGER.info(materialBufferItems.toString());
        Survivalmatic.LOGGER.info(dumpBufferItems.toString());
    }
    public Map<Item, Integer> getMaterialBufferItems() {
        return materialBufferItems;
    }
    public Map<Item, Integer> getDumpBufferItems() {
        return dumpBufferItems;
    }
    public int getTracker() {
        return tracker;
    }
}
