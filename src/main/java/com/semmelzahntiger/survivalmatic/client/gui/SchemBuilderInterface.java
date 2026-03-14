package com.semmelzahntiger.survivalmatic.client.gui;

import com.semmelzahntiger.survivalmatic.client.gui.widgets.ScrollableDropDownList;
import com.semmelzahntiger.survivalmatic.client.gui.widgets.ScrollableList;
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
                0.05f,0.1f, 0.4f, 0.1f, 3, Component.empty(), this)
                .setEntryWidthRatio(0.7f)
                .setEntryHeightRatio(0.8f)
                // .setListOffset(0.04f)
                .setSelectedMargin(0.02f, 0.02f, 0.01f, 0.01f)
                .build();
        for (int i = 0; i < 5; i++) {
            dropDownList.addTextEntry(Component.literal("Test " + i));
        }
        addRenderableWidget(dropDownList);
        ScrollableList scrollableList = ScrollableList.Builder.of(0.5f, 0.1f, 0.4f, 0.1f, 5, this, Component.empty())
                .setBorderMargins(0.03f, 0.03f, 0.04f, 0.04f)
                .build();
        for (int i = 0; i < 10; i++) {
            scrollableList.addTextEntry(Component.literal("Test " + i));
        }
        addRenderableWidget(scrollableList);
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
