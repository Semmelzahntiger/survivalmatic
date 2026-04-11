package com.semmelzahntiger.survivalmatic.client.gui.widgets.base;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class AbstractVWidget extends AbstractWidget {
    protected final Screen parentScreen;
    protected float xRelative;
    protected float yRelative;

    public <W extends AbstractVWidget,T extends WidgetBuilder<W,T>> AbstractVWidget(WidgetBuilder<W,T> builder) {
        super(builder.xRelative,builder.yRelative, builder.width, builder.height, builder.pMessage);
        this.parentScreen = builder.parentScreen;
    }
    public int getXBound() {
        return getX() + getWidth();
    }
    public int getYBound() {
        return getY() + getHeight();
    }
    public float getXRelative() {
        return xRelative;
    }
    public void setXRelative(float xRelative) {
        this.xRelative = xRelative;
        setX((int) (xRelative * parentScreen.width));
    }
    public float getYRelative() {
        return yRelative;
    }
    public void setYRelative(float yRelative) {
        this.yRelative = yRelative;
        setY((int) (yRelative * parentScreen.height));
    }
    public int getCenterX() {
        return getX() + getWidth() / 2;
    }
    public int getCenterY() {
        return getY() + getHeight() / 2;
    }
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(!isActive()) {
            return false;
        }
        if(!isValidClickButton(pButton)) {
            return false;
        }
        if(!clicked(pMouseX, pMouseY)) {
            return false;
        }
        this.onClick(pMouseX, pMouseY);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
    protected abstract static class WidgetBuilder<W extends AbstractVWidget,T extends WidgetBuilder<W,T>> {
        public final int xRelative;
        public final int yRelative;
        public final int width;
        public final int height;
        public final Screen parentScreen;
        public Component pMessage;

        protected WidgetBuilder(float xRelative, float yRelative, int width, int height, Screen parentScreen) {
            this((int) (xRelative * parentScreen.width),(int) (yRelative * parentScreen.height), width, height, parentScreen);
        }
        protected WidgetBuilder(int x, int y, int width, int height, Screen parentScreen) {
            this.xRelative = x;
            this.yRelative = y;
            this.width = width;
            this.height = height;
            this.parentScreen = parentScreen;
        }
        public T setMessage(Component pMessage) {
            this.pMessage = pMessage;
            return self();
        }
        @SuppressWarnings("unchecked")
        T self() {
            return (T) this;
        }
        public abstract W build();

    }
}
