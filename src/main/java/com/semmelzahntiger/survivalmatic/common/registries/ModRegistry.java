package com.semmelzahntiger.survivalmatic.common.registries;

import com.semmelzahntiger.survivalmatic.client.gui.menus.SchemBuilderMenu;
import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.blocks.SchemBuilder;
import com.semmelzahntiger.survivalmatic.common.items.SchemBuilderBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class ModRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static final RegistryObject<Block> SCHEM_BUILDER = BLOCKS.register("schematic_builder", () -> new SchemBuilder(
            BlockBehaviour.Properties.of()
    ));
    public static final RegistryObject<BlockEntityType<SchemBuilderBlockEntity>> SCHEM_BUILDER_BE = BLOCK_ENTITIES.register(
            "schematic_builder_block_entity", () -> BlockEntityType.Builder.of(SchemBuilderBlockEntity::new, SCHEM_BUILDER.get()).build(null)
    );
    public static final RegistryObject<MenuType<SchemBuilderMenu>> SCHEM_BUILDER_MENU = MENUS.register("schem_builder_menu", () -> IForgeMenuType.create(SchemBuilderMenu::new));
    public static final RegistryObject<BlockItem> SCHEM_BUILDER_BLOCK_ITEM = ITEMS.register("schem_builder_item",
            () -> new SchemBuilderBlockItem(SCHEM_BUILDER.get(), new Item.Properties()));
    public static final RegistryObject<CreativeModeTab> CREATIVE_MODE_TAB = CREATIVE_MODE_TABS.register("survivalmatic", () -> CreativeModeTab.builder()
            .title(Component.literal("Survivalmatic"))
            .build());
}
