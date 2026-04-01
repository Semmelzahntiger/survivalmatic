package com.semmelzahntiger.survivalmatic.client.gui.screens;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.client.data.ClientSchematicData;
import com.semmelzahntiger.survivalmatic.client.gui.menus.SchemBuilderMenu;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.*;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.base.IScrollableList;
import com.semmelzahntiger.survivalmatic.client.schem.ClientPayloadManager;
import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import com.semmelzahntiger.survivalmatic.helper.RenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class SchemBuilderInterface extends AbstractContainerScreen<SchemBuilderMenu> {
    public static final ResourceLocation INVENTORY = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory_background.png");
    public static final ResourceLocation GUI = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/base_gui.png");
    public SchemBuilderInterface(SchemBuilderMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageHeight = 90;
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
    public ScrollableList itemList;

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
    private BasicStringWidget excavateWidget;
    private BasicStringWidget airWidget;

    @Override
    protected void init() {
        // Sorry Mojang but what the actual fuck is this Slot logic with the background
        this.leftPos = (this.width - imageWidth) / 2;
        this.topPos = (int) ((this.height + imageHeight * 0.3f) / 2);
        initSchematicWidgets();
        initBuilderButtons();
        initTextWidgets();
    }
    protected void initSchematicWidgets() {
        ScrollableDropDownList dropDownList = ScrollableDropDownList.Builder.of(
                        0.33f,0.14f, 0.125f, 0.04f, 4, Component.empty(), this)
                .setEntryWidthRatio(0.9f)
                .setEntryHeightRatio(0.9f)
                .setListOffset(0.01f)
                .setSelectedMargin(0.005f, 0.005f, 0.003f, 0.003f)
                .build();
        ClientSchematicData.INSTANCE.reloadSchematics();
        for (String availableSchematicName : ClientSchematicData.INSTANCE.getAvailableSchematicNames()) {
            dropDownList.addTextEntry(Component.literal(availableSchematicName));
        }
        ScrollableList itemList = ScrollableList.Builder.of(0.48f, 0.14f, 0.2f, 0.04f, 4, this,Component.empty())
                .setBorderMargins(0.005f, 0.005f, 0.003f, 0.003f)
                .build();
        for (int i = 0; i < 5; i++) {
            itemList.addTextEntry(Component.literal(String.valueOf(i)));
        }

        TextVButton newLoad = TextVButton.Builder.of(0.33f, 0.34f, 0.0625f, 0.05f, this)
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

        TextVButton newUploadButton = TextVButton.Builder.of(0.3925f, 0.34f, 0.0625f, 0.05f, this)
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
        addRenderableWidget(dropDownList);
        addRenderableWidget(itemList);
        addRenderableWidget(newLoad);
        addRenderableWidget(newUploadButton);
        this.schematicList = dropDownList;
        this.itemList = itemList;
        this.loadButton = newLoad;
        this.uploadButton = newUploadButton;
    }
    protected void initBuilderButtons() {
        float aspectRatio = this.width / (float) this.height;
        float buttonSize = 0.025f;
        float buttonY = 0.4f;
        float buttonStartX = 0.33f;
        int buttonIdx = 0;
        GenericVButton buildButton = GenericVButton.Builder.of(buttonStartX + buttonSize * buttonIdx++, buttonY, buttonSize, buttonSize*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 0))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/build.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/build_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        GenericVButton pauseButton = GenericVButton.Builder.of(buttonStartX + buttonSize * buttonIdx++, buttonY, buttonSize, buttonSize*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 1))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/pause.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/pause_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        GenericVButton resumeButton = GenericVButton.Builder.of(buttonStartX + buttonSize * buttonIdx++, buttonY, buttonSize, buttonSize*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 2))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/resume.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/resume_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        GenericVButton cancelButton = GenericVButton.Builder.of(buttonStartX + buttonSize * buttonIdx++, buttonY, buttonSize, buttonSize*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 3))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/cancel.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/cancel_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        GenericVButton trashButton = GenericVButton.Builder.of(buttonStartX + buttonSize * buttonIdx++, buttonY, buttonSize, buttonSize*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 4))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/trash.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/trash_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        buttonIdx = 0;
        buttonY = buttonY + buttonSize * aspectRatio;
        GenericVButton togglePlacesAir = GenericVButton.Builder.of(buttonStartX + buttonSize * buttonIdx++, buttonY, buttonSize, buttonSize*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 5))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/air.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/air_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        GenericVButton togglePreserveExcavation = GenericVButton.Builder.of(buttonStartX + buttonSize * buttonIdx++, buttonY, buttonSize, buttonSize*aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 6))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/excavate.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/excavate_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        // WIP
        GenericVButton showHologramButton = GenericVButton.Builder.of(buttonStartX + buttonSize * buttonIdx++, buttonY, buttonSize, buttonSize* aspectRatio, this)
                .setOnPress(onPress -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, 7))
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/hologram.png"))
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/hologram_disabled.png"))
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"))
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"))
                .build();
        addRenderableWidget(buildButton);
        addRenderableWidget(cancelButton);
        addRenderableWidget(pauseButton);
        addRenderableWidget(resumeButton);
        addRenderableWidget(trashButton);
        addRenderableWidget(togglePlacesAir);
        addRenderableWidget(togglePreserveExcavation);
        addRenderableWidget(showHologramButton);
        this.buildButton = buildButton;
        this.cancelButton = cancelButton;
        this.pauseButton = pauseButton;
        this.resumeButton = resumeButton;
        this.trashButton = trashButton;
        this.toggleAirButton = togglePlacesAir;
        this.toggleExcavateButton = togglePreserveExcavation;
    }
    protected void initTextWidgets() {
        float textX = 0.48f;
        float textY = 0.3f;
        float offset = 0.04f;
        float yIdx = 0;
        BasicStringWidget stateWidget = BasicStringWidget.Builder.of(textX, textY + offset * yIdx++, 0.2f, 0.04f, this)
                .text(Component.empty())
                .build();
        BasicStringWidget progressWidget = BasicStringWidget.Builder.of(textX, textY + offset * yIdx++, 0.2f, 0.04f, this)
                .text(Component.empty())
                .build();
        BasicStringWidget pausedWidget = BasicStringWidget.Builder.of(textX, textY + offset * yIdx++, 0.2f, 0.04f, this)
                .text(Component.empty())
                .build();
        BasicStringWidget excavateWidget = BasicStringWidget.Builder.of(textX, textY + offset * yIdx++, 0.2f, 0.04f, this)
                .text(Component.empty())
                .build();
        BasicStringWidget airWidget = BasicStringWidget.Builder.of(textX, textY + offset * yIdx++, 0.2f, 0.04f, this)
                .text(Component.empty())
                .build();
        addRenderableWidget(stateWidget);
        addRenderableWidget(progressWidget);
        addRenderableWidget(pausedWidget);
        addRenderableWidget(excavateWidget);
        addRenderableWidget(airWidget);
        this.stateWidget = stateWidget;
        this.progressWidget = progressWidget;
        this.pauseWidget = pausedWidget;
        this.excavateWidget = excavateWidget;
        this.airWidget = airWidget;
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

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {

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
        int placesAir = this.getMenu().data.get(4);
        int excavates = this.getMenu().data.get(5);
        excavateWidget.setText(Component.translatable("survivalmatic.schembuilder.keeps_excavated").append(Boolean.toString(excavates == 1)));
        airWidget.setText(Component.translatable("survivalmatic.schembuilder.places_air").append(Boolean.toString(placesAir == 1)));
    }
    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float v, int i, int i1) {
        guiGraphics.blit(INVENTORY, getGuiLeft(), getGuiTop(),0,0,this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        guiGraphics.blit(GUI, RenderHelper.toAbsoluteX(0.305f, this), RenderHelper.toAbsoluteY(0.085f, this),
                RenderHelper.toAbsoluteX(0.4f, this), RenderHelper.toAbsoluteY(0.43f, this),
                0,0, 188, 98, 188, 98);
    }
}
