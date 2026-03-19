package com.semmelzahntiger.survivalmatic.client.gui.widgets.base;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public abstract class AbstractVButton<W extends AbstractVButton<W>> extends AbstractVWidget {
    protected final ResourceLocation texture;
    protected final ResourceLocation textureDisabled;
    protected final ResourceLocation hoverTexture;
    protected float hoverLeftMargin;
    protected float hoverTopMargin;
    protected float hoverRightMargin;
    protected float hoverBottomMargin;

    protected final VOnPress<W> onPress;
    public <T extends Builder<W,T>> AbstractVButton(Builder<W,T> builder) {
        super(builder);
        this.onPress = builder.onPress;
        texture = builder.buttonTextureEnabled;
        textureDisabled = builder.buttonTextureDisabled;
        hoverTexture = builder.buttonHoverTexture;
        hoverLeftMargin  = builder.hoverLeftMargin;
        hoverTopMargin  = builder.hoverTopMargin;
        hoverRightMargin  = builder.hoverRightMargin;
        hoverBottomMargin  = builder.hoverBottomMargin;
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
        onPress.onPress((W) this);
    }
    public void renderHoveredEnabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }
    public void renderHoveredDisabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }
    public void renderEnabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(texture,this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0,0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
    }
    public void renderDisabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(textureDisabled,this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0,0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
    }
    public void renderAlways(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }
    @FunctionalInterface
    public interface VOnPress<B extends AbstractVButton<B>> {
        void onPress(B button);
    }

    public abstract static class Builder<W extends AbstractVButton<W>,T extends Builder<W,T>> extends AbstractVWidget.WidgetBuilder<W, T> {
        protected ResourceLocation buttonTextureEnabled = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_full.png");
        protected ResourceLocation buttonTextureDisabled = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_full.png");
        protected ResourceLocation buttonHoverTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_hollow.png");
        protected float hoverLeftMargin = 0;
        protected float hoverRightMargin = 0;
        protected float hoverTopMargin = 0;
        protected float hoverBottomMargin = 0;

        protected VOnPress<W> onPress = button -> {};
        public Builder(float xRelative, float yRelative, float widthRelative, float heightRelative, Screen parentScreen) {
            super(xRelative, yRelative, widthRelative, heightRelative, parentScreen);
        }
        public T setOnPress(VOnPress<W> onPress) {
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
        public T setHoverMargins(float leftMargin, float topMargin, float rightMargin, float bottomMargin) {
            this.hoverLeftMargin = leftMargin;
            this.hoverRightMargin = rightMargin;
            this.hoverTopMargin = topMargin;
            this.hoverBottomMargin = bottomMargin;
            return self();
        }
        @Override
        T self() {
            return (T) this;
        }
    }

}
