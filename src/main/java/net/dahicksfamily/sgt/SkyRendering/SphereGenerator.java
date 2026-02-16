package net.dahicksfamily.sgt.SkyRendering;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class SphereGenerator {

    public static BufferBuilder.@NotNull RenderedBuffer generateSphere(float radius, int segments, int rings) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        for (int lat = 0; lat < rings; lat++) {
            float theta1 = ((float)lat / rings) * (float)Math.PI;
            float theta2 = ((float)(lat + 1) / rings) * (float)Math.PI;

            for (int lon = 0; lon < segments; lon++) {
                float phi1 = ((float)lon / segments) * 2.0f * (float)Math.PI;
                float phi2 = ((float)(lon + 1) / segments) * 2.0f * (float)Math.PI;

                float u1 = (float)lon / segments;
                float u2 = (float)(lon + 1) / segments;
                float v1 = (float)lat / rings;
                float v2 = (float)(lat + 1) / rings;

                addVertexFromAngles(bufferBuilder, radius, theta1, phi1, u1, v1);
                addVertexFromAngles(bufferBuilder, radius, theta1, phi2, u2, v1);
                addVertexFromAngles(bufferBuilder, radius, theta2, phi2, u2, v2);

                addVertexFromAngles(bufferBuilder, radius, theta1, phi1, u1, v1);
                addVertexFromAngles(bufferBuilder, radius, theta2, phi2, u2, v2);
                addVertexFromAngles(bufferBuilder, radius, theta2, phi1, u1, v2);
            }
        }

        return bufferBuilder.end();
    }

    private static void addVertexFromAngles(BufferBuilder builder, float radius, float theta, float phi, float u, float v) {
        float sinTheta = (float)Math.sin(theta);
        float cosTheta = (float)Math.cos(theta);
        float sinPhi = (float)Math.sin(phi);
        float cosPhi = (float)Math.cos(phi);

        float x = radius * sinTheta * cosPhi;
        float y = radius * cosTheta;
        float z = radius * sinTheta * sinPhi;

        float nx = sinTheta * cosPhi;
        float ny = cosTheta;
        float nz = sinTheta * sinPhi;

        builder.vertex(x, y, z)
                .uv(u, v)
                .color(255, 255, 255, 255)
                .normal(nx, ny, nz)
                .endVertex();
    }

    private static void addVertexArray(BufferBuilder builder, float[] v) {
        builder.vertex(v[0], v[1], v[2])
                .uv(v[3], v[4])
                .color(255, 255, 255, 255)
                .normal(v[5], v[6], v[7])
                .endVertex();
    }

    private static void addVertex(BufferBuilder builder, float x, float y, float z, float u, float v, float radius) {
        float nx = x / radius;
        float ny = y / radius;
        float nz = z / radius;

        builder.vertex(x, y, z)
                .uv(u, v)
                .color(255, 255, 255, 255)
                .normal(nx, ny, nz)
                .endVertex();
    }
}