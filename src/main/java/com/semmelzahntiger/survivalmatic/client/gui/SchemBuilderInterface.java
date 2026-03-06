package com.semmelzahntiger.survivalmatic.client.gui;

import com.semmelzahntiger.survivalmatic.client.gui.widgets.ScrollableDropDownList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SchemBuilderInterface extends Screen {

    public SchemBuilderInterface(Component pTitle) {
        super(pTitle);
    }

    @Override
    protected void init() {
        ScrollableDropDownList dropDownList = ScrollableDropDownList.Builder.of(
                0,0, this.width /2, (int) (this.height*0.1), 3, Component.empty())
                .build();
        dropDownList.addTextEntry(Component.literal("Test 1"));
        dropDownList.addTextEntry(Component.literal("Test 2"));
        dropDownList.addTextEntry(Component.literal("Test 3"));
        dropDownList.addTextEntry(Component.literal("Test 4"));
        dropDownList.addTextEntry(Component.literal("Test 5"));
        addRenderableWidget(dropDownList);

    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }
}
