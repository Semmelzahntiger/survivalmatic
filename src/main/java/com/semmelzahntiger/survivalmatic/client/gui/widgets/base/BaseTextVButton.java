package com.semmelzahntiger.survivalmatic.client.gui.widgets.base;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, text.getString(), this.getX() + width / 2, this.getY() + height / 2 - font.lineHeight / 2, 0xFFFFFFFF);
    }

    public abstract static class Builder<W extends BaseTextVButton<W>, T extends Builder<W,T>> extends AbstractVButton.Builder<W, T> {
        protected Component text = Component.empty();
        public Builder(float xRelative, float yRelative, int widthRelative, int heightRelative, Screen parentScreen) {
            super(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
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
