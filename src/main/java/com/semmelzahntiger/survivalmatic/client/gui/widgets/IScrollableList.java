package com.semmelzahntiger.survivalmatic.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.BiConsumer;

public interface IScrollableList {
    List<ListEntry> getEntries();
    int getVisibleEntries();
    int getEntryCount();
    void setEntryCount(int entryCount);
    int getIndexShift();
    void setIndexShift(int shift);
    int getEntryXPosition();
    int getStartOfList();
    int getEntryWidth();
    int getEntryHeight();
    int getScroll();
    void setScroll(int scroll);
    ResourceLocation getDefaultEntryTexture();

    default void scroll(double scrollAmount) {
        int sign = (int) Math.signum(scrollAmount);
        int maxDiff = getEntryCount() - getVisibleEntries();
        if(maxDiff > 0) {
            setIndexShift(Mth.clamp(getIndexShift() - sign, 0, maxDiff));
            setScroll(-(getIndexShift() * getEntryHeight()));
        }
        realignEntries();
    }
    default void addTextEntry(Component text) {
        getEntries().add(new IScrollableList.TextEntry(getEntryXPosition(), getEntryWidth(), getEntryHeight(), text, getDefaultEntryTexture()));
        setEntryCount(getEntryCount() + 1);
        realignEntries();
    }
    default void addTextEntry(Component text, ResourceLocation entryTexture) {
        getEntries().add(new IScrollableList.TextEntry(getEntryXPosition(), getEntryWidth(), getEntryHeight(), text, entryTexture));
        setEntryCount(getEntryCount() + 1);
        realignEntries();
    }
    default void addTextEntry(Component text, ResourceLocation entryTexture, Runnable onSelect) {
        getEntries().add(new IScrollableList.TextEntry(getEntryXPosition(), getEntryWidth(), getEntryHeight(), text, entryTexture, onSelect));
        setEntryCount(getEntryCount() + 1);
        realignEntries();
    }
    default void addImageEntry(ResourceLocation image, BiConsumer<IScrollableList.ImageEntry, ResourceLocation> imageRenderer) {
        getEntries().add(new IScrollableList.ImageEntry(getEntryXPosition(), getEntryWidth(), getEntryHeight(), getDefaultEntryTexture(), image, imageRenderer));
        setEntryCount(getEntryCount() + 1);
        realignEntries();
    }
    default void addImageEntry(ResourceLocation image, ResourceLocation entryTexture, BiConsumer<IScrollableList.ImageEntry, ResourceLocation> imageRenderer) {
        getEntries().add(new IScrollableList.ImageEntry(getEntryXPosition(), getEntryWidth(), getEntryHeight(), getDefaultEntryTexture(), image, imageRenderer));
        setEntryCount(getEntryCount() + 1);
        realignEntries();
    }
    default void addImageEntry(ResourceLocation image, ResourceLocation entryTexture, Runnable onSelect, BiConsumer<IScrollableList.ImageEntry, ResourceLocation> imageRenderer) {
        getEntries().add(new IScrollableList.ImageEntry(getEntryXPosition(), getEntryWidth(), getEntryHeight(), getDefaultEntryTexture(),onSelect, image, imageRenderer));
        setEntryCount(getEntryCount() + 1);
        realignEntries();
    }
    default void addCustomEntry(BiConsumer<IScrollableList.CustomEntry, IScrollableList.CustomEntry.RenderWrapper> customRenderer) {
        getEntries().add(new IScrollableList.CustomEntry(getEntryXPosition(), getEntryWidth(), getEntryHeight(), getDefaultEntryTexture(), customRenderer));
        setEntryCount(getEntryCount() + 1);
        realignEntries();
    }
    default void addEntry(IScrollableList.ListEntry entry) {
        getEntries().add(entry);
        setEntryCount(getEntryCount() + 1);
        realignEntries();
    }
    default void updateEntries() {
        for (ListEntry entry : getEntries()) {
            entry.setX(getEntryXPosition());
            entry.setWidth(getEntryWidth());
            entry.setHeight(getEntryHeight());
        }
        realignEntries();
    }
    default void realignEntries() {
        List<ListEntry> entries = getEntries();
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setY(getStartOfList() + i*getEntryHeight() + getScroll());
        }
    }
    default void resetEntries() {
        getEntries().clear();
        realignEntries();
    }


    abstract class ListEntry {
        protected int x;
        protected int y = 0;
        protected int width;
        protected int height;
        protected final ResourceLocation entryTexture;
        protected final Runnable onSelected;

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

    class TextEntry extends ListEntry {
        public final Component text;
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
            PoseStack pose = guiGraphics.pose();
            int lineHeight = Minecraft.getInstance().font.lineHeight;
            float scale = (float) height / 2 / lineHeight ;
            pose.pushPose();
            pose.scale(scale, scale, 1.0f);
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, (int) ((x + (float) width / 2) / scale), (int) ((y + (float) height / 2) / scale), 0xFFFFFF);
            pose.popPose();
        }
    }

    class ImageEntry extends ListEntry {
        protected final ResourceLocation imageLocation;
        protected final BiConsumer<ImageEntry, ResourceLocation> renderer;


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

    class CustomEntry extends ListEntry {
        protected final BiConsumer<CustomEntry, RenderWrapper> customRenderer;

        protected CustomEntry(int x, int width, int height,ResourceLocation entryTexture, BiConsumer<CustomEntry, RenderWrapper> renderer) {
            super(x,width, height, entryTexture);
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
