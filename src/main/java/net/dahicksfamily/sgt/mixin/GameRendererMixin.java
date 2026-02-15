package net.dahicksfamily.sgt.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @ModifyConstant(method = "tickFov", constant = @Constant(floatValue = 0.1F))
    private float sgtunclampZoom(float constant) {
        return 0.0001f;
    }
}