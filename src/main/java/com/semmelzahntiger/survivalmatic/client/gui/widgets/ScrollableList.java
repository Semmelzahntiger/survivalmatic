package com.semmelzahntiger.survivalmatic.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class ScrollableList extends AbstractVWidget {
    public final ResourceLocation defaultEntryTexture;
    public final ResourceLocation background;
    public final ResourceLocation border;
    public final int borderLeftMargin;
    public final int borderTopMargin;
    public final int borderRightMargin;
    public final int borderBottomMargin;

    public final ResourceLocation sliderTexture;
    public final int sliderMargin;

    public final int visibleEntries;

    protected int entryHeight;
    protected int listHeight;

    protected final List<ScrollableDropDownList.ListEntry> entries = new ArrayList<>();
    protected int entryCount = 0;
    protected int currentYScroll = 0;
    protected int scrollIndexShift = 0;

    protected int scrollSliderSize = 0;
    protected int offsetForScrollSlider = 0;


    private ScrollableList(Builder builder) {
        super(builder.xRelative, builder.yRelative, builder.widthRelative, builder.heightRelative, builder.screen, builder.message);
        defaultEntryTexture = builder.defaultEntryTexture;
        background = builder.backgroundTexture;
        border = builder.listBorderTexture;
        borderLeftMargin = relativeToAbsoluteX(builder.borderLeftMargin);
        borderTopMargin = relativeToAbsoluteY(builder.borderTopMargin);
        borderRightMargin = relativeToAbsoluteX(builder.borderRightMargin) + borderLeftMargin;
        borderBottomMargin = relativeToAbsoluteY(builder.borderBottomMargin) + borderTopMargin;
        sliderTexture = builder.sliderTexture;
        sliderMargin = relativeToAbsoluteX(builder.sliderMargin);
        visibleEntries = builder.visibleEntries;

        entryHeight = this.getHeight();
        listHeight = visibleEntries * entryHeight;
        this.setHeight(listHeight);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        // Todo: Render Slider
        RenderSystem.enableBlend();
        this.scrollSliderSize = entryCount != 0 ? listHeight * (visibleEntries / entryCount) : 0;
        this.offsetForScrollSlider = entryCount > visibleEntries ? listHeight / (entryCount - visibleEntries) : 0;
        guiGraphics.blit(background, this.getX(), this.getY(), this.getWidth(), this.getHeight(),0,0,this.getWidth(),this.getHeight(), this.getWidth(), this.getHeight());
        guiGraphics.enableScissor(this.getX(), this.getY(), this.getXBound(), this.getYBound());
        for (ScrollableDropDownList.ListEntry entry : entries) {
            entry.render(guiGraphics, i, i1, v);
        }
        guiGraphics.disableScissor();
        guiGraphics.blit(border, this.getX() - borderLeftMargin, this.getY() - borderTopMargin, this.getWidth() + borderRightMargin, this.getHeight() + borderBottomMargin,
                0,0,
                this.getWidth() + borderRightMargin, this.getHeight() + borderBottomMargin,
                this.getWidth() + borderRightMargin, this.getHeight() + borderBottomMargin);
        RenderSystem.disableBlend();
    }
    public void addTextEntry(Component text) {
        this.entries.add(new ScrollableDropDownList.TextEntry(this.getX(), this.getWidth(), this.entryHeight, text, this.defaultEntryTexture));
        entryCount++;
        realignEntries();
    }
    public void addTextEntry(Component text, ResourceLocation entryTexture) {
        this.entries.add(new ScrollableDropDownList.TextEntry(this.getX(), this.getWidth(), this.entryHeight, text, entryTexture));
        entryCount++;
        realignEntries();
    }
    public void addTextEntry(Component text, ResourceLocation entryTexture, Runnable onSelect) {
        this.entries.add(new ScrollableDropDownList.TextEntry(this.getX(), this.getWidth(), this.entryHeight, text, entryTexture, onSelect));
    }
    public void addImageEntry(ResourceLocation image, BiConsumer<ScrollableDropDownList.ImageEntry, ResourceLocation> imageRenderer) {
        this.entries.add(new ScrollableDropDownList.ImageEntry(this.getX(), this.getWidth(), this.entryHeight, this.defaultEntryTexture, image, imageRenderer));
        entryCount++;
        realignEntries();
    }
    public void addImageEntry(ResourceLocation image, ResourceLocation entryTexture, BiConsumer<ScrollableDropDownList.ImageEntry, ResourceLocation> imageRenderer) {
        this.entries.add(new ScrollableDropDownList.ImageEntry(this.getX(), this.getWidth(), this.entryHeight,entryTexture, image, imageRenderer));
        entryCount++;
        realignEntries();
    }
    public void addImageEntry(ResourceLocation image, ResourceLocation entryTexture,Runnable onSelect, BiConsumer<ScrollableDropDownList.ImageEntry, ResourceLocation> imageRenderer) {
        this.entries.add(new ScrollableDropDownList.ImageEntry(this.getX(), this.getWidth(), this.entryHeight,entryTexture, onSelect, image, imageRenderer));
        entryCount++;
        realignEntries();
    }
    public void addCustomEntry(BiConsumer<ScrollableDropDownList.CustomEntry, ScrollableDropDownList.CustomEntry.RenderWrapper> customRenderer) {
        this.entries.add(new ScrollableDropDownList.CustomEntry(this.getX(), this.getWidth(), this.entryHeight, this.defaultEntryTexture, customRenderer));
        entryCount++;
        realignEntries();
    }
    public void addEntry(ScrollableDropDownList.ListEntry entry) {
        this.entries.add(entry);
    }
    protected void realignEntries() {
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setY((int) (this.getY() + i*entryHeight + currentYScroll));
        }
    }
    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if(isHovered) {
            scroll(pDelta);
            return true;
        }
        return false;
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

    public static class Builder {
        protected final float xRelative;
        protected final float yRelative;
        protected final float widthRelative;
        protected final float heightRelative;
        protected final Screen screen;
        protected final Component message;
        protected final int visibleEntries;
        protected ResourceLocation defaultEntryTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/list_entry.png");
        protected ResourceLocation backgroundTexture =  ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/amk.png");
        protected ResourceLocation listBorderTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_border.png");
        protected float borderLeftMargin = 0;
        protected float borderTopMargin = 0;
        protected float borderRightMargin = 0;
        protected float borderBottomMargin = 0;
        protected ResourceLocation sliderTexture;
        protected float sliderMargin = 0;

        private Builder(float x, float y, float width, float height,int visibleEntries, Screen screen, Component message) {
            this.xRelative = x;
            this.yRelative = y;
            this.widthRelative = width;
            this.heightRelative = height;
            this.visibleEntries = visibleEntries;
            this.screen = screen;
            this.message = message;
        }
        public static Builder of(float x, float y, float width, float height,int visibleEntries, Screen screen, Component message) {
            return new Builder(x,y,width,height, visibleEntries, screen, message);
        }
        public Builder setBackgroundTexture(ResourceLocation texture) {
            this.backgroundTexture = texture;
            return this;
        }
        public Builder setBorderMargins(float left, float right, float top, float bottom) {
            this.borderLeftMargin = left;
            this.borderTopMargin = top;
            this.borderRightMargin = right;
            this.borderBottomMargin = bottom;
            return this;
        }
        public Builder setEntryTexture(ResourceLocation texture) {
            this.sliderTexture = texture;
            return this;
        }
        public Builder setDefaultEntryTexture(ResourceLocation texture) {
            this.defaultEntryTexture = texture;
            return this;
        }
        public Builder setSliderTexture(ResourceLocation texture) {
            this.sliderTexture = texture;
            return this;
        }
        public Builder setSliderMargin(float margin) {
            this.sliderMargin = margin;
            return this;
        }
        public ScrollableList build() {
            return new ScrollableList(this);
        }
    }
}
