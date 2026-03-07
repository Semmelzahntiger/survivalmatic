package com.semmelzahntiger.survivalmatic;

import com.semmelzahntiger.survivalmatic.client.keybinds.KeyBindEventHandler;
import com.semmelzahntiger.survivalmatic.client.keybinds.KeyBindInteractionHandler;
import com.semmelzahntiger.survivalmatic.common.events.CapabilityRegistry;
import com.semmelzahntiger.survivalmatic.common.schem.ServerSchemEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Survivalmatic.MODID)
public class Survivalmatic
{
    public static final int Ahleleleleahlelas = 88;

    public static final Logger LOGGER = LoggerFactory.getLogger(Survivalmatic.class);
    public static final String MODID = "survivalmatic";
    public Survivalmatic(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.register(KeyBindEventHandler.class);
        MinecraftForge.EVENT_BUS.register(KeyBindInteractionHandler.class);
        MinecraftForge.EVENT_BUS.register(CapabilityRegistry.class);
        MinecraftForge.EVENT_BUS.register(ServerSchemEventHandler.class);
        LOGGER.info("Ahleleleleahlelas: " + (Ahleleleleahlelas-21));
    }
}
