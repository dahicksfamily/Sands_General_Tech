package net.dahicksfamily.sgt.SkyRendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.dahicksfamily.sgt.client.ModShaders;
import net.dahicksfamily.sgt.space.Star;
import net.dahicksfamily.sgt.space.atmosphere.Atmosphere;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SkyboxRenderer {

    private static final float SKY_DOME_RADIUS  = 92.0f;
    private static final float SUN_ANGULAR_SCALE = 2.0f;

    public static final float TINT_EARTH  = 0.65f;
    public static final float TINT_ALIEN  = 0.05f;
    public static final float TINT_THIN   = 0.15f;

    private static VertexBuffer skyDomeVbo;
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;
        skyDomeVbo = buildDome(SKY_DOME_RADIUS, 48, 24);
        initialized = true;
    }

    public static void cleanup() {
        if (skyDomeVbo != null) { skyDomeVbo.close(); skyDomeVbo = null; }
        initialized = false;
    }

    public static void renderPlanetSky(PoseStack poseStack, Matrix4f projMat,
                                       Atmosphere atmo,
                                       Vec3 sunDir, Star star, double distToStarKm,
                                       Vec3 vanillaSkyColor, float vanillaTintStrength) {
        initialize();
        ShaderInstance shader = ModShaders.getPlanetSkyboxShader();
        if (shader == null) return;

        Vec3  sunColor = star != null ? star.getColor() : new Vec3(1,1,1);
        float starRad  = star != null ? (float) star.radius : 696_000.0f;

        float sunAngRad = distToStarKm > 0
                ? (float) Math.atan(starRad / distToStarKm) * SUN_ANGULAR_SCALE
                : 0.01f;

        double lum    = star != null ? star.luminosity : 1.0;
        double distAU = distToStarKm / 149_597_870.7;
        float  exposure = (float) Math.sqrt(lum / Math.max(distAU * distAU, 0.01));
        exposure *= (1.0f / Math.max(1.0f, atmo.surfaceDensity * 0.4f));
        exposure  = Math.max(0.1f, Math.min(exposure, 3.5f));

        Vec3 tint = (vanillaSkyColor != null && vanillaSkyColor.lengthSqr() > 0.001)
                ? vanillaSkyColor : new Vec3(1,1,1);

        ModShaders.setPlanetSkyboxUniforms(
                new Vector3f((float) sunDir.x, (float) sunDir.y, (float) sunDir.z),
                new Vector3f((float) sunColor.x, (float) sunColor.y, (float) sunColor.z),
                sunAngRad,
                new Vector3f((float) atmo.rayleighCoeff.x,
                        (float) atmo.rayleighCoeff.y, (float) atmo.rayleighCoeff.z),
                new Vector3f((float) atmo.mieCoeff.x,
                        (float) atmo.mieCoeff.y, (float) atmo.mieCoeff.z),
                atmo.mieDensity, atmo.mieAnisotropy,
                atmo.surfaceDensity, atmo.scaleHeight,
                new Vector3f((float) atmo.airglowColor.x,
                        (float) atmo.airglowColor.y, (float) atmo.airglowColor.z),
                atmo.airglowIntensity,
                new Vector3f((float) atmo.terminatorBandColor.x,
                        (float) atmo.terminatorBandColor.y, (float) atmo.terminatorBandColor.z),
                atmo.terminatorBandIntensity,
                exposure,
                new Vector3f((float) tint.x, (float) tint.y, (float) tint.z),
                vanillaTintStrength
        );

        glAtmoBlend();
        poseStack.pushPose();
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderColor(1,1,1,1);
        skyDomeVbo.bind();
        skyDomeVbo.drawWithShader(poseStack.last().pose(), projMat, shader);
        VertexBuffer.unbind();
        poseStack.popPose();
        glRestore();
    }

    public static void renderSpaceSky(PoseStack poseStack, Matrix4f projMat) {
        initialize();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.disableBlend();

        var plain = net.minecraft.client.renderer.GameRenderer.getPositionColorShader();
        if (plain != null) {
            poseStack.pushPose();
            RenderSystem.setShader(() -> plain);
            RenderSystem.setShaderColor(0,0,0,0);
            skyDomeVbo.bind();
            skyDomeVbo.drawWithShader(poseStack.last().pose(), projMat, plain);
            VertexBuffer.unbind();
            poseStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1,1,1,1);
    }

    private static void glAtmoBlend() {
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    private static void glRestore() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1,1,1,1);
    }

    private static VertexBuffer buildDome(float radius, int hSegs, int vSegs) {
        int vertCount   = hSegs * vSegs * 6;
        BufferBuilder bb = new BufferBuilder(vertCount * 12 + 64);
        bb.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);

        for (int v = 0; v < vSegs; v++) {
            float phi0 = (float) v      / vSegs * (float) Math.PI;
            float phi1 = (float)(v + 1) / vSegs * (float) Math.PI;
            for (int h = 0; h < hSegs; h++) {
                float theta0 = (float) h      / hSegs * (float)(Math.PI * 2);
                float theta1 = (float)(h + 1) / hSegs * (float)(Math.PI * 2);
                float[] p00 = sph(radius, phi0, theta0), p10 = sph(radius, phi1, theta0);
                float[] p01 = sph(radius, phi0, theta1), p11 = sph(radius, phi1, theta1);
                addP(bb, p00); addP(bb, p11); addP(bb, p01);
                addP(bb, p00); addP(bb, p10); addP(bb, p11);
            }
        }

        VertexBuffer vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        vbo.bind(); vbo.upload(bb.end()); VertexBuffer.unbind();
        return vbo;
    }

    private static float[] sph(float r, float phi, float theta) {
        float sp = (float) Math.sin(phi);
        return new float[]{ r*sp*(float)Math.cos(theta), r*(float)Math.cos(phi), r*sp*(float)Math.sin(theta) };
    }

    private static void addP(BufferBuilder bb, float[] p) {
        bb.vertex(p[0], p[1], p[2]).endVertex();
    }
}