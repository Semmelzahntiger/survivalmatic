package com.semmelzahntiger.survivalmatic.common.network.packets.s2c;

public class SchemBuilderStatePacket {
    private final int state;
    public SchemBuilderStatePacket(int state) {
        this.state = state;
    }
    public int getState() {
        return state;
    }
}
