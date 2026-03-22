package com.semmelzahntiger.survivalmatic.client.gui;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.client.data.ClientSchematicData;
import com.semmelzahntiger.survivalmatic.client.gui.menus.SchemBuilderMenu;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.BasicStringWidget;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.GenericVButton;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.TextVButton;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.base.IScrollableList;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.ScrollableDropDownList;
import com.semmelzahntiger.survivalmatic.client.schem.ClientPayloadManager;
import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class SchemBuilderInterface extends AbstractContainerScreen<SchemBuilderMenu> {
    public static final ResourceLocation GUI = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/bg.png");


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

    public GenericVButton buildButton;
    public GenericVButton pauseButton;
    public GenericVButton resumeButton;
    public GenericVButton cancelButton;
    public GenericVButton trashButton;
    public GenericVButton toggleAirButton;
    public GenericVButton toggleExcavateButton;

    public BasicStringWidget stateWidget;
    public BasicStringWidget progressWidget;
    private BasicStringWidget pauseWidget;

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


        GenericVButton buildButton = GenericVButton.Builder.of(0.42f, 0.1f, 0.05f, 0.05f*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 0))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/build.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/build_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        GenericVButton pauseButton = GenericVButton.Builder.of(0.47f, 0.1f, 0.05f, 0.05f*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 1))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/pause.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/pause_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        GenericVButton resumeButton = GenericVButton.Builder.of(0.52f, 0.1f, 0.05f, 0.05f*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 2))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/resume.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/resume_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        GenericVButton cancelButton = GenericVButton.Builder.of(0.57f, 0.1f, 0.05f, 0.05f*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 3))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/cancel.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/cancel_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        GenericVButton trashButton = GenericVButton.Builder.of(0.62f, 0.1f, 0.05f, 0.05f*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 4))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/trash.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/trash_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        GenericVButton togglePlacesAir = GenericVButton.Builder.of(0.67f, 0.1f, 0.05f, 0.05f*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 5))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/air.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/air_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        GenericVButton togglePreserveExcavation = GenericVButton.Builder.of(0.72f, 0.1f, 0.05f, 0.05f*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 6))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/excavate.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/excavate_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        // WIP
        GenericVButton showHologramButton = GenericVButton.Builder.of(0.8f, 0.1f, 0.05f, 0.05f* aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 7))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/hologram.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/hologram_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        BasicStringWidget stateWidget = BasicStringWidget.Builder.of(0.42f, 0.3f, 0.2f, 0.1f, this)
                .text(Component.empty())
                .build();
        BasicStringWidget progressWidget = BasicStringWidget.Builder.of(0.42f, 0.4f, 0.2f, 0.1f, this)
                .text(Component.empty())
                .build();
        BasicStringWidget pausedWidget = BasicStringWidget.Builder.of(0.42f, 0.5f, 0.2f, 0.1f, this)
                .text(Component.empty())
                .build();

        addRenderableWidget(dropDownList);

        addRenderableWidget(newLoad);
        addRenderableWidget(newUploadButton);

        addRenderableWidget(buildButton);
        addRenderableWidget(cancelButton);
        addRenderableWidget(pauseButton);
        addRenderableWidget(resumeButton);
        addRenderableWidget(trashButton);
        addRenderableWidget(togglePlacesAir);
        addRenderableWidget(togglePreserveExcavation);
        addRenderableWidget(showHologramButton);

        addRenderableWidget(stateWidget);
        addRenderableWidget(progressWidget);
        addRenderableWidget(pausedWidget);
        this.buildButton = buildButton;
        this.cancelButton = cancelButton;
        this.pauseButton = pauseButton;
        this.resumeButton = resumeButton;
        this.trashButton = trashButton;
        this.toggleAirButton = togglePlacesAir;
        this.toggleExcavateButton = togglePreserveExcavation;

        this.schematicList = dropDownList;
        this.loadButton = newLoad;
        this.uploadButton = newUploadButton;

        this.stateWidget = stateWidget;
        this.progressWidget = progressWidget;
        this.pauseWidget = pausedWidget;
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int stateAsInt = this.getMenu().data.get(0);
        SchemBuilderBlockEntity.STATE state = SchemBuilderBlockEntity.STATE.getByID(stateAsInt);
        setButtonsByState(state);
        updateStringWidgets(state);
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
    public void setButtonsByState(SchemBuilderBlockEntity.STATE state) {
        switch (state) {
            case IDLE, RECEIVING_DATA, SCHEMATIC_LOCKED, DUMPING -> {
                buildButton.active = false;
                cancelButton.active = false;
                pauseButton.active = false;
                resumeButton.active = false;
                trashButton.active = false;
                toggleAirButton.active = true;
                toggleExcavateButton.active = true;
            }
            case READY -> {
                buildButton.active = true;
                cancelButton.active = true;
                pauseButton.active = false;
                resumeButton.active = false;
                trashButton.active = true;
                toggleAirButton.active = true;
                toggleExcavateButton.active = true;
            }
            case BUILDING -> {
                buildButton.active = false;
                cancelButton.active = false;
                pauseButton.active = true;
                resumeButton.active = true;
                trashButton.active = true;
                toggleAirButton.active = false;
                toggleExcavateButton.active = false;
            }
        }
        uploadButton.active = state == SchemBuilderBlockEntity.STATE.IDLE;
    }
    public void updateStringWidgets(SchemBuilderBlockEntity.STATE state) {
        stateWidget.setText(state.getStateMessage());
        int currentIndex = this.getMenu().data.get(1);
        int lastIndex = this.getMenu().data.get(2);
        progressWidget.setText(Component.translatable("survivalmatic.schembuilder.progress").append(currentIndex + " / " + lastIndex ));
        int paused = this.getMenu().data.get(3);
        pauseWidget.setText(Component.translatable("survivalmatic.schembuilder.paused").append(Boolean.toString(paused == 1)));
    }
    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float v, int i, int i1) {
        guiGraphics.blit(GUI, leftPos, height  , width / 2, height / 2, 0, 0, width /2, height /2, width / 2, height / 2);
    }
}
