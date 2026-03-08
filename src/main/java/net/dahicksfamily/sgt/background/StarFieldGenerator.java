package net.dahicksfamily.sgt.background;

import java.util.Random;

public class StarFieldGenerator {

    private static final float[][] GAL = {
            {-0.867f,  0.197f,  0.460f},
            {-0.457f, -0.855f,  0.238f},
            { 0.187f, -0.463f,  0.866f}
    };

    public static StarFieldData generate(long seed) {
        Random rng  = new Random(seed);
        StarFieldData data = new StarFieldData();
        data.seed = seed;

        addMainSequence (rng, data, 25000); 
        addSpecialStars (rng, data);
        addExoticObjects(rng, data);
        addNebulae      (rng, data, 48);
        addGalaxies     (rng, data, 28);

        return data;
    }

    private static void addMainSequence(Random rng, StarFieldData data, int count) {
        float[] cw = {0.001f, 0.012f, 0.072f, 0.172f, 0.372f, 0.622f, 1.000f};
        BackgroundObject.Type[] types = {
                BackgroundObject.Type.STAR_O, BackgroundObject.Type.STAR_B,
                BackgroundObject.Type.STAR_A, BackgroundObject.Type.STAR_F,
                BackgroundObject.Type.STAR_G, BackgroundObject.Type.STAR_K,
                BackgroundObject.Type.STAR_M
        };
        float[][] colours = {
                {0.55f, 0.65f, 1.00f},
                {0.70f, 0.80f, 1.00f},
                {0.88f, 0.92f, 1.00f},
                {1.00f, 1.00f, 0.88f},
                {1.00f, 0.94f, 0.68f},
                {1.00f, 0.72f, 0.38f},
                {1.00f, 0.38f, 0.18f}
        };
        float[][] sizes = {
                {2.5f,5.0f},{1.8f,4.0f},{1.2f,2.5f},
                {0.9f,1.8f},{0.7f,1.5f},{0.6f,1.2f},{0.4f,0.9f}
        };

        for (int i = 0; i < count; i++) {
            BackgroundObject o = new BackgroundObject();
            float roll = rng.nextFloat();
            int t = 6;
            for (int j = 0; j < cw.length; j++) if (roll < cw[j]) { t = j; break; }
            o.type = types[t];
            placeSky(o, rng, 0.70f);
            float[] col = colours[t];
            o.r = c(col[0] + (rng.nextFloat()-0.5f)*0.07f);
            o.g = c(col[1] + (rng.nextFloat()-0.5f)*0.07f);
            o.b = c(col[2] + (rng.nextFloat()-0.5f)*0.07f);
            float[] sz = sizes[t];
            o.size            = sz[0] + rng.nextFloat()*(sz[1]-sz[0]);
            o.brightness      = 0.3f + rng.nextFloat()*0.7f;
            o.variablePeriod  = 0f;
            o.variableAmplitude = 0.02f + rng.nextFloat()*0.04f;
            o.variablePhase   = rng.nextFloat();
            o.seed = rng.nextLong();
            data.simpleStars.add(o);
        }
    }

    private static void addSpecialStars(Random rng, StarFieldData data) {
 
        for (int i = 0; i < 130; i++) {
            BackgroundObject o = new BackgroundObject();
            o.type = BackgroundObject.Type.CEPHEID_VARIABLE;
            placeSky(o, rng, 0.65f);
            o.r = 1.0f; o.g = 0.88f; o.b = 0.45f;
            o.size              = 2.2f + rng.nextFloat()*2.5f;
            o.brightness        = 0.55f + rng.nextFloat()*0.45f;
            o.variablePeriod    = 20f + rng.nextFloat()*400f;
            o.variableAmplitude = 0.28f + rng.nextFloat()*0.35f;
            o.variablePhase     = rng.nextFloat();
            o.seed = rng.nextLong();
            data.simpleStars.add(o);
        }

 
        for (int i = 0; i < 65; i++) {
            BackgroundObject o = new BackgroundObject();
            o.type = BackgroundObject.Type.WOLF_RAYET;
            placeSky(o, rng, 0.78f);
            o.r = 0.35f; o.g = 0.65f+rng.nextFloat()*0.35f; o.b = 1.0f;
            o.size              = 2.8f + rng.nextFloat()*2.8f;
            o.brightness        = 0.65f + rng.nextFloat()*0.35f;
            o.variablePeriod    = 8f + rng.nextFloat()*80f;
            o.variableAmplitude = 0.07f + rng.nextFloat()*0.13f;
            o.variablePhase     = rng.nextFloat();
            o.seed = rng.nextLong();
            data.simpleStars.add(o);
        }

 
        for (int i = 0; i < 42; i++) {
            BackgroundObject o = new BackgroundObject();
            o.type = BackgroundObject.Type.NEUTRON_STAR;
            placeSky(o, rng, 0.80f);
            o.r = 0.82f; o.g = 0.88f; o.b = 1.0f;
            o.size              = 1.4f + rng.nextFloat()*1.4f;
            o.brightness        = 0.80f + rng.nextFloat()*0.20f;
            o.variablePeriod    = 0.5f + rng.nextFloat()*5f;
            o.variableAmplitude = 0.45f + rng.nextFloat()*0.45f;
            o.variablePhase     = rng.nextFloat();
            o.seed = rng.nextLong();
            data.simpleStars.add(o);
        }
    }

    private static void addExoticObjects(Random rng, StarFieldData data) {
 
        for (int i = 0; i < 2; i++) {
            BackgroundObject o = new BackgroundObject();
            o.type = BackgroundObject.Type.BLACK_HOLE;
            placeSky(o, rng, 0.65f);
            o.r = 1.0f; o.g = 0.62f; o.b = 0.22f;
            o.size       = 1.8f + rng.nextFloat()*0.8f; 
            o.brightness = 0.75f + rng.nextFloat()*0.25f;
            o.variablePhase = rng.nextFloat();
            o.seed = rng.nextLong();
            data.exoticObjects.add(o);
        }

 
        for (int i = 0; i < 4; i++) {
            BackgroundObject o = new BackgroundObject();
            o.type = BackgroundObject.Type.QUASAR;
            placeSky(o, rng, 0.15f);
            o.r = 1.0f; o.g = 0.92f; o.b = 0.72f;
            o.size       = 2.0f + rng.nextFloat()*1.5f; 
            o.brightness = 0.85f + rng.nextFloat()*0.15f;
            o.jetAngle   = rng.nextFloat() * (float)Math.PI;
            o.pulsePeriod        = 4f + rng.nextFloat()*20f;
            o.variableAmplitude  = 0.20f + rng.nextFloat()*0.30f;
            o.variablePhase      = rng.nextFloat();
            o.seed = rng.nextLong();
            data.exoticObjects.add(o);
        }
    }

    private static void addNebulae(Random rng, StarFieldData data, int count) {
        float[][][] paletteTypes = {
                {{1.0f,0.25f,0.25f},{1.0f,0.50f,0.40f},{0.75f,0.15f,0.45f}},
                {{0.35f,0.45f,1.0f},{0.60f,0.70f,1.0f},{0.25f,0.35f,0.85f}},
                {{0.25f,0.85f,0.80f},{0.18f,0.65f,1.0f},{0.45f,1.0f,0.85f}},
                {{0.55f,0.18f,0.78f},{0.85f,0.35f,0.18f},{0.28f,0.75f,0.45f}}
        };

        for (int i = 0; i < count; i++) {
            BackgroundObject o = new BackgroundObject();
            o.type = BackgroundObject.Type.NEBULA;
            placeSky(o, rng, 0.88f);
            int pt = rng.nextInt(4);
            float[][] pal = paletteTypes[pt];
            o.r = pal[0][0]; o.g = pal[0][1]; o.b = pal[0][2];
            o.size         = 0.05f + rng.nextFloat()*0.2f; 
            o.brightness   = 0.25f + rng.nextFloat()*0.55f;
            o.nebulaLayers = 5 + rng.nextInt(6);
            o.nebulaSpread = 0.40f + rng.nextFloat()*0.55f;
            o.nebulaLayerR    = new float[o.nebulaLayers];
            o.nebulaLayerG    = new float[o.nebulaLayers];
            o.nebulaLayerB    = new float[o.nebulaLayers];
            o.nebulaLayerSize = new float[o.nebulaLayers];
            o.nebulaLayerRot  = new float[o.nebulaLayers];
            for (int l = 0; l < o.nebulaLayers; l++) {
                float[] col = pal[rng.nextInt(3)];
                float v = 0.65f + rng.nextFloat()*0.35f;
                o.nebulaLayerR[l]    = col[0]*v;
                o.nebulaLayerG[l]    = col[1]*v;
                o.nebulaLayerB[l]    = col[2]*v;
                o.nebulaLayerSize[l] = 0.5f + rng.nextFloat()*1.0f;
                o.nebulaLayerRot[l]  = rng.nextFloat()*(float)Math.PI*2f;
            }
            o.seed = rng.nextLong();
            data.nebulae.add(o);
        }
    }

    private static void addGalaxies(Random rng, StarFieldData data, int count) {
        for (int i = 0; i < count; i++) {
            BackgroundObject o = new BackgroundObject();
            o.type = BackgroundObject.Type.GALAXY;
            placeSky(o, rng, 0.08f);
            boolean spiral = rng.nextBoolean();
            if (spiral) { o.r = 0.88f; o.g = 0.90f; o.b = 1.00f; }
            else         { o.r = 1.00f; o.g = 0.88f; o.b = 0.68f; }
            o.size          = 1f + rng.nextFloat()*3f; 
            o.brightness    = 0.20f + rng.nextFloat()*0.55f;
            o.galaxyAspect  = 0.15f + rng.nextFloat()*0.70f;
            o.galaxyRotAngle = rng.nextFloat()*(float)Math.PI*2f;
 
 
 
            o.variableAmplitude = spiral ? 1.0f : 0.0f; 
            o.seed = rng.nextLong();
            data.galaxies.add(o);
        }
    }

    private static void placeSky(BackgroundObject o, Random rng, float bandBias) {
        float[] d = rng.nextFloat() < bandBias ? sampleBand(rng) : sampleSphere(rng);
        o.x = d[0]; o.y = d[1]; o.z = d[2];
    }

    private static float[] sampleBand(Random rng) {
        float lon = rng.nextFloat() * (float)(Math.PI*2);
        float lat = (float)(rng.nextGaussian() * 0.26);
        float cl  = (float)Math.cos(lat);
        return galToSky(cl*(float)Math.cos(lon), cl*(float)Math.sin(lon), (float)Math.sin(lat));
    }

    private static float[] sampleSphere(Random rng) {
        float x, y, z, d;
        do {
            x = rng.nextFloat()*2-1; y = rng.nextFloat()*2-1; z = rng.nextFloat()*2-1;
            d = x*x+y*y+z*z;
        } while (d > 1 || d < 1e-6f);
        float inv = 1f/(float)Math.sqrt(d);
        return new float[]{x*inv,y*inv,z*inv};
    }

    private static float[] galToSky(float gx, float gy, float gz) {
        float sx = GAL[0][0]*gx + GAL[1][0]*gy + GAL[2][0]*gz;
        float sy = GAL[0][1]*gx + GAL[1][1]*gy + GAL[2][1]*gz;
        float sz = GAL[0][2]*gx + GAL[1][2]*gy + GAL[2][2]*gz;
        float len = (float)Math.sqrt(sx*sx+sy*sy+sz*sz);
        if (len < 1e-6f) return new float[]{0,1,0};
        return new float[]{sx/len,sy/len,sz/len};
    }

    private static float c(float v) { return Math.max(0, Math.min(1, v)); }
}