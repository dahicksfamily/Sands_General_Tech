package net.dahicksfamily.sgt.SkyRendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class SkyRenderer {
    public static void RenderSkybox(Minecraft game, PoseStack poseStack, float partialTick, Camera camera, Matrix4f projectionMatrix) {
        poseStack.pushPose();

        SpaceObjectRenderer.renderBodies(poseStack, projectionMatrix, partialTick, camera);

        poseStack.popPose();
    }
}