package com.semmelzahntiger.survivalmatic.client.gui.screens;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.client.data.ClientSchematicData;
import com.semmelzahntiger.survivalmatic.client.data.ClientSchemBuilderInterfaceData;
import com.semmelzahntiger.survivalmatic.client.gui.menus.SchemBuilderMenu;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.*;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.base.IScrollableList;
import com.semmelzahntiger.survivalmatic.client.schem.ClientPayloadManager;
import com.semmelzahntiger.survivalmatic.common.blockentities.SchemBuilderBlockEntity;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class SchemBuilderInterface extends AbstractContainerScreen<SchemBuilderMenu> {
    public static final ResourceLocation GUI = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/schem_builder_gui_2.png");
    public static final ResourceLocation STANDARD_BG = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/standard_bg.png");
    public static final ResourceLocation DEFAULT_ENTRY = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/default_entry.png");
    public static final ResourceLocation DEFAULT_LIST_BORDER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/default_border.png");
    public static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/generic_button.png");
    public static final ResourceLocation BUTTON_BORDER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/generic_button_border.png");
    public static final ResourceLocation SCHEMATIC_BORDER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/schematic_border.png");
    public static final ResourceLocation SCHEMATIC_LIST_BORDER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/schematic_list_border.png");

    ScrollableList materialsList;
    ScrollableList dumpingList;
    ScrollableDropDownList schematicsList;
    TextVButton loadButton;
    TextVButton uploadButton;

    GenericVButton buildButton;
    GenericVButton pauseButton;
    GenericVButton resumeButton;
    GenericVButton cancelButton;
    GenericVButton trashButton;
    GenericVButton placesAirButton;
    GenericVButton excavatesButton;
    GenericVButton hologramButton;

    BasicStringWidget clientLogDisplayWidget;
    BasicStringWidget schematicNameDisplayWidget;
    BasicStringWidget stateDisplayWidget;
    BasicStringWidget progressDisplayWidget;
    BasicStringWidget pausedDisplayWidget;
    BasicStringWidget placesAirDisplayWidget;
    BasicStringWidget excavateDisplayWidget;

    public int materialTracker = -1;
    public int dumpTracker = -1;
    public int schematicDataTracker = -1;

    public SchemBuilderInterface(SchemBuilderMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 338;
        this.imageHeight = 197;
    }


    @Override
    protected void init() {
        this.leftPos = (this.width - imageWidth) / 2;
        this.topPos = (this.height - imageHeight) / 2;
        initSchematicWidgets();
        initBuilderButtons();
        initTextWidgets();
    }
    protected void initSchematicWidgets() {
        ScrollableList materialsList = ScrollableList.Builder.of(getGuiLeft()+6, getGuiTop()+35, 48,22, 4, this, Component.empty())
                .setBackgroundTexture(STANDARD_BG,48,88)
                .setDefaultEntryTexture(DEFAULT_ENTRY, 48, 22)
                .setListBorderTexture(DEFAULT_LIST_BORDER, 52, 92,2,2,2,2)
                .build();
        ScrollableList dumpingList = ScrollableList.Builder.of(getGuiLeft()+imageWidth-54, getGuiTop()+35,48, 22,4, this, Component.empty())
                .setBackgroundTexture(STANDARD_BG,48,88)
                .setDefaultEntryTexture(DEFAULT_ENTRY, 48, 22)
                .setListBorderTexture(DEFAULT_LIST_BORDER, 52, 92,2,2,2,2)
                .build();
        // Todo: Add Textures and Margins
        ScrollableDropDownList schematicsList = ScrollableDropDownList.Builder.of(getGuiLeft()+69, getGuiTop()+29, 62, 12, 3, Component.empty(), this)
                .setSelectedEntryBorderTexture(SCHEMATIC_BORDER, 66,16,2,2,2,2)
                .setListBorderTexture(SCHEMATIC_LIST_BORDER, 66, 36,2,2,2,2)
                .setListOffset(4)
                .build();
        ClientSchematicData.getInstance().reloadSchematics();
        for (String availableSchematicName : ClientSchematicData.getInstance().getAvailableSchematicNames()) {
            schematicsList.addTextEntry(Component.literal(availableSchematicName));
        }

        TextVButton loadButton = TextVButton.Builder.of(getGuiLeft()+69, getGuiTop()+86, 30,20, this)
                .setEnabledTexture(BUTTON_TEXTURE, 30,20)
                .setDisabledTexture(BUTTON_TEXTURE, 30,20)
                .setHoverTextureEnabled(BUTTON_BORDER, 32, 22)
                .setHoverTextureDisabled(BUTTON_BORDER, 32, 22)
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
                .setText(Component.translatable("survivalmatic.schembuilder.button.load"))
                .build();
        // Todo: Finish Waiting Logic
        TextVButton uploadButton = TextVButton.Builder.of(getGuiLeft()+101, getGuiTop()+86, 30,20, this)
                .setEnabledTexture(BUTTON_TEXTURE, 30,20)
                .setDisabledTexture(BUTTON_TEXTURE, 30,20)
                .setHoverTextureEnabled(BUTTON_BORDER, 32, 22)
                .setHoverTextureDisabled(BUTTON_BORDER, 32, 22)
                .setHoverMargins(1,1,1,1)
                .setOnPress(button -> {
                    if(ClientSchematicData.INSTANCE.getSchematicInstance() == null) {
                        Survivalmatic.LOGGER.info("No Schematic Loaded, skip uploading...");
                        return;
                    }
                    Tuple<CompletableFuture<Boolean>, CompletableFuture<Boolean>> futures = ClientPayloadManager.INSTANCE.newRequest(
                            ClientSchematicData.INSTANCE.getSchematicInstance(), getMenu().pos
                    );
                    clientLogDisplayWidget.setText(Component.translatable("survivalmatic.schembuilder.log.send_request"));
                    futures.getA().thenAccept(val -> {
                        if(val) {
                            clientLogDisplayWidget.setText(Component.translatable("survivalmatic.schembuilder.log.request_accepted"));
                        }
                        else {
                            clientLogDisplayWidget.setText(Component.translatable("survivalmatic.schembuilder.log.request_denied"));
                        }
                    });
                    futures.getB().thenAccept(val -> {
                        if(val) {
                            clientLogDisplayWidget.setText(Component.translatable("survivalmatic.schembuilder.log.transfer_success"));
                        }
                        else {
                            clientLogDisplayWidget.setText(Component.translatable("survivalmatic.schembuilder.log.transfer_failed"));
                        }
                    });
                })
                .setText(Component.translatable("survivalmatic.schembuilder.button.upload"))
                .build();
        addRenderableWidget(materialsList);
        addRenderableWidget(dumpingList);
        addRenderableWidget(schematicsList);
        addRenderableWidget(loadButton);
        addRenderableWidget(uploadButton);

        this.materialsList = materialsList;
        this.dumpingList = dumpingList;
        this.schematicsList = schematicsList;

        this.loadButton = loadButton;
        this.uploadButton = uploadButton;
    }
    protected void initBuilderButtons() {
        int buttonInitialPosX = getGuiLeft()+134;
        int buttonInitialPosY = getGuiTop()+90;
        int buttonXPosIndex = 0;
        GenericVButton buildButton = GenericVButton.Builder.of(buttonInitialPosX + 16 * buttonXPosIndex++, buttonInitialPosY, 16,16, this)
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/build.png"), 32,32)
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/build_disabled.png"), 32,32)
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"), 32,32)
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"), 32,32)
                .setOnPress(button -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId,0))
                .build();
        GenericVButton pauseButton = GenericVButton.Builder.of(buttonInitialPosX + 16 * buttonXPosIndex++, buttonInitialPosY, 16,16, this)
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/pause.png"), 32,32)
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/pause_disabled.png"), 32,32)
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"), 32,32)
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"), 32,32)
                .setOnPress(button -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId,1))
                .build();
        GenericVButton resumeButton = GenericVButton.Builder.of(buttonInitialPosX + 16 * buttonXPosIndex++, buttonInitialPosY, 16,16, this)
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/resume.png"), 32,32)
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/resume_disabled.png"), 32,32)
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"), 32,32)
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"), 32,32)
                .setOnPress(button -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId,2))
                .build();
        GenericVButton cancelButton = GenericVButton.Builder.of(buttonInitialPosX + 16 * buttonXPosIndex++, buttonInitialPosY, 16,16, this)
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/cancel.png"), 32,32)
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/cancel_disabled.png"), 32,32)
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"), 32,32)
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"), 32,32)
                .setOnPress(button -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId,3))
                .build();
        GenericVButton trashButton = GenericVButton.Builder.of(buttonInitialPosX + 16 * buttonXPosIndex++, buttonInitialPosY, 16,16, this)
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/trash.png"), 32,32)
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/trash_disabled.png"), 32,32)
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"), 32,32)
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"), 32,32)
                .setOnPress(button -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId,4))
                .build();
        GenericVButton placesAirButton = GenericVButton.Builder.of(buttonInitialPosX + 16 * buttonXPosIndex++, buttonInitialPosY, 16,16, this)
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/air.png"), 32,32)
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/air_disabled.png"), 32,32)
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"), 32,32)
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"), 32,32)
                .setOnPress(button -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId,5))
                .build();
        GenericVButton excavatesButton = GenericVButton.Builder.of(buttonInitialPosX + 16 * buttonXPosIndex++, buttonInitialPosY, 16,16, this)
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/excavate.png"), 32,32)
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/excavate_disabled.png"), 32,32)
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"), 32,32)
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"), 32,32)
                .setOnPress(button -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId,6))
                .build();
        GenericVButton hologramButton = GenericVButton.Builder.of(buttonInitialPosX + 16 * buttonXPosIndex++, buttonInitialPosY, 16,16, this)
                .setEnabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/hologram.png"), 32,32)
                .setDisabledTexture(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/hologram_disabled.png"), 32,32)
                .setHoverTextureEnabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover.png"), 32,32)
                .setHoverTextureDisabled(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/button_hover_disabled.png"), 32,32)
                .setOnPress(button -> this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId,7))
                .build();
        addRenderableWidget(buildButton);
        addRenderableWidget(pauseButton);
        addRenderableWidget(resumeButton);
        addRenderableWidget(cancelButton);
        addRenderableWidget(trashButton);
        addRenderableWidget(placesAirButton);
        addRenderableWidget(excavatesButton);
        addRenderableWidget(hologramButton);

        this.buildButton = buildButton;
        this.pauseButton = pauseButton;
        this.resumeButton = resumeButton;
        this.cancelButton = cancelButton;
        this.trashButton = trashButton;
        this.placesAirButton = placesAirButton;
        this.excavatesButton = excavatesButton;
        this.hologramButton = hologramButton;
    }
    protected void initTextWidgets() {
        int initialTextWidgetPosX = getGuiLeft() + 134;
        int initialTextWidgetPosY = getGuiTop() + 29;
        int textWidgetWidth = 139;
        int textWidgetHeight = 10;
        int widgetYPosIndex = 0;

        BasicStringWidget clientLogDisplayWidget = BasicStringWidget.Builder.of(getGuiLeft()+67, getGuiTop()+6, 204,11, this)
                .text(Component.literal("Placeholder"))
                .build();

        BasicStringWidget schematicNameDisplayWidget = BasicStringWidget.Builder.of(initialTextWidgetPosX, initialTextWidgetPosY + 10 * widgetYPosIndex++, textWidgetWidth,textWidgetHeight, this)
                .text(Component.literal("Placeholder"))
                .build();
        BasicStringWidget stateDisplayWidget = BasicStringWidget.Builder.of(initialTextWidgetPosX, initialTextWidgetPosY + 10 * widgetYPosIndex++, textWidgetWidth,textWidgetHeight, this)
                .text(Component.literal("Placeholder"))
                .build();
        BasicStringWidget progressDisplayWidget = BasicStringWidget.Builder.of(initialTextWidgetPosX, initialTextWidgetPosY + 10 * widgetYPosIndex++, textWidgetWidth,textWidgetHeight, this)
                .text(Component.literal("Placeholder"))
                .build();
        BasicStringWidget pausedDisplayWidget = BasicStringWidget.Builder.of(initialTextWidgetPosX, initialTextWidgetPosY + 10 * widgetYPosIndex++, textWidgetWidth,textWidgetHeight, this)
                .text(Component.literal("Placeholder"))
                .build();
        BasicStringWidget placesAirDisplayWidget = BasicStringWidget.Builder.of(initialTextWidgetPosX, initialTextWidgetPosY + 10 * widgetYPosIndex++, textWidgetWidth,textWidgetHeight, this)
                .text(Component.literal("Placeholder"))
                .build();
        BasicStringWidget excavateDisplayWidget = BasicStringWidget.Builder.of(initialTextWidgetPosX, initialTextWidgetPosY + 10 * widgetYPosIndex++, textWidgetWidth,textWidgetHeight, this)
                .text(Component.literal("Placeholder"))
                .build();
        addRenderableOnly(clientLogDisplayWidget);
        addRenderableOnly(schematicNameDisplayWidget);
        addRenderableOnly(stateDisplayWidget);
        addRenderableOnly(progressDisplayWidget);
        addRenderableOnly(pausedDisplayWidget);
        addRenderableOnly(placesAirDisplayWidget);
        addRenderableOnly(excavateDisplayWidget);
        this.clientLogDisplayWidget = clientLogDisplayWidget;
        this.schematicNameDisplayWidget = schematicNameDisplayWidget;
        this.stateDisplayWidget = stateDisplayWidget;
        this.progressDisplayWidget = progressDisplayWidget;
        this.pausedDisplayWidget = pausedDisplayWidget;
        this.placesAirDisplayWidget = placesAirDisplayWidget;
        this.excavateDisplayWidget = excavateDisplayWidget;
    }

    // Todo: Extract custom entry logic
    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        ClientSchemBuilderInterfaceData interfaceData = ClientSchemBuilderInterfaceData.getInstance();
        int materialTracker = interfaceData.getMaterialBufferTracker();
        int dumpTracker = interfaceData.getDumpBufferTracker();
        int schematicDataTracker = interfaceData.getSchematicDataTracker();
        // Todo: Create dedicated Entry type for item list
        if(this.materialTracker != materialTracker) {
            this.materialTracker = materialTracker;
            materialsList.resetEntries();
            ClientSchemBuilderInterfaceData.getInstance().getMaterials().forEach((item, values) -> {
                materialsList.addCustomEntry(((customEntry, renderWrapper) -> {
                    if(customEntry.getY() < materialsList.getY() || customEntry.getY() >= materialsList.getYBound()) {
                        return;
                    }
                    GuiGraphics guiGraphics = renderWrapper.guiGraphics();
                    int entryWidth = customEntry.getWidth();
                    int entryHeight = customEntry.getHeight();
                    float scaleX = (float) entryWidth / 16;
                    float scaleY = (float) entryHeight / 16;
                    float usedScaling = Math.min(1.0f, Math.min(scaleX, scaleY));
                    float scalePositionAdjustment = 1.0f / usedScaling;
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().scale(usedScaling, usedScaling, 1.0f);
                    guiGraphics.renderItem(item, (int) (customEntry.getX() * scalePositionAdjustment), (int) (customEntry.getY() * scalePositionAdjustment));
                    Font font = Minecraft.getInstance().font;
                    FormattedCharSequence charSequence = FormattedCharSequence.forward(values.getA() + "/" + values.getB(), Style.EMPTY);
                    int textHeight = font.lineHeight;
                    int textWidth = font.width(charSequence);
                    float widthScaling = Math.min(1.0f, (float) (customEntry.getWidth()-16) / textWidth);
                    float heightScaling = Math.min(1.0f, (float) customEntry.getHeight() / textHeight);
                    float scaling = Math.min(widthScaling, heightScaling);
                    float positionScaleCorrectionMultiplier = 1.0f / scaling;
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().scale(scaling, scaling, 1.0f);
                    guiGraphics.drawString(font, charSequence, (int) ((customEntry.getX() + customEntry.getWidth() - textWidth) * positionScaleCorrectionMultiplier),
                            (int) ((customEntry.getY() + ((float) customEntry.getHeight() / 2) - ((float) textHeight / 2)) * positionScaleCorrectionMultiplier), 0xFFFFFF);
                    guiGraphics.pose().popPose();
                }));
            });
        }
        if(this.dumpTracker != dumpTracker) {
            this.dumpTracker = dumpTracker;
            dumpingList.resetEntries();
            ClientSchemBuilderInterfaceData.getInstance().getDumpBufferItems().forEach((item, values) -> {
                dumpingList.addCustomEntry((customEntry, renderWrapper) -> {
                    if(customEntry.getY() < materialsList.getY() || customEntry.getY() >= materialsList.getYBound()) {
                        return;
                    }
                    GuiGraphics guiGraphics = renderWrapper.guiGraphics();
                    int entryWidth = customEntry.getWidth();
                    int entryHeight = customEntry.getHeight();
                    float scaleX = (float) entryWidth / 16;
                    float scaleY = (float) entryHeight / 16;
                    float usedScaling = Math.min(1.0f, Math.min(scaleX, scaleY));
                    float scalePositionAdjustment = 1.0f / usedScaling;
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().scale(usedScaling, usedScaling, 1.0f);
                    guiGraphics.renderItem(item, (int) (customEntry.getX() * scalePositionAdjustment), (int) (customEntry.getY() * scalePositionAdjustment));
                    Font font = Minecraft.getInstance().font;
                    FormattedCharSequence charSequence = FormattedCharSequence.forward(String.valueOf(values), Style.EMPTY);
                    int textHeight = font.lineHeight;
                    int textWidth = font.width(charSequence);
                    float widthScaling = Math.min(1.0f, (float) (customEntry.getWidth()-16) / textWidth);
                    float heightScaling = Math.min(1.0f, (float) customEntry.getHeight() / textHeight);
                    float scaling = Math.min(widthScaling, heightScaling);
                    float positionScaleCorrectionMultiplier = 1.0f / scaling;
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().scale(scaling, scaling, 1.0f);
                    guiGraphics.drawString(font, charSequence, (int) ((customEntry.getX() + customEntry.getWidth() - textWidth) * positionScaleCorrectionMultiplier),
                            (int) ((customEntry.getY() + ((float) customEntry.getHeight() / 2) - ((float) textHeight / 2)) * positionScaleCorrectionMultiplier), 0xFFFFFF);
                    guiGraphics.pose().popPose();
                });
            });
        }
        if(this.schematicDataTracker != schematicDataTracker) {
            this.schematicNameDisplayWidget.setText(Component.translatable("survivalmatic.schembuilder.schematic.schematic_name").append(interfaceData.getSchematicName()));
            // Todo: Width, Height, Length Widget updating
        }
        int stateAsInt = this.getMenu().data.get(0);
        SchemBuilderBlockEntity.STATE state = SchemBuilderBlockEntity.STATE.getByID(stateAsInt);
        updateButtons(state);
        updateLabels(state);
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {

    }

    public void updateButtons(SchemBuilderBlockEntity.STATE state) {
        switch (state) {
            case IDLE, RECEIVING_DATA, SCHEMATIC_LOCKED, DUMPING -> {
                buildButton.active = false;
                cancelButton.active = true;
                pauseButton.active = false;
                resumeButton.active = false;
                trashButton.active = false;
                placesAirButton.active = true;
                excavatesButton.active = true;
            }
            case READY -> {
                buildButton.active = true;
                cancelButton.active = true;
                pauseButton.active = false;
                resumeButton.active = false;
                trashButton.active = true;
                placesAirButton.active = true;
                excavatesButton.active = true;
            }
            case BUILDING -> {
                buildButton.active = false;
                cancelButton.active = false;
                pauseButton.active = true;
                resumeButton.active = true;
                trashButton.active = true;
                placesAirButton.active = false;
                excavatesButton.active = false;
            }
        }
        uploadButton.active = state == SchemBuilderBlockEntity.STATE.IDLE;
        loadButton.active = state == SchemBuilderBlockEntity.STATE.IDLE;
    }
    public void updateLabels(SchemBuilderBlockEntity.STATE state) {
        stateDisplayWidget.setText(state.getStateMessage());
        int currentIndex = this.getMenu().data.get(1);
        int lastIndex = this.getMenu().data.get(2);
        progressDisplayWidget.setText(Component.translatable("survivalmatic.schembuilder.build.progress").append(currentIndex + " / " + lastIndex ));
        int paused = this.getMenu().data.get(3);
        pausedDisplayWidget.setText(Component.translatable("survivalmatic.schembuilder.build.paused").append(Boolean.toString(paused == 1)));
        int placesAir = this.getMenu().data.get(4);
        int excavates = this.getMenu().data.get(5);
        excavateDisplayWidget.setText(Component.translatable("survivalmatic.schembuilder.build.keeps_excavated").append(Boolean.toString(excavates == 1)));
        placesAirDisplayWidget.setText(Component.translatable("survivalmatic.schembuilder.build.places_air").append(Boolean.toString(placesAir == 1)));
    }
    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float v, int i, int i1) {
        guiGraphics.blit(GUI, getGuiLeft(), getGuiTop(), imageWidth, imageHeight, 0,0 , imageWidth, imageHeight, imageWidth, imageHeight);
    }
}
