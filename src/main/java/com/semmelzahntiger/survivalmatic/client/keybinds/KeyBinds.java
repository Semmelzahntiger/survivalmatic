package com.semmelzahntiger.survivalmatic.client.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.jarjar.nio.util.Lazy;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class KeyBinds {
    public static final Lazy<KeyMapping> OPEN_SCHEMBUILDER = Lazy.of(() -> new KeyMapping(
            "key.survivalmatic.debug1",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            "key.categories.misc"
    ));


}
