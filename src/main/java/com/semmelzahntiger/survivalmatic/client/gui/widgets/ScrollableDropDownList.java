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
    public final int selectedBorderLeftMargin;
    public final int selectedBorderTopMargin;
    public final int selectedBorderRightMargin;
    public final int selectedBorderBottomMargin;

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

    protected int currentYScroll = 0;
    protected int scrollIndexShift = 0;

    protected int scrollSliderSize;
    protected int offsetForScrollSlider;
    List<IScrollableList.ListEntry<?>> entries = new ArrayList<>();
    protected int entryCount = 0;

    protected int hoveredIndex = 0;
    IScrollableList.ListEntry<?> selectedEntry = null;

    protected ScrollableDropDownList(Builder builder) {
        super(builder.x, builder.y, builder.width, builder.height, builder.screen, builder.message);
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
        this.hoverRightMargin = relativeToAbsoluteX(builder.hoveredRightMargin) + this.hoverLeftMargin;
        this.hoverBottomMargin = relativeToAbsoluteY(builder.hoveredBottomMargin) + this.hoverTopMargin;

        this.selectedBorderLeftMargin = relativeToAbsoluteX(builder.selectedLeftMargin);
        this.selectedBorderTopMargin = relativeToAbsoluteY(builder.selectedTopMargin);
        this.selectedBorderRightMargin = relativeToAbsoluteX(builder.selectedRightMargin) + this.selectedBorderLeftMargin;
        this.selectedBorderBottomMargin = relativeToAbsoluteY(builder.selectedBottomMargin) + this.selectedBorderTopMargin;

        this.listBorderLeftMargin = relativeToAbsoluteX(builder.listLeftMargin);
        this.listBorderTopMargin = relativeToAbsoluteY(builder.listTopMargin);
        this.listBorderRightMargin = relativeToAbsoluteX(builder.listRightMargin) + this.listBorderLeftMargin;
        this.listBorderBottomMargin = relativeToAbsoluteY(builder.listBottomMargin) + this.listBorderTopMargin;

        this.sliderRightMargin = relativeToAbsoluteX(builder.sliderMargin);
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
        this.openedHeight = relativeToAbsoluteY(builder.height+ builder.listOffset) + this.listHeight;

        System.out.println("Offset Calc:" + this.getWidth() + " " + this.entryWidth + " " + (this.getWidth() - this.entryWidth) / 2);
        System.out.println("Selected Margins: " + selectedBorderLeftMargin + " " + selectedBorderRightMargin + " " + selectedBorderTopMargin + " " + selectedBorderBottomMargin);
        System.out.println("Center Position: " + this.getX() + " " + this.getWidth() +" "+ (this.getX() + this.getWidth()) / 2);
        System.out.println("Center for Entries" + " " + entryOffsettedX + " " + entryWidth + " " + (entryOffsettedX + (entryWidth / 2)));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        RenderSystem.enableBlend();
        this.scrollSliderSize = entryCount != 0 ? listHeight * (visibleEntries / entryCount) : 0;
        this.offsetForScrollSlider = entryCount > visibleEntries ? listHeight / (entryCount - visibleEntries) : 0;
        guiGraphics.blit(BACKGROUND, this.getX(), this.getY(), this.getWidth(), closedHeight,0,0,this.getWidth(),closedHeight, this.getWidth(), closedHeight);

        if(selectedEntry != null) {
            selectedEntry.renderInternal(guiGraphics, this.getX(), this.getY(), this.getWidth(), closedHeight, i, i1, v);
        }
        guiGraphics.blit(ACTIVE_BORDER,this.getX() - this.selectedBorderLeftMargin, this.getY() - this.selectedBorderTopMargin,
                this.getWidth() + this.selectedBorderRightMargin, this.closedHeight + this.selectedBorderBottomMargin,
                0,0,entryWidth,entryHeight, entryWidth, entryHeight);

        if(isOpened) {
            guiGraphics.enableScissor(this.getX(), listStart, this.getXBound(), this.listStart+ this.listHeight);
            for (IScrollableList.ListEntry entry : entries) {
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
        public Builder setListOffset(float offset) {
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
        public Builder setSliderMargin(float margin) {
            this.sliderMargin = margin;
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
}
