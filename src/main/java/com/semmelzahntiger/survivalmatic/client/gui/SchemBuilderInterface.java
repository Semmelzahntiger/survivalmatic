package com.semmelzahntiger.survivalmatic.client.gui;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.client.data.ClientSchematicData;
import com.semmelzahntiger.survivalmatic.client.gui.menus.SchemBuilderMenu;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.GenericVButton;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.TextVButton;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.base.IScrollableList;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.ScrollableDropDownList;
import com.semmelzahntiger.survivalmatic.client.schem.ClientPayloadManager;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

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
    public TextVButton loadButton;
    public TextVButton uploadButton;

    @Override
    protected void init() {
        super.init();
        float aspectRatio = this.width / (float) this.height;
        ScrollableDropDownList dropDownList = ScrollableDropDownList.Builder.of(
                0.2f,0.1f, 0.1f, 0.04f, 4, Component.empty(), this)
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
        TextVButton newLoad = TextVButton.Builder.of(0.31f, 0.1f, 0.1f, 0.1f, this)
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
        TextVButton newUploadButton = TextVButton.Builder.of(0.31f, 0.2f, 0.1f, 0.1f, this)
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
        addRenderableWidget(newLoad);
        addRenderableWidget(newUploadButton);

        this.schematicList = dropDownList;
        this.loadButton = newLoad;
        this.uploadButton = newUploadButton;

        GenericVButton buildButton = GenericVButton.Builder.of(0.42f, 0.1f, 0.05f, 0.05f*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 0))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/build.png"))
                .build();
        GenericVButton pauseButton = GenericVButton.Builder.of(0.47f, 0.1f, 0.05f, 0.05f*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 1))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/pause.png"))
                .build();
        GenericVButton resumeButton = GenericVButton.Builder.of(0.52f, 0.1f, 0.05f, 0.05f*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 2))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/resume.png"))
                .build();
        GenericVButton cancelButton = GenericVButton.Builder.of(0.57f, 0.1f, 0.05f, 0.05f*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 3))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/cancel.png"))
                .build();
        GenericVButton trashButton = GenericVButton.Builder.of(0.62f, 0.1f, 0.05f, 0.05f*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 4))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/trash.png"))
                .build();
        addRenderableWidget(buildButton);
        addRenderableWidget(cancelButton);
        addRenderableWidget(pauseButton);
        addRenderableWidget(resumeButton);
        addRenderableWidget(trashButton);

    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);

    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {

    }
}
