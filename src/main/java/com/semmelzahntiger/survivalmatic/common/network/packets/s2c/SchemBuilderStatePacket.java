package com.semmelzahntiger.survivalmatic.common.network.packets.s2c;

public class SchemBuilderStatePacket {
    private final int state;
    private final String message;
    public SchemBuilderStatePacket(int state, String message) {
        this.state = state;
        this.message = message;
    }
    public int getState() {
        return state;
    }
    public String getMessage() {
        return message;
    }
}
