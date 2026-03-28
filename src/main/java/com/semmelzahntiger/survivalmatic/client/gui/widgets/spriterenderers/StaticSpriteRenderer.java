package com.semmelzahntiger.survivalmatic.client.gui.widgets.spriterenderers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

public class StaticSpriteRenderer extends AbstractSpriteRenderer {
    public StaticSpriteRenderer(float xRelative, float yRelative, float widthRelative, float heightRelative, ResourceLocation spriteLocation, Screen parentScreen) {
        super(xRelative, yRelative, widthRelative, heightRelative, spriteLocation, parentScreen);
    }
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(spriteLocation, x,y,width, height, uOffset, vOffset, uWidth, vHeight, texWidth, texHeight);
    }
}
