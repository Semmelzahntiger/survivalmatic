package com.semmelzahntiger.survivalmatic.client.gui;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.client.data.ClientSchematicData;
import com.semmelzahntiger.survivalmatic.client.gui.menus.SchemBuilderMenu;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.IScrollableList;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.ScrollableDropDownList;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.ScrollableList;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.TextVButton;
import com.semmelzahntiger.survivalmatic.client.schem.ClientPayloadManager;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class SchemBuilderInterface extends AbstractContainerScreen<SchemBuilderMenu> {
    public SchemBuilderInterface(SchemBuilderMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    public enum InterfaceState {
        IDLE,
        SCHEMATIC_SELECTED,
        LOADING_SCHEMATIC,
        LOADED_SCHEMATIC,
        UPLOADING_SCHEMATIC,
    }
    public InterfaceState state = InterfaceState.IDLE;
    public ScrollableDropDownList schematicList;
    public TextVButton<?> loadButton;
    public TextVButton<?> uploadButton;

    @Override
    protected void init() {
        ScrollableDropDownList dropDownList = ScrollableDropDownList.Builder.of(
                0.05f,0.1f, 0.1f, 0.04f, 4, Component.empty(), this)
                .setEntryWidthRatio(0.95f)
                .setEntryHeightRatio(0.9f)
                .setListOffset(0.01f)
                .setSelectedMargin(0.005f, 0.005f, 0.003f, 0.003f)
                .build();
        ClientSchematicData.INSTANCE.reloadSchematics();
        for (String availableSchematicName : ClientSchematicData.INSTANCE.getAvailableSchematicNames()) {
            dropDownList.addTextEntry(Component.literal(availableSchematicName));
        }
        addRenderableWidget(dropDownList);
        TextVButton<?> newLoad = TextVButton.TextButtonBuilder.of(0.5f, 0.04f, 0.1f, 0.1f, this)
                .setOnPress(button -> {
                    if(dropDownList.getSelectedEntry() != null) {
                        IScrollableList.TextEntry selectedEntry = (IScrollableList.TextEntry) dropDownList.getSelectedEntry();
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
        TextVButton<?> newUploadButton = TextVButton.TextButtonBuilder.of(0.6f, 0.04f, 0.1f, 0.1f, this)
                .setOnPress(button -> {
                    if(ClientSchematicData.INSTANCE.getSchematicInstance() != null) {
                        Tuple<CompletableFuture<Boolean>, CompletableFuture<Boolean>> futures = ClientPayloadManager.INSTANCE.newRequest();
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
                    }
                })
                .setText(Component.literal("Upload"))
                .build();
        addRenderableWidget(newLoad);
        addRenderableWidget(newUploadButton);

        this.schematicList = dropDownList;
        this.loadButton = newLoad;
        this.uploadButton = newUploadButton;
    }


    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {

    }
}
