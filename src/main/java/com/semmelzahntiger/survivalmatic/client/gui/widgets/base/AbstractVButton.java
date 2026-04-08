package com.semmelzahntiger.survivalmatic.client.gui.widgets.base;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.semmelzahntiger.survivalmatic.Survivalmatic.MODID;

public abstract class AbstractVButton<W extends AbstractVButton<W>> extends AbstractVWidget {
    protected final ResourceLocation texture;
    protected final ResourceLocation textureDisabled;
    protected final ResourceLocation hoverTextureEnabled;
    protected final ResourceLocation hoverTextureDisabled;
    protected int hoverLeftMargin;
    protected int hoverTopMargin;
    protected int hoverRightMargin;
    protected int hoverBottomMargin;

    protected final VOnPress<W> onPress;
    public <T extends Builder<W,T>> AbstractVButton(Builder<W,T> builder) {
        super(builder);
        this.onPress = builder.onPress;
        texture = builder.buttonTextureEnabled;
        textureDisabled = builder.buttonTextureDisabled;
        hoverTextureEnabled = builder.buttonTextureHoverEnabled;
        hoverTextureDisabled = builder.buttonTextureHoverDisabled;
        hoverLeftMargin  = builder.hoverTextureLeftMargin;
        hoverTopMargin  = builder.hoverTextureTopMargin;
        hoverRightMargin  = builder.hoverTextureRightMargin;
        hoverBottomMargin  = builder.hoverTextureBottomMargin;
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

        }
        else {
            renderHoveredDisabled(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        onPress.onPress((W) this);
    }

    public void renderHoveredEnabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(hoverTextureEnabled,this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0,0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
    }
    public void renderHoveredDisabled(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(hoverTextureDisabled,this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0,0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
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
        public ResourceLocation buttonTextureEnabled = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_full");
        public int buttonTextureWidth = 256;
        public int buttonTextureHeight = 256;
        public ResourceLocation buttonTextureDisabled = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_full.png");
        public int buttonTextureWidthDisabled = 256;
        public int buttonTextureHeightDisabled = 256;
        public ResourceLocation buttonTextureHoverEnabled = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_hollow.png");
        public int buttonTextureWidthHover = 256;
        public int buttonTextureHeightHover = 256;
        public ResourceLocation buttonTextureHoverDisabled = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dummy_hollow.pint");
        public int buttonTextureWidthHoverDisabled = 256;
        public int buttonTextureHeightHoverDisabled = 256;

        public int hoverTextureLeftMargin = 0;
        public int hoverTextureRightMargin = 0;
        public int hoverTextureTopMargin = 0;
        public int hoverTextureBottomMargin = 0;

        protected VOnPress<W> onPress = button -> {};
        protected Builder(float xRelative, float yRelative, int width, int height, Screen parentScreen) {
            super(xRelative, yRelative, width, height, parentScreen);
        }
        protected Builder(int x, int y, int width, int height, Screen parentScreen) {
            super(x, y, width, height, parentScreen);
        }
        public T setOnPress(VOnPress<W> onPress) {
            this.onPress = onPress;
            return self();
        }
        public T setEnabledTexture(ResourceLocation textureEnabled, int textureWidth, int textureHeight) {
            this.buttonTextureEnabled = textureEnabled;
            this.buttonTextureWidth = textureWidth;
            this.buttonTextureHeight = textureHeight;
            return self();
        }
        public T setDisabledTexture(ResourceLocation textureDisabled, int textureWidth, int textureHeight) {
            this.buttonTextureDisabled = textureDisabled;
            this.buttonTextureWidthDisabled = textureWidth;
            this.buttonTextureHeightDisabled = textureHeight;
            return self();
        }
        public T setHoverTextureEnabled(ResourceLocation texture, int textureWidth, int textureHeight) {
            this.buttonTextureHoverEnabled = texture;
            this.buttonTextureWidthHover = textureWidth;
            this.buttonTextureHeightHover = textureHeight;
            return self();
        }
        public T setHoverTextureDisabled(ResourceLocation textureDisabled, int textureWidth, int textureHeight) {
            this.buttonTextureHoverDisabled = textureDisabled;
            this.buttonTextureWidthHoverDisabled = textureWidth;
            this.buttonTextureHeightHoverDisabled = textureHeight;
            return self();
        }
        public T setHoverMargins(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
            this.hoverTextureLeftMargin = leftMargin;
            this.hoverTextureRightMargin = rightMargin;
            this.hoverTextureTopMargin = topMargin;
            this.hoverTextureBottomMargin = bottomMargin;
            return self();
        }
        @Override
        T self() {
            return (T) this;
        }
    }

}
