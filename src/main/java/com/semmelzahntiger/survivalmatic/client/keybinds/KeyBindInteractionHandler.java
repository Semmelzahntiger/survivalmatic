package com.semmelzahntiger.survivalmatic.client.keybinds;

import com.semmelzahntiger.survivalmatic.client.gui.SchemBuilderInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.semmelzahntiger.survivalmatic.client.keybinds.KeyBinds.OPEN_SCHEMBUILDER;

public class KeyBindInteractionHandler {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while(OPEN_SCHEMBUILDER.get().consumeClick()) {

            }
        }
    }
}
