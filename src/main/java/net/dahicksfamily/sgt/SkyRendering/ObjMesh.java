package net.dahicksfamily.sgt.SkyRendering;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.*;

 
public class ObjMesh {

 
    public VertexBuffer vertexBuffer;
    public ResourceLocation texture;
     
    public float halfExtent = 1.0f;
    public int vertexCount = 0;

 
    private static final int CHUNK = 4096;

 

     
    public static ObjMesh load(String bodyName, ResourceLocation fallbackTexture) {
        ResourceManager rm = Minecraft.getInstance().getResourceManager();
        ResourceLocation objLoc = new ResourceLocation("sgt",
                "models/celestial/" + bodyName.toLowerCase() + ".obj");

        Optional<Resource> res = rm.getResource(objLoc);
        if (res.isEmpty()) return null;

        try (InputStream is = res.get().open();
             BufferedReader br = new BufferedReader(new InputStreamReader(is), 65536)) {

            return parse(br, bodyName, fallbackTexture, rm);

        } catch (IOException e) {
            System.err.println("[SGT] Failed to load OBJ for " + bodyName + ": " + e.getMessage());
            return null;
        }
    }

 

    private static ObjMesh parse(BufferedReader br, String bodyName,
                                 ResourceLocation fallbackTexture,
                                 ResourceManager rm) throws IOException {

 
        float[] vx = new float[CHUNK], vy = new float[CHUNK], vz = new float[CHUNK];
        float[] tx = new float[CHUNK], ty = new float[CHUNK];
        float[] nx = new float[CHUNK], ny = new float[CHUNK], nz = new float[CHUNK];
        int vc = 0, tc = 0, nc = 0;

 
        float[] faceData = new float[CHUNK * 8];
        int fc = 0; 

 
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

 
        ResourceLocation resolvedTex = fallbackTexture;
        String mtlLib = null;

        String line;
        while ((line = br.readLine()) != null) {
            if (line.isEmpty() || line.charAt(0) == '#') continue;

 
            char c0 = line.charAt(0);

            if (c0 == 'v') {
                if (line.length() < 2) continue;
                char c1 = line.charAt(1);

                if (c1 == ' ') {
 
                    float[] f = parseFloats3(line, 2);
                    if (vx.length == vc) { vx = grow(vx); vy = grow(vy); vz = grow(vz); }
                    vx[vc] = f[0]; vy[vc] = f[1]; vz[vc] = f[2]; vc++;

                    if (f[0] < minX) minX = f[0]; if (f[0] > maxX) maxX = f[0];
                    if (f[1] < minY) minY = f[1]; if (f[1] > maxY) maxY = f[1];
                    if (f[2] < minZ) minZ = f[2]; if (f[2] > maxZ) maxZ = f[2];

                } else if (c1 == 't') {
 
                    float[] f = parseFloats2(line, 3);
                    if (tx.length == tc) { tx = grow(tx); ty = grow(ty); }
                    tx[tc] = f[0]; ty[tc] = f[1]; tc++;

                } else if (c1 == 'n') {
 
                    float[] f = parseFloats3(line, 3);
                    if (nx.length == nc) { nx = grow(nx); ny = grow(ny); nz = grow(nz); }
                    nx[nc] = f[0]; ny[nc] = f[1]; nz[nc] = f[2]; nc++;
                }

            } else if (c0 == 'f') {
 
 
                int[] indices = parseFaceTokens(line); 
                int fv = indices.length / 3;
                int tris = fv - 2;
 
                if (faceData.length < (fc + tris * 3) * 8)
                    faceData = Arrays.copyOf(faceData, faceData.length * 2);

                for (int t = 0; t < tris; t++) {
 
                    int[] tri = { 0, t + 1, t + 2 };
                    for (int corner : tri) {
                        int vi = indices[corner * 3]     - 1; 
                        int ti = indices[corner * 3 + 1] - 1;
                        int ni = indices[corner * 3 + 2] - 1;

                        faceData[fc * 8]     = (vi >= 0 && vi < vc) ? vx[vi] : 0f;
                        faceData[fc * 8 + 1] = (vi >= 0 && vi < vc) ? vy[vi] : 0f;
                        faceData[fc * 8 + 2] = (vi >= 0 && vi < vc) ? vz[vi] : 0f;
                        faceData[fc * 8 + 3] = (ni >= 0 && ni < nc) ? nx[ni] : 0f;
                        faceData[fc * 8 + 4] = (ni >= 0 && ni < nc) ? ny[ni] : 0f;
                        faceData[fc * 8 + 5] = (ni >= 0 && ni < nc) ? nz[ni] : 1f;
                        faceData[fc * 8 + 6] = (ti >= 0 && ti < tc) ? tx[ti] : 0f;
                        faceData[fc * 8 + 7] = (ti >= 0 && ti < tc) ? 1f - ty[ti] : 0f; 
                        fc++;
                    }
                }

            } else if (c0 == 'm' && line.startsWith("mtllib ")) {
                mtlLib = line.substring(7).trim();

            }
 
        }

 
        float extX = (maxX - minX) * 0.5f;
        float extY = (maxY - minY) * 0.5f;
        float extZ = (maxZ - minZ) * 0.5f;
        float halfExtent = Math.max(extX, Math.max(extY, extZ));
        if (halfExtent == 0) halfExtent = 1f;

 
        if (mtlLib != null) {
            ResourceLocation mtlLoc = new ResourceLocation("sgt",
                    "models/celestial/" + mtlLib);
            Optional<Resource> mtlRes = rm.getResource(mtlLoc);
            if (mtlRes.isPresent()) {
                try (InputStream mis = mtlRes.get().open();
                     BufferedReader mbr = new BufferedReader(
                             new InputStreamReader(mis), 8192)) {
                    String ml;
                    while ((ml = mbr.readLine()) != null) {
                        if (ml.startsWith("map_Kd ")) {
                            String texName = ml.substring(7).trim();
 
                            int slash = Math.max(texName.lastIndexOf('/'),
                                    texName.lastIndexOf('\\'));
                            if (slash >= 0) texName = texName.substring(slash + 1);
 
                            int dot = texName.lastIndexOf('.');
                            if (dot >= 0) texName = texName.substring(0, dot);
                            resolvedTex = new ResourceLocation("sgt",
                                    "textures/misc/celestial/bodys/" + texName + ".png");
                            break;
                        }
                    }
                }
            }
        }

 
 
 
        BufferBuilder bb = new BufferBuilder(fc * (3 + 2 + 1 + 3) * 4 + 64);
        bb.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        for (int i = 0; i < fc; i++) {
            float px = faceData[i * 8],     py = faceData[i * 8 + 1], pz = faceData[i * 8 + 2];
            float fnx = faceData[i * 8 + 3], fny = faceData[i * 8 + 4], fnz = faceData[i * 8 + 5];
            float u  = faceData[i * 8 + 6], v  = faceData[i * 8 + 7];

            bb.vertex(px, py, pz)
                    .uv(u, v)
                    .color(255, 255, 255, 255)
                    .normal(fnx, fny, fnz)
                    .endVertex();
        }

        BufferBuilder.RenderedBuffer rendered = bb.end();
        VertexBuffer vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        vbo.bind();
        vbo.upload(rendered);
        VertexBuffer.unbind();

        ObjMesh mesh = new ObjMesh();
        mesh.vertexBuffer = vbo;
        mesh.texture      = resolvedTex;
        mesh.halfExtent   = halfExtent;
        mesh.vertexCount  = fc;
        return mesh;
    }

 
 
 

    private static float[] parseFloats3(String line, int start) {
        float[] r = new float[3];
        int idx = 0, i = start;
        int len = line.length();
        while (i < len && idx < 3) {
            while (i < len && line.charAt(i) == ' ') i++;
            int j = i;
            while (j < len && line.charAt(j) != ' ') j++;
            if (j > i) r[idx++] = Float.parseFloat(line.substring(i, j));
            i = j;
        }
        return r;
    }

    private static float[] parseFloats2(String line, int start) {
        float[] r = new float[2];
        int idx = 0, i = start;
        int len = line.length();
        while (i < len && idx < 2) {
            while (i < len && line.charAt(i) == ' ') i++;
            int j = i;
            while (j < len && line.charAt(j) != ' ') j++;
            if (j > i) r[idx++] = Float.parseFloat(line.substring(i, j));
            i = j;
        }
        return r;
    }

     
    private static int[] parseFaceTokens(String line) {
 
        int tokenCount = 0;
        boolean inToken = false;
        for (int i = 2; i < line.length(); i++) {
            boolean sp = line.charAt(i) == ' ';
            if (!sp && !inToken) { tokenCount++; inToken = true; }
            else if (sp) inToken = false;
        }

        int[] result = new int[tokenCount * 3];
        int ri = 0, i = 2;
        int len = line.length();

        while (i < len) {
            while (i < len && line.charAt(i) == ' ') i++;
            if (i >= len) break;
            int j = i;
            while (j < len && line.charAt(j) != ' ') j++;
 
            String tok = line.substring(i, j);
            int s1 = tok.indexOf('/');
            if (s1 < 0) {
                result[ri++] = parseInt(tok, 0, tok.length());
                result[ri++] = 0;
                result[ri++] = 0;
            } else {
                result[ri++] = parseInt(tok, 0, s1);
                int s2 = tok.indexOf('/', s1 + 1);
                if (s2 < 0) {
                    result[ri++] = parseInt(tok, s1 + 1, tok.length());
                    result[ri++] = 0;
                } else {
                    result[ri++] = (s2 == s1 + 1) ? 0 : parseInt(tok, s1 + 1, s2);
                    result[ri++] = parseInt(tok, s2 + 1, tok.length());
                }
            }
            i = j;
        }
        return Arrays.copyOf(result, ri);
    }

    private static int parseInt(String s, int from, int to) {
        if (from >= to) return 0;
        int v = 0;
        boolean neg = false;
        if (s.charAt(from) == '-') { neg = true; from++; }
        for (int i = from; i < to; i++) v = v * 10 + (s.charAt(i) - '0');
        return neg ? -v : v;
    }

    private static float[] grow(float[] a) { return Arrays.copyOf(a, a.length * 2); }

 

    public void close() {
        if (vertexBuffer != null) { vertexBuffer.close(); vertexBuffer = null; }
    }
}