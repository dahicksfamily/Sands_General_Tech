package net.dahicksfamily.sgt.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class ModKeyBindings {

    public static final String KEY_CATEGORY = "key.categories.sgt";

    public static final KeyMapping TOGGLE_PLANET_LABELS = new KeyMapping(
            "key.sgt.toggle_planet_labels",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_PERIOD,
            KEY_CATEGORY
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_PLANET_LABELS);
    }
}