package net.dahicksfamily.sgt.SkyRendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class CelestialBuffers {
    private static VertexBuffer testBuffer;
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;

        testBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float size = 1000f;
        bufferBuilder.vertex(-size, -size, -100).color(1f, 0f, 0f, 1f).endVertex();
        bufferBuilder.vertex(size, -size, -100).color(1f, 0f, 0f, 1f).endVertex();
        bufferBuilder.vertex(size, size, -100).color(1f, 0f, 0f, 1f).endVertex();
        bufferBuilder.vertex(-size, size, -100).color(1f, 0f, 0f, 1f).endVertex();

        testBuffer.bind();
        testBuffer.upload(bufferBuilder.end());
        VertexBuffer.unbind();

        initialized = true;
    }

    public static void renderTest(PoseStack poseStack, Matrix4f projectionMatrix) {
        if (!initialized) {
            initialize();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (testBuffer != null) {
            testBuffer.bind();
            assert GameRenderer.getPositionColorShader() != null;
            testBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionColorShader());
            VertexBuffer.unbind();
        }

    }

    public static void cleanup() {
        if (testBuffer != null) {
            testBuffer.close();
        }
        initialized = false;
    }
}