package net.dahicksfamily.sgt.client;

import net.dahicksfamily.sgt.SGT;
import net.dahicksfamily.sgt.space.atmosphere.Atmosphere;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;
import java.util.List;

@Mod.EventBusSubscriber(modid = SGT.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModShaders {

    private static ShaderInstance celestialBodyShader;
    private static ShaderInstance atmosphereShader;

    public static final float SHADOW_VISUAL_SCALE = 15.0f;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        new ResourceLocation(SGT.MOD_ID, "celestial_body"),
                        DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL),
                shader -> celestialBodyShader = shader);
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        new ResourceLocation(SGT.MOD_ID, "atmosphere"),
                        DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL),
                shader -> atmosphereShader = shader);
    }

    public static ShaderInstance getCelestialBodyShader() { return celestialBodyShader; }
    public static ShaderInstance getAtmosphereShader()    { return atmosphereShader; }

    /**
     * Upload all lighting for one body draw call.
     *
     * @param skyRot       mat3(poseStack) after sky-dome rotations, before planet transforms.
     *                     Converts orbital-space directions → view-space to match vertexNormal.
     * @param lightDirOrb  FROM body TOWARD star, orbital space
     * @param starAngRad   atan(star.radius / distToStar) — raw, not scaled
     * @param shadowCasters up to 4; unused slots padded to radius=0 automatically
     * @param reflectors   up to 4; unused slots padded to black automatically
     */
    public static void setCelestialBodyLighting(
            Matrix3f skyRot,
            Vector3f lightDirOrb,
            float    starAngRad,
            float    ambientLight,
            List<ShadowCaster>   shadowCasters,
            List<ReflectedLight> reflectors) {

        if (celestialBodyShader == null) return;

        Vector3f lightView = skyRot.transform(new Vector3f(lightDirOrb));
        setCB3f("LightDirection", lightView.x, lightView.y, lightView.z);
        setCB1f("AmbientLight",   ambientLight);
        setCB1f("StarRadius",     starAngRad * SHADOW_VISUAL_SCALE);

        String[] DIRS = {"ShadowCasterDir0","ShadowCasterDir1","ShadowCasterDir2","ShadowCasterDir3"};
        String[] RADS = {"ShadowCasterRadius0","ShadowCasterRadius1","ShadowCasterRadius2","ShadowCasterRadius3"};
        for (int i = 0; i < 4; i++) {
            if (i < shadowCasters.size()) {
                ShadowCaster sc = shadowCasters.get(i);
                Vector3f dv = skyRot.transform(new Vector3f(sc.direction));
                setCB3f(DIRS[i], dv.x, dv.y, dv.z);
                setCB1f(RADS[i], sc.angularRadius * SHADOW_VISUAL_SCALE);
            } else {
                setCB3f(DIRS[i], 0f, 0f, -1f);
                setCB1f(RADS[i], 0f);
            }
        }

        String[] RDIRS = {"ReflectedLightDir0","ReflectedLightDir1","ReflectedLightDir2","ReflectedLightDir3"};
        String[] RCOLS = {"ReflectedLightColor0","ReflectedLightColor1","ReflectedLightColor2","ReflectedLightColor3"};
        for (int i = 0; i < 4; i++) {
            if (i < reflectors.size()) {
                ReflectedLight r = reflectors.get(i);
                Vector3f dv = skyRot.transform(new Vector3f(r.direction));
                setCB3f(RDIRS[i], dv.x, dv.y, dv.z);
                setCB3f(RCOLS[i], r.color.x, r.color.y, r.color.z);
            } else {
                setCB3f(RDIRS[i], 0f, 1f, 0f);
                setCB3f(RCOLS[i], 0f, 0f, 0f);
            }
        }
    }

    public static void setAtmosphereUniforms(Vector3f lightDirView, Atmosphere atmo,
                                             float planetRadFrac, float ambientLight) {
        if (atmosphereShader == null) return;
        setAtmo3f("LightDirection",          lightDirView.x, lightDirView.y, lightDirView.z);
        setAtmo1f("AmbientLight",            ambientLight);
        setAtmo1f("PlanetRadiusFraction",    planetRadFrac);
        setAtmo1f("SurfaceDensity",          atmo.surfaceDensity);
        setAtmo1f("ScaleHeight",             atmo.scaleHeight);
        setAtmo1f("MieDensity",              atmo.mieDensity);
        setAtmo1f("MieAnisotropy",           atmo.mieAnisotropy);
        setAtmo1f("AirglowIntensity",        atmo.airglowIntensity);
        setAtmo1f("ShadowSoftness",          atmo.shadowSoftness);
        setAtmo1f("TerminatorBandIntensity", atmo.terminatorBandIntensity);
        setAtmo3f("RayleighCoeff",
                (float)atmo.rayleighCoeff.x,(float)atmo.rayleighCoeff.y,(float)atmo.rayleighCoeff.z);
        setAtmo3f("MieCoeff",
                (float)atmo.mieCoeff.x,(float)atmo.mieCoeff.y,(float)atmo.mieCoeff.z);
        setAtmo3f("AirglowColor",
                (float)atmo.airglowColor.x,(float)atmo.airglowColor.y,(float)atmo.airglowColor.z);
        setAtmo3f("TerminatorBandColor",
                (float)atmo.terminatorBandColor.x,
                (float)atmo.terminatorBandColor.y,
                (float)atmo.terminatorBandColor.z);
    }


    public record ShadowCaster(
            Vector3f direction,
            float    angularRadius
    ) {}

    public record ReflectedLight(
            Vector3f direction,
            Vector3f color
    ) {}


    private static void setCB1f(String n, float v) {
        if (celestialBodyShader == null) return;
        var u = celestialBodyShader.getUniform(n); if (u != null) u.set(v);
    }
    private static void setCB3f(String n, float x, float y, float z) {
        if (celestialBodyShader == null) return;
        var u = celestialBodyShader.getUniform(n); if (u != null) u.set(x, y, z);
    }
    private static void setAtmo1f(String n, float v) {
        if (atmosphereShader == null) return;
        var u = atmosphereShader.getUniform(n); if (u != null) u.set(v);
    }
    private static void setAtmo3f(String n, float x, float y, float z) {
        if (atmosphereShader == null) return;
        var u = atmosphereShader.getUniform(n); if (u != null) u.set(x, y, z);
    }

    public static void setLightDirection(Vector3f d) { setCB3f("LightDirection",d.x,d.y,d.z); }
    public static void setAmbientLight(float a)      { setCB1f("AmbientLight",a); }
}