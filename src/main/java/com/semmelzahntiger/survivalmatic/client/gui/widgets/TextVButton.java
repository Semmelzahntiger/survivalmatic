package com.semmelzahntiger.survivalmatic.client.gui.widgets;

import com.semmelzahntiger.survivalmatic.client.gui.widgets.base.BaseTextVButton;
import net.minecraft.client.gui.screens.Screen;

public class TextVButton extends BaseTextVButton<TextVButton> {
    public <T extends BaseTextButtonBuilder<TextVButton, T>> TextVButton(BaseTextButtonBuilder<TextVButton, T> builder) {
        super(builder);
    }
    public static class TextVButtonBuilder extends BaseTextButtonBuilder<TextVButton, TextVButtonBuilder> {

        public TextVButtonBuilder(float xRelative, float yRelative, float widthRelative, float heightRelative, Screen parentScreen) {
            super(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }
        public static TextVButtonBuilder of(float xRelative, float yRelative, float widthRelative, float heightRelative, Screen parentScreen) {
            return new TextVButtonBuilder(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }
        @Override
        public TextVButton build() {
            return new TextVButton(this);
        }
    }
}
