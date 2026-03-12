package net.dahicksfamily.sgt.SkyRendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dahicksfamily.sgt.space.*;
import net.dahicksfamily.sgt.space.atmosphere.Atmosphere;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class SkyRenderer {
    private static Vec3 latestSkyColor = new Vec3(1, 1, 1);

    public static void RenderSkybox(Minecraft game, PoseStack poseStack,
                                    float partialTick, Camera camera,
                                    Matrix4f projectionMatrix) {
        if (game.level == null) return;

        ResourceKey<Level> dim      = game.level.dimension();
        SolarSystem        sol      = SolarSystem.getInstance();
        CelestialBody      observer = sol.getBodyAtDimension(dim);
        boolean            onPlanet = observer != null && observer.hasAtmosphere();

        if (!onPlanet) {
            RenderSystem.clearColor(0f, 0f, 0f, 0f);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
            SkyboxRenderer.renderSpaceSky(poseStack, projectionMatrix);

            poseStack.pushPose();
            SpaceObjectRenderer.renderBodies(poseStack, projectionMatrix, partialTick, camera);
            poseStack.popPose();
            return;
        }

        poseStack.pushPose();
        SpaceObjectRenderer.renderBodies(poseStack, projectionMatrix, partialTick, camera);
        poseStack.popPose();

        Vec3 vanillaColor = game.level.getSkyColor(camera.getPosition(), partialTick);
        if (vanillaColor != null && vanillaColor.lengthSqr() > 0.0) {
            latestSkyColor = vanillaColor;
        }

        Atmosphere atmo = observer.atmosphere;
        float tintStrength;
        if      (atmo.surfaceDensity > 5.0f) tintStrength = SkyboxRenderer.TINT_ALIEN;
        else if (atmo.surfaceDensity < 0.05f) tintStrength = SkyboxRenderer.TINT_ALIEN;
        else if (atmo.mieDensity > 0.5f)      tintStrength = SkyboxRenderer.TINT_ALIEN;
        else                                  tintStrength = SkyboxRenderer.TINT_EARTH;

        Vec3   sunDir    = SpaceObjectRenderer.getSunDirectionForObserver(observer);
        Star   lightSrc  = sol.getPrimaryLightSource(observer);
        Vec3   obsPos    = SpaceObjectRenderer.getObserverAbsPos(observer);
        Vec3   starPos   = SpaceObjectRenderer.getStarAbsPos(lightSrc);
        double distKm    = (starPos != null && obsPos != null)
                ? starPos.subtract(obsPos).length()
                : 149_597_870.7;

        SkyboxRenderer.renderPlanetSky(poseStack, projectionMatrix,
                atmo, sunDir, lightSrc, distKm,
                latestSkyColor, tintStrength);
    }
}