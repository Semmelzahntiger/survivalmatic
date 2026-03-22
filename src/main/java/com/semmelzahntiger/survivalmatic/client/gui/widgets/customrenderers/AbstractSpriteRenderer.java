package com.semmelzahntiger.survivalmatic.client.gui.widgets.customrenderers;

import com.semmelzahntiger.survivalmatic.client.gui.widgets.base.AbstractVWidget;
import net.minecraft.client.gui.GuiGraphics;

public abstract class AbstractSpriteRenderer {
    public abstract void render(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks);
}
