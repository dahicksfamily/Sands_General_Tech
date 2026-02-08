package net.dahicksfamily.sgt.SpaceRendering.generators;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class SkyboxCubeGen {
    private static final float size = 1.0f;

    private static final Vector3f p0 = new Vector3f(-size / 2, -size / 2, -size / 2);
    private static final Vector3f p1 = new Vector3f(size / 2, -size / 2, -size / 2);
    private static final Vector3f p2 = new Vector3f(size / 2, size / 2, -size / 2);
    private static final Vector3f p3 = new Vector3f(-size / 2, size / 2, -size / 2);
    private static final Vector3f p4 = new Vector3f(-size / 2, -size / 2, size / 2);
    private static final Vector3f p5 = new Vector3f(size / 2, -size / 2, size / 2);
    private static final Vector3f p6 = new Vector3f(size / 2, size / 2, size / 2);
    private static final Vector3f p7 = new Vector3f(-size / 2, size / 2, size / 2);

    public static Vector3f[] getCubeVertexes() {
        Vector3f[] vertices = new Vector3f[24];

        vertices[0] = p0;
        vertices[1] = p1;
        vertices[2] = p2;
        vertices[3] = p3;

        vertices[4] = p7; // Bottom-left
        vertices[5] = p6; // Bottom-right
        vertices[6] = p5; // Top-right
        vertices[7] = p4; // Top-left

        vertices[8] = p0;
        vertices[9] = p3;
        vertices[10] = p7;
        vertices[11] = p4;

        vertices[12] = p1;
        vertices[13] = p5;
        vertices[14] = p6;
        vertices[15] = p2;

        vertices[16] = p0;
        vertices[17] = p4;
        vertices[18] = p5;
        vertices[19] = p1;

        vertices[20] = p3;
        vertices[21] = p2;
        vertices[22] = p6;
        vertices[23] = p7;
        return vertices;
    }
}
