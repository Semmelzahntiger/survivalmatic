package com.semmelzahntiger.survivalmatic.client.data;

import com.semmelzahntiger.survivalmatic.common.network.packets.s2c.interfacedata.SchemBuilderItemBufferPacket;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class SchemBuilderInterfaceData {
    private int state = 0;
    private String displayMessage = "";
    public Map<Item, Integer> materialBufferItems = new HashMap<>();
    public Map<Item, Integer> dumpBufferItems = new HashMap<>();
    private static final SchemBuilderInterfaceData INSTANCE = new SchemBuilderInterfaceData();

    public static SchemBuilderInterfaceData getINSTANCE() {
        return INSTANCE;
    }
    public void computeItemPacket(SchemBuilderItemBufferPacket packet) {
        materialBufferItems = packet.materialBufferItems;
        dumpBufferItems = packet.dumpBufferItems;
    }
    public void setState(int state) {
        this.state = state;
    }
    public int getState() {
        return state;
    }
    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }
    public String getDisplayMessage() {
        return displayMessage;
    }
}
