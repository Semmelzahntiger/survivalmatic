package com.semmelzahntiger.survivalmatic.helper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

public class RenderHelper {
    public static void blitRelative(GuiGraphics graphics, ResourceLocation texture, float xRelative, float yRelative, float widthRelative, float heightRelative, int uOffset, int vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight, Screen screen) {
        int x = (int) (screen.width * xRelative);
        int y = (int) (screen.height * yRelative);
        int width = (int) (screen.width * widthRelative);
        int height = (int) (screen.height * heightRelative);
        graphics.blit(texture, x,y,width,height,uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
    }
    public static int toAbsoluteX(float relativeX, Screen screen) {
        return (int) (screen.width * relativeX);
    }
    public static int toAbsoluteY(float relativeY, Screen screen) {
        return (int) (screen.height * relativeY);
    }
}
