package net.dahicksfamily.sgt.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.dahicksfamily.sgt.SpaceRendering.SpaceRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@OnlyIn(Dist.CLIENT)
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow
    private VertexBuffer skyBuffer;

    @Shadow
    private boolean doesMobEffectBlockSky(Camera pCamera) {
        return false;
    }

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    public void renderSky(PoseStack pPoseStack, Matrix4f pProjectionMatrix, float pPartialTick, Camera pCamera, boolean pIsFoggy, Runnable pSkyFogSetup, CallbackInfo CallbackInfo) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) {
            return;
        }

        pSkyFogSetup.run();
        if (!pIsFoggy) {
            FogType fogtype = pCamera.getFluidInCamera();
            if (fogtype != FogType.POWDER_SNOW && fogtype != FogType.LAVA && !this.doesMobEffectBlockSky(pCamera)) {
                SpaceRenderer.renderSkybox(mc, pPoseStack, pPartialTick, pCamera);
            }
        }

        CallbackInfo.cancel();
    }
}