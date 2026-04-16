package com.semmelzahntiger.survivalmatic.client.gui.widgets.base;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public class AbstractStringWidget<W extends AbstractStringWidget<W>> extends AbstractVWidget {
    public static ResourceLocation debug = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/marked_area.png");
    public Component text;
    public String rawText;
    public final boolean isCentered;
    public final boolean drawRawText;
    public final boolean alwaysScale;
    public int color;
    protected <T extends Builder<W,T>> AbstractStringWidget(Builder<W,T> builder) {
        super(builder);
        text = builder.text;
        rawText = builder.rawText;
        isCentered = builder.isCentered;
        drawRawText = builder.drawRawText;
        color = builder.color;
        alwaysScale = builder.alwaysScale;
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
    // Todo: Scaling
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        renderBackground(guiGraphics, i, i1, v);
        Font font = Minecraft.getInstance().font;
        FormattedCharSequence charSequence = this.drawRawText ? FormattedCharSequence.forward(rawText, Style.EMPTY) : text.getVisualOrderText();
        int textHeight = font.lineHeight;
        int textWidth = font.width(charSequence);
        float widthScaling = alwaysScale ? (float) this.width / textWidth : Math.min(1.0f, (float) this.width / textWidth);
        float heightScaling = alwaysScale ? (float) this.height / textHeight : Math.min(1.0f, (float) this.height / textHeight);
        float scaling = Math.min(widthScaling, heightScaling);
        float positionScaleCorrectionMultiplier = 1.0f / scaling;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scaling, scaling, 1.0f);

        if(isCentered) {
            guiGraphics.drawCenteredString(font, charSequence,(int) (getCenterX() * positionScaleCorrectionMultiplier), (int) ((getCenterY() - (float) textHeight / 2 * positionScaleCorrectionMultiplier) * positionScaleCorrectionMultiplier), color);
        }
        else {
            guiGraphics.drawString(font, charSequence,(int) (getX() * positionScaleCorrectionMultiplier), (int) (getY() * positionScaleCorrectionMultiplier), color);
        }
        guiGraphics.pose().popPose();
    }
    protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // guiGraphics.blit(debug, this.getX(), this.getY(), this.getWidth(), this.getHeight(),0,0,256,256, 256, 256);
    }
    protected abstract static class Builder<W extends AbstractStringWidget<W>, T extends AbstractVWidget.WidgetBuilder<W,T>> extends AbstractVWidget.WidgetBuilder<W,T> {
        public Component text = Component.empty();
        public String rawText = "";
        public boolean isCentered = false;
        public boolean drawRawText = false;
        public boolean alwaysScale = false;
        public int color = 0xFFFFFF;

        protected Builder(float xRelative, float yRelative, int widthRelative, int heightRelative, Screen parentScreen) {
            super(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }
        protected Builder(int x, int y, int widthRelative, int heightRelative, Screen parentScreen) {
            super(x, y, widthRelative, heightRelative, parentScreen);
        }
        public T text(Component text) {
            this.text = text;
            return self();
        }
        public T rawText(String rawText) {
            this.rawText = rawText;
            return self();
        }
        public T renderRaw() {
            this.drawRawText = true;
            return self();
        }
        public T alwaysScale() {
            this.alwaysScale = true;
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
