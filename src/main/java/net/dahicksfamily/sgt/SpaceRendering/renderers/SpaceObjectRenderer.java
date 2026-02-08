package net.dahicksfamily.sgt.SpaceRendering.renderers;

import com.mojang.blaze3d.vertex.*;
import net.dahicksfamily.sgt.SpaceRendering.SpaceRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;


@OnlyIn(Dist.CLIENT)
public class SpaceObjectRenderer {
    public static void renderPlanetaryBodies(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick) {
        poseStack.pushPose();

        renderPlanets(poseStack, projectionMatrix, partialTick);

        poseStack.popPose();
    }

    public static void renderPlanets(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick) {

        SpaceRenderer.drawStarBuffer(poseStack, projectionMatrix, 0.5f);

    }
}
