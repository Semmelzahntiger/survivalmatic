package com.semmelzahntiger.survivalmatic.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class ScrollableDropDownList extends AbstractVWidget {
    // Debug
    protected final ResourceLocation AREA = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/marked_area.png");

    public final ResourceLocation DEFAULT_ENTRY_TEXTURE;
    public final ResourceLocation BACKGROUND;
    public final ResourceLocation HOVERED;
    public final int hoverLeftMargin;
    public final int hoverTopMargin;
    public final int hoverRightMargin;
    public final int hoverBottomMargin;

    public final ResourceLocation ACTIVE_BORDER;
    public final int activeBorderLeftMargin;
    public final int activeBorderTopMargin;
    public final int activeBorderRightMargin;
    public final int activeBorderBottomMargin;

    public final ResourceLocation LIST_BORDER;
    public final int listBorderLeftMargin;
    public final int listBorderTopMargin;
    public final int listBorderRightMargin;
    public final int listBorderBottomMargin;

    public final ResourceLocation SLIDER;
    public final int sliderRightMargin;

    public final int visibleEntries;
    public final float entryWidthRatio;
    public final float entryHeightRatio;
    public final int listOffset;

    protected int listStart;

    protected int entryCenteringOffsetX;
    protected int entryWidth;
    protected int entryHeight;
    protected int entryOffsettedX;

    protected int closedHeight;
    protected int openedHeight;

    protected int listHeight;
    protected boolean isOpened = false;

    protected double currentYScroll = 0;
    protected int scrollIndexShift = 0;



    protected int scrollSliderSize;
    protected int offsetForScrollSlider;
    List<ListEntry> entries = new ArrayList<>();
    protected int entryCount = 0;

    protected int hoveredIndex = 0;
    ListEntry selectedEntry = null;

    protected ScrollableDropDownList(Builder builder) {
        super(builder.x,  builder.y, builder.width, builder.height, builder.screen, builder.message);
        // Textures
        this.DEFAULT_ENTRY_TEXTURE = builder.entryTexture;
        this.BACKGROUND = builder.background;
        this.HOVERED = builder.hovered;
        this.ACTIVE_BORDER = builder.selectedBorder;
        this.LIST_BORDER = builder.listBorder;
        this.SLIDER = builder.slider;
        // Texture Margins
        this.hoverLeftMargin = relativeToAbsoluteX(builder.hoveredLeftMargin);
        this.hoverTopMargin = relativeToAbsoluteY(builder.hoveredTopMargin);
        this.hoverRightMargin = relativeToAbsoluteX(builder.hoveredRightMargin);
        this.hoverBottomMargin = relativeToAbsoluteY(builder.hoveredBottomMargin);

        this.activeBorderLeftMargin = relativeToAbsoluteX(builder.selectedLeftMargin);
        this.activeBorderTopMargin = relativeToAbsoluteY(builder.selectedTopMargin);
        this.activeBorderRightMargin = relativeToAbsoluteX(builder.selectedRightMargin);
        this.activeBorderBottomMargin = relativeToAbsoluteY(builder.selectedBottomMargin);

        this.listBorderLeftMargin = relativeToAbsoluteX(builder.selectedLeftMargin);
        this.listBorderTopMargin = relativeToAbsoluteY(builder.selectedTopMargin);
        this.listBorderRightMargin = relativeToAbsoluteX(builder.selectedRightMargin);
        this.listBorderBottomMargin = relativeToAbsoluteY(builder.selectedBottomMargin);

        this.sliderRightMargin = relativeToAbsoluteX(builder.selectedLeftMargin);
        // Fixed Data
        this.visibleEntries = builder.visibleEntries;
        this.entryWidthRatio = builder.listEntryWidthRatio;
        this.entryHeightRatio = builder.listEntryHeightRatio;

        this.listOffset = relativeToAbsoluteY(builder.listOffset);
        // Dynamically changed data
        this.closedHeight = relativeToAbsoluteY(builder.height);

        listStart = this.getY() + this.getHeight() + listOffset;
        this.entryWidth = (int) (this.getWidth() * this.entryWidthRatio);
        this.entryHeight = (int) (this.getHeight() * this.entryHeightRatio);
        this.entryCenteringOffsetX = (this.getWidth() - this.entryWidth) / 2;
        this.entryOffsettedX = this.getX() + this.entryCenteringOffsetX;

        this.listHeight = visibleEntries * entryHeight;
        this.openedHeight = relativeToAbsoluteY(builder.height + builder.listOffset) + this.listHeight;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        RenderSystem.enableBlend();
        this.scrollSliderSize = entryCount != 0 ? listHeight * (visibleEntries / entryCount) : 0;
        this.offsetForScrollSlider = entryCount > visibleEntries ? listHeight / (entryCount - visibleEntries) : 0;
        guiGraphics.blit(BACKGROUND, this.getX(), this.getY(), this.getWidth(), closedHeight,0,0,256,256, 256, 256);

        if(selectedEntry != null) {
            selectedEntry.renderInternal(guiGraphics, this.getX(), this.getY(), this.getWidth(), closedHeight, i, i1, v);
        }
        guiGraphics.blit(ACTIVE_BORDER,this.getX() - this.activeBorderLeftMargin, this.getY() - this.activeBorderTopMargin,
                this.getWidth() + this.activeBorderRightMargin, this.closedHeight + this.activeBorderBottomMargin,
                0,0,256,256, 256, 256);

        if(isOpened) {
            guiGraphics.blit(LIST_BORDER, this.entryOffsettedX - this.listBorderLeftMargin, this.listStart - this.listBorderTopMargin,
                    this.entryWidth + this.listBorderRightMargin, this.listHeight + listBorderBottomMargin,
                    0,0,256,256, 256, 256);
            guiGraphics.enableScissor(this.getX(), listStart, this.getWidth(), this.getHeight());
            for (ListEntry entry : entries) {
                entry.render(guiGraphics, i, i1, v);
            }
            //Debug
            guiGraphics.blit(AREA, entryOffsettedX, listStart, this.getWidth(), listHeight,0,0,256,256, 256, 256);
            guiGraphics.disableScissor();
        }
        hoveredIndex = isHoveringOverIndex(i,i1);
        if(hoveredIndex > -1) {
            guiGraphics.blit(HOVERED, entryOffsettedX - hoverLeftMargin, listStart + entryHeight * hoveredIndex - hoverTopMargin,
                    entryWidth + hoverRightMargin, entryHeight + hoverBottomMargin,
                    0,0, 256, 256, 256, 256);
        }
        RenderSystem.disableBlend();
    }
    protected void recalculateDynamicData() {
        this.closedHeight = this.getHeight();

        this.listStart = this.getY() + this.getHeight() + listOffset;
        this.entryWidth = (int) (this.getWidth() * this.entryWidthRatio);
        this.entryHeight = (int) (this.getHeight() * this.entryHeightRatio);
        this.entryCenteringOffsetX = (this.getWidth() - this.entryWidth) / 2;
        this.entryOffsettedX = this.getX() + this.entryCenteringOffsetX;

        this.listHeight = visibleEntries * entryHeight;
        this.openedHeight = relativeToAbsoluteY(this.getHeight() + this.listOffset) + this.listHeight;
        updateEntries();
    }
    protected void open() {
        isOpened = true;
        this.setHeightWithoutUpdatingList(openedHeight);
        realignEntries();
    }
    protected void close() {
        isOpened = false;
        this.setHeightWithoutUpdatingList(closedHeight);
        currentYScroll = 0;
    }
    protected void realignEntries() {
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setY((int) (listStart + i*entryHeight + currentYScroll));
        }
    }
    protected void scroll(double scrollAmount) {
        int sign = (int) Math.signum(scrollAmount);
        int maxDiff = entryCount - visibleEntries;
        if(maxDiff > 0) {
            scrollIndexShift = Mth.clamp(scrollIndexShift - sign, 0, maxDiff);
            currentYScroll = -(scrollIndexShift * entryHeight);
        }
        realignEntries();
    }
    // -1 means not hovering over any entry
    protected int isHoveringOverIndex(int mouseX, int mouseY) {
        if(isOpened) {
            int verticalSpacePerEntry = listHeight / visibleEntries;
            boolean inXSpace = mouseX >= this.entryOffsettedX && mouseX < this.entryOffsettedX + entryWidth;
            boolean inYSpace = mouseY >= listStart && mouseY < listStart + listHeight;
            if(inXSpace && inYSpace) {
                return (mouseY - (listStart)) / verticalSpacePerEntry;
            }
        }
        return -1;
    }
    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if(isHovered && isOpened) {
            scroll(pDelta);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        boolean mainElementClicked = pMouseX >= this.getX() && pMouseX < this.getXBound() && pMouseY >= this.getY() && pMouseY <= this.getY() + closedHeight;
        boolean listClicked = pMouseX >= this.entryOffsettedX && pMouseX < this.entryOffsettedX + this.entryWidth && pMouseY >= listStart && pMouseY <= listStart + listHeight;
        if(mainElementClicked) {
            onClickMainElement(pMouseX, pMouseY);
        }
        if(isOpened && listClicked) {
            onClickListElement(pMouseX, pMouseY);
        }
    }

    public void onClickMainElement(double pMouseX, double pMouseY) {
        if(isOpened) {
            close();
        }
        else {
            open();
        }
    }
    public void onClickListElement(double pMouseX, double pMouseY) {
        int clickedIndex = hoveredIndex + scrollIndexShift;
        if(clickedIndex < entries.size()) {
            selectedEntry = entries.get(clickedIndex);
            selectedEntry.onSelected.run();
            close();
        }
    }
    public void addTextEntry(Component text) {
        this.entries.add(new TextEntry(this.getX() + entryCenteringOffsetX, this.entryWidth, this.entryHeight, text));
        entryCount++;
        realignEntries();
    }
    public void addTextEntry(Component text, ResourceLocation entryTexture) {
        this.entries.add(new TextEntry(this.getX(), this.entryWidth, this.entryHeight, text, entryTexture));
        entryCount++;
        realignEntries();
    }
    public void addTextEntry(Component text, ResourceLocation entryTexture, Runnable onSelect) {
        this.entries.add(new TextEntry(this.getX(), this.entryWidth, this.entryHeight, text, entryTexture, onSelect));
    }
    public void addImageEntry(ResourceLocation image, BiConsumer<ImageEntry, ResourceLocation> imageRenderer) {
        this.entries.add(new ImageEntry(this.getX() + entryCenteringOffsetX, this.entryWidth, this.entryHeight, image, imageRenderer));
        entryCount++;
        realignEntries();
    }
    public void addImageEntry(ResourceLocation image, ResourceLocation entryTexture, BiConsumer<ImageEntry, ResourceLocation> imageRenderer) {
        this.entries.add(new ImageEntry(this.getX() + entryCenteringOffsetX, this.entryWidth, this.entryHeight,entryTexture, image, imageRenderer));
        entryCount++;
        realignEntries();
    }
    public void addImageEntry(ResourceLocation image, ResourceLocation entryTexture,Runnable onSelect, BiConsumer<ImageEntry, ResourceLocation> imageRenderer) {
        this.entries.add(new ImageEntry(this.getX() + entryCenteringOffsetX, this.entryWidth, this.entryHeight,entryTexture, onSelect, image, imageRenderer));
        entryCount++;
        realignEntries();
    }
    public void addCustomEntry(BiConsumer<CustomEntry, CustomEntry.RenderWrapper> customRenderer) {
        this.entries.add(new CustomEntry(this.getX() + entryCenteringOffsetX, this.entryWidth, this.entryHeight, customRenderer));
        entryCount++;
        realignEntries();
    }

    @Override
    public void setX(int pX) {
        super.setX(pX);
        recalculateDynamicData();
    }

    @Override
    public void setY(int pY) {
        super.setY(pY);
        recalculateDynamicData();
    }

    @Override
    public void setWidth(int pWidth) {
        super.setWidth(pWidth);
        recalculateDynamicData();
    }

    @Override
    public void setHeight(int value) {
        super.setHeight(value);
        recalculateDynamicData();
    }
    protected void updateEntries() {
        for (ListEntry entry : entries) {
            entry.setX(entryOffsettedX);
            entry.setWidth(entryWidth);
            entry.setHeight(entryHeight);
        }
        realignEntries();
    }
    public void setHeightWithoutUpdatingList(int value) {
        super.setHeight(value);
    }
    public static class Builder {
        public final Screen screen;
        public final float x;
        public final float y;
        public final float width;
        public final float height;
        public float listEntryWidthRatio = 1.0f;
        public float listEntryHeightRatio = 1.0f;
        public final int visibleEntries;
        public float listOffset;
        public final Component message;
        public ResourceLocation entryTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/list_entry.png");
        public ResourceLocation background =  ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/amk.png");
        public ResourceLocation hovered = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/hovered.png");
        public float hoveredLeftMargin = 0;
        public float hoveredRightMargin = 0;
        public float hoveredTopMargin = 0;
        public float hoveredBottomMargin = 0;
        public ResourceLocation selectedBorder = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_border.png");
        public float selectedLeftMargin = 0;
        public float selectedRightMargin = 0;
        public float selectedTopMargin = 0;
        public float selectedBottomMargin = 0;
        public ResourceLocation listBorder = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_border.png");
        public float listLeftMargin = 0;
        public float listRightMargin = 0;
        public float listTopMargin = 0;
        public float listBottomMargin = 0;
        public ResourceLocation slider = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/slider.png");
        public float sliderMargin = 0;

        protected Builder(float xRelative, float yRelative, float widthRelative, float heightRelative, int pVisibleEntries, Component pMessage, Screen screen) {
            x = xRelative;
            y = yRelative;
            width = widthRelative;
            height = heightRelative;
            visibleEntries = pVisibleEntries;
            message = pMessage;
            this.screen = screen;
        }
        public static Builder of(float x, float y, float width, float height, int visibleEntries, Component message, Screen screen) {
            return new Builder(x, y, width, height, visibleEntries, message, screen);
        }
        public Builder withListOffset(int offset) {
            listOffset = offset;
            return this;
        }
        public Builder setEntryWidthRatio(float widthRatio) {
            this.listEntryWidthRatio = widthRatio;
            return this;
        }
        public Builder setEntryHeightRatio(float heightRatio) {
            this.listEntryHeightRatio = heightRatio;
            return this;
        }
        public Builder setEntryTexture(ResourceLocation pEntryTexture) {
            this.entryTexture = pEntryTexture;
            return this;
        }
        public Builder setBackground(ResourceLocation pBackground) {
            this.background = pBackground;
            return this;
        }
        public Builder setHovered(ResourceLocation pHovered, int leftMargin, int rightMargin, int topMargin, int bottomMargin) {
            this.hovered = pHovered;
            return this;
        }
        public Builder setSelectedBorder(ResourceLocation pActiveBorder, int leftMargin, int rightMargin, int topMargin, int bottomMargin) {
            this.selectedBorder = pActiveBorder;
            return this;
        }
        public Builder setListBorder(ResourceLocation pListBorder) {
            this.listBorder = pListBorder;
            return this;
        }
        public Builder setSlider(ResourceLocation pSlider) {
            this.slider = pSlider;
            return this;
        }
        public Builder setHoveredMargins(float leftMargin, float rightMargin, float topMargin, float bottomMargin) {
            this.hoveredLeftMargin = leftMargin;
            this.hoveredRightMargin = rightMargin;
            this.hoveredTopMargin = topMargin;
            this.hoveredBottomMargin = bottomMargin;
            return this;
        }
        public Builder setSelectedMargin(float leftMargin, float rightMargin, float topMargin, float bottomMargin) {
            this.selectedLeftMargin = leftMargin;
            this.selectedRightMargin = rightMargin;
            this.selectedTopMargin = topMargin;
            this.selectedBottomMargin = bottomMargin;
            return this;
        }
        public Builder setListBorderMargins(float leftMargin, float rightMargin, float topMargin, float bottomMargin) {
            this.listLeftMargin = leftMargin;
            this.listRightMargin = rightMargin;
            this.listTopMargin = topMargin;
            this.listBottomMargin = bottomMargin;
            return this;
        }
        public ScrollableDropDownList build() {
            return new ScrollableDropDownList(this);
        }
    }
    public abstract class ListEntry {
        protected int x;
        protected int y = 0;
        protected int width;
        protected int height;
        protected final ResourceLocation entryTexture;
        protected final Runnable onSelected;


        protected ListEntry(int x,int width, int height) {
            this(x, width, height, DEFAULT_ENTRY_TEXTURE);
        }
        protected ListEntry(int x, int width, int height, ResourceLocation pEntryTexture) {
            this(x,width,height,pEntryTexture,() -> {});
        }
        protected ListEntry(int x, int width, int height, ResourceLocation pEntryTexture, Runnable onSelected) {
            this.x = x;
            this.width = width;
            this.height = height;
            this.entryTexture = pEntryTexture;
            this.onSelected = onSelected;
        }
        public ResourceLocation getEntryTexture() {
            return entryTexture;
        }
        public void render(GuiGraphics pGraphics, int mouseX, int mouseY, float partialTick) {
            renderInternal(pGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), mouseX, mouseY, partialTick);
        }
        public void renderInternal(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick) {
            guiGraphics.blit(getEntryTexture(), x, y, width, height,0,0, 256, 112, 256, 112);
        }
        public int getX() { return x; }
        public int getY() { return y; }
        public void setX(int x) { this.x = x; }
        public void setY(int y) { this.y = y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public void setWidth(int width) { this.width = width; }
        public void setHeight(int height) { this.height = height; }
    }
    public class TextEntry extends ListEntry {
        protected final Component text;
        public TextEntry(int x,int width, int height, Component text) {
            super(x, width, height);
            this.text = text;
        }
        public TextEntry(int x, int width, int height, Component text, ResourceLocation customEntryTexture) {
            super(x, width, height, customEntryTexture);
            this.text = text;
        }

        protected TextEntry(int x, int width, int height, Component text, ResourceLocation pEntryTexture, Runnable onSelected) {
            super(x, width, height, pEntryTexture, onSelected);
            this.text = text;
        }

        @Override
        public void renderInternal(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick) {
            super.renderInternal(guiGraphics, x, y, width, height, mouseX, mouseY, partialTick);
            guiGraphics.drawString(Minecraft.getInstance().font, text, x, y, 0xFFFFFF, false);
        }
    }
    public class ImageEntry extends ListEntry {
        protected final ResourceLocation imageLocation;
        protected final BiConsumer<ImageEntry, ResourceLocation> renderer;

        public ImageEntry(int x, int width, int height, ResourceLocation imageLocation, BiConsumer<ImageEntry, ResourceLocation> renderer) {
            super(x, width, height);
            this.imageLocation = imageLocation;
            this.renderer = renderer;
        }

        protected ImageEntry(int x, int width, int height, ResourceLocation pEntryTexture, ResourceLocation imageLocation, BiConsumer<ImageEntry, ResourceLocation> renderer) {
            super(x, width, height, pEntryTexture);
            this.imageLocation = imageLocation;
            this.renderer = renderer;
        }

        protected ImageEntry(int x, int width, int height, ResourceLocation pEntryTexture, Runnable onSelected, ResourceLocation imageLocation, BiConsumer<ImageEntry, ResourceLocation> renderer) {
            super(x, width, height, pEntryTexture, onSelected);
            this.imageLocation = imageLocation;
            this.renderer = renderer;
        }

        @Override
        public void renderInternal(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick) {
            super.renderInternal(guiGraphics, x, y, width, height, mouseX, mouseY, partialTick);
            renderer.accept(this, imageLocation);
        }
    }
    public class CustomEntry extends ListEntry {
        protected final BiConsumer<CustomEntry, RenderWrapper> customRenderer;

        protected CustomEntry(int x, int width, int height, BiConsumer<CustomEntry, RenderWrapper> renderer) {
            super(x,width, height);
            this.customRenderer = renderer;
        }

        @Override
        public void renderInternal(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick) {
            super.renderInternal(guiGraphics, x, y, width, height, mouseX, mouseY, partialTick);
            customRenderer.accept(this, new RenderWrapper(guiGraphics, x, y, width, height, mouseX, mouseY, partialTick));
        }
        public record RenderWrapper(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick) {}
    }
}
