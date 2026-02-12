package net.dahicksfamily.sgt.SkyRendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkyRenderer {
    public static void RenderSkybox(Minecraft game, PoseStack poseStack, float partialTick, Camera camera) {
        double fov = game.gameRenderer.getFov(camera, partialTick, true);

        RenderSystem.depthMask(false);

        poseStack.pushPose();
        RenderSystem.depthMask(true);
        poseStack.popPose();
    }
}
