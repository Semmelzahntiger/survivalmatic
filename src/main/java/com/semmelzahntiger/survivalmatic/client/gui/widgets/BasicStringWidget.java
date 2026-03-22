package com.semmelzahntiger.survivalmatic.client.gui.widgets;

import com.semmelzahntiger.survivalmatic.client.gui.widgets.base.AbstractStringWidget;
import net.minecraft.client.gui.screens.Screen;

public class BasicStringWidget extends AbstractStringWidget<BasicStringWidget> {
    public BasicStringWidget(Builder builder) {
        super(builder);
    }
    public static class Builder extends AbstractStringWidget.Builder<BasicStringWidget, Builder> {

        protected Builder(float xRelative, float yRelative, float widthRelative, float heightRelative, Screen parentScreen) {
            super(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }
        public static Builder of(float xRelative, float yRelative, float widthRelative, float heightRelative, Screen parentScreen) {
            return new Builder(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }

        @Override
        public BasicStringWidget build() {
            return new BasicStringWidget(this);
        }
    }
}
