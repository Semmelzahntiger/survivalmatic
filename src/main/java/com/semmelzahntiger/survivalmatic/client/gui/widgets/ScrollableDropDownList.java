package com.semmelzahntiger.survivalmatic.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
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
    private final ResourceLocation ACTIVE_BORDER;

    private boolean isOpened = false;

    private double currentYScroll = 0;
    private int scrollIndexShift = 0;

    private final int closedYSize;
    private final int openedYSize;
    private final int entryHeight;
    private final int visibleEntries;

    List<ListEntry> entries = new ArrayList<>();
    private int entryCount = 0;

    private int hoveredIndex = 0;
    ListEntry hoveredEntry = null;
    ListEntry selectedEntry = null;

    private ScrollableDropDownList(int pX, int pY, int pWidth, int pHeight, int visibleEntries, ResourceLocation entry, ResourceLocation background, ResourceLocation hovered, ResourceLocation activeBorder, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage);
        closedYSize = pHeight;
        openedYSize = pHeight * (visibleEntries + 1);
        this.entryHeight = pHeight;
        this.visibleEntries = visibleEntries;
        this.DEFAULT_ENTRY_TEXTURE = entry;
        this.BACKGROUND = background;
        this.HOVERED = hovered;
        this.ACTIVE_BORDER = activeBorder;
        // Debug
        open();
    }
    private ScrollableDropDownList(Builder builder) {
        this(builder.x, builder.y, builder.width, builder.height, builder.visibleEntries, builder.entryTexture, builder.background, builder.hovered, builder.activeBorder, builder.message);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
//        guiGraphics.enableScissor(this.getX(), this.getY(), this.getWidth(), this.getHeight());
//        // Todo: Render currently selected
        guiGraphics.blit(BACKGROUND, this.getX(), this.getY(), this.getWidth(), this.getHeight(),0,0,256,256, 256, 256);
//        guiGraphics.disableScissor();
        //guiGraphics.enableScissor(this.getX(), this.getY() + entryHeight, this.getWidth(), this.getHeight());
        for (ListEntry entry : entries) {
            entry.render(guiGraphics, i, i1, v);
        }
        //guiGraphics.disableScissor();
        RenderSystem.enableBlend();
        guiGraphics.blit(AREA, this.getX(), this.getY()+entryHeight, this.getWidth(), this.getHeight()-entryHeight,0,0,256,256, 256, 256);
        hoveredIndex = isHoveringOver(i,i1);
        if(hoveredIndex > -1) {
            System.out.println(hoveredIndex);
            guiGraphics.blit(HOVERED, this.getX(), this.getY() + (entryHeight * (hoveredIndex+1)),
                    this.getWidth(), entryHeight, 0,0, 256, 256, 256, 256);
        }
        RenderSystem.disableBlend();
//        guiGraphics.disableScissor();
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
        int start = this.getY() + entryHeight;
        int end = this.getHeight();
        int spacePerEntry = (end - start) / visibleEntries;
        boolean inXSpace = mouseX >= this.getX() && mouseX <= this.getX() + getWidth();
        boolean inYSpace = mouseY >= this.getY() + entryHeight && mouseY <= this.getY() + getHeight();
        if(inXSpace && inYSpace) {
            return (mouseY - (this.getY() + entryHeight)) / spacePerEntry;
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
        public final Component message;
        public ResourceLocation entryTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/list_entry.png");
        public ResourceLocation background =  ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/amk.png");;
        public ResourceLocation hovered = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/hovered.png");
        public ResourceLocation activeBorder = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/active.png");

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
        public Builder setEntryTexture(ResourceLocation pEntryTexture) {
            return this;
        }
        public Builder setBackground(ResourceLocation pBackground) {
            return this;
        }
        public Builder setHovered(ResourceLocation pHovered) {
            return this;
        }
        public Builder setActiveBorder(ResourceLocation pActiveBorder) {
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

        public abstract void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);
        public int getX() { return x; }
        public int getY() { return y; }
        public void setX(int x) { this.x = x; }
        public void setY(int y) { this.y = y; }
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
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.blit(entryTexture, this.getX(), this.getY(), this.width, this.height, 0,0, 256, 112, 256, 112);
            guiGraphics.drawString(Minecraft.getInstance().font, text, this.getX(), this.getY(), 0xFFFFFF, false);
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
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
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
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            customRenderer.accept(this, new RenderWrapper(guiGraphics, mouseX, mouseY, partialTick));
        }
        public record RenderWrapper(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
    }
}
