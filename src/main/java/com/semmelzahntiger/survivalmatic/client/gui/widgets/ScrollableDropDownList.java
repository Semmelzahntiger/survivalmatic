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

import static com.semmelzahntiger.survivalmatic.ExampleMod.MODID;

public class ScrollableDropDownList extends AbstractWidget {
    private static final ResourceLocation ENTRY_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/list_entry.png");
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/amk.png");
    private static final ResourceLocation AREA = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/marked_area.png");
    private static final ResourceLocation HOVERED = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/hovered.png");
    private boolean isOpened = false;

    private double currentYScroll = 0;
    private double scrollIndexShift = 0;

    private final int closedYSize;
    private final int openedYSize;
    private final int entryHeight;
    private final int visibleEntries;

    List<ListEntry> entries = new ArrayList<>();
    private int entryCount = 0;

    private int hoveredIndex = 0;
    ListEntry hoveredEntry = null;
    ListEntry selectedEntry = null;

    public ScrollableDropDownList(int pX, int pY, int pWidth, int pHeight, int visibleEntries, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage);
        closedYSize = pHeight;
        openedYSize = pHeight * (visibleEntries + 1);
        this.entryHeight = pHeight;
        this.visibleEntries = visibleEntries;
        open();
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
    public void onClick(double pMouseX, double pMouseY) {
        super.onClick(pMouseX, pMouseY);
    }
    public void addTextEntry(Component text) {
        this.entries.add(new TextEntry(this.width, entryHeight, text));
        entryCount++;
        realignEntries();
    }
    public void addImageEntry() {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
    public abstract static class ListEntry {
        protected int x = 0;
        protected int y = 0;
        protected final int width;
        protected final int height;

        protected ListEntry(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public abstract void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);
        public int getX() { return x; }
        public int getY() { return y; }
        public void setX(int x) { this.x = x; }
        public void setY(int y) { this.y = y; }
    }
    public static class TextEntry extends ListEntry {
        private final Component text;
        public TextEntry(int width, int height, Component text) {
            super(width, height);
            this.text = text;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, ENTRY_TEXTURE);
            guiGraphics.blit(ENTRY_TEXTURE, this.getX(), this.getY(), this.width, this.height, 0,0, 256, 112, 256, 112);
            guiGraphics.drawString(Minecraft.getInstance().font, text, this.getX(), this.getY(), 0xFFFFFF, false);
        }
    }
    public static class ImageEntry extends ListEntry {
        private final ResourceLocation imageLocation;

        public ImageEntry(int width, int height, ResourceLocation imageLocation) {
            super(width, height);
            this.imageLocation = imageLocation;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        }
    }
}
