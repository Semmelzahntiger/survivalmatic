package com.semmelzahntiger.survivalmatic.client.keybinds;

import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.semmelzahntiger.survivalmatic.client.keybinds.KeyBinds.OPEN_SCHEMBUILDER;

public class KeyBindEventHandler {
    @SubscribeEvent
    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SCHEMBUILDER.get());
    }
}
