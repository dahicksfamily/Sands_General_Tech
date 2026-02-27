package net.dahicksfamily.sgt.space;

import net.dahicksfamily.sgt.space.atmosphere.Atmosphere;
import net.dahicksfamily.sgt.dimension.ModDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
        sol.rotationPeriod = 609.12;
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
        earth.rotationPeriod = 23.9345;
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
        earth.atmosphere = Atmosphere.earthLike();
        registerCelestialBody(earth);
        assignDimension(earth, Level.OVERWORLD);

        CelestialBody luna = new CelestialBody();
        luna.name = "Moon";
        luna.mass = 7.342e22;
        luna.radius = 1737.4;
        luna.rotationPeriod = 655.728;
        luna.axialTilt = Math.toRadians(1.54);
        luna.albedo = 0.12f;
        luna.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/luna.png");
        luna.tidallyLocked = true;
        luna.tidalLockingOffset = Math.toRadians(90);
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
        mars.rotationPeriod = 24.6229;
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
        mars.atmosphere = Atmosphere.marsLike();
        registerCelestialBody(mars);

        CelestialBody mercury = new CelestialBody();
        mercury.name = "Mercury";
        mercury.mass = 3.285e23;
        mercury.radius = 2439.7;
        mercury.rotationPeriod = 1407.6;
        mercury.axialTilt = Math.toRadians(0.034);
        mercury.albedo = 0.088f;
        mercury.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/mercury.png");
        mercury.tidallyLocked = false;
        mercury.longitudeAtEpoch = 0;
        mercury.parent = sol;
        mercury.semiMajorAxis = 0.387;
        mercury.eccentricity = 0.2056;
        mercury.inclination = Math.toRadians(7.005);
        mercury.longitudeOfAscendingNode = Math.toRadians(48.33);
        mercury.argumentOfPeriapsis = Math.toRadians(29.12);
        mercury.meanAnomalyAtEpoch = 0;
        mercury.epoch = 0;
        mercury.period = 87.97;
        registerCelestialBody(mercury);

        CelestialBody venus = new CelestialBody();
        venus.name = "Venus";
        venus.mass = 4.867e24;
        venus.radius = 6051.8;
        venus.rotationPeriod = -5832.5;
        venus.axialTilt = Math.toRadians(177.36);
        venus.albedo = 0.689f;
        venus.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/venus.png");
        venus.tidallyLocked = false;
        venus.longitudeAtEpoch = 0;
        venus.parent = sol;
        venus.semiMajorAxis = 0.723;
        venus.eccentricity = 0.0067;
        venus.inclination = Math.toRadians(3.394);
        venus.longitudeOfAscendingNode = Math.toRadians(76.68);
        venus.argumentOfPeriapsis = Math.toRadians(54.85);
        venus.meanAnomalyAtEpoch = 0;
        venus.epoch = 0;
        venus.period = 224.70;
        venus.atmosphere = Atmosphere.venusLike();
        registerCelestialBody(venus);

        CelestialBody jupiter = new CelestialBody();
        jupiter.name = "Jupiter";
        jupiter.mass = 1.898e27;
        jupiter.radius = 69911;
        jupiter.rotationPeriod = 9.925;
        jupiter.axialTilt = Math.toRadians(3.13);
        jupiter.albedo = 0.343f;
        jupiter.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/jupiter.png");
        jupiter.tidallyLocked = false;
        jupiter.longitudeAtEpoch = 0;
        jupiter.parent = sol;
        jupiter.semiMajorAxis = 5.203;
        jupiter.eccentricity = 0.0489;
        jupiter.inclination = Math.toRadians(1.303);
        jupiter.longitudeOfAscendingNode = Math.toRadians(100.46);
        jupiter.argumentOfPeriapsis = Math.toRadians(273.87);
        jupiter.meanAnomalyAtEpoch = 0;
        jupiter.epoch = 0;
        jupiter.period = 4332.59;
        registerCelestialBody(jupiter);

        CelestialBody saturn = new CelestialBody();
        saturn.name = "Saturn";
        saturn.mass = 5.683e26;
        saturn.radius = 58232;
        saturn.rotationPeriod = 10.656;
        saturn.axialTilt = Math.toRadians(26.73);
        saturn.albedo = 0.342f;
        saturn.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/saturn.png");
        saturn.tidallyLocked = false;
        saturn.longitudeAtEpoch = 0;
        saturn.parent = sol;
        saturn.semiMajorAxis = 9.537;
        saturn.eccentricity = 0.0565;
        saturn.inclination = Math.toRadians(2.485);
        saturn.longitudeOfAscendingNode = Math.toRadians(113.72);
        saturn.argumentOfPeriapsis = Math.toRadians(339.39);
        saturn.meanAnomalyAtEpoch = 0;
        saturn.epoch = 0;
        saturn.period = 10759.22;
        registerCelestialBody(saturn);

        CelestialBody uranus = new CelestialBody();
        uranus.name = "Uranus";
        uranus.mass = 8.681e25;
        uranus.radius = 25362;
        uranus.rotationPeriod = -17.24;
        uranus.axialTilt = Math.toRadians(97.77);
        uranus.albedo = 0.300f;
        uranus.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/uranus.png");
        uranus.tidallyLocked = false;
        uranus.longitudeAtEpoch = 0;
        uranus.parent = sol;
        uranus.semiMajorAxis = 19.191;
        uranus.eccentricity = 0.0457;
        uranus.inclination = Math.toRadians(0.772);
        uranus.longitudeOfAscendingNode = Math.toRadians(74.01);
        uranus.argumentOfPeriapsis = Math.toRadians(96.54);
        uranus.meanAnomalyAtEpoch = 0;
        uranus.epoch = 0;
        uranus.period = 30688.5;
        registerCelestialBody(uranus);

        CelestialBody neptune = new CelestialBody();
        neptune.name = "Neptune";
        neptune.mass = 1.024e26;
        neptune.radius = 24622;
        neptune.rotationPeriod = 16.11;
        neptune.axialTilt = Math.toRadians(28.32);
        neptune.albedo = 0.290f;
        neptune.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/neptune.png");
        neptune.tidallyLocked = false;
        neptune.longitudeAtEpoch = 0;
        neptune.parent = sol;
        neptune.semiMajorAxis = 30.069;
        neptune.eccentricity = 0.0113;
        neptune.inclination = Math.toRadians(1.769);
        neptune.longitudeOfAscendingNode = Math.toRadians(131.72);
        neptune.argumentOfPeriapsis = Math.toRadians(273.19);
        neptune.meanAnomalyAtEpoch = 0;
        neptune.epoch = 0;
        neptune.period = 60182.0;
        registerCelestialBody(neptune);

        CelestialBody pluto = new CelestialBody();
        pluto.name = "Pluto";
        pluto.mass = 1.309e22;
        pluto.radius = 1188.3;
        pluto.rotationPeriod = -153.2928;
        pluto.axialTilt = Math.toRadians(122.53);
        pluto.albedo = 0.52f;
        pluto.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/pluto.png");
        pluto.tidallyLocked = true;
        pluto.longitudeAtEpoch = 0;
        pluto.eccentricity = 0.2488;
        pluto.inclination = Math.toRadians(17.16);
        pluto.longitudeOfAscendingNode = Math.toRadians(110.30);
        pluto.argumentOfPeriapsis = Math.toRadians(113.76);
        pluto.meanAnomalyAtEpoch = 0;
        pluto.epoch = 0;
        pluto.period = 90560;
        Atmosphere plutoAtmo = new Atmosphere();
        plutoAtmo.surfaceDensity      = 0.00001f;
        plutoAtmo.scaleHeight         = 0.20f;
        plutoAtmo.rayleighCoeff       = new Vec3(0.60, 0.78, 1.0);
        plutoAtmo.mieDensity          = 0.01f;
        plutoAtmo.airglowColor        = new Vec3(0.3, 0.6, 1.0);
        plutoAtmo.airglowIntensity    = 0.008f;
        plutoAtmo.terminatorBandColor     = new Vec3(0.6, 0.7, 1.0);
        plutoAtmo.terminatorBandIntensity = 0.25f;
        pluto.atmosphere = plutoAtmo;
        registerCelestialBody(pluto);

        CelestialBody phobos = new CelestialBody();
        phobos.name = "Phobos";
        phobos.mass = 1.0659e16;
        phobos.radius = 11.2667;
        phobos.rotationPeriod = 7.65;
        phobos.axialTilt = 0;
        phobos.albedo = 0.071f;
        phobos.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/phobos.png");
        phobos.tidallyLocked = true;
        phobos.tidalLockingOffset = 0;
        phobos.longitudeAtEpoch = 0;
        phobos.parent = mars;
        phobos.semiMajorAxis = 9376;
        phobos.eccentricity = 0.0151;
        phobos.inclination = 0;
        phobos.longitudeOfAscendingNode = 0;
        phobos.argumentOfPeriapsis = 0;
        phobos.meanAnomalyAtEpoch = 0;
        phobos.epoch = 0;
        phobos.period = 0.3189;
        registerCelestialBody(phobos);

        CelestialBody deimos = new CelestialBody();
        deimos.name = "Deimos";
        deimos.mass = 1.4762e15;
        deimos.radius = 6.2;
        deimos.rotationPeriod = 30.35;
        deimos.axialTilt = 0;
        deimos.albedo = 0.068f;
        deimos.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/deimos.png");
        deimos.tidallyLocked = true;
        deimos.tidalLockingOffset = 0;
        deimos.longitudeAtEpoch = 0;
        deimos.parent = mars;
        deimos.semiMajorAxis = 23463;
        deimos.eccentricity = 0.0002;
        deimos.inclination = 0;
        deimos.longitudeOfAscendingNode = 0;
        deimos.argumentOfPeriapsis = 0;
        deimos.meanAnomalyAtEpoch = 0;
        deimos.epoch = 0;
        deimos.period = 1.263;
        registerCelestialBody(deimos);

        CelestialBody io = new CelestialBody();
        io.name = "Io";
        io.mass = 8.93e22;
        io.radius = 1821.6;
        io.rotationPeriod = 42.46;
        io.axialTilt = 0;
        io.albedo = 0.63f;
        io.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/io.png");
        io.tidallyLocked = true;
        io.tidalLockingOffset = 0;
        io.longitudeAtEpoch = 0;
        io.parent = jupiter;
        io.semiMajorAxis = 421700;
        io.eccentricity = 0.0041;
        io.inclination = 0;
        io.longitudeOfAscendingNode = 0;
        io.argumentOfPeriapsis = 0;
        io.meanAnomalyAtEpoch = 0;
        io.epoch = 0;
        io.period = 1.769;
        Atmosphere ioAtmo = new Atmosphere();
        ioAtmo.surfaceDensity      = 0.0001f;
        ioAtmo.scaleHeight         = 0.08f;
        ioAtmo.outerHeightFraction = 0.04f;
        ioAtmo.rayleighCoeff       = new Vec3(1.0, 0.85, 0.50);
        ioAtmo.mieDensity          = 0.02f;
        ioAtmo.airglowColor        = new Vec3(1.0, 0.8, 0.05);
        ioAtmo.airglowIntensity    = 0.04f;
        ioAtmo.shadowSoftness      = 0.08f;
        ioAtmo.terminatorBandColor     = new Vec3(1.0, 0.8, 0.1);
        ioAtmo.terminatorBandIntensity = 0.5f;
        io.atmosphere = ioAtmo;
        registerCelestialBody(io);

        CelestialBody europa = new CelestialBody();
        europa.name = "Europa";
        europa.mass = 4.80e22;
        europa.radius = 1560.8;
        europa.rotationPeriod = 85.23;
        europa.axialTilt = 0;
        europa.albedo = 0.67f;
        europa.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/europa.png");
        europa.tidallyLocked = true;
        europa.tidalLockingOffset = 0;
        europa.longitudeAtEpoch = 0;
        europa.parent = jupiter;
        europa.semiMajorAxis = 670900;
        europa.eccentricity = 0.009;
        europa.inclination = 0;
        europa.longitudeOfAscendingNode = 0;
        europa.argumentOfPeriapsis = 0;
        europa.meanAnomalyAtEpoch = 0;
        europa.epoch = 0;
        europa.period = 3.551;
        Atmosphere europaAtmo = new Atmosphere();
        europaAtmo.surfaceDensity      = 0.00001f;
        europaAtmo.scaleHeight         = 0.10f;
        europaAtmo.outerHeightFraction = 0.04f;
        europaAtmo.rayleighCoeff       = new Vec3(0.85, 0.90, 1.0);
        europaAtmo.mieDensity          = 0.0f;
        europaAtmo.airglowColor        = new Vec3(0.2, 0.55, 1.0);
        europaAtmo.airglowIntensity    = 0.012f;
        europaAtmo.terminatorBandColor     = new Vec3(0.4, 0.7, 1.0);
        europaAtmo.terminatorBandIntensity = 0.2f;
        europa.atmosphere = europaAtmo;
        registerCelestialBody(europa);

        CelestialBody ganymede = new CelestialBody();
        ganymede.name = "Ganymede";
        ganymede.mass = 1.48e23;
        ganymede.radius = 2634.1;
        ganymede.rotationPeriod = 171.7;
        ganymede.axialTilt = 0;
        ganymede.albedo = 0.43f;
        ganymede.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/ganymede.png");
        ganymede.tidallyLocked = true;
        ganymede.tidalLockingOffset = 0;
        ganymede.longitudeAtEpoch = 0;
        ganymede.parent = jupiter;
        ganymede.semiMajorAxis = 1070400;
        ganymede.eccentricity = 0.0013;
        ganymede.inclination = 0;
        ganymede.longitudeOfAscendingNode = 0;
        ganymede.argumentOfPeriapsis = 0;
        ganymede.meanAnomalyAtEpoch = 0;
        ganymede.epoch = 0;
        ganymede.period = 7.155;
        registerCelestialBody(ganymede);

        CelestialBody callisto = new CelestialBody();
        callisto.name = "Callisto";
        callisto.mass = 1.08e23;
        callisto.radius = 2410.3;
        callisto.rotationPeriod = 400.5;
        callisto.axialTilt = 0;
        callisto.albedo = 0.19f;
        callisto.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/callisto.png");
        callisto.tidallyLocked = true;
        callisto.tidalLockingOffset = 0;
        callisto.longitudeAtEpoch = 0;
        callisto.parent = jupiter;
        callisto.semiMajorAxis = 1882700;
        callisto.eccentricity = 0.0074;
        callisto.inclination = 0;
        callisto.longitudeOfAscendingNode = 0;
        callisto.argumentOfPeriapsis = 0;
        callisto.meanAnomalyAtEpoch = 0;
        callisto.epoch = 0;
        callisto.period = 16.689;
        registerCelestialBody(callisto);

        CelestialBody titan = new CelestialBody();
        titan.name = "Titan";
        titan.mass = 1.345e23;
        titan.radius = 2574.7;
        titan.rotationPeriod = 382.68;
        titan.axialTilt = 0;
        titan.albedo = 0.22f;
        titan.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/titan.png");
        titan.tidallyLocked = true;
        titan.tidalLockingOffset = 0;
        titan.longitudeAtEpoch = 0;
        titan.parent = saturn;
        titan.semiMajorAxis = 1221870;
        titan.eccentricity = 0.0288;
        titan.inclination = 0;
        titan.longitudeOfAscendingNode = 0;
        titan.argumentOfPeriapsis = 0;
        titan.meanAnomalyAtEpoch = 0;
        titan.epoch = 0;
        titan.period = 15.945;
        titan.atmosphere = Atmosphere.titanLike();
        registerCelestialBody(titan);

        CelestialBody triton = new CelestialBody();
        triton.name = "Triton";
        triton.mass = 2.14e22;
        triton.radius = 1353.4;
        triton.rotationPeriod = -141.04;
        triton.axialTilt = 0;
        triton.albedo = 0.76f;
        triton.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/triton.png");
        triton.tidallyLocked = true;
        triton.tidalLockingOffset = 0;
        triton.longitudeAtEpoch = 0;
        triton.parent = neptune;
        triton.semiMajorAxis = 354759;
        triton.eccentricity = 0.000016;
        triton.inclination = Math.toRadians(156.865);
        triton.longitudeOfAscendingNode = 0;
        triton.argumentOfPeriapsis = 0;
        triton.meanAnomalyAtEpoch = 0;
        triton.epoch = 0;
        triton.period = 5.877;
        Atmosphere tritonAtmo = new Atmosphere();
        tritonAtmo.surfaceDensity      = 0.00001f;
        tritonAtmo.scaleHeight         = 0.15f;
        tritonAtmo.outerHeightFraction = 0.06f;
        tritonAtmo.rayleighCoeff       = new Vec3(0.91, 0.87, 1.0);
        tritonAtmo.mieDensity          = 0.03f;
        tritonAtmo.airglowColor        = new Vec3(0.9, 0.4, 0.7);
        tritonAtmo.airglowIntensity    = 0.02f;
        tritonAtmo.terminatorBandColor     = new Vec3(0.9, 0.5, 0.8);
        tritonAtmo.terminatorBandIntensity = 0.3f;
        triton.atmosphere = tritonAtmo;
        registerCelestialBody(triton);

        CelestialBody charon = new CelestialBody();
        charon.name = "Charon";
        charon.mass = 1.586e21;
        charon.radius = 606.0;
        charon.rotationPeriod = -153.2928; // tidally locked with Pluto
        charon.axialTilt = 0;
        charon.albedo = 0.38f;
        charon.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/charon.png");
        charon.tidallyLocked = true;
        charon.tidalLockingOffset = 0;
        charon.longitudeAtEpoch = 0;
        charon.eccentricity = 0.0;
        charon.inclination = 0;
        charon.longitudeOfAscendingNode = 0;
        charon.argumentOfPeriapsis = 0;
        charon.meanAnomalyAtEpoch = 0;
        charon.epoch = 0;
        charon.period = 6.387; // orbital period around Pluto barycenter (days)

        registerCelestialBody(charon);

        Barycenter plutoBary = Barycenter.of("Pluto-Charon Barycenter",
                sol, 39.48, 90560.0, 0.249, Math.toRadians(17.1), 0.0);

        pluto.parent = plutoBary;
        pluto.semiMajorAxis = 2035;       // km - Pluto offset from barycentre

        charon.parent = plutoBary;
        charon.semiMajorAxis = 17536;     // km - Charon offset (opposite side)
        charon.longitudeAtEpoch = Math.PI; // starts 180° from Pluto

        assignDimension(charon, ModDimensions.SPACE_LEVEL_KEY);
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