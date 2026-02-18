package net.dahicksfamily.sgt.space;

import net.dahicksfamily.sgt.time.GlobalTime;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class SolarSystem {
    private Star centralStar;
    private List<CelestialBody> bodies;
    private List<Star> stars;
    private Vec3 barycenter;
    private static SolarSystem instance;

    private SolarSystem() {
        this.bodies = new ArrayList<>();
        this.stars = new ArrayList<>();
        this.barycenter = Vec3.ZERO;
    }

    public static SolarSystem getInstance() {
        if (instance == null) {
            instance = new SolarSystem();
        }
        return instance;
    }

    public void initialize() {
        this.bodies = PlanetsProvider.getAllBodies();
        this.stars = PlanetsProvider.getAllStars();

        if (!stars.isEmpty()) {
            this.centralStar = stars.get(0);
        }

        updateBarycenter();
    }

    public void tick() {
        updateBarycenter();
    }

    private void updateBarycenter() {
        if (bodies.isEmpty()) {
            barycenter = Vec3.ZERO;
            return;
        }

        double currentTime = getCurrentTime();
        double totalMass = 0;
        Vec3 weightedPosition = Vec3.ZERO;

        for (CelestialBody body : bodies) {
            Vec3 pos = body.getPositionAtTime(currentTime);
            weightedPosition = weightedPosition.add(pos.scale(body.mass));
            totalMass += body.mass;
        }

        if (totalMass > 0) {
            barycenter = weightedPosition.scale(1.0 / totalMass);
        }
    }

    public Vec3 getAbsolutePosition(CelestialBody body) {
        Vec3 relativePos = body.getPositionAtTime(getCurrentTime());

        if (body.parent != null) {
            relativePos = relativePos.add(getAbsolutePosition(body.parent));
        }

        return relativePos;
    }

    public Vec3 getRelativePosition(CelestialBody target, CelestialBody observer) {
        Vec3 targetPos = getAbsolutePosition(target);
        Vec3 observerPos = getAbsolutePosition(observer);
        return targetPos.subtract(observerPos);
    }

    public CelestialBody getBodyAtDimension(ResourceKey<Level> dimension) {
        return PlanetsProvider.getBodyByDimension(dimension);
    }

    public List<CelestialBody> getVisibleBodies(ResourceKey<Level> dimension) {
        return PlanetsProvider.getVisibleBodies(dimension);
    }

    public Vec3 getGravitationalAcceleration(Vec3 position, ResourceKey<Level> dimension) {
        Vec3 totalAccel = Vec3.ZERO;

        for (CelestialBody body : bodies) {
            Vec3 bodyPos = getAbsolutePosition(body);
            Vec3 toBody = bodyPos.subtract(position);
            double distance = toBody.length();

            if (distance > 0) {
                double G = 6.67430e-11;
                double accelMagnitude = G * body.mass / (distance * distance);
                Vec3 accelDirection = toBody.normalize();
                totalAccel = totalAccel.add(accelDirection.scale(accelMagnitude));
            }
        }

        return totalAccel;
    }

    public Star getPrimaryLightSource(CelestialBody observer) {
        return centralStar;
    }

    public float getSkyBrightness(CelestialBody observer) {
        if (centralStar == null) return 0.0f;

        Vec3 relativePos = getRelativePosition(centralStar, observer);
        double distance = relativePos.length();

        if (distance == 0) return 1.0f;

        return centralStar.getBrightness(relativePos);
    }

    public double getCurrentTime() {
        return GlobalTime.getInstance().getTotalDays();
    }

    public List<CelestialBody> getBodies() {
        return new ArrayList<>(bodies);
    }

    public List<Star> getStars() {
        return new ArrayList<>(stars);
    }

    public Vec3 getBarycenter() {
        return barycenter;
    }

    public Star getCentralStar() {
        return centralStar;
    }
}