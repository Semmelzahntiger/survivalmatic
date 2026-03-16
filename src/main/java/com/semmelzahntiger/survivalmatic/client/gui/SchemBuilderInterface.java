package com.semmelzahntiger.survivalmatic.client.gui;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.client.data.ClientSchematicData;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.IScrollableList;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.ScrollableDropDownList;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.ScrollableList;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;

public class SchemBuilderInterface extends Screen {
    public enum InterfaceState {
        IDLE,
        SCHEMATIC_SELECTED,
        LOADING_SCHEMATIC,
        LOADED_SCHEMATIC,
        UPLOADING_SCHEMATIC,
    }
    public InterfaceState state = InterfaceState.IDLE;
    public ScrollableDropDownList schematicList;
    public Button loadButton;
    public Button uploadButton;

    public SchemBuilderInterface(Component pTitle) {
        super(pTitle);
    }

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
        Button loadButton = Button.builder(
                Component.literal("Load"),
                onPress -> {
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
                }
        ).bounds(this.width / 2, 20, (int) (this.width * 0.1f), (int) (this.height * 0.1f)).build();
        Button uploadButton = Button.builder(Component.literal("Upload"), onPress -> {

        })
                .bounds((int) (this.width * 0.6f), 20, (int) (this.width * 0.1f), (int) (this.height *0.1f))
                .build();
        addRenderableWidget(loadButton);
        addRenderableWidget(uploadButton);
        this.schematicList = dropDownList;
        this.loadButton = loadButton;
        this.uploadButton = uploadButton;
    }

    @Override
    public void tick() {
        super.tick();
        switch (state) {
            case IDLE -> {
                loadButton.active = false;
            }
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }
}
