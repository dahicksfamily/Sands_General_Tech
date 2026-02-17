package net.dahicksfamily.sgt.space;

import net.dahicksfamily.sgt.dimension.ModDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlanetsProvider {
    private static final List<CelestialBody> CELESTIAL_BODIES = new ArrayList<>();
    private static final List<Star> STARS = new ArrayList<>();
    private static final Map<CelestialBody, ResourceKey<Level>> BODY_DIMENSIONS = new HashMap<>();

    public static void registerCelestialBodies() {
        Star sol = new Star();
        sol.name = "Sun";
        sol.mass = 1.989e30;
        sol.radius = 696340;
        sol.luminosity = 1.0;
        sol.temperature = 5778;
        sol.spectralType = "G2V";
        sol.rotationPeriod = 609.12; // hours (25.05 days)
        sol.axialTilt = Math.toRadians(7.25);
        sol.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/sol.png");
        sol.parent = null;
        sol.tidallyLocked = false;
        sol.longitudeAtEpoch = 0;
        registerStar(sol);

        CelestialBody earth = new CelestialBody();
        earth.name = "Earth";
        earth.mass = 5.972e24;
        earth.radius = 6371;
        earth.rotationPeriod = 23.9345; // hours (sidereal day)
        earth.axialTilt = Math.toRadians(23.44);
        earth.albedo = 0.306f;
        earth.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/earth.png");
        earth.tidallyLocked = false;
        earth.longitudeAtEpoch = 0;

        earth.parent = sol;
        earth.semiMajorAxis = 1.0;
        earth.eccentricity = 0.0167;
        earth.inclination = 0;
        earth.longitudeOfAscendingNode = 0;
        earth.argumentOfPeriapsis = Math.toRadians(102.94);
        earth.meanAnomalyAtEpoch = 0;
        earth.epoch = 0;
        earth.period = 365.25;

        registerCelestialBody(earth);
        assignDimension(earth, Level.OVERWORLD);

        CelestialBody luna = new CelestialBody();
        luna.name = "Moon";
        luna.mass = 7.342e22;
        luna.radius = 1737.4;
        luna.rotationPeriod = 655.728; // hours (27.32 days)
        luna.axialTilt = Math.toRadians(1.54);
        luna.albedo = 0.12f;
        luna.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/luna.png");
        luna.tidallyLocked = true;
        luna.tidalLockingOffset = Math.toRadians(0); // 0 = front of texture faces Earth
        luna.longitudeAtEpoch = 0;

        luna.parent = earth;
        luna.semiMajorAxis = 384400;
        luna.eccentricity = 0.0549;
        luna.inclination = Math.toRadians(5.145);
        luna.longitudeOfAscendingNode = 0;
        luna.argumentOfPeriapsis = 0;
        luna.meanAnomalyAtEpoch = 0;
        luna.epoch = 0;
        luna.period = 27.32;

        registerCelestialBody(luna);

        CelestialBody mars = new CelestialBody();
        mars.name = "Mars";
        mars.mass = 6.4171e23;
        mars.radius = 3389.5;
        mars.rotationPeriod = 24.6229; // hours (slightly longer than Earth)
        mars.axialTilt = Math.toRadians(25.19);
        mars.albedo = 0.25f;
        mars.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/mars.png");
        mars.tidallyLocked = false;
        mars.longitudeAtEpoch = 0;

        mars.parent = sol;
        mars.semiMajorAxis = 1.524;
        mars.eccentricity = 0.0934;
        mars.inclination = Math.toRadians(1.85);
        mars.longitudeOfAscendingNode = Math.toRadians(49.58);
        mars.argumentOfPeriapsis = Math.toRadians(286.5);
        mars.meanAnomalyAtEpoch = 0;
        mars.epoch = 0;
        mars.period = 686.98;

        registerCelestialBody(mars);
    }

    private static void registerCelestialBody(CelestialBody body) {
        CELESTIAL_BODIES.add(body);
    }

    private static void registerStar(Star star) {
        STARS.add(star);
        CELESTIAL_BODIES.add(star);
    }

    public static void assignDimension(CelestialBody body, ResourceKey<Level> dimension) {
        BODY_DIMENSIONS.put(body, dimension);
    }

    public static List<CelestialBody> getAllBodies() {
        return new ArrayList<>(CELESTIAL_BODIES);
    }

    public static List<Star> getAllStars() {
        return new ArrayList<>(STARS);
    }

    public static CelestialBody getBodyByName(String name) {
        return CELESTIAL_BODIES.stream()
                .filter(body -> body.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    public static ResourceKey<Level> getDimension(CelestialBody body) {
        return BODY_DIMENSIONS.get(body);
    }

    public static CelestialBody getBodyByDimension(ResourceKey<Level> dimension) {
        return BODY_DIMENSIONS.entrySet().stream()
                .filter(entry -> entry.getValue().equals(dimension))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public static List<CelestialBody> getVisibleBodies(ResourceKey<Level> currentDimension) {
        CelestialBody currentBody = getBodyByDimension(currentDimension);

        if (currentDimension.equals(ModDimensions.SPACE_LEVEL_KEY)) {
            return new ArrayList<>(CELESTIAL_BODIES);
        }

        if (currentBody == null) {
            currentBody = getBodyByName("Earth");
        }

        List<CelestialBody> visible = new ArrayList<>();

        for (CelestialBody body : CELESTIAL_BODIES) {
            if (body != currentBody) {
                visible.add(body);
            }
        }

        return visible;
    }

    public static List<CelestialBody> getOrbitingBodies(CelestialBody parent) {
        return CELESTIAL_BODIES.stream()
                .filter(body -> body.parent == parent)
                .collect(Collectors.toList());
    }
}
