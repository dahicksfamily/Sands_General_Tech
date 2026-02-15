package net.dahicksfamily.sgt.SkyRendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.dahicksfamily.sgt.space.CelestialBody;
import net.dahicksfamily.sgt.space.SolarSystem;
import net.dahicksfamily.sgt.space.Star;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

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

        BufferBuilder.RenderedBuffer sphereData = SphereGenerator.generateSphere(1.0f, 64, 32);

        sphereBuffer.bind();
        sphereBuffer.upload(sphereData);
        VertexBuffer.unbind();

        initialized = true;
    }

    public static void renderBodies(PoseStack poseStack, Matrix4f projectionMatrix,
                                    float partialTick, Camera camera) {

        if (!initialized) {
            initialize();
        }

        if (minecraft.level == null) return;

        ResourceKey<Level> dimension = minecraft.level.dimension();
        SolarSystem solarSystem = SolarSystem.getInstance();

        long currentTime = minecraft.level.getGameTime();
        if (currentTime - lastCacheUpdate > 5) {
            updatePositionCache();
            lastCacheUpdate = currentTime;
        }

        CelestialBody observer = solarSystem.getBodyAtDimension(dimension);
        List<CelestialBody> visibleBodies = solarSystem.getVisibleBodies(dimension);

        poseStack.pushPose();

        if (observer != null && observer.name.equals("Earth")) {
            double currentDay = solarSystem.getCurrentTime();
            double yearProgress = (currentDay % 365.25) / 365.25;

            double tiltAngle = Math.toRadians(23.44);

            double seasonalTilt = tiltAngle * Math.cos(2 * Math.PI * yearProgress);

            poseStack.mulPose(Axis.XP.rotation((float) seasonalTilt));
        }

        for (CelestialBody body : visibleBodies) {
            if (body instanceof Star) {
                renderStar(poseStack, projectionMatrix, (Star) body, observer);
            } else {
                renderPlanet(poseStack, projectionMatrix, body, observer, solarSystem);
            }
            
            if (showLabels) {
                Vec3 relativePos = getRelativePosition(body, observer);
                Vec3 skyPos = relativePos.normalize().scale(100);
                float apparentSize = calculateApparentSize(body, relativePos);

                renderLabel(poseStack, projectionMatrix, body.name, skyPos, body, camera, observer);
            }
        }

        poseStack.popPose();
    }

    private static void renderLabel(PoseStack poseStack, Matrix4f projectionMatrix,
                                    String name, Vec3 skyPos, CelestialBody body, Camera camera, CelestialBody observer) {

        poseStack.pushPose();

        poseStack.translate(skyPos.x, skyPos.y + calculateApparentSize(body, getRelativePosition(body, observer)) * (Math.PI - 1.5), skyPos.z);

        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));

        float scale = 0.05f;
        poseStack.scale(-scale, -scale, scale);

        Font font = minecraft.font;
        Component text = Component.literal(name);
        int textWidth = font.width(text);

        int bgColor = 0x80000000;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        font.drawInBatch(text, -textWidth / 2f, 0, 0xFFFFFF, false,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL,
                bgColor, 15728880);

        bufferSource.endBatch();

        poseStack.popPose();
    }

    private static float calculateApparentSize(CelestialBody body, Vec3 relativePos) {
        double distanceKm = relativePos.length();
        if (distanceKm == 0) return 0.01f;

        double angularRadians = 2.0 * Math.atan(body.radius / distanceKm);

        double screenSize = 100.0 * angularRadians;

        return (float) screenSize;
    }

    private static void renderStar(PoseStack poseStack, Matrix4f projectionMatrix,
                                   Star star, CelestialBody observer) {

        Vec3 relativePos = getRelativePosition(star, observer);
        Vec3 skyPos = relativePos.normalize().scale(100);
        float apparentSize = calculateApparentSize(star, relativePos);

        Vec3 color = star.getColor();
        float brightness = star.getBrightness(relativePos);
        brightness = Math.min(brightness * 2.0f, 1.0f);

        poseStack.pushPose();

        poseStack.translate(skyPos.x, skyPos.y, skyPos.z);

        SolarSystem solarSystem = SolarSystem.getInstance();
        double currentTime = solarSystem.getCurrentTime();

        double rotationAngle = (currentTime * 24.0 / star.rotationPeriod) * 2 * Math.PI;

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
                                     SolarSystem solarSystem) {

        Vec3 relativePos = getRelativePosition(body, observer);
        Vec3 skyPos = relativePos.normalize().scale(100);
        float apparentSize = calculateApparentSize(body, relativePos);

        Star lightSource = solarSystem.getPrimaryLightSource(observer);
        if (lightSource == null) return;

        Vec3 lightSourcePos = getRelativePosition(lightSource, observer);

        float brightness = calculatePlanetBrightness(body, relativePos, lightSourcePos);

        poseStack.pushPose();

        poseStack.translate(skyPos.x, skyPos.y, skyPos.z);


        Vec3 sunDirection = lightSourcePos.subtract(relativePos).normalize();

        double sunYaw = Math.atan2(sunDirection.x, sunDirection.z);
        double sunPitch = -Math.asin(sunDirection.y);

        poseStack.mulPose(Axis.YP.rotation((float) sunYaw));
        poseStack.mulPose(Axis.XP.rotation((float) sunPitch));

        double currentTime = solarSystem.getCurrentTime();
        double rotationAngle = (currentTime * 24.0 / body.rotationPeriod) * 2 * Math.PI;

        poseStack.mulPose(Axis.ZP.rotation((float) body.axialTilt));
        poseStack.mulPose(Axis.YP.rotation((float) rotationAngle));

        poseStack.scale(apparentSize, apparentSize, apparentSize);

        RenderSystem.setShaderTexture(0, body.texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0f);

        sphereBuffer.bind();
        assert GameRenderer.getPositionTexColorShader() != null;
        sphereBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix,
                GameRenderer.getPositionTexColorShader());
        VertexBuffer.unbind();

        poseStack.popPose();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static float calculatePlanetBrightness(CelestialBody body, Vec3 bodyPos, Vec3 lightPos) {
        double distanceToLight = lightPos.length();
        double distanceToObserver = bodyPos.length();

        if (distanceToLight == 0 || distanceToObserver == 0) return 1.0f;

        double sunIllumination = 1.0 / (distanceToLight * distanceToLight);
        double apparentBrightness = (body.albedo * body.radius * body.radius * sunIllumination) /
                (distanceToObserver * distanceToObserver);

        apparentBrightness *= 2.4e21;

        return Math.min(Math.max((float)apparentBrightness, 0.15f), 1.0f);
    }


    private static Vec3 getRelativePosition(CelestialBody target, CelestialBody observer) {
        Vec3 targetPos = positionCache.getOrDefault(target, Vec3.ZERO);
        Vec3 observerPos = observer != null ? positionCache.getOrDefault(observer, Vec3.ZERO) : Vec3.ZERO;
        return targetPos.subtract(observerPos);
    }

    private static void updatePositionCache() {
        SolarSystem solarSystem = SolarSystem.getInstance();
        positionCache.clear();

        for (CelestialBody body : solarSystem.getBodies()) {
            Vec3 absolutePos = solarSystem.getAbsolutePosition(body);
            positionCache.put(body, absolutePos);
        }
    }

    public static void cleanup() {
        if (sphereBuffer != null) {
            sphereBuffer.close();
        }
        initialized = false;
    }
}
