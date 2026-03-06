package com.semmelzahntiger.survivalmatic.common.network.packets.c2s.schem;

import java.util.UUID;

public class InitialRequestPacket {
    private final UUID requestUUID;
    public InitialRequestPacket(UUID requestUUID) {
        this.requestUUID = requestUUID;
    }
}
