package com.semmelzahntiger.survivalmatic.client.gui.widgets.spriterenderers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

public class AnimatedSpriteRenderer extends AbstractSpriteRenderer {
    public AnimatedSpriteRenderer(float xRelative, float yRelative, float widthRelative, float heightRelative, ResourceLocation spriteLocation, Screen parentScreen) {
        super(xRelative, yRelative, widthRelative, heightRelative, spriteLocation, parentScreen);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

    }
}
