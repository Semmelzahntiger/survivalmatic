package com.semmelzahntiger.survivalmatic;

import com.semmelzahntiger.survivalmatic.client.gui.screens.SchemBuilderInterface;
import com.semmelzahntiger.survivalmatic.client.keybinds.KeyBindEventHandler;
import com.semmelzahntiger.survivalmatic.client.keybinds.KeyBindInteractionHandler;
import com.semmelzahntiger.survivalmatic.common.network.ModNetwork;
import com.semmelzahntiger.survivalmatic.common.registries.ModRegistry;
import com.semmelzahntiger.survivalmatic.common.schem.ServerSchemEventHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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
    public static final int Ahleleleahlelas = 88;

    public static final Logger LOGGER = LoggerFactory.getLogger(Survivalmatic.class);
    public static final String MODID = "survivalmatic";

    public static final Path SCHEMATIC_PATH = FMLPaths.GAMEDIR.get().resolve("schematics");
    public Survivalmatic(FMLJavaModLoadingContext context)
    {
        LOGGER.info("Ahleleleahlelas: " + (Ahleleleahlelas-21));
        IEventBus modEventBus = context.getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.register(this);
        modEventBus.register(KeyBindEventHandler.class);
        forgeEventBus.register(KeyBindInteractionHandler.class);
        forgeEventBus.register(ServerSchemEventHandler.class);

        ModRegistry.BLOCKS.register(modEventBus);
        ModRegistry.ITEMS.register(modEventBus);
        ModRegistry.BLOCK_ENTITIES.register(modEventBus);
        ModRegistry.MENUS.register(modEventBus);
        ModRegistry.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::addItemsToTab);
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
    public void onClientSetup(FMLClientSetupEvent event) {
        ModNetwork.init();
        event.enqueueWork(() -> {
            MenuScreens.register(ModRegistry.SCHEM_BUILDER_MENU.get(), SchemBuilderInterface::new);
        });
    }
    public void addItemsToTab(BuildCreativeModeTabContentsEvent event) {
        if(event.getTab() == ModRegistry.CREATIVE_MODE_TAB.get()) {
            event.accept(ModRegistry.SCHEM_BUILDER_BLOCK_ITEM.get());
        }
    }
}
