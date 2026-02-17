package net.dahicksfamily.sgt.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dahicksfamily.sgt.SkyRendering.CelestialBuffers;
import net.dahicksfamily.sgt.SkyRendering.SkyRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(LevelRenderer.class)
public class LevelRenderingMixin {

    @Shadow @Final private Minecraft minecraft;

    @Shadow
    private boolean doesMobEffectBlockSky(Camera pCamera) {
        return false;
    }

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    public void SGTRenderSky(PoseStack poseStack, Matrix4f projectionMatrixParam, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        ClientLevel level = this.minecraft.level;
        if (level == null) return;

        double fov = minecraft.gameRenderer.getFov(camera, partialTick, true);
        Matrix4f projectionMatrix = minecraft.gameRenderer.getProjectionMatrix(fov);

        skyFogSetup.run();
        FogRenderer.levelFogColor();

        if (!isFoggy) {
            FogType fogtype = camera.getFluidInCamera();
            if (fogtype != FogType.POWDER_SNOW && fogtype != FogType.LAVA && !this.doesMobEffectBlockSky(camera)) {
                SkyRenderer.RenderSkybox(minecraft, poseStack, partialTick, camera, projectionMatrix);
            }
        }

        ci.cancel();
    }
}