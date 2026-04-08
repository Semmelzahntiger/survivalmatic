package com.semmelzahntiger.survivalmatic.client.gui.screens;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.client.data.ClientSchematicData;
import com.semmelzahntiger.survivalmatic.client.data.SchemBuilderInterfaceData;
import com.semmelzahntiger.survivalmatic.client.gui.menus.SchemBuilderMenu;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.*;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.base.IScrollableList;
import com.semmelzahntiger.survivalmatic.client.schem.ClientPayloadManager;
import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import com.semmelzahntiger.survivalmatic.helper.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class SchemBuilderInterface extends AbstractContainerScreen<SchemBuilderMenu> {
    public static final ResourceLocation GUI = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/schem_builder_gui.png");
    public static final ResourceLocation STANDARD_BG = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/standard_bg.png");
    public static final ResourceLocation DEFAULT_ENTRY = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/default_entry.png");
    public static final ResourceLocation DEFAULT_LIST_BORDER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/default_border.png");
    public static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/generic_button.png");
    public static final ResourceLocation BUTTON_BORDER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/generic_button_border.png");
    public static final ResourceLocation SCHEMATIC_BORDER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/schematic_border.png");
    public static final ResourceLocation SCHEMATIC_LIST_BORDER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/schematic_list_border.png");
    public SchemBuilderInterface(SchemBuilderMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 338;
        this.imageHeight = 173;
    }
    public int tracker = -1;

    @Override
    protected void init() {
        this.leftPos = (this.width - imageWidth) / 2;
        this.topPos = (this.height - imageHeight) / 2;
        initSchematicWidgets();
        initBuilderButtons();
        initTextWidgets();
    }
    protected void initSchematicWidgets() {
        ScrollableList materialsList = ScrollableList.Builder.of(getGuiLeft()+6, getGuiTop()+11, 48,22, 4, this, Component.empty())
                .setBackgroundTexture(STANDARD_BG,48,88)
                .setDefaultEntryTexture(DEFAULT_ENTRY, 48, 22)
                .setListBorderTexture(DEFAULT_LIST_BORDER, 52, 92,2,2,2,2)
                .build();
        ScrollableList dumpingList = ScrollableList.Builder.of(getGuiLeft()+imageWidth-53, getGuiTop()+11,48, 22,4, this, Component.empty())
                .setBackgroundTexture(STANDARD_BG,48,88)
                .setDefaultEntryTexture(DEFAULT_ENTRY, 48, 22)
                .setListBorderTexture(DEFAULT_LIST_BORDER, 52, 92,2,2,2,2)
                .build();
        // Todo: Add Textures and Margins
        ScrollableDropDownList schematicsList = ScrollableDropDownList.Builder.of(getGuiLeft()+69, getGuiTop()+5, 62, 12, 3, Component.empty(), this)
                .setSelectedEntryBorderTexture(SCHEMATIC_BORDER, 66,16,2,2,2,2)
                .setListBorderTexture(SCHEMATIC_LIST_BORDER, 66, 36,2,2,2,2)
                .setListOffset(4)
                .build();
        ClientSchematicData.getInstance().reloadSchematics();
        for (String availableSchematicName : ClientSchematicData.getInstance().getAvailableSchematicNames()) {
            schematicsList.addTextEntry(Component.literal(availableSchematicName));
        }
        TextVButton newLoad = TextVButton.Builder.of(getGuiLeft()+69, getGuiTop()+60, 30,20, this)
                .setEnabledTexture(BUTTON_TEXTURE, 30,20)
                .setDisabledTexture(BUTTON_TEXTURE, 30,20)
                .setHoverTextureEnabled(BUTTON_BORDER, 32, 22)
                .setHoverMargins(1,1,1,1)
                .setOnPress(button -> {
                    if(schematicsList.getSelectedEntry() != null) {
                        IScrollableList.TextEntry selectedEntry = (IScrollableList.TextEntry) schematicsList.getSelectedEntry();
                        Component text = selectedEntry.text;
                        File file = ClientSchematicData.INSTANCE.getSchematicFile(text.getString());
                        Survivalmatic.LOGGER.info("Loading schematic {}", file.getName());
                        SchematicReader.getStreamableInstanceAsync(file).thenAccept(optional -> {
                            optional.ifPresent(streamable -> {
                                ClientSchematicData.INSTANCE.setSchematicInstance(streamable);
                                Survivalmatic.LOGGER.info("Loaded schematic '{}' ({} Bytes)", streamable.name, streamable.dataSize);
                            });
                        });
                    }
                    else {
                        Survivalmatic.LOGGER.warn("No schematic selected in Dropdown List, skip loading...");
                    }
                })
                .setText(Component.literal("Load"))
                .build();
////        // Todo: Finish Waiting Logic
        TextVButton newUploadButton = TextVButton.Builder.of(getGuiLeft()+69+32, getGuiTop()+60, 30,20, this)
                .setEnabledTexture(BUTTON_TEXTURE, 30,20)
                .setDisabledTexture(BUTTON_TEXTURE, 30,20)
                .setHoverTextureEnabled(BUTTON_BORDER, 32, 22)
                .setHoverMargins(1,1,1,1)
                .setOnPress(button -> {
                    if(ClientSchematicData.INSTANCE.getSchematicInstance() == null) {
                        Survivalmatic.LOGGER.info("No Schematic Loaded, skip uploading...");
                        return;
                    }
                    Tuple<CompletableFuture<Boolean>, CompletableFuture<Boolean>> futures = ClientPayloadManager.INSTANCE.newRequest(
                            ClientSchematicData.INSTANCE.getSchematicInstance(), getMenu().pos
                    );
                    futures.getA().thenAccept(val -> {
                        if(val) {

                        }
                        else {

                        }
                    });
                    futures.getB().thenAccept(val -> {
                        if(val) {

                        }
                        else {

                        }
                    });
                })
                .setText(Component.literal("Upload"))
                .build();
        addRenderableWidget(materialsList);
        addRenderableWidget(dumpingList);
        addRenderableWidget(schematicsList);
        addRenderableWidget(newLoad);
        addRenderableWidget(newUploadButton);
    }
    protected void initBuilderButtons() {
    }
    protected void initTextWidgets() {
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
//        int dataTracker = SchemBuilderInterfaceData.getINSTANCE().getTracker();
//        if(tracker != dataTracker) {
//            tracker = dataTracker;
//            Map<ItemStack, Integer> materialBuffer =  SchemBuilderInterfaceData.getINSTANCE().getMaterialBufferItems();
//            itemList.resetEntries();
//            materialBuffer.forEach((item, amt) -> {
//                itemList.addCustomEntry(((customEntry, renderWrapper) -> {
//                    GuiGraphics guiGraphics = renderWrapper.guiGraphics();
//                    guiGraphics.renderItem(item, customEntry.getX(), customEntry.getY());
//                }));
////                itemList.addTextEntry(Component.literal("Item"));
//            });
////            Map<ItemStack, Integer> dumpBuffer =  SchemBuilderInterfaceData.getINSTANCE().getDumpBufferItems();
//        }
//        int stateAsInt = this.getMenu().data.get(0);
//        SchemBuilderBlockEntity.STATE state = SchemBuilderBlockEntity.STATE.getByID(stateAsInt);
//        setButtonsByState(state);
//        updateStringWidgets(state);
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {

    }

    public void setButtonsByState(SchemBuilderBlockEntity.STATE state) {

    }
    public void updateStringWidgets(SchemBuilderBlockEntity.STATE state) {

    }
    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float v, int i, int i1) {
        guiGraphics.blit(GUI, getGuiLeft(), getGuiTop(), imageWidth, imageHeight, 0,0 , imageWidth, imageHeight, imageWidth, imageHeight);
    }
}
