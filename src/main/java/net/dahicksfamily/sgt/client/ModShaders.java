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
    private static ShaderInstance ringShader;
    private static ShaderInstance starBillboardShader;
    private static ShaderInstance blackHoleShader;
    private static ShaderInstance quasarShader;
    private static ShaderInstance nebulaShader;
    private static ShaderInstance galaxyShader;

    public static final float SHADOW_VISUAL_SCALE = 15.0f;
    public static final int   MAX_SHADOW_CASTERS  = 8;

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

        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        new ResourceLocation(SGT.MOD_ID, "ring"),
                        DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL),
                shader -> ringShader = shader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(SGT.MOD_ID, "star_billboard"),
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL), s -> starBillboardShader = s);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(SGT.MOD_ID, "black_hole"),
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL), s -> blackHoleShader = s);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(SGT.MOD_ID, "quasar"),
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL), s -> quasarShader = s);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(SGT.MOD_ID, "nebula"),
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL), s -> nebulaShader = s);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(SGT.MOD_ID, "galaxy"),
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL), s -> galaxyShader = s);

    }

    public static ShaderInstance getCelestialBodyShader() { return celestialBodyShader; }
    public static ShaderInstance getAtmosphereShader()    { return atmosphereShader; }
    public static ShaderInstance getRingShader() { return ringShader; }
    public static ShaderInstance getStarBillboardShader() { return starBillboardShader; }
    public static ShaderInstance getBlackHoleShader()     { return blackHoleShader; }
    public static ShaderInstance getQuasarShader()        { return quasarShader; }
    public static ShaderInstance getNebulaShader()        { return nebulaShader; }
    public static ShaderInstance getGalaxyShader()        { return galaxyShader; }

 

    private static final String[] CASTER_DIRS = {
            "ShadowCasterDir0","ShadowCasterDir1","ShadowCasterDir2","ShadowCasterDir3",
            "ShadowCasterDir4","ShadowCasterDir5","ShadowCasterDir6","ShadowCasterDir7"
    };
    private static final String[] CASTER_RADS = {
            "ShadowCasterRadius0","ShadowCasterRadius1","ShadowCasterRadius2","ShadowCasterRadius3",
            "ShadowCasterRadius4","ShadowCasterRadius5","ShadowCasterRadius6","ShadowCasterRadius7"
    };

     
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

 
 
        for (int i = 0; i < MAX_SHADOW_CASTERS; i++) {
            if (i < shadowCasters.size()) {
                ShadowCaster sc = shadowCasters.get(i);
 
 
 
                Vector3f dv = skyRot.transform(new Vector3f(sc.direction()));
                setCB3f(CASTER_DIRS[i], dv.x, dv.y, dv.z);
                setCB1f(CASTER_RADS[i], sc.angularRadius()); 
            } else {
                setCB3f(CASTER_DIRS[i], 0f, 0f, -1f);
                setCB1f(CASTER_RADS[i], 0f); 
            }
        }

 
        String[] RDIRS = {"ReflectedLightDir0","ReflectedLightDir1",
                "ReflectedLightDir2","ReflectedLightDir3"};
        String[] RCOLS = {"ReflectedLightColor0","ReflectedLightColor1",
                "ReflectedLightColor2","ReflectedLightColor3"};
        for (int i = 0; i < 4; i++) {
            if (i < reflectors.size()) {
                ReflectedLight r  = reflectors.get(i);
                Vector3f       dv = skyRot.transform(new Vector3f(r.direction()));
                setCB3f(RDIRS[i], dv.x, dv.y, dv.z);
                setCB3f(RCOLS[i], r.color().x, r.color().y, r.color().z);
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

 

    public record ShadowCaster(Vector3f direction, float angularRadius) {}
    public record ReflectedLight(Vector3f direction, Vector3f color)    {}

 

    public static void setRingUniforms(Vector3f lightDirView, Vector3f lightDirModel, float opacity) {
        if (ringShader == null) return;
        setRing3f("LightDirection",      lightDirView.x,  lightDirView.y,  lightDirView.z);
        setRing3f("LightDirectionModel", lightDirModel.x, lightDirModel.y, lightDirModel.z);
        setRing1f("RingOpacity", opacity);
    }

    private static void setRing1f(String n, float v) {
        if (ringShader == null) return;
        var u = ringShader.getUniform(n); if (u != null) u.set(v);
    }
    private static void setRing3f(String n, float x, float y, float z) {
        if (ringShader == null) return;
        var u = ringShader.getUniform(n); if (u != null) u.set(x, y, z);
    }

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

    public static void setStarBillboardTime(float t) {
        if (starBillboardShader == null) return;
        var u = starBillboardShader.getUniform("Time"); if (u != null) u.set(t);
    }
    public static void setBlackHoleUniforms(float t, float phase) {
        if (blackHoleShader == null) return;
        var u = blackHoleShader.getUniform("Time");  if (u != null) u.set(t);
        var p = blackHoleShader.getUniform("Phase"); if (p != null) p.set(phase);
    }
    public static void setQuasarUniforms(float t, float jetAngle, float period,
                                         float amp, float phase) {
        if (quasarShader == null) return;
        var ut = quasarShader.getUniform("Time");      if (ut != null) ut.set(t);
        var uj = quasarShader.getUniform("JetAngle");  if (uj != null) uj.set(jetAngle);
        var up = quasarShader.getUniform("Period");    if (up != null) up.set(period);
        var ua = quasarShader.getUniform("Amplitude"); if (ua != null) ua.set(amp);
        var uh = quasarShader.getUniform("Phase");     if (uh != null) uh.set(phase);
    }
    public static void setNebulaUniforms(float r, float g, float b, float alpha, float rot) {
        if (nebulaShader == null) return;
        var uc = nebulaShader.getUniform("LayerColor"); if (uc != null) uc.set(r,g,b);
        var ua = nebulaShader.getUniform("LayerAlpha"); if (ua != null) ua.set(alpha);
        var ur = nebulaShader.getUniform("LayerRot");   if (ur != null) ur.set(rot);
    }
    public static void setGalaxyUniforms(float r, float g, float b, float bright, float aspect) {
        if (galaxyShader == null) return;
        var uc = galaxyShader.getUniform("GalaxyColor");  if (uc != null) uc.set(r,g,b);
        var ub = galaxyShader.getUniform("Brightness");   if (ub != null) ub.set(bright);
        var ua = galaxyShader.getUniform("Aspect");       if (ua != null) ua.set(aspect);
    }

    public static void setLightDirection(Vector3f d) { setCB3f("LightDirection", d.x, d.y, d.z); }
    public static void setAmbientLight(float a)      { setCB1f("AmbientLight", a); }
}