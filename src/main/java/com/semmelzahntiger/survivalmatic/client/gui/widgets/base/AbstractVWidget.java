package com.semmelzahntiger.survivalmatic.client.gui.widgets.base;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class AbstractVWidget extends AbstractWidget {
    protected final Screen parentScreen;
    protected float xRelative;
    protected float yRelative;
    protected float widthRelative;
    protected float heightRelative;
    public AbstractVWidget(float xRelative, float yRelative, float widthRelative, float heightRelative, Screen screen, Component pMessage) {
        super((int) (screen.width * xRelative), (int) (screen.height * yRelative), (int) (screen.width * widthRelative), (int) (screen.height * heightRelative), pMessage);
        this.parentScreen = screen;
        this.xRelative = xRelative;
        this.yRelative = yRelative;
        this.widthRelative = widthRelative;
        this.heightRelative = heightRelative;
    }
    public <W extends AbstractVWidget,T extends WidgetBuilder<W,T>> AbstractVWidget(WidgetBuilder<W,T> builder) {
       this(builder.xRelative, builder.yRelative, builder.widthRelative, builder.heightRelative, builder.parentScreen, builder.pMessage);
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
        setX(relativeToAbsoluteX(xRelative));
    }
    public void setWidthRelative(float widthRelative) {
        this.widthRelative = widthRelative;
        setWidth(relativeToAbsoluteX(widthRelative));
    }
    protected final int relativeToAbsoluteX(float x) {
        return (int) (x * parentScreen.width);
    }
    public float getYRelative() {
        return yRelative;
    }
    public void setYRelative(float yRelative) {
        this.yRelative = yRelative;
        setY(relativeToAbsoluteY(yRelative));
    }
    public void setHeightRelative(float heightRelative) {
        this.heightRelative = heightRelative;
        setHeight(relativeToAbsoluteY(heightRelative));
    }
    protected final int relativeToAbsoluteY(float y) {
        return (int) (y * parentScreen.height);
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
    public abstract static class WidgetBuilder<W extends AbstractVWidget,T extends WidgetBuilder<W,T>> {
        protected final float xRelative;
        protected final float yRelative;
        protected final float widthRelative;
        protected final float heightRelative;
        protected final Screen parentScreen;
        protected Component pMessage;

        public WidgetBuilder(float xRelative, float yRelative, float widthRelative, float heightRelative, Screen parentScreen) {
            this.xRelative = xRelative;
            this.yRelative = yRelative;
            this.widthRelative = widthRelative;
            this.heightRelative = heightRelative;
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
