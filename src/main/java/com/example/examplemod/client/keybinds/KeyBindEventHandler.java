package com.example.examplemod.client.keybinds;

import com.example.examplemod.client.gui.SchemBuilderInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.example.examplemod.client.keybinds.KeyBinds.OPEN_SCHEMBUILDER;

public class KeyBindEventHandler {
    @SubscribeEvent
    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        System.out.println("I REGISTED HERE YOU WISSIKH");
        event.register(OPEN_SCHEMBUILDER.get());
    }
}
