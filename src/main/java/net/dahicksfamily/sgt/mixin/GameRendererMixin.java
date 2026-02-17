package net.dahicksfamily.sgt.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dahicksfamily.sgt.client.LightmapAccess;
import net.dahicksfamily.sgt.client.TrueDarkness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    private Minecraft minecraft;

    @Shadow
    private LightTexture lightTexture;


    @ModifyConstant(method = "tickFov", constant = @Constant(floatValue = 0.1F))
    private float sgtunclampZoom(float constant) {
        return 0.00001f;
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void onRenderLevel(float partialTick, long nanos, PoseStack poseStack, CallbackInfo ci) {
        LightmapAccess lightmap = (LightmapAccess) this.lightTexture;
        if (lightmap.sgt_isDirty()) {
            this.minecraft.getProfiler().push("trueDarkness");
            TrueDarkness.updateLuminance(partialTick, this.minecraft, (GameRenderer)(Object)this, lightmap.sgt_prevFlicker());
            this.minecraft.getProfiler().pop();
        }
    }
}
