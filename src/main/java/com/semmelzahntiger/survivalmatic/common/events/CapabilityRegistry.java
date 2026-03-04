package com.semmelzahntiger.survivalmatic.common.events;

import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class CapabilityRegistry {
    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<BlockEntity> event) {
        if(event.getObject() instanceof SchemBuilderBlockEntity schemBE) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "schem_builder_item_handler"), schemBE);
        }
    }
}
