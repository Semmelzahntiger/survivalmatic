package com.semmelzahntiger.survivalmatic.client.gui.widgets;

import com.semmelzahntiger.survivalmatic.client.gui.widgets.base.AbstractStringWidget;
import net.minecraft.client.gui.screens.Screen;

public class BasicStringWidget extends AbstractStringWidget<BasicStringWidget> {
    public BasicStringWidget(Builder builder) {
        super(builder);
    }
    public static class Builder extends AbstractStringWidget.Builder<BasicStringWidget, Builder> {

        protected Builder(float xRelative, float yRelative, int widthRelative, int heightRelative, Screen parentScreen) {
            super(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }
        protected Builder(int x, int y, int width, int height, Screen parentScreen) {
            super(x, y, width, height, parentScreen);
        }
        public static Builder of(float xRelative, float yRelative, int widthRelative, int heightRelative, Screen parentScreen) {
            return new Builder(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }
        public static Builder of(int x, int y, int width, int height, Screen parentScreen) {
            return new Builder(x, y, width, height, parentScreen);
        }

        @Override
        public BasicStringWidget build() {
            return new BasicStringWidget(this);
        }
    }
}
