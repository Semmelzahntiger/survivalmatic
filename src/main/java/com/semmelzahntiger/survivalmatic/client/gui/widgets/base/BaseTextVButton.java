package com.semmelzahntiger.survivalmatic.client.gui.widgets.base;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

public abstract class BaseTextVButton<W extends BaseTextVButton<W>> extends AbstractVButton<W> {
    protected Component text;
    public <T extends Builder<W, T>> BaseTextVButton(Builder<W,T> builder) {
        super(builder);
        this.text = builder.text;
    }
    public void setText(Component text) {
        this.text = text;
    }

    @Override
    public void renderAlways(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        FormattedCharSequence charSequence = text.getVisualOrderText();
        int textHeight = font.lineHeight;
        int textWidth = font.width(charSequence);
        float widthScaling = Math.min(1.0f, (float) this.width / textWidth);
        float heightScaling = Math.min(1.0f, (float) this.height / textHeight);
        float scaling = Math.min(widthScaling, heightScaling);
        float positionScaleCorrectionMultiplier = 1.0f / scaling;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scaling, scaling, 1.0f);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, text.getString(), (int) (this.getCenterX() * positionScaleCorrectionMultiplier),
                (int) ((this.getCenterY() - ((float) font.lineHeight / 2) * scaling) * positionScaleCorrectionMultiplier), 0xFFFFFFFF);
        guiGraphics.pose().popPose();
    }

    protected abstract static class Builder<W extends BaseTextVButton<W>, T extends Builder<W,T>> extends AbstractVButton.Builder<W, T> {
        protected Component text = Component.empty();
        protected Builder(float xRelative, float yRelative, int widthRelative, int heightRelative, Screen parentScreen) {
            super(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }
        protected Builder(int x, int y, int widthRelative, int heightRelative, Screen parentScreen) {
            super(x, y, widthRelative, heightRelative, parentScreen);
        }
        public T setText(Component text) {
            this.text = text;
            return self();
        }

        @Override
        T self() {
            return (T) this;
        }
    }
}
