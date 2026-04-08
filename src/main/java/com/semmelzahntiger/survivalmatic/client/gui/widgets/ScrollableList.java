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

public class ScrollableList extends AbstractVWidget implements IScrollableList {
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

    protected final List<IScrollableList.ListEntry<?>> entries = new ArrayList<>();
    protected int entryCount = 0;
    protected int currentYScroll = 0;
    protected int scrollIndexShift = 0;

    protected int scrollSliderSize = 0;
    protected int offsetForScrollSlider = 0;


    private ScrollableList(Builder builder) {
        super(builder);
        defaultEntryTexture = builder.defaultEntryTexture;
        background = builder.backgroundTexture;
        border = builder.listBorderTexture;
        borderLeftMargin = builder.borderLeftMargin;
        borderTopMargin = builder.borderTopMargin;
        borderRightMargin = builder.borderRightMargin + borderLeftMargin;
        borderBottomMargin = builder.borderBottomMargin + borderTopMargin;
        sliderTexture = builder.sliderTexture;
        sliderMargin = builder.sliderMargin;
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
        for (IScrollableList.ListEntry<?> entry : entries) {
            entry.render(guiGraphics, i, i1, v);
        }
        guiGraphics.disableScissor();
        guiGraphics.blit(border, this.getX() - borderLeftMargin, this.getY() - borderTopMargin, this.getWidth() + borderRightMargin, this.getHeight() + borderBottomMargin,
                0,0,
                this.getWidth() + borderRightMargin, this.getHeight() + borderBottomMargin,
                this.getWidth() + borderRightMargin, this.getHeight() + borderBottomMargin);
        RenderSystem.disableBlend();
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
        return this.getX();
    }

    @Override
    public int getStartOfList() {
        return this.getY();
    }

    @Override
    public int getEntryWidth() {
        return this.getWidth();
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
        return defaultEntryTexture;
    }

    @Override
    public List<ListEntry<?>> getEntries() {
        return entries;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if(isHovered) {
            scroll(pDelta);
            return true;
        }
        return false;
    }

    public static class Builder extends AbstractVWidget.WidgetBuilder<ScrollableList, Builder> {
        public final Component message;
        public final int visibleEntries;
        public ResourceLocation defaultEntryTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/default_rectangle.png");
        public int entryTextureWidth = 256;
        public int entryTextureHeight = 256;
        public ResourceLocation backgroundTexture =  ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/default_rectangle.png");
        public int backgroundTextureWidth = 256;
        public int backgroundTextureHeight = 256;
        public ResourceLocation listBorderTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_hollow.png");
        public int listBorderTextureWidth = 256;
        public int listBorderTextureHeight = 256;
        public int borderLeftMargin = 0;
        public int borderTopMargin = 0;
        public int borderRightMargin = 0;
        public int borderBottomMargin = 0;
        public ResourceLocation sliderTexture;
        public int sliderTextureWidth = 256;
        public int sliderTextureHeight = 256;
        public int sliderMargin = 0;

        protected Builder(float x, float y, int width, int height,int visibleEntries, Screen screen, Component message) {
            super(x,y,width,height, screen);
            this.visibleEntries = visibleEntries;
            this.message = message;
        }
        protected Builder(int x, int y, int width, int height, int visibleEntries, Screen screen, Component message) {
            super(x,y,width,height, screen);
            this.visibleEntries = visibleEntries;
            this.message = message;
        }
        public static Builder of(float x, float y, int width, int height,int visibleEntries, Screen screen, Component message) {
            return new Builder(x,y,width,height, visibleEntries, screen, message);
        }
        public static Builder of(int x, int y, int width, int height,int visibleEntries, Screen screen, Component message) {
            return new Builder(x,y,width,height,visibleEntries, screen, message);
        }
        public Builder setDefaultEntryTexture(ResourceLocation defaultEntryTexture, int width, int height) {
            this.defaultEntryTexture = defaultEntryTexture;
            this.entryTextureWidth = width;
            this.entryTextureHeight = height;
            return this;
        }
        public Builder setBackgroundTexture(ResourceLocation backgroundTexture, int width, int height) {
            this.backgroundTexture = backgroundTexture;
            this.backgroundTextureWidth = width;
            this.backgroundTextureHeight = height;
            return this;
        }
        public Builder setListBorderTexture(ResourceLocation listBorderTexture, int width, int height, int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
            this.listBorderTexture = listBorderTexture;
            this.listBorderTextureWidth = width;
            this.listBorderTextureHeight = height;
            this.borderLeftMargin = leftMargin;
            this.borderTopMargin = topMargin;
            this.borderRightMargin = rightMargin;
            this.borderBottomMargin = bottomMargin;
            return this;
        }
        public Builder setSliderTexture(ResourceLocation sliderTexture, int width, int height, int margin) {
            this.sliderTexture = sliderTexture;
            this.sliderTextureWidth = width;
            this.sliderTextureHeight = height;
            this.sliderMargin = margin;
            return this;
        }
        public ScrollableList build() {
            return new ScrollableList(this);
        }
    }
}
