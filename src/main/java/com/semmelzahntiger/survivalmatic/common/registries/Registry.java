package com.semmelzahntiger.survivalmatic.common.registries;

import com.semmelzahntiger.survivalmatic.client.gui.menus.SchemBuilderMenu;
import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.blocks.SchemBuilder;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class Registry {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static final Capability<IItemHandler> ITEM_HANDLER = CapabilityManager.get(new CapabilityToken<>() {});

    public static final RegistryObject<Block> SCHEM_BUILDER = BLOCKS.register("schematic_builder", () -> new SchemBuilder(
            BlockBehaviour.Properties.of()
    ));
    public static final RegistryObject<BlockEntityType<SchemBuilderBlockEntity>> SCHEM_BUILDER_BE = BLOCK_ENTITIES.register(
            "schematic_builder_block_entity", () -> BlockEntityType.Builder.of(SchemBuilderBlockEntity::new, SCHEM_BUILDER.get()).build(null)
    );
    public static final RegistryObject<MenuType<SchemBuilderMenu>> SCHEM_BUILDER_MENU = MENUS.register("schem_builder_menu", () -> IForgeMenuType.create(SchemBuilderMenu::new));
}
