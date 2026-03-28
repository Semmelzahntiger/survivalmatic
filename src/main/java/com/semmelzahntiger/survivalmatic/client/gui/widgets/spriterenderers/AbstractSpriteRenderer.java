package com.semmelzahntiger.survivalmatic.client.gui.widgets.spriterenderers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractSpriteRenderer {
    public final Screen parentScreen;
    public ResourceLocation spriteLocation;
    public int x;
    public int y;
    public int width;
    public int height;
    public int uOffset = 0;
    public int vOffset = 0;
    public int uWidth;
    public int vHeight;
    public int texWidth;
    public int texHeight;
    public AbstractSpriteRenderer(float xRelative, float yRelative, float widthRelative, float heightRelative, ResourceLocation spriteLocation, Screen parentScreen) {
        this.parentScreen = parentScreen;
        this.x = (int) (xRelative * parentScreen.width);
        this.y = (int) (yRelative * parentScreen.height);
        this.width = (int) (widthRelative * parentScreen.width);
        this.height = (int) (heightRelative * parentScreen.height);
        this.uWidth = (int) (widthRelative  * parentScreen.width);
        this.vHeight = (int) (heightRelative * parentScreen.height);
        this.texWidth = (int) (widthRelative * parentScreen.width);
        this.texHeight = (int) (heightRelative * parentScreen.height);
        this.spriteLocation = spriteLocation;
    }

    public abstract void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);
    public AbstractSpriteRenderer setUOffset(int uOffset) {
        this.uOffset = uOffset;
        return this;
    }
    public AbstractSpriteRenderer setVOffset(int vOffset) {
        this.vOffset = vOffset;
        return this;
    }
    public AbstractSpriteRenderer setUWidth(int uWidth) {
        this.uWidth = uWidth;
        return this;
    }
    public AbstractSpriteRenderer setVHeight(int vHeight) {
        this.vHeight = vHeight;
        return this;
    }
    public AbstractSpriteRenderer setTexWidth(int texWidth) {
        this.texWidth = texWidth;
        return this;
    }
    public AbstractSpriteRenderer setTexHeight(int texHeight) {
        this.texHeight = texHeight;
        return this;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public AbstractSpriteRenderer setSpriteLocation(ResourceLocation spriteLocation) {
        this.spriteLocation = spriteLocation;
        return this;
    }
}
