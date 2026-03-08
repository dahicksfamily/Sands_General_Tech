package net.dahicksfamily.sgt.background;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.dahicksfamily.sgt.client.ModShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class StarFieldRenderer {

    private static final float STAR_DIST    = 500f;
    private static final float SIZE_SCALE   = 0.28f;
 
    private static final float EXOTIC_SCALE = 0.22f;

    private static StarFieldData data;

 
    private static VertexBuffer simpleStarVBO;
 
    private static final List<VertexBuffer> exoticVBOs   = new ArrayList<>();
 
    private static final List<VertexBuffer> nebulaVBOs   = new ArrayList<>();
 
    private static final List<int[]>        nebulaIndex  = new ArrayList<>(); 
 
    private static final List<VertexBuffer> galaxyVBOs   = new ArrayList<>();

    private static boolean initialized = false;

 

    public static void initialize(long seed) {
        cleanup();
        data = StarFieldGenerator.generate(seed);

        simpleStarVBO = buildSimpleStarVBO(data.simpleStars);

 
        for (BackgroundObject o : data.exoticObjects) {
            exoticVBOs.add(buildSingleQuad(o, EXOTIC_SCALE, 0f));
        }

 
        for (int ni = 0; ni < data.nebulae.size(); ni++) {
            BackgroundObject o = data.nebulae.get(ni);
            for (int l = 0; l < o.nebulaLayers; l++) {
                float layerSize = o.size * o.nebulaLayerSize[l];
                float rot       = o.nebulaLayerRot[l];
                float spread    = o.nebulaSpread * o.size * 0.5f / STAR_DIST;
                float ox = (float)Math.cos(rot) * spread;
                float oy = (float)Math.sin(rot) * spread;
                BackgroundObject shifted = shiftedCopy(o, ox, oy, layerSize);
                nebulaVBOs.add(buildSingleQuad(shifted, 1.0f, rot));
                nebulaIndex.add(new int[]{ni, l});
            }
        }

 
        for (BackgroundObject o : data.galaxies) {
            galaxyVBOs.add(buildGalaxyQuad(o));
        }

        initialized = true;
    }

    public static void cleanup() {
        if (simpleStarVBO != null) { simpleStarVBO.close(); simpleStarVBO = null; }
        exoticVBOs.forEach(VertexBuffer::close);   exoticVBOs.clear();
        nebulaVBOs.forEach(VertexBuffer::close);   nebulaVBOs.clear();
        galaxyVBOs.forEach(VertexBuffer::close);   galaxyVBOs.clear();
        nebulaIndex.clear();
        initialized = false;
    }

 

    public static void render(PoseStack poseStack, Matrix4f proj, float partialTick) {
        if (!initialized) initialize(12345L);
        if (data == null) return;

        long  gameTick = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.getGameTime() : 0;
        float time     = (gameTick + partialTick) * 0.05f;

        if (Minecraft.getInstance().level != null)
            SupernovaManager.tick(gameTick, data.seed);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

 
 
 
        renderGalaxies  (poseStack, proj);
        renderNebulae   (poseStack, proj);
        renderSimple    (poseStack, proj, time);
        renderExotics   (poseStack, proj, time);
        renderSupernovae(poseStack, proj, gameTick, time);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1,1,1,1);
    }

 
 
 
 

    private static void renderSimple(PoseStack poseStack, Matrix4f proj, float time) {
        ShaderInstance sh = ModShaders.getStarBillboardShader();
        if (sh == null || simpleStarVBO == null) return;

 
 
 
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE);
        RenderSystem.setShaderColor(1,1,1,1);

        ModShaders.setStarBillboardTime(time);
        RenderSystem.setShader(() -> sh);

        simpleStarVBO.bind();
        simpleStarVBO.drawWithShader(poseStack.last().pose(), proj, sh);
        VertexBuffer.unbind();
    }

 

    private static void renderExotics(PoseStack poseStack, Matrix4f proj, float time) {
        for (int i = 0; i < data.exoticObjects.size(); i++) {
            BackgroundObject o = data.exoticObjects.get(i);
            VertexBuffer vbo   = exoticVBOs.get(i);
            if (o.type == BackgroundObject.Type.BLACK_HOLE) {
                renderBlackHole(poseStack, proj, o, vbo, time);
            } else if (o.type == BackgroundObject.Type.QUASAR) {
                renderQuasar   (poseStack, proj, o, vbo, time);
            }
        }
    }

    private static void renderBlackHole(PoseStack poseStack, Matrix4f proj,
                                        BackgroundObject o, VertexBuffer vbo, float time) {
        ShaderInstance sh = ModShaders.getBlackHoleShader();
        if (sh == null) return;
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        ModShaders.setBlackHoleUniforms(time, o.variablePhase);
        RenderSystem.setShader(() -> sh);
        RenderSystem.setShaderColor(o.r, o.g, o.b, o.brightness);
        vbo.bind();
        vbo.drawWithShader(poseStack.last().pose(), proj, sh);
        VertexBuffer.unbind();
    }

    private static void renderQuasar(PoseStack poseStack, Matrix4f proj,
                                     BackgroundObject o, VertexBuffer vbo, float time) {
        ShaderInstance sh = ModShaders.getQuasarShader();
        if (sh == null) return;
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE);
        ModShaders.setQuasarUniforms(time, o.jetAngle, o.pulsePeriod,
                o.variableAmplitude, o.variablePhase);
        RenderSystem.setShader(() -> sh);
        RenderSystem.setShaderColor(o.r, o.g, o.b, o.brightness);
        vbo.bind();
        vbo.drawWithShader(poseStack.last().pose(), proj, sh);
        VertexBuffer.unbind();
    }

 

    private static void renderNebulae(PoseStack poseStack, Matrix4f proj) {
        ShaderInstance sh = ModShaders.getNebulaShader();
        if (sh == null) return;
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE);

        for (int vi = 0; vi < nebulaVBOs.size(); vi++) {
            int[]           idx = nebulaIndex.get(vi);
            BackgroundObject o  = data.nebulae.get(idx[0]);
            int              l  = idx[1];
            float alpha = o.brightness * (0.4f + 0.3f * (float)Math.sin(l * 1.3f + 0.5f));
            ModShaders.setNebulaUniforms(o.nebulaLayerR[l], o.nebulaLayerG[l],
                    o.nebulaLayerB[l], alpha, o.nebulaLayerRot[l]);
            RenderSystem.setShader(() -> sh);
            nebulaVBOs.get(vi).bind();
            nebulaVBOs.get(vi).drawWithShader(poseStack.last().pose(), proj, sh);
            VertexBuffer.unbind();
        }
    }

 

    private static void renderGalaxies(PoseStack poseStack, Matrix4f proj) {
        ShaderInstance sh = ModShaders.getGalaxyShader();
        if (sh == null) return;
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE);

        for (int i = 0; i < data.galaxies.size(); i++) {
            BackgroundObject o = data.galaxies.get(i);
            ModShaders.setGalaxyUniforms(o.r, o.g, o.b, o.brightness, o.galaxyAspect);
            RenderSystem.setShader(() -> sh);
            galaxyVBOs.get(i).bind();
            galaxyVBOs.get(i).drawWithShader(poseStack.last().pose(), proj, sh);
            VertexBuffer.unbind();
        }
    }

 

    private static void renderSupernovae(PoseStack poseStack, Matrix4f proj,
                                         long gameTick, float time) {
        ShaderInstance sh = ModShaders.getStarBillboardShader();
        if (sh == null) return;
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE);

        for (SupernovaEvent sn : SupernovaManager.getActive()) {
            float progress = sn.progress(gameTick);
            float bright   = sn.brightness(gameTick);
            float cr = 1.0f;
            float cg = Math.max(0.0f, 1.0f - progress * 1.5f);
            float cb = Math.max(0.0f, 1.0f - progress * 3.0f);

            BackgroundObject tmp = new BackgroundObject();
            tmp.x = sn.x; tmp.y = sn.y; tmp.z = sn.z;
            tmp.size              = sn.peakSize * (0.5f + progress * 1.5f);
            tmp.brightness        = bright;
            tmp.variableAmplitude = 0.08f;
            tmp.variablePhase     = 0f;

            VertexBuffer quad = buildSingleQuad(tmp, 1.0f, 0f);
            ModShaders.setStarBillboardTime(time);
            RenderSystem.setShader(() -> sh);
            RenderSystem.setShaderColor(cr, cg, cb, bright);
            quad.bind();
            quad.drawWithShader(poseStack.last().pose(), proj, sh);
            VertexBuffer.unbind();
            quad.close(); 
        }
    }

 

    private static VertexBuffer buildSimpleStarVBO(List<BackgroundObject> stars) {
        BufferBuilder bb = new BufferBuilder(stars.size() * 6 * 40);
        bb.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        for (BackgroundObject o : stars) appendQuad(bb, o, SIZE_SCALE, 0f);
        BufferBuilder.RenderedBuffer rendered = bb.end();
        VertexBuffer vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        vbo.bind(); vbo.upload(rendered); VertexBuffer.unbind();
        return vbo;
    }

    private static VertexBuffer buildSingleQuad(BackgroundObject o,
                                                float sizeScale, float rot) {
        BufferBuilder bb = new BufferBuilder(6 * 40);
        bb.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        appendQuad(bb, o, sizeScale, rot);
        BufferBuilder.RenderedBuffer rendered = bb.end();
        VertexBuffer vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        vbo.bind(); vbo.upload(rendered); VertexBuffer.unbind();
        return vbo;
    }

    private static VertexBuffer buildGalaxyQuad(BackgroundObject o) {
        BufferBuilder bb = new BufferBuilder(6 * 40);
        bb.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        float cx = o.x * STAR_DIST, cy = o.y * STAR_DIST, cz = o.z * STAR_DIST;
        float s  = SIZE_SCALE * o.size;
        float[] R = perp(o.x, o.y, o.z);
        float[] U = cross(R, new float[]{o.x, o.y, o.z});
        normalize3(U);

        float cos = (float)Math.cos(o.galaxyRotAngle);
        float sin = (float)Math.sin(o.galaxyRotAngle);
        float[] Rr = {R[0]*cos-U[0]*sin, R[1]*cos-U[1]*sin, R[2]*cos-U[2]*sin};
        float[] Ur = {R[0]*sin+U[0]*cos, R[1]*sin+U[1]*cos, R[2]*sin+U[2]*cos};

        float sw = s, sh = s * o.galaxyAspect;
 
        float spiralFlag = o.variableAmplitude > 0.5f ? 1.0f : -1.0f;

        float[][] corners = {
                {cx-sw*Rr[0]-sh*Ur[0], cy-sw*Rr[1]-sh*Ur[1], cz-sw*Rr[2]-sh*Ur[2]},
                {cx+sw*Rr[0]-sh*Ur[0], cy+sw*Rr[1]-sh*Ur[1], cz+sw*Rr[2]-sh*Ur[2]},
                {cx+sw*Rr[0]+sh*Ur[0], cy+sw*Rr[1]+sh*Ur[1], cz+sw*Rr[2]+sh*Ur[2]},
                {cx-sw*Rr[0]+sh*Ur[0], cy-sw*Rr[1]+sh*Ur[1], cz-sw*Rr[2]+sh*Ur[2]}
        };
        float[][] uvs  = {{0,0},{1,0},{1,1},{0,1}};
        int[][] tris   = {{0,1,2},{0,2,3}};

        for (int[] tri : tris) for (int vi : tri)
            bb.vertex(corners[vi][0], corners[vi][1], corners[vi][2])
                    .uv(uvs[vi][0], uvs[vi][1])
                    .color(o.r, o.g, o.b, o.brightness)
                    .normal(o.variablePhase*2f-1f, o.galaxyAspect*2f-1f, spiralFlag)
                    .endVertex();

        BufferBuilder.RenderedBuffer rendered = bb.end();
        VertexBuffer vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        vbo.bind(); vbo.upload(rendered); VertexBuffer.unbind();
        return vbo;
    }

    private static void appendQuad(BufferBuilder bb, BackgroundObject o,
                                   float sizeScale, float rot) {
        float cx = o.x * STAR_DIST, cy = o.y * STAR_DIST, cz = o.z * STAR_DIST;
        float s  = sizeScale * o.size;
        float[] R = perp(o.x, o.y, o.z);
        float[] U = cross(R, new float[]{o.x, o.y, o.z});
        normalize3(U);

        if (rot != 0f) {
            float cos = (float)Math.cos(rot), sin = (float)Math.sin(rot);
            float[] Rr = {R[0]*cos-U[0]*sin, R[1]*cos-U[1]*sin, R[2]*cos-U[2]*sin};
            float[] Ur = {R[0]*sin+U[0]*cos, R[1]*sin+U[1]*cos, R[2]*sin+U[2]*cos};
            R = Rr; U = Ur;
        }

        float[][] corners = {
                {cx-s*(R[0]+U[0]), cy-s*(R[1]+U[1]), cz-s*(R[2]+U[2])},
                {cx+s*(R[0]-U[0]), cy+s*(R[1]-U[1]), cz+s*(R[2]-U[2])},
                {cx+s*(R[0]+U[0]), cy+s*(R[1]+U[1]), cz+s*(R[2]+U[2])},
                {cx-s*(R[0]-U[0]), cy-s*(R[1]-U[1]), cz-s*(R[2]-U[2])}
        };
        float[][] uvs = {{0,0},{1,0},{1,1},{0,1}};
        int[][] tris  = {{0,1,2},{0,2,3}};

        float nx = o.variablePhase     * 2f - 1f;
        float ny = o.variableAmplitude * 2f - 1f;
        float nz = o.variablePeriod > 0
                ? Math.min(1f, o.variablePeriod / 500f) * 2f - 1f
                : -1f;

        for (int[] tri : tris) for (int vi : tri)
            bb.vertex(corners[vi][0], corners[vi][1], corners[vi][2])
                    .uv(uvs[vi][0], uvs[vi][1])
                    .color(o.r, o.g, o.b, o.brightness)
                    .normal(nx, ny, nz)
                    .endVertex();
    }

 

    private static BackgroundObject shiftedCopy(BackgroundObject src,
                                                float ox, float oy, float size) {
        BackgroundObject o = new BackgroundObject();
        o.x = src.x + ox; o.y = src.y + oy; o.z = src.z;
        float len = (float)Math.sqrt(o.x*o.x+o.y*o.y+o.z*o.z);
        if (len > 1e-6f) { o.x/=len; o.y/=len; o.z/=len; }
        o.size              = size;
        o.brightness        = src.brightness;
        o.variableAmplitude = src.variableAmplitude;
        o.variablePhase     = src.variablePhase;
        return o;
    }

    private static float[] perp(float x, float y, float z) {
        float[] ref = (Math.abs(y) < 0.9f) ? new float[]{0,1,0} : new float[]{1,0,0};
        float[] r   = cross(new float[]{x,y,z}, ref);
        normalize3(r);
        return r;
    }

    private static float[] cross(float[] a, float[] b) {
        return new float[]{
                a[1]*b[2]-a[2]*b[1],
                a[2]*b[0]-a[0]*b[2],
                a[0]*b[1]-a[1]*b[0]
        };
    }

    private static void normalize3(float[] v) {
        float len = (float)Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
        if (len < 1e-6f) { v[0]=0;v[1]=1;v[2]=0; return; }
        v[0]/=len; v[1]/=len; v[2]/=len;
    }
}