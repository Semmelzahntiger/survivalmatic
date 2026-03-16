package com.semmelzahntiger.survivalmatic.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class TextVButton extends AbstractVButton {
    protected Component text;
    public <T extends TextButtonBuilder<T>> TextVButton(TextButtonBuilder<T> builder) {
        super(builder);
        this.text = builder.text;
    }
    public void setText(Component text) {
        this.text = text;
    }
    @Override
    public void renderHoveredEnabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    public void renderHoveredDisabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    public void renderAlways(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, text.getString(), width / 2, height / 2, 0xFFFFFFFF);
    }

    public static class TextButtonBuilder<T extends TextButtonBuilder<T>> extends AbstractVButton.ButtonBuilder<TextButtonBuilder<T>> {
        protected Component text = Component.empty();
        public TextButtonBuilder(float xRelative, float yRelative, float widthRelative, float heightRelative, Screen parentScreen) {
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
