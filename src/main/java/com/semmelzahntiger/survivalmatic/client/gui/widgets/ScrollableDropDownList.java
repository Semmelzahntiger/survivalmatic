package com.semmelzahntiger.survivalmatic.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class ScrollableDropDownList extends AbstractWidget {
    private final ResourceLocation DEFAULT_ENTRY_TEXTURE;
    private final ResourceLocation BACKGROUND;
    // Debug
    private final ResourceLocation AREA = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/marked_area.png");

    private final ResourceLocation HOVERED;
    private final int hoverLeftMargin;
    private final int hoverTopMargin;
    private final int hoverRightMargin;
    private final int hoverBottomMargin;

    private final ResourceLocation ACTIVE_BORDER;
    private final int activeBorderLeftMargin;
    private final int activeBorderTopMargin;
    private final int activeBorderRightMargin;
    private final int activeBorderBottomMargin;

    private final ResourceLocation LIST_BORDER;
    private final int listBorderLeftMargin;
    private final int listBorderTopMargin;
    private final int listBorderRightMargin;
    private final int listBorderBottomMargin;

    private final ResourceLocation SLIDER;
    private final int sliderRightMargin;

    private boolean isOpened = false;

    private double currentYScroll = 0;
    private int scrollIndexShift = 0;

    public final int listOffset;
    private final int closedYSize;
    private final int openedYSize;
    private final int listStart;
    private final int listYSize;
    private final int entryHeight;
    private final int visibleEntries;

    private int scrollSliderSize;
    private int offsetForScrollSlider;
    List<ListEntry> entries = new ArrayList<>();
    private int entryCount = 0;

    private int hoveredIndex = 0;
    ListEntry selectedEntry = null;

    private ScrollableDropDownList(Builder builder) {
        super(builder.x,  builder.y, builder.width, builder.height, builder.message);
        this.listOffset = builder.listOffset;
        this.closedYSize = builder.height;
        this.openedYSize = builder.height * (builder.visibleEntries + 1) + this.listOffset;
        this.entryHeight = builder.height;
        this.visibleEntries = builder.visibleEntries;
        this.DEFAULT_ENTRY_TEXTURE = builder.entryTexture;
        this.BACKGROUND = builder.background;
        this.HOVERED = builder.hovered;
        this.ACTIVE_BORDER = builder.selectedBorder;
        this.LIST_BORDER = builder.listBorder;
        this.SLIDER = builder.slider;
        this.listStart = builder.y + builder.height + listOffset;
        this.listYSize = this.openedYSize - this.closedYSize - this.listOffset;

        this.hoverLeftMargin = builder.hoveredLeftMargin;
        this.hoverTopMargin = builder.hoveredTopMargin;
        this.hoverRightMargin = builder.hoveredRightMargin;
        this.hoverBottomMargin = builder.hoveredBottomMargin;

        this.activeBorderLeftMargin = builder.hoveredLeftMargin;
        this.activeBorderTopMargin = builder.hoveredTopMargin;
        this.activeBorderRightMargin = builder.hoveredRightMargin;
        this.activeBorderBottomMargin = builder.hoveredBottomMargin;

        this.listBorderLeftMargin = builder.hoveredLeftMargin;
        this.listBorderTopMargin = builder.hoveredTopMargin;
        this.listBorderRightMargin = builder.hoveredRightMargin;
        this.listBorderBottomMargin = builder.hoveredBottomMargin;

        this.sliderRightMargin = builder.sliderMargin;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        RenderSystem.enableBlend();
        this.scrollSliderSize = entryCount != 0 ? listYSize * (visibleEntries / entryCount) : 0;
        this.offsetForScrollSlider = entryCount > visibleEntries ? listYSize / (entryCount - visibleEntries) : 0;

        guiGraphics.blit(BACKGROUND, this.getX(), this.getY(), this.getWidth(), this.getHeight(),0,0,256,256, 256, 256);
        if(selectedEntry != null) {
            selectedEntry.renderInternal(guiGraphics, this.getX(), this.getY(), this.getWidth(), listStart, i, i1, v);
        }
        guiGraphics.blit(ACTIVE_BORDER,this.getX() - activeBorderLeftMargin, this.getY() - activeBorderTopMargin,
                this.getWidth() + activeBorderRightMargin, closedYSize + activeBorderBottomMargin,
                0,0,256,256, 256, 256);
        if(isOpened) {
            guiGraphics.blit(LIST_BORDER, this.getX() - listBorderLeftMargin, listStart-listBorderTopMargin,  this.width + listBorderRightMargin, listStart + this.getHeight(),
                    0,0,256,256, 256, 256);
            guiGraphics.enableScissor(this.getX(), listStart, this.getWidth(), this.getHeight());
            for (ListEntry entry : entries) {
                entry.render(guiGraphics, i, i1, v);
            }
            //Debug
            guiGraphics.blit(AREA, this.getX(), listStart, this.getWidth(), listYSize,0,0,256,256, 256, 256);
            guiGraphics.disableScissor();
        }
        hoveredIndex = isHoveringOver(i,i1);
        if(hoveredIndex > -1) {
            guiGraphics.blit(HOVERED, this.getX() - hoverLeftMargin, this.getY() + (entryHeight * (hoveredIndex+1)) - hoverTopMargin,
                    this.getWidth() + hoverRightMargin, entryHeight + hoverBottomMargin,
                    0,0, 256, 256, 256, 256);
        }
        RenderSystem.disableBlend();
    }
    protected void open() {
        isOpened = true;
        this.setHeight(openedYSize);
        realignEntries();
    }
    protected void close() {
        isOpened = false;
        this.setHeight(closedYSize);
        currentYScroll = 0;
    }
    private void realignEntries() {
        int index = 1;
        for (ListEntry entry : entries) {
            entry.setY((int) (this.getY() + (index * entryHeight) + currentYScroll));
            index++;
        }
    }
    private void scroll(double scrollAmount) {
        int sign = (int) Math.signum(scrollAmount);
        int maxDiff = entryCount - visibleEntries;
        if(maxDiff > 0) {
            scrollIndexShift = Mth.clamp(scrollIndexShift + sign, 0, maxDiff);
            currentYScroll = -(scrollIndexShift * entryHeight);
        }
        realignEntries();
    }
    // -1 means not hovering over any entry
    private int isHoveringOver(int mouseX, int mouseY) {
        if(isOpened) {
            int end = this.getHeight();
            int spacePerEntry = (end - listStart) / visibleEntries;
            boolean inXSpace = mouseX >= this.getX() && mouseX <= this.getX() + getWidth();
            boolean inYSpace = mouseY >= listStart && mouseY <= this.getY() + getHeight();
            if(inXSpace && inYSpace) {
                return (mouseY - (listStart)) / spacePerEntry;
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
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(this.active && this.visible && this.isValidClickButton(pButton)) {
            boolean mainElementClicked = pMouseX >= this.getX() && pMouseX < this.getX() + this.getWidth() && pMouseY >= this.getY() && pMouseY <= this.getY() + closedYSize;
            boolean listClicked = pMouseX >= this.getX() && pMouseX < this.getX() + this.getWidth() && pMouseY >= this.getY() + closedYSize && pMouseY <= this.getY() + openedYSize;
            if(mainElementClicked) {
                onClickMainElement(pMouseX, pMouseY);
                return true;
            }
            if(isOpened && listClicked) {
                onClickListElement(pMouseX, pMouseY);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        super.onClick(pMouseX, pMouseY);
    }

    @Override
    protected boolean clicked(double pMouseX, double pMouseY) {
        return super.clicked(pMouseX, pMouseY);
    }

    private void onClickMainElement(double pMouseX, double pMouseY) {
        if(isOpened) {
            close();
        }
        else {
            open();
        }
    }
    private void onClickListElement(double pMouseX, double pMouseY) {
        int clickedIndex = hoveredIndex + scrollIndexShift;
        if(clickedIndex < entries.size()) {
            selectedEntry = entries.get(clickedIndex);
            close();
        }
    }
    public void addTextEntry(Component text) {
        this.entries.add(new TextEntry(this.width, entryHeight, text));
        entryCount++;
        realignEntries();
    }
    public void addImageEntry(ResourceLocation image, BiConsumer<ImageEntry, ResourceLocation> imageRenderer) {
        this.entries.add(new ImageEntry(this.width, entryHeight, image, imageRenderer));
        entryCount++;
        realignEntries();
    }
    public void addCustomEntry(BiConsumer<CustomEntry, CustomEntry.RenderWrapper> customRenderer) {
        this.entries.add(new CustomEntry(this.width, entryHeight, customRenderer));
        entryCount++;
        realignEntries();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
    public static class Builder {
        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public final int visibleEntries;
        public int listOffset;
        public final Component message;
        public ResourceLocation entryTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/list_entry.png");
        public ResourceLocation background =  ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/amk.png");
        public ResourceLocation hovered = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/hovered.png");
        public int hoveredLeftMargin = 0;
        public int hoveredRightMargin = 0;
        public int hoveredTopMargin = 0;
        public int hoveredBottomMargin = 0;
        public ResourceLocation selectedBorder = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_border.png");
        public int selectedLeftMargin = 0;
        public int selectedRightMargin = 0;
        public int selectedTopMargin = 0;
        public int selectedBottomMargin = 0;
        public ResourceLocation listBorder = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_border.png");
        public int listLeftMargin = 0;
        public int listRightMargin = 0;
        public int listTopMargin = 0;
        public int listBottomMargin = 0;
        public ResourceLocation slider = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/slider.png");
        public int sliderMargin = 0;

        private Builder(int pX, int pY, int pWidth, int pHeight, int pVisibleEntries, Component pMessage) {
            x = pX;
            y = pY;
            width = pWidth;
            height = pHeight;
            visibleEntries = pVisibleEntries;
            message = pMessage;
        }
        public static Builder of(int x, int y, int width, int height, int visibleEntries, Component message) {
            return new Builder(x, y, width, height, visibleEntries, message);
        }
        public Builder withListOffset(int offset) {
            listOffset = offset;
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
        public ScrollableDropDownList build() {
            return new ScrollableDropDownList(this);
        }
    }
    public abstract class ListEntry {
        protected int x = 0;
        protected int y = 0;
        protected final int width;
        protected final int height;
        protected final ResourceLocation entryTexture;

        protected ListEntry(int width, int height) {
            this(width, height, DEFAULT_ENTRY_TEXTURE);
        }
        protected ListEntry(int width, int height, ResourceLocation pEntryTexture) {
            this.width = width;
            this.height = height;
            this.entryTexture = pEntryTexture;
        }
        public ResourceLocation getEntryTexture() {
            return entryTexture;
        }
        public void render(GuiGraphics pGraphics, int mouseX, int mouseY, float partialTick) {
            renderInternal(pGraphics, this.getX(), this.getY(), this.width, this.getHeight(), mouseX, mouseY, partialTick);
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
    }
    public class TextEntry extends ListEntry {
        private final Component text;
        public TextEntry(int width, int height, Component text) {
            super(width, height);
            this.text = text;
        }
        public TextEntry(int width, int height, Component text, ResourceLocation customEntryTexture) {
            super(width, height, customEntryTexture);
            this.text = text;
        }

        @Override
        public void renderInternal(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick) {
            super.renderInternal(guiGraphics, x, y, width, height, mouseX, mouseY, partialTick);
            guiGraphics.drawString(Minecraft.getInstance().font, text, x, y, 0xFFFFFF, false);
        }
    }
    public class ImageEntry extends ListEntry {
        private final ResourceLocation imageLocation;
        private final BiConsumer<ImageEntry, ResourceLocation> renderer;

        public ImageEntry(int width, int height, ResourceLocation imageLocation, BiConsumer<ImageEntry, ResourceLocation> renderer) {
            super(width, height);
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
        private final BiConsumer<CustomEntry, RenderWrapper> customRenderer;

        protected CustomEntry(int width, int height, BiConsumer<CustomEntry, RenderWrapper> renderer) {
            super(width, height);
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
