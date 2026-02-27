package com.semmelzahntiger.survivalmatic;

import com.semmelzahntiger.survivalmatic.client.keybinds.KeyBindEventHandler;
import com.semmelzahntiger.survivalmatic.client.keybinds.KeyBindInteractionHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExampleMod.MODID)
public class ExampleMod
{
    public static final String MODID = "survivalmatic";
    public ExampleMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.register(KeyBindEventHandler.class);
        MinecraftForge.EVENT_BUS.register(KeyBindInteractionHandler.class);
    }
}
