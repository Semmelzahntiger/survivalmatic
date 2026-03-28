package com.semmelzahntiger.survivalmatic.client.gui.widgets.base;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class AbstractStringWidget<W extends AbstractStringWidget<W>> extends AbstractVWidget {
    public static ResourceLocation debug = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/marked_area.png");
    public float scaleRatio = 1.0f;
    public Component text;
    public String rawText;
    public final boolean isCentered;
    public int color;
    public <T extends Builder<W,T>> AbstractStringWidget(Builder<W,T> builder) {
        super(builder.xRelative, builder.yRelative, builder.widthRelative, builder.heightRelative, builder.parentScreen, builder.pMessage);
        text = builder.text;
        rawText = builder.rawText;
        isCentered = builder.isCentered;
        color = builder.color;
    }
    public W setText(Component text) {
        this.text = text;
        return (W) this;
    }
    public W setRawText(String rawText) {
        this.rawText = rawText;
        return (W) this;
    }
    public W setColor(int color) {
        this.color = color;
        return (W) this;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        renderBackground(guiGraphics, i, i1, v);
        Font font = Minecraft.getInstance().font;
        if(isCentered) {
            guiGraphics.drawCenteredString(font, text, getCenterX(), getCenterY(), color);
        }
        else {
            guiGraphics.drawString(font, text, getX(), getY(), color);
        }
    }
    protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(debug, this.getX(), this.getY(), this.getWidth(), this.getHeight(),0,0,256,256, 256, 256);
    }
    public abstract static class Builder<W extends AbstractStringWidget<W>, T extends AbstractVWidget.WidgetBuilder<W,T>> extends AbstractVWidget.WidgetBuilder<W,T> {
        public Component text = Component.empty();
        public String rawText = "";
        public boolean isCentered = false;
        public int color = 0xFFFFFF;

        protected Builder(float xRelative, float yRelative, float widthRelative, float heightRelative, Screen parentScreen) {
            super(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }
        public T text(Component text) {
            this.text = text;
            return self();
        }
        public T rawText(String rawText) {
            this.rawText = rawText;
            return self();
        }
        public T setCentered() {
            this.isCentered = true;
            return self();
        }
        public T color(int color) {
            this.color = color;
            return self();
        }

        @Override
        T self() {
            return (T)this;
        }
    }
}
