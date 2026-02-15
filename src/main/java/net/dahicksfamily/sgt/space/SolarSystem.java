package net.dahicksfamily.sgt.space;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class SolarSystem {
    // The star at the center of this solar system
    private Star centralStar;

    // All bodies in this system
    private List<CelestialBody> bodies;
    private List<Star> stars;

    // Time management
    private double currentTime; // In-game time (ticks or custom unit)
    private double timeScale; // How fast time passes (1.0 = real time)

    // Barycenter (center of mass) - for multi-star systems
    private Vec3 barycenter;

    // Singleton instance (you probably only have one solar system)
    private static SolarSystem instance;

    private SolarSystem() {
        this.bodies = new ArrayList<>();
        this.stars = new ArrayList<>();
        this.currentTime = 0;
        this.timeScale = 1.0 / 24000.0;
        this.barycenter = Vec3.ZERO;
    }

    public static SolarSystem getInstance() {
        if (instance == null) {
            instance = new SolarSystem();
        }
        return instance;
    }

    /**
     * Initialize the solar system with all celestial bodies
     */
    public void initialize() {
        // Get all bodies from PlanetsProvider
        this.bodies = PlanetsProvider.getAllBodies();
        this.stars = PlanetsProvider.getAllStars();

        // Set the central star (assuming first star is the sun)
        if (!stars.isEmpty()) {
            this.centralStar = stars.get(0);
        }

        // Calculate initial barycenter
        updateBarycenter();
    }

    /**
     * Update the solar system state
     * Call this every tick from your tick event
     */
    public void tick() {
        // Increment time based on timescale
        currentTime += timeScale;

        // Update positions of all bodies
        // Positions are calculated on-demand via getPositionAtTime()

        // Update barycenter if needed (for multi-body systems)
        if (timeScale > 0) {
            updateBarycenter();
        }
    }

    /**
     * Calculate the barycenter (center of mass) of the system
     */
    private void updateBarycenter() {
        if (bodies.isEmpty()) {
            barycenter = Vec3.ZERO;
            return;
        }

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

    /**
     * Get absolute position of a body (relative to barycenter)
     */
    public Vec3 getAbsolutePosition(CelestialBody body) {
        Vec3 relativePos = body.getPositionAtTime(currentTime);

        // If body has a parent, add parent's position recursively
        if (body.parent != null) {
            relativePos = relativePos.add(getAbsolutePosition(body.parent));
        }

        return relativePos;
    }

    /**
     * Get position of a body relative to another body (for rendering)
     * e.g., Moon position as seen from Earth
     */
    public Vec3 getRelativePosition(CelestialBody target, CelestialBody observer) {
        Vec3 targetPos = getAbsolutePosition(target);
        Vec3 observerPos = getAbsolutePosition(observer);
        return targetPos.subtract(observerPos);
    }

    /**
     * Get the body at a specific dimension
     */
    public CelestialBody getBodyAtDimension(ResourceKey<Level> dimension) {
        return PlanetsProvider.getBodyByDimension(dimension);
    }

    /**
     * Get all bodies visible from a specific location
     */
    public List<CelestialBody> getVisibleBodies(ResourceKey<Level> dimension) {
        return PlanetsProvider.getVisibleBodies(dimension);
    }

    /**
     * Calculate gravitational acceleration at a point
     * Useful for custom gravity in space dimension
     */
    public Vec3 getGravitationalAcceleration(Vec3 position, ResourceKey<Level> dimension) {
        Vec3 totalAccel = Vec3.ZERO;

        for (CelestialBody body : bodies) {
            Vec3 bodyPos = getAbsolutePosition(body);
            Vec3 toBody = bodyPos.subtract(position);
            double distance = toBody.length();

            if (distance > 0) {
                // F = GMm/r², a = GM/r²
                double G = 6.67430e-11; // Gravitational constant
                double accelMagnitude = G * body.mass / (distance * distance);
                Vec3 accelDirection = toBody.normalize();
                totalAccel = totalAccel.add(accelDirection.scale(accelMagnitude));
            }
        }

        return totalAccel;
    }

    /**
     * Get the primary light source (star) for a location
     */
    public Star getPrimaryLightSource(CelestialBody observer) {
        // For now, just return the central star
        // Could be enhanced for binary star systems
        return centralStar;
    }

    /**
     * Calculate sky brightness at observer location
     * Useful for custom sky rendering
     */
    public float getSkyBrightness(CelestialBody observer) {
        if (centralStar == null) return 0.0f;

        Vec3 relativePos = getRelativePosition(centralStar, observer);
        double distance = relativePos.length();

        if (distance == 0) return 1.0f;

        // Inverse square law
        return centralStar.getBrightness(relativePos);
    }

    /**
     * Get time in various formats
     */
    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double time) {
        this.currentTime = time;
    }

    public double getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(double scale) {
        this.timeScale = scale;
    }

    /**
     * Convert Minecraft world time to solar system time
     */
    public double getTimeFromWorldTime(long worldTime) {
        // Minecraft: 24000 ticks = 1 day
        // You can scale this however you want
        return worldTime / 24000.0; // Returns time in Minecraft days
    }

    /**
     * Sync solar system time with Minecraft world time
     */
    public void syncWithWorld(Level level) {
        this.currentTime = getTimeFromWorldTime(level.getDayTime());
    }

    // Getters
    public Star getCentralStar() {
        return centralStar;
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
}
