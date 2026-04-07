package com.semmelzahntiger.survivalmatic.client.gui.widgets;

import com.semmelzahntiger.survivalmatic.client.gui.widgets.base.AbstractVButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

public class GenericVButton extends AbstractVButton<GenericVButton> {
    public <T extends AbstractVButton.Builder<GenericVButton, T>> GenericVButton(AbstractVButton.Builder<GenericVButton, T> builder) {
        super(builder);
    }


    public static class Builder extends AbstractVButton.Builder<GenericVButton, Builder> {

        public Builder(float xRelative, float yRelative, int widthRelative, int heightRelative, Screen parentScreen) {
            super(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }
        public static Builder of(float xRelative, float yRelative, int widthRelative, int heightRelative, Screen parentScreen) {
            return new Builder(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }

        @Override
        public GenericVButton build() {
            return new GenericVButton(this);
        }
    }
}
