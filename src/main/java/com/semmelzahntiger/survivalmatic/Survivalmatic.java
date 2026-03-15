package com.semmelzahntiger.survivalmatic;

import com.semmelzahntiger.survivalmatic.client.keybinds.KeyBindEventHandler;
import com.semmelzahntiger.survivalmatic.client.keybinds.KeyBindInteractionHandler;
import com.semmelzahntiger.survivalmatic.common.events.CapabilityRegistry;
import com.semmelzahntiger.survivalmatic.common.schem.ServerSchemEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(Survivalmatic.MODID)
public class Survivalmatic
{
    public static final int Ahleleleleahlelas = 88;

    public static final Logger LOGGER = LoggerFactory.getLogger(Survivalmatic.class);
    public static final String MODID = "survivalmatic";

    public static final Path SCHEMATIC_PATH = FMLPaths.GAMEDIR.get().resolve("schematics");
    public Survivalmatic(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.register(this);
        modEventBus.register(KeyBindEventHandler.class);
        forgeEventBus.register(KeyBindInteractionHandler.class);
        forgeEventBus.register(CapabilityRegistry.class);
        forgeEventBus.register(ServerSchemEventHandler.class);
        LOGGER.info("Ahleleleleahlelas: " + (Ahleleleleahlelas-21));
        try {
            if(!Files.exists(SCHEMATIC_PATH)) {
                Survivalmatic.LOGGER.info("Schematic Directory does not exist. Creating it...");
                Files.createDirectory(SCHEMATIC_PATH);
            }
            else {
                Survivalmatic.LOGGER.info("Schematic Directory already exists");
            }
        }
        catch (IOException e) {
            Survivalmatic.LOGGER.error("Couldn't create or scan schematic directory", e);
        }
    }
}
