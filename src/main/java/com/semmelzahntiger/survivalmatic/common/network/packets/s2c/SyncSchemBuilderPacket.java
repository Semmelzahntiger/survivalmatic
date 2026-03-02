package com.semmelzahntiger.survivalmatic.common.network.packets.s2c;

public class SyncSchemBuilderPacket {
    private final int state;
    public SyncSchemBuilderPacket(int state) {
        this.state = state;
    }
    public int getState() {
        return state;
    }
}
