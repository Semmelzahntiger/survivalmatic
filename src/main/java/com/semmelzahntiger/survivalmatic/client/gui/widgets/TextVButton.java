package com.semmelzahntiger.survivalmatic.client.gui.widgets;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.base.BaseTextVButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

public class TextVButton extends BaseTextVButton<TextVButton> {
    public <T extends BaseTextVButton.Builder<TextVButton, T>> TextVButton(BaseTextVButton.Builder<TextVButton, T> builder) {
        super(builder);
    }

    @Override
    public void renderHoveredEnabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(this.hoverTexture, this.getX() - relativeToAbsoluteX(this.hoverLeftMargin), this.getY() - relativeToAbsoluteY(this.hoverTopMargin),
                this.getWidth() + relativeToAbsoluteX(this.hoverLeftMargin + this.hoverRightMargin),
                this.getHeight() + relativeToAbsoluteY(hoverTopMargin + hoverBottomMargin),
                0,0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
    }

    @Override
    public void renderHoveredDisabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    public static class Builder extends BaseTextVButton.Builder<TextVButton, Builder> {

        public Builder(float xRelative, float yRelative, float widthRelative, float heightRelative, Screen parentScreen) {
            super(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }
        public static Builder of(float xRelative, float yRelative, float widthRelative, float heightRelative, Screen parentScreen) {
            return new Builder(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }
        @Override
        public TextVButton build() {
            Survivalmatic.LOGGER.info("x: {}, y: {}, width: {}, height: {}, parentScreenWidth: {} parentScreenHeight: {}", this.xRelative, this.yRelative, this.widthRelative, this.heightRelative, this.parentScreen.width, this.parentScreen.height);
            return new TextVButton(this);
        }
    }
}
