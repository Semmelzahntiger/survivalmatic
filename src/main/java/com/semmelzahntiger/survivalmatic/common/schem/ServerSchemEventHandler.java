package com.semmelzahntiger.survivalmatic.common.schem;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// Handles Lifecycle of ServerSchematicHandler
public class ServerSchemEventHandler {
    @SubscribeEvent
    public static void serverStartedEvent(ServerStartedEvent event) {
        ServerSchematicHandler.createInstance();
    }
    @SubscribeEvent
    public static void serverStoppedEvent(ServerStoppedEvent event) {
        ServerSchematicHandler.voidInstance();
    }
}
