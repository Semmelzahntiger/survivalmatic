package com.semmelzahntiger.survivalmatic.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.base.AbstractVWidget;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.base.IScrollableList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class ScrollableDropDownList extends AbstractVWidget implements IScrollableList {
    public final ResourceLocation DEFAULT_ENTRY_TEXTURE;
    public final int entryTextureWidth;
    public final int entryTextureHeight;
    public final ResourceLocation BACKGROUND;
    public final int backgroundTextureWidth;
    public final int backgroundTextureHeight;
    public final ResourceLocation HOVERED;
    public final int hoveredEntryTextureWidth;
    public final int hoveredEntryTextureHeight;
    public final int hoverLeftMargin;
    public final int hoverTopMargin;
    public final int hoverRightMargin;
    public final int hoverBottomMargin;

    public final ResourceLocation ACTIVE_BORDER;
    public final int selectedEntryTextureWidth;
    public final int selectedEntryTextureHeight;
    public final int selectedBorderLeftMargin;
    public final int selectedBorderTopMargin;
    public final int selectedBorderRightMargin;
    public final int selectedBorderBottomMargin;

    public final ResourceLocation LIST_BORDER;
    public final int listBorderTextureWidth;
    public final int listBorderTextureHeight;
    public final int listBorderLeftMargin;
    public final int listBorderTopMargin;
    public final int listBorderRightMargin;
    public final int listBorderBottomMargin;

    public final ResourceLocation SLIDER;
    public final int sliderTextureWidth;
    public final int sliderTextureHeight;
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

    protected int currentYScroll = 0;
    protected int scrollIndexShift = 0;

    protected int scrollSliderSize;
    protected int offsetForScrollSlider;
    List<IScrollableList.ListEntry<?>> entries = new ArrayList<>();
    protected int entryCount = 0;

    protected int hoveredIndex = 0;
    IScrollableList.ListEntry<?> selectedEntry = null;

    protected ScrollableDropDownList(Builder builder) {
        super(builder);
        // Textures
        this.DEFAULT_ENTRY_TEXTURE = builder.entryTexture;
        this.entryTextureWidth = builder.entryTextureWidth;
        this.entryTextureHeight = builder.entryTextureHeight;
        this.BACKGROUND = builder.backgroundTexture;
        this.backgroundTextureWidth = builder.backgroundTextureWidth;
        this.backgroundTextureHeight = builder.backgroundTextureHeight;
        this.HOVERED = builder.hoveredEntryTexture;
        this.hoveredEntryTextureWidth = builder.hoveredEntryTextureWidth;
        this.hoveredEntryTextureHeight = builder.hoveredEntryTextureHeight;
        this.ACTIVE_BORDER = builder.selectedEntryBorderTexture;
        this.selectedEntryTextureWidth = builder.selectedEntryBorderTextureWidth;
        this.selectedEntryTextureHeight = builder.selectedEntryBorderTextureHeight;
        this.LIST_BORDER = builder.listBorderTexture;
        this.listBorderTextureWidth = builder.listBorderTextureWidth;
        this.listBorderTextureHeight = builder.listBorderTextureHeight;
        this.SLIDER = builder.sliderTexture;
        this.sliderTextureWidth = builder.sliderTextureWidth;
        this.sliderTextureHeight = builder.sliderTextureHeight;
        // Texture Margins
        this.hoverLeftMargin = builder.hoveredEntryTextureLeftMargin;
        this.hoverTopMargin = builder.hoveredEntryTextureTopMargin;
        this.hoverRightMargin = builder.hoveredEntryTextureRightMargin + this.hoverLeftMargin;
        this.hoverBottomMargin = builder.hoveredEntryTextureBottomMargin + this.hoverTopMargin;

        this.selectedBorderLeftMargin = builder.selectedEntryTextureLeftMargin;
        this.selectedBorderTopMargin = builder.selectedEntryTextureTopMargin;
        this.selectedBorderRightMargin = builder.selectedEntryTextureRightMargin + this.selectedBorderLeftMargin;
        this.selectedBorderBottomMargin = builder.selectedEntryTextureBottomMargin + this.selectedBorderTopMargin;

        this.listBorderLeftMargin = builder.listBorderTextureLeftMargin;
        this.listBorderTopMargin = builder.listBorderTextureTopMargin;
        this.listBorderRightMargin = builder.listBorderTextureRightMargin + this.listBorderLeftMargin;
        this.listBorderBottomMargin = builder.listBorderTextureBottomMargin + this.listBorderTopMargin;

        this.sliderRightMargin = builder.sliderTextureMargin;
        // Fixed Data
        this.visibleEntries = builder.visibleEntries;
        this.entryWidthRatio = builder.listEntryWidthRatio;
        this.entryHeightRatio = builder.listEntryHeightRatio;

        this.listOffset = builder.listOffset;
        // Dynamically changed data
        this.closedHeight = builder.height;

        listStart = this.getY() + this.getHeight() + listOffset;
        this.entryWidth = (int) (this.getWidth() * this.entryWidthRatio);
        this.entryHeight = (int) (this.getHeight() * this.entryHeightRatio);

        this.entryCenteringOffsetX = (this.getWidth() - this.entryWidth) / 2;
        this.entryOffsettedX = this.getX() + this.entryCenteringOffsetX;

        this.listHeight = visibleEntries * entryHeight;
        this.openedHeight = builder.height+ builder.listOffset + this.listHeight;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        RenderSystem.enableBlend();
        this.scrollSliderSize = entryCount != 0 ? listHeight * (visibleEntries / entryCount) : 0;
        this.offsetForScrollSlider = entryCount > visibleEntries ? listHeight / (entryCount - visibleEntries) : 0;
        guiGraphics.blit(BACKGROUND, this.getX(), this.getY(), this.getWidth(), closedHeight,0,0,this.getWidth(),closedHeight, this.getWidth(), closedHeight);

        if(selectedEntry != null) {
            guiGraphics.enableScissor(this.getX(), this.getY(), this.getXBound(), this.getY() + closedHeight);
            selectedEntry.renderInternal(guiGraphics, this.getX(), this.getY(), this.getWidth(), closedHeight, i, i1, v);
            guiGraphics.disableScissor();
        }
        guiGraphics.blit(ACTIVE_BORDER,this.getX() - this.selectedBorderLeftMargin, this.getY() - this.selectedBorderTopMargin,
                this.getWidth() + this.selectedBorderRightMargin, this.closedHeight + this.selectedBorderBottomMargin,
                0,0,entryWidth,entryHeight, entryWidth, entryHeight);

        if(isOpened) {
            guiGraphics.enableScissor(this.getX(), listStart, this.getXBound(), this.listStart+ this.listHeight);
            for (IScrollableList.ListEntry<?> entry : entries) {
                entry.render(guiGraphics, i, i1, v);
            }
            guiGraphics.disableScissor();
            guiGraphics.blit(LIST_BORDER, this.entryOffsettedX - this.listBorderLeftMargin, this.listStart - this.listBorderTopMargin,
                    this.entryWidth + this.listBorderRightMargin, this.listHeight + listBorderBottomMargin,
                    0,0,256,256, 256, 256);
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
        this.openedHeight = this.getHeight() + this.listOffset + this.listHeight;
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
    public ListEntry<?> getSelectedEntry() {
        return selectedEntry;
    }

    @Override
    public List<ListEntry<?>> getEntries() {
        return entries;
    }

    @Override
    public int getVisibleEntries() {
        return visibleEntries;
    }

    @Override
    public int getEntryCount() {
        return entryCount;
    }

    @Override
    public void setEntryCount(int entryCount) {
        this.entryCount = entryCount;
    }

    @Override
    public int getIndexShift() {
        return scrollIndexShift;
    }

    @Override
    public void setIndexShift(int shift) {
        this.scrollIndexShift = shift;
    }

    @Override
    public int getEntryXPosition() {
        return entryOffsettedX;
    }

    @Override
    public int getStartOfList() {
        return listStart;
    }

    @Override
    public int getEntryWidth() {
        return entryWidth;
    }

    @Override
    public int getEntryHeight() {
        return entryHeight;
    }

    @Override
    public int getScroll() {
        return currentYScroll;
    }

    @Override
    public void setScroll(int scroll) {
        this.currentYScroll = scroll;
    }

    @Override
    public ResourceLocation getDefaultEntryTexture() {
        return DEFAULT_ENTRY_TEXTURE;
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
            selectedEntry.onSelected();
            close();
        }
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

    public void setHeightWithoutUpdatingList(int value) {
        super.setHeight(value);
    }
    public static class Builder extends AbstractVWidget.WidgetBuilder<ScrollableDropDownList, Builder> {
        public float listEntryWidthRatio = 1.0f;
        public float listEntryHeightRatio = 1.0f;
        public final int visibleEntries;
        public int listOffset;
        public final Component message;
        public ResourceLocation entryTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/default_rectangle.png");
        public int entryTextureWidth = 256;
        public int entryTextureHeight = 256;
        public ResourceLocation backgroundTexture =  ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/default_rectangle.png");
        public int backgroundTextureWidth = 256;
        public int backgroundTextureHeight = 256;
        public ResourceLocation hoveredEntryTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/hover_border.png");
        public int hoveredEntryTextureWidth = 256;
        public int hoveredEntryTextureHeight = 256;
        public int hoveredEntryTextureLeftMargin = 0;
        public int hoveredEntryTextureRightMargin = 0;
        public int hoveredEntryTextureTopMargin = 0;
        public int hoveredEntryTextureBottomMargin = 0;
        public ResourceLocation selectedEntryBorderTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_hollow.png");
        public int selectedEntryBorderTextureWidth = 256;
        public int selectedEntryBorderTextureHeight = 256;
        public int selectedEntryTextureLeftMargin = 0;
        public int selectedEntryTextureRightMargin = 0;
        public int selectedEntryTextureTopMargin = 0;
        public int selectedEntryTextureBottomMargin = 0;
        public ResourceLocation listBorderTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_hollow.png");
        public int listBorderTextureWidth = 256;
        public int listBorderTextureHeight = 256;
        public int listBorderTextureLeftMargin = 0;
        public int listBorderTextureRightMargin = 0;
        public int listBorderTextureTopMargin = 0;
        public int listBorderTextureBottomMargin = 0;
        public ResourceLocation sliderTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/slider.png");
        public int sliderTextureWidth = 256;
        public int sliderTextureHeight = 256;
        public int sliderTextureMargin = 0;

        protected Builder(float xRelative, float yRelative, int width, int height, int pVisibleEntries, Component pMessage, Screen screen) {
            super(xRelative, yRelative, width, height, screen);
            visibleEntries = pVisibleEntries;
            message = pMessage;
        }
        protected Builder(int x, int y, int width, int height, int pVisibleEntries, Component pMessage, Screen screen) {
            super(x, y, width, height, screen);
            visibleEntries = pVisibleEntries;
            message = pMessage;
        }
        public static Builder of(float x, float y, int width, int height, int visibleEntries, Component message, Screen screen) {
            return new Builder(x, y, width, height, visibleEntries, message, screen);
        }
        public static Builder of(int x, int y, int width, int height, int visibleEntries, Component message, Screen screen) {
            return new Builder(x, y, width, height, visibleEntries, message, screen);
        }
        public Builder setListOffset(int offset) {
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
        public Builder setEntryTexture(ResourceLocation pEntryTexture, int width, int height) {
            this.entryTexture = pEntryTexture;
            this.entryTextureWidth = width;
            this.entryTextureHeight = height;
            return this;
        }
        public Builder setBackgroundTexture(ResourceLocation pBackground, int width, int height) {
            this.backgroundTexture = pBackground;
            this.backgroundTextureWidth = width;
            this.backgroundTextureHeight = height;
            return this;
        }
        public Builder setHoveredEntryTexture(ResourceLocation pHovered, int width, int height, int leftMargin, int rightMargin, int topMargin, int bottomMargin) {
            this.hoveredEntryTexture = pHovered;
            this.hoveredEntryTextureWidth = width;
            this.hoveredEntryTextureHeight = height;
            this.hoveredEntryTextureLeftMargin = leftMargin;
            this.hoveredEntryTextureRightMargin = rightMargin;
            this.hoveredEntryTextureTopMargin = topMargin;
            this.hoveredEntryTextureBottomMargin = bottomMargin;
            return this;
        }
        public Builder setSelectedEntryBorderTexture(ResourceLocation pActiveBorder, int width, int height, int leftMargin, int rightMargin, int topMargin, int bottomMargin) {
            this.selectedEntryBorderTexture = pActiveBorder;
            this.selectedEntryBorderTextureWidth = width;
            this.selectedEntryBorderTextureHeight = height;
            this.selectedEntryTextureLeftMargin = leftMargin;
            this.selectedEntryTextureRightMargin = rightMargin;
            this.selectedEntryTextureTopMargin = topMargin;
            this.selectedEntryTextureBottomMargin = bottomMargin;
            return this;
        }
        public Builder setListBorderTexture(ResourceLocation pListBorder, int width, int height, int leftMargin, int rightMargin, int topMargin, int bottomMargin) {
            this.listBorderTexture = pListBorder;
            this.listBorderTextureWidth = width;
            this.listBorderTextureHeight = height;
            this.listBorderTextureLeftMargin = leftMargin;
            this.listBorderTextureRightMargin = rightMargin;
            this.listBorderTextureTopMargin = topMargin;
            this.listBorderTextureBottomMargin = bottomMargin;
            return this;
        }
        public Builder setSliderTexture(ResourceLocation pSlider, int width, int height, int rightMargin) {
            this.sliderTexture = pSlider;
            this.sliderTextureWidth = width;
            this.sliderTextureHeight = height;
            this.sliderTextureMargin = rightMargin;
            return this;
        }
        public ScrollableDropDownList build() {
            return new ScrollableDropDownList(this);
        }
    }
}
