package net.dahicksfamily.sgt.SkyRendering;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SphereGenerator {
    public static BufferBuilder.RenderedBuffer generateSphere(float radius, int segments, int rings) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        System.out.println("Generating sphere: segments=" + segments + ", rings=" + rings);

        for (int lat = 0; lat < rings; lat++) {
            float theta1 = lat * (float)Math.PI / rings;
            float theta2 = (lat + 1) * (float)Math.PI / rings;

            for (int lon = 0; lon < segments; lon++) {
                float phi1 = lon * 2 * (float)Math.PI / segments;
                float phi2 = (lon + 1) * 2 * (float)Math.PI / segments;

                Vertex v1 = sphereVertex(radius, theta1, phi1);
                Vertex v2 = sphereVertex(radius, theta1, phi2);
                Vertex v3 = sphereVertex(radius, theta2, phi2);
                Vertex v4 = sphereVertex(radius, theta2, phi1);

                addVertex(bufferBuilder, v1);
                addVertex(bufferBuilder, v2);
                addVertex(bufferBuilder, v3);
                addVertex(bufferBuilder, v4);
            }
        }

        return bufferBuilder.end();
    }

    private static Vertex sphereVertex(float radius, float theta, float phi) {
        float x = radius * (float)(Math.sin(theta) * Math.cos(phi));
        float y = radius * (float)(Math.cos(theta));
        float z = radius * (float)(Math.sin(theta) * Math.sin(phi));

        float nx = x / radius;
        float ny = y / radius;
        float nz = z / radius;

        float u = (float)(phi / (2 * Math.PI));
        float v = (float)(theta / Math.PI);

        u = Math.max(0.0f, Math.min(1.0f, u));
        v = Math.max(0.0f, Math.min(1.0f, v));

        return new Vertex(x, y, z, u, v, nx, ny, nz);
    }

    private static void addVertex(BufferBuilder builder, Vertex v) {
        builder.vertex(v.x, v.y, v.z)
                .uv(v.u, v.v)
                .color(255, 255, 255, 255)
                .normal(v.nx, v.ny, v.nz)
                .endVertex();
    }

    private static class Vertex {
        float x, y, z;
        float u, v;
        float nx, ny, nz;

        Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
            this.nx = nx;
            this.ny = ny;
            this.nz = nz;
        }
    }
}
