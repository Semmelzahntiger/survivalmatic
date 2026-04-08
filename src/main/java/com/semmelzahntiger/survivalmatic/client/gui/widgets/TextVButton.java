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
        guiGraphics.blit(this.hoverTextureEnabled, this.getX() - this.hoverLeftMargin, this.getY() - this.hoverTopMargin,
                this.getWidth() + this.hoverLeftMargin + this.hoverRightMargin,
                this.getHeight() + hoverTopMargin + hoverBottomMargin,
                0,0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
    }

    @Override
    public void renderHoveredDisabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(this.hoverTextureDisabled, this.getX() - this.hoverLeftMargin, this.getY() - this.hoverTopMargin,
                this.getWidth() + this.hoverLeftMargin + this.hoverRightMargin,
                this.getHeight() + hoverTopMargin + hoverBottomMargin,
                0,0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
    }

    public static class Builder extends BaseTextVButton.Builder<TextVButton, Builder> {

        protected Builder(float xRelative, float yRelative, int width, int height, Screen parentScreen) {
            super(xRelative, yRelative, width, height, parentScreen);
        }
        protected Builder(int x, int y, int width, int height, Screen parentScreen) {
            super(x, y, width, height, parentScreen);
        }
        public static Builder of(float xRelative, float yRelative, int width, int height, Screen parentScreen) {
            return new Builder(xRelative, yRelative, width, height, parentScreen);
        }
        public static Builder of(int x, int y, int width, int height, Screen parentScreen) {
            return new Builder(x, y, width, height, parentScreen);
        }
        @Override
        public TextVButton build() {
            Survivalmatic.LOGGER.info("x: {}, y: {}, width: {}, height: {}, parentScreenWidth: {} parentScreenHeight: {}", this.xRelative, this.yRelative, this.width, this.height, this.parentScreen.width, this.parentScreen.height);
            return new TextVButton(this);
        }
    }
}
