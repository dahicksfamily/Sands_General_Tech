package net.dahicksfamily.sgt.SkyRendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.dahicksfamily.sgt.client.ModShaders;
import net.dahicksfamily.sgt.space.CelestialBody;
import net.dahicksfamily.sgt.space.SolarSystem;
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

        for (CelestialBody body : visibleBodies) {
            Vec3 rel = getRelativePosition(body, observer);
            Vec3 sky = rel.normalize().scale(100);
            if (body instanceof Star) {
                renderStar(poseStack, projectionMatrix, (Star) body, observer);
            } else {
                renderPlanet(poseStack, projectionMatrix, body, observer, solarSystem);
            }

            if (showLabels) {
                Vec3 relativePos = getRelativePosition(body, observer);
                Vec3 skyPos = relativePos.normalize().scale(100);
                renderLabel(poseStack, projectionMatrix, body.name, skyPos, body, camera, observer);
            }
        }

        poseStack.popPose();
    }

    private static void renderLabel(PoseStack poseStack, Matrix4f projectionMatrix,
                                    String name, Vec3 skyPos, CelestialBody body, Camera camera, CelestialBody observer) {

        if (observer != null && body.name.equals(observer.name)) {
            return;
        }

        Vec3 relativePos = getRelativePosition(body, observer);
        if (relativePos.length() == 0) return;

        poseStack.pushPose();

        poseStack.translate(skyPos.x, skyPos.y + calculateApparentSize(body, relativePos) * (Math.PI - 1.5), skyPos.z);

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
        if (relativePos.length() == 0) return;

        Vec3 skyPos = relativePos.normalize().scale(100);
        float apparentSize = calculateApparentSize(star, relativePos);

        Vec3 color = star.getColor();
        float brightness = star.getBrightness(relativePos);
        brightness = Math.min(brightness * 2.0f, 1.0f);

        poseStack.pushPose();

        poseStack.translate(skyPos.x, skyPos.y, skyPos.z);

        SolarSystem solarSystem = SolarSystem.getInstance();
        double currentTime = solarSystem.getCurrentTime();
        double rotationAngle = star.getRotationAngle(currentTime);

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
        if (relativePos.length() == 0) return;

        Vec3 skyPos = relativePos.normalize().scale(100);
        float apparentSize = calculateApparentSize(body, relativePos);

        Star lightSource = solarSystem.getPrimaryLightSource(observer);
        if (lightSource == null) return;

        Vec3 lightSourcePos = getRelativePosition(lightSource, observer);

        poseStack.pushPose();

        poseStack.translate(skyPos.x, skyPos.y, skyPos.z);

        double currentTime = solarSystem.getCurrentTime();

        if (body.tidallyLocked &&
                body.parent != null &&
                positionCache.containsKey(body) &&
                positionCache.containsKey(body.parent)) {

            Vec3 bodyAbs = positionCache.get(body);
            Vec3 parentAbs = positionCache.get(body.parent);

            poseStack.mulPose(Axis.XP.rotation((float) body.axialTilt));
            pointToPosition(poseStack, bodyAbs, parentAbs, body.tidalLockingOffset);

        } else {
            poseStack.mulPose(Axis.YP.rotation((float) body.getRotationAngle(currentTime)));
            poseStack.mulPose(Axis.XP.rotation((float) body.axialTilt));
        }

        poseStack.scale(apparentSize, apparentSize, apparentSize);

        Vec3 lightDir = lightSourcePos.subtract(relativePos).normalize();
        Vector3f lightDirVec = new Vector3f(
                (float) lightDir.x,
                (float) lightDir.y,
                (float) lightDir.z
        );

        RenderSystem.setShaderTexture(0, body.texture);

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        ShaderInstance shader = ModShaders.getCelestialBodyShader();

        if (shader != null) {
            ModShaders.setLightDirection(lightDirVec);
            ModShaders.setAmbientLight(0.15f);
            RenderSystem.setShader(() -> shader);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        sphereBuffer.bind();
        sphereBuffer.drawWithShader(
                poseStack.last().pose(),
                projectionMatrix,
                shader != null ? shader : GameRenderer.getPositionTexColorShader()
        );

        VertexBuffer.unbind();

        poseStack.popPose();
    }

    private static void pointToPosition(PoseStack poseStack, Vec3 from, Vec3 to, double offsetRadians) {

        Vec3 direction = to.subtract(from);
        if (direction.length() == 0) return;

        direction = direction.normalize();

        Vector3f modelForward = new Vector3f(1, 0.3f, 1);
        modelForward.normalize();

        Vector3f targetDir = new Vector3f(
                (float) direction.x,
                (float) direction.y,
                (float) direction.z
        );

        Quaternionf rotation = new Quaternionf().rotationTo(modelForward, targetDir);

        poseStack.mulPose(rotation);

        poseStack.mulPose(Axis.YP.rotation((float) offsetRadians));
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
            positionCache.put(body, solarSystem.getAbsolutePosition(body));
        }
        for (Star star : solarSystem.getStars()) {
            positionCache.put(star, solarSystem.getAbsolutePosition(star));
        }
    }

    public static void cleanup() {
        if (sphereBuffer != null) {
            sphereBuffer.close();
        }
        initialized = false;
    }
}
