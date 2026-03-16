package com.semmelzahntiger.survivalmatic.client.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public abstract class AbstractVButton extends AbstractVWidget {
    protected final ResourceLocation texture;
    protected final ResourceLocation textureDisabled;
    protected final ResourceLocation hoverTexture;
    protected final VOnPress onPress;
    public <T extends ButtonBuilder<T>> AbstractVButton(ButtonBuilder<T> builder) {
        super(builder);
        this.onPress = builder.onPress;
        texture = builder.buttonTextureEnabled;
        textureDisabled = builder.buttonTextureDisabled;
        hoverTexture = builder.buttonHoverTexture;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(isActive()) {
            renderEnabled(guiGraphics, mouseX, mouseY, partialTick);
        }
        else {
            renderDisabled(guiGraphics, mouseX, mouseY, partialTick);
        }
        renderAlways(guiGraphics, mouseX, mouseY, partialTick);
        if(isActive() && isHovered()) {
            renderHoveredEnabled(guiGraphics, mouseX, mouseY, partialTick);
        }
        else if(isActive() && !isHovered()) {
            renderHoveredDisabled(guiGraphics, mouseX, mouseY, partialTick);
        }


    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        onPress.onPress(this);
    }
    public abstract void renderHoveredEnabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);
    public abstract void renderHoveredDisabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);
    public void renderEnabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(texture,this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0,0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
    }
    public void renderDisabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(textureDisabled,this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0,0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
    }
    public void renderAlways(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }
    @FunctionalInterface
    public interface VOnPress {
        void onPress(AbstractVButton button);
    }

    public static class ButtonBuilder<T extends ButtonBuilder<T>> extends AbstractVWidget.WidgetBuilder<ButtonBuilder<T>> {
        protected ResourceLocation buttonTextureEnabled = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_full.png");
        protected ResourceLocation buttonTextureDisabled = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_full.png");
        protected ResourceLocation buttonHoverTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_hollow.png");

        protected VOnPress onPress = button -> {};
        public ButtonBuilder(float xRelative, float yRelative, float widthRelative, float heightRelative, Screen parentScreen) {
            super(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }
        public T setOnPress(VOnPress onPress) {
            this.onPress = onPress;
            return self();
        }
        public T setEnabledTexture(ResourceLocation textureEnabled) {
            this.buttonTextureEnabled = textureEnabled;
            return self();
        }
        public T setDisabledTexture(ResourceLocation textureDisabled) {
            this.buttonTextureDisabled = textureDisabled;
            return self();
        }
        public T setHoverTexture(ResourceLocation texture) {
            this.buttonHoverTexture = texture;
            return self();
        }
        @Override
        T self() {
            return (T) this;
        }
    }

}
