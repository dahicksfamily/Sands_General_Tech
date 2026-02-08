package net.dahicksfamily.sgt.SpaceRendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.dahicksfamily.sgt.SpaceRendering.generators.SkyboxCubeGen;
import net.dahicksfamily.sgt.SpaceRendering.renderers.SpaceObjectRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class SpaceRenderer {
    private static VertexBuffer Star_Buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
    private static VertexBuffer Skybox_Buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

    public static void setupBuffers() {
        BufferBuilder bufferbuilder =  Tesselator.getInstance().getBuilder();
        Star_Buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder.RenderedBuffer starsRendered = drawStars(bufferbuilder);
        Star_Buffer.bind();
        Star_Buffer.upload(starsRendered);
        VertexBuffer.unbind();

        Skybox_Buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder.RenderedBuffer skyboxRendered = buildSkyBox(bufferbuilder);
        Skybox_Buffer.bind();
        Skybox_Buffer.upload(skyboxRendered);
        VertexBuffer.unbind();
    }

    public static void renderSkybox(Minecraft mc, PoseStack poseStack, float partialTick, Camera camera)
    {
        double fov = mc.gameRenderer.getFov(camera, partialTick, true);
        Matrix4f projectionMatrix = mc.gameRenderer.getProjectionMatrix(fov);
        FogRenderer.levelFogColor();

        assert mc.level != null;
        if (mc.player.getEyePosition(partialTick).y < mc.level.getMinBuildHeight()) {
            return;
        }

        poseStack.pushPose();

        SpaceObjectRenderer.renderPlanetaryBodies(poseStack, projectionMatrix, partialTick);
        RenderSystem.depthMask(true);
        poseStack.popPose();
    }

    public static void drawStarBuffer(PoseStack poseStack, Matrix4f projectionMatrix, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        Star_Buffer.bind();
        Star_Buffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionColorShader());
        VertexBuffer.unbind();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
    }

    private static BufferBuilder.RenderedBuffer buildSkyBox(BufferBuilder pBuilder) {
        Vector3f[] cubeVertecies = SkyboxCubeGen.getCubeVertexes();
        pBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (Vector3f vertex : cubeVertecies) {
            pBuilder.vertex(vertex.x, vertex.y, vertex.z).color(10, 11, 20, 255).endVertex();
        }

        return pBuilder.end();
    }

    private static BufferBuilder.RenderedBuffer drawStars(BufferBuilder pBuilder) {
        RandomSource randomsource = RandomSource.create(1000L);
        pBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Band tilt angle (in radians) - tilts the galactic plane
        double bandTiltAngle = Math.PI / 6.0; // 30 degrees tilt
        double tiltCos = Math.cos(bandTiltAngle);
        double tiltSin = Math.sin(bandTiltAngle);

        for(int i = 0; i < 7000; ++i) {
            double d0, d1, d2;

            // 70% of stars form a galactic band, 30% are scattered
            if (randomsource.nextFloat() < 0.7f) {
                // Band distribution - concentrated around galactic plane
                double bandAngle = randomsource.nextDouble() * Math.PI * 2.0; // Around the sky
                double bandWidth = randomsource.nextGaussian() * 0.08; // Thinner band (was 0.15)
                double bandRadius = 0.3 + randomsource.nextDouble() * 0.7; // Distance from center

                // Create position in band's local coordinates
                double localX = Math.cos(bandAngle) * bandRadius;
                double localY = bandWidth;
                double localZ = Math.sin(bandAngle) * bandRadius;

                // Apply tilt rotation around the X-axis
                d0 = localX;
                d1 = localY * tiltCos - localZ * tiltSin;
                d2 = localY * tiltSin + localZ * tiltCos;
            } else {
                // Scattered background stars
                d0 = (double)(randomsource.nextFloat() * 2.0F - 1.0F);
                d1 = (double)(randomsource.nextFloat() * 2.0F - 1.0F);
                d2 = (double)(randomsource.nextFloat() * 2.0F - 1.0F);
            }

            double d3 = (double)(0.15F + randomsource.nextFloat() * 0.1F);
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d4 < 1.0D && d4 > 0.01D) {
                d4 = 1.0D / Math.sqrt(d4);
                d0 *= d4;
                d1 *= d4;
                d2 *= d4;
                double d5 = d0 * 100.0D;
                double d6 = d1 * 100.0D;
                double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = randomsource.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                // Stellar classification based on Hertzsprung-Russell diagram
                float starType = randomsource.nextFloat();
                float redness, greenness, blueness, brightness;

                if (starType < 0.01f) {
                    // O-type: Very hot blue stars (rare, very bright)
                    redness = 0.6f;
                    greenness = 0.7f;
                    blueness = 1.0f;
                    brightness = 0.8f + randomsource.nextFloat() * 0.2f;
                } else if (starType < 0.05f) {
                    // B-type: Hot blue-white stars (rare, bright)
                    redness = 0.7f;
                    greenness = 0.8f;
                    blueness = 1.0f;
                    brightness = 0.6f + randomsource.nextFloat() * 0.3f;
                } else if (starType < 0.15f) {
                    // A-type: White stars (uncommon, bright)
                    redness = 0.9f;
                    greenness = 0.9f;
                    blueness = 1.0f;
                    brightness = 0.5f + randomsource.nextFloat() * 0.3f;
                } else if (starType < 0.35f) {
                    // F-type: Yellow-white stars (common)
                    redness = 1.0f;
                    greenness = 0.95f;
                    blueness = 0.85f;
                    brightness = 0.4f + randomsource.nextFloat() * 0.3f;
                } else if (starType < 0.50f) {
                    // G-type: Yellow stars like our Sun (common)
                    redness = 1.0f;
                    greenness = 0.9f;
                    blueness = 0.7f;
                    brightness = 0.35f + randomsource.nextFloat() * 0.25f;
                } else if (starType < 0.75f) {
                    // K-type: Orange stars (very common, dimmer)
                    redness = 1.0f;
                    greenness = 0.7f;
                    blueness = 0.5f;
                    brightness = 0.2f + randomsource.nextFloat() * 0.25f;
                } else if (starType < 0.98f) {
                    // M-type: Red dwarfs (most common, very dim)
                    redness = 1.0f;
                    greenness = 0.5f;
                    blueness = 0.3f;
                    brightness = 0.1f + randomsource.nextFloat() * 0.2f;
                } else {
                    // Red giants/supergiants (rare, very bright despite being cool)
                    redness = 1.0f;
                    greenness = 0.4f;
                    blueness = 0.2f;
                    brightness = 0.6f + randomsource.nextFloat() * 0.3f;
                    d3 *= 1.5; // Make them appear larger
                }

                for(int j = 0; j < 4; ++j) {
                    double d18 = (double)((j & 2) - 1) * d3;
                    double d19 = (double)((j + 1 & 2) - 1) * d3;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + 0.0D * d13;
                    double d24 = 0.0D * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;

                    pBuilder.vertex(d5 + d25, d6 + d23, d7 + d26)
                            .color(redness, greenness, blueness, brightness)
                            .endVertex();
                }
            }
        }

        return pBuilder.end();
    }
}
