package net.dahicksfamily.sgt.SkyRendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.dahicksfamily.sgt.client.ModShaders;
import net.dahicksfamily.sgt.space.atmosphere.Atmosphere;
import net.dahicksfamily.sgt.space.CelestialBody;
import net.dahicksfamily.sgt.space.SolarSystem;
import net.dahicksfamily.sgt.space.PlanetsProvider;
import net.dahicksfamily.sgt.space.Star;
import net.dahicksfamily.sgt.time.GlobalTime;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpaceObjectRenderer {
    private static final Minecraft minecraft = Minecraft.getInstance();

    private static VertexBuffer sphereBuffer;
    private static boolean initialized = false;

    private static final Map<CelestialBody, Vec3> positionCache = new HashMap<>();
    private static long lastCacheUpdate = 0;

    private static boolean showLabels = false;


    public static void toggleLabels() {
        showLabels = !showLabels;
        assert minecraft.player != null;
        minecraft.player.displayClientMessage(
                Component.literal("Planet Labels: " + (showLabels ? "ON" : "OFF")),
                true
        );
    }

    public static void initialize() {
        if (initialized) return;

        sphereBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

        RenderSystem.disableCull();
        BufferBuilder.RenderedBuffer sphereData = SphereGenerator.generateSphere(1.0f, 64, 32);
        RenderSystem.enableCull();

        sphereBuffer.bind();
        sphereBuffer.upload(sphereData);
        VertexBuffer.unbind();

        initialized = true;
    }


    public static void renderBodies(PoseStack poseStack, Matrix4f projectionMatrix,
                                    float partialTick, Camera camera) {

        if (!initialized) initialize();
        if (minecraft.level == null) return;

        ResourceKey<Level> dimension = minecraft.level.dimension();
        SolarSystem solarSystem = SolarSystem.getInstance();

        long currentTime = minecraft.level.getGameTime();
        if (currentTime - lastCacheUpdate > 5) {
            updatePositionCache();
            lastCacheUpdate = currentTime;
        }

        CelestialBody observer = solarSystem.getBodyAtDimension(dimension);
        GlobalTime.getInstance().setCurrentObserver(observer);

        List<CelestialBody> visibleBodies = solarSystem.getVisibleBodies(dimension);

        poseStack.pushPose();

        if (observer != null) {
            double latitude = Math.toRadians(45.0);
            double axialTilt = observer.axialTilt;

            double timeInHours = GlobalTime.getInstance().getTotalDays() * 24.0;
            double rotationProgress = (timeInHours % observer.rotationPeriod) / observer.rotationPeriod;
            double skyRotationAngle = rotationProgress * 2.0 * Math.PI;

            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float)(90.0 - Math.toDegrees(latitude))));
            poseStack.mulPose(Axis.XP.rotation((float) axialTilt));
            poseStack.mulPose(Axis.ZP.rotation((float) skyRotationAngle));
        }


        org.joml.Matrix3f skyRot = new org.joml.Matrix3f(poseStack.last().pose());

        for (CelestialBody body : visibleBodies) {
            if (body instanceof Star star) {
                renderStar(poseStack, projectionMatrix, star, observer, skyRot);
            } else {
                renderPlanet(poseStack, projectionMatrix, body, observer, solarSystem, skyRot);
            }

            if (showLabels) {
                Vec3 relativePos = getRelativePosition(body, observer);
                Vec3 skyPos = relativePos.normalize().scale(100);
                renderLabel(poseStack, projectionMatrix, body.name, skyPos, body, camera, observer, skyRot);
            }
        }

        poseStack.popPose();
    }


    private static void renderStar(PoseStack poseStack, Matrix4f projectionMatrix,
                                   Star star, CelestialBody observer, org.joml.Matrix3f skyRot) {

        Vec3 relativePos = getRelativePosition(star, observer);
        if (relativePos.length() == 0) return;

        Vec3 skyPos = relativePos.normalize().scale(100);
        float apparentSize = calculateApparentSize(star, relativePos);

        Vec3 color = star.getColor();
        float brightness = Math.min(star.getBrightness(relativePos) * 2.0f, 1.0f);

        poseStack.pushPose();
        poseStack.translate(skyPos.x, skyPos.y, skyPos.z);

        SolarSystem solarSystem = SolarSystem.getInstance();
        double rotationAngle = star.getRotationAngle(solarSystem.getCurrentTime());
        poseStack.mulPose(Axis.ZP.rotation((float) star.axialTilt));
        poseStack.mulPose(Axis.YP.rotation((float) rotationAngle));
        poseStack.scale(apparentSize, apparentSize, apparentSize);

        RenderSystem.setShaderTexture(0, star.texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(
                (float) color.x * brightness,
                (float) color.y * brightness,
                (float) color.z * brightness,
                1.0f
        );

        sphereBuffer.bind();
        assert GameRenderer.getPositionTexColorShader() != null;
        sphereBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix,
                GameRenderer.getPositionTexColorShader());
        VertexBuffer.unbind();
        poseStack.popPose();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }


    private static void renderPlanet(PoseStack poseStack, Matrix4f projectionMatrix,
                                     CelestialBody body, CelestialBody observer,
                                     SolarSystem solarSystem, org.joml.Matrix3f skyRot) {

        Vec3 relativePos = getRelativePosition(body, observer);
        if (relativePos.length() == 0) return;

        Vec3 skyPos = relativePos.normalize().scale(100);
        float apparentSize = calculateApparentSize(body, relativePos);

        Star lightSource = solarSystem.getPrimaryLightSource(observer);
        if (lightSource == null) return;

        Vec3 lightSourcePos = getRelativePosition(lightSource, observer);
        Vec3 lightDir       = lightSourcePos.subtract(relativePos).normalize();

        poseStack.pushPose();
        poseStack.translate(skyPos.x, skyPos.y, skyPos.z);

        double currentTime = solarSystem.getCurrentTime();

        if (body.tidallyLocked &&
                body.parent != null &&
                positionCache.containsKey(body) &&
                positionCache.containsKey(body.parent)) {

            Vec3 bodyAbs   = positionCache.get(body);
            Vec3 parentAbs = positionCache.get(body.parent);
            poseStack.mulPose(Axis.XP.rotation((float) body.axialTilt));
            pointToPosition(poseStack, bodyAbs, parentAbs, body.tidalLockingOffset);
        } else {
            poseStack.mulPose(Axis.YP.rotation((float) body.getRotationAngle(currentTime)));
            poseStack.mulPose(Axis.XP.rotation((float) body.axialTilt));
        }

        poseStack.scale(apparentSize, apparentSize, apparentSize);

        Vector3f lightDirOrbital = new Vector3f(
                (float) lightDir.x, (float) lightDir.y, (float) lightDir.z);

        Vec3 bodyAbsPos = positionCache.getOrDefault(body, Vec3.ZERO);
        Vec3 starAbsPos = positionCache.getOrDefault(lightSource, Vec3.ZERO);
        Vec3 toStar     = starAbsPos.subtract(bodyAbsPos);
        double distBodyToStar = toStar.length();
        Vec3 toStarUnit = distBodyToStar > 0 ? toStar.normalize() : Vec3.ZERO;
        float starAngRadius = (distBodyToStar > 0)
                ? (float) Math.atan(lightSource.radius / distBodyToStar) : 0f;


        java.util.List<double[]> casterCandidates = new java.util.ArrayList<>();
        java.util.List<ModShaders.ReflectedLight> reflectors = new java.util.ArrayList<>();

        for (CelestialBody other : PlanetsProvider.getAllBodies()) {
            if (other == body || other instanceof Star) continue;

            Vec3   otherAbsPos  = positionCache.getOrDefault(other, Vec3.ZERO);
            Vec3   toOther      = otherAbsPos.subtract(bodyAbsPos);
            double distToOther  = toOther.length();
            if (distToOther == 0) continue;
            Vec3   toOtherUnit  = toOther.normalize();
            double dot          = toOtherUnit.dot(toStarUnit);

            boolean sameFamily =
                    (other.parent != null && other.parent == body) ||
                            (other.parent != null && body.parent != null && other.parent == body.parent) ||
                            (other == body.parent);

            if (sameFamily && distToOther < distBodyToStar && dot > 0.0) {
                float angRadius = (float) Math.atan(other.radius / distToOther);
                casterCandidates.add(new double[]{
                        distToOther, angRadius,
                        toOtherUnit.x, toOtherUnit.y, toOtherUnit.z
                });
            }

            if (sameFamily && reflectors.size() < 4 && other.albedo > 0 && dot > 0) {
                double ratio      = other.radius / distToOther;
                double irradiance = other.albedo * ratio * ratio;
                if (irradiance > 0.001) {
                    Vec3 sc = lightSource.getColor();
                    reflectors.add(new ModShaders.ReflectedLight(
                            new Vector3f((float)toOtherUnit.x,
                                    (float)toOtherUnit.y,
                                    (float)toOtherUnit.z),
                            new Vector3f((float)(sc.x * irradiance),
                                    (float)(sc.y * irradiance),
                                    (float)(sc.z * irradiance))));
                }
            }
        }

        casterCandidates.sort((a, b) -> Double.compare(a[0], b[0]));

        java.util.List<ModShaders.ShadowCaster> shadowCasters = new java.util.ArrayList<>();
        for (int ci = 0; ci < Math.min(4, casterCandidates.size()); ci++) {
            double[] c = casterCandidates.get(ci);
            shadowCasters.add(new ModShaders.ShadowCaster(
                    new Vector3f((float)c[2], (float)c[3], (float)c[4]),
                    (float)c[1]));
        }

        RenderSystem.setShaderTexture(0, body.texture);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        final float AMBIENT = 0.15f;
        ShaderInstance bodyShader = ModShaders.getCelestialBodyShader();
        if (bodyShader != null) {
            ModShaders.setCelestialBodyLighting(
                    skyRot, lightDirOrbital, starAngRadius, AMBIENT, shadowCasters, reflectors);
            RenderSystem.setShader(() -> bodyShader);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        sphereBuffer.bind();
        sphereBuffer.drawWithShader(
                poseStack.last().pose(), projectionMatrix,
                bodyShader != null ? bodyShader : GameRenderer.getPositionTexColorShader()
        );
        VertexBuffer.unbind();
        poseStack.popPose();

        if (body.hasAtmosphere()) {
            renderAtmosphere(poseStack, projectionMatrix, body, relativePos, lightDir, apparentSize, skyRot);
        }
    }

    private static void renderAtmosphere(PoseStack poseStack, Matrix4f projectionMatrix,
                                         CelestialBody body, Vec3 relativePos,
                                         Vec3 lightDir, float planetSize,
                                         org.joml.Matrix3f skyRot) {

        Atmosphere atmo = body.atmosphere;
        if (atmo == null) return;

        ShaderInstance atmoShader = ModShaders.getAtmosphereShader();
        if (atmoShader == null) return;

        float atmoScale = planetSize * (1.0f + atmo.outerHeightFraction);

        float planetRadiusFrac = 1.0f / (1.0f + atmo.outerHeightFraction);

        Vec3 skyPos = relativePos.normalize().scale(100);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        Vector3f lightDirView = skyRot.transform(
                new org.joml.Vector3f((float)lightDir.x, (float)lightDir.y, (float)lightDir.z));
        ModShaders.setAtmosphereUniforms(lightDirView, atmo, planetRadiusFrac, 0.15f);

        poseStack.pushPose();
        poseStack.translate(skyPos.x, skyPos.y, skyPos.z);
        poseStack.scale(atmoScale, atmoScale, atmoScale);

        RenderSystem.setShader(() -> atmoShader);
        RenderSystem.setShaderTexture(0, body.texture);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        sphereBuffer.bind();
        sphereBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, atmoShader);
        VertexBuffer.unbind();

        poseStack.popPose();

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }


    private static void renderLabel(PoseStack poseStack, Matrix4f projectionMatrix,
                                    String name, Vec3 skyPos, CelestialBody body,
                                    Camera camera, CelestialBody observer,
                                    org.joml.Matrix3f skyRot) {

        if (observer != null && body.name.equals(observer.name)) return;

        Vec3 relativePos = getRelativePosition(body, observer);
        if (relativePos.length() == 0) return;

        org.joml.Matrix4f accumMat = new org.joml.Matrix4f(poseStack.last().pose());
        accumMat.m30(0).m31(0).m32(0);
        org.joml.Matrix4f cancelMat = accumMat.invert(new org.joml.Matrix4f());
        org.joml.Quaternionf cancelQ = cancelMat.getNormalizedRotation(new org.joml.Quaternionf());

        float bodyAppSize = calculateApparentSize(body, relativePos);
        float offsetDist  = bodyAppSize * 1.2f + 0.5f;
        Vec3 labelPos = new Vec3(
                skyPos.x,
                skyPos.y + offsetDist,
                skyPos.z);

        poseStack.pushPose();
        poseStack.translate(labelPos.x, labelPos.y, labelPos.z);
        poseStack.mulPose(cancelQ);

        float scale = 0.05f;
        poseStack.scale(scale, -scale, scale);

        Font font = minecraft.font;
        Component text = Component.literal(name);
        int textWidth = font.width(text);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        font.drawInBatch(text, -textWidth / 2f, 0, 0xFFFFFF, false,
                poseStack.last().pose(), bufferSource,
                Font.DisplayMode.NORMAL, 0x80000000, 15728880);
        bufferSource.endBatch();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        poseStack.popPose();
    }


    private static float calculateApparentSize(CelestialBody body, Vec3 relativePos) {
        double distanceKm = relativePos.length();
        if (distanceKm == 0) return 0.01f;
        double angularRadians = 2.0 * Math.atan(body.radius / distanceKm);
        return (float)(100.0 * angularRadians);
    }

    private static void pointToPosition(PoseStack poseStack, Vec3 from, Vec3 to,
                                        double offsetRadians) {
        Vec3 direction = to.subtract(from);
        if (direction.length() == 0) return;
        direction = direction.normalize();

        Vector3f modelForward = new Vector3f(1, 0.3f, 1);
        modelForward.normalize();
        Vector3f targetDir = new Vector3f(
                (float) direction.x, (float) direction.y, (float) direction.z);

        Quaternionf rotation = new Quaternionf().rotationTo(modelForward, targetDir);
        poseStack.mulPose(rotation);
        poseStack.mulPose(Axis.YP.rotation((float) offsetRadians));
    }

    private static Vec3 getRelativePosition(CelestialBody target, CelestialBody observer) {
        Vec3 targetPos   = positionCache.getOrDefault(target, Vec3.ZERO);
        Vec3 observerPos = observer != null ? positionCache.getOrDefault(observer, Vec3.ZERO) : Vec3.ZERO;
        return targetPos.subtract(observerPos);
    }

    private static void updatePositionCache() {
        SolarSystem solarSystem = SolarSystem.getInstance();
        positionCache.clear();
        for (CelestialBody body : solarSystem.getBodies()) {
            positionCache.put(body, solarSystem.getAbsolutePosition(body));
        }
        for (Star star : solarSystem.getStars()) {
            positionCache.put(star, solarSystem.getAbsolutePosition(star));
        }
    }

    public static VertexBuffer getSphereBuffer()  { return sphereBuffer; }
    public static void bindSphereBuffer()          { if (sphereBuffer != null) sphereBuffer.bind(); }

    public static void cleanup() {
        if (sphereBuffer != null) sphereBuffer.close();
        initialized = false;
    }
}