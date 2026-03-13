package com.semmelzahntiger.survivalmatic.client.gui.widgets;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.swing.*;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class ScrollableList extends AbstractVWidget {
    public final Screen screen;

    public final ResourceLocation background;
    public final ResourceLocation border;
    public final int borderLeftMargin;
    public final int borderTopMargin;
    public final int borderRightMargin;
    public final int borderBottomMargin;

    public final ResourceLocation sliderTexture;
    public final int sliderMargin;

    public final int visibleEntries;

    public int entryHeight;


    private ScrollableList(Builder builder) {
        super(builder.xRelative, builder.yRelative, builder.widthRelative, builder.heightRelative, builder.screen, pMessage);
    }
    public static class Builder {
        protected final float xRelative;
        protected final float yRelative;
        protected final float widthRelative;
        protected final float heightRelative;
        protected final Screen screen;
        public ResourceLocation defaultEntryTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/list_entry.png");
        public ResourceLocation backgroundTexture=  ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/amk.png");
        protected float borderLeftMargin = 0;
        protected float borderTopMargin = 0;
        protected float borderRightMargin = 0;
        protected float borderBottomMargin = 0;
        protected ResourceLocation sliderTexture;
        protected float sliderMargin = 0;

        private Builder(float x, float y, float width, float height, Screen screen) {
            this.xRelative = x;
            this.yRelative = y;
            this.widthRelative = width;
            this.heightRelative = height;
            this.screen = screen;
        }
        public static Builder of(float x, float y, float width, float height, Screen screen) {
            return new Builder(x,y,width,height, screen);
        }
        public Builder setBackgroundTexture(ResourceLocation texture) {
            this.backgroundTexture = texture;
        }
        public Builder setBorderMargins(float left, float top, float right, float bottom) {
            this.borderLeftMargin = left;
            this.borderTopMargin = top;
            this.borderRightMargin = right;
            this.borderBottomMargin = bottom;
            return this;
        }
    }
}
