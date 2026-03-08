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
        CelestialRing saturnRings = new CelestialRing();
        saturnRings.innerRadius = 1.11f;
        saturnRings.outerRadius = 2.44f;
        saturnRings.opacity     = 1.0f;
        saturnRings.texture     = new ResourceLocation("sgt",
                "textures/misc/celestial/rings/saturn_rings.png");
        saturn.ring = saturnRings;
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
        CelestialRing uranusRings = new CelestialRing();
        uranusRings.innerRadius = 1.60f;
        uranusRings.outerRadius = 2.05f;
        uranusRings.opacity     = 0.65f;
        uranusRings.texture     = new ResourceLocation("sgt",
                "textures/misc/celestial/rings/uranus_rings.png");
        uranus.ring = uranusRings;
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
        CelestialRing neptuneRings = new CelestialRing();
        neptuneRings.innerRadius = 2.10f;
        neptuneRings.outerRadius = 2.60f;
        neptuneRings.opacity     = 0.45f;
        neptuneRings.texture     = new ResourceLocation("sgt",
                "textures/misc/celestial/rings/neptune_rings.png");
        neptune.ring = neptuneRings;
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
        charon.rotationPeriod = -153.2928; 
        charon.axialTilt = 0;
        charon.albedo = 0.38f;
        charon.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/charon.png");
        charon.tidallyLocked = true;
        charon.tidalLockingOffset = 0;
        charon.eccentricity = 0.0;
        charon.epoch = 0;
        charon.period = 6.387; 

        registerCelestialBody(charon);

        Barycenter plutoBary = Barycenter.of("Pluto-Charon Barycenter",
                sol, 39.48, 90560.0, 0.249, Math.toRadians(17.1), 0.0);

        pluto.parent = plutoBary;
        pluto.semiMajorAxis = 2035;
        pluto.eccentricity = 0.0;
        pluto.inclination = Math.toRadians(119.6); 
        pluto.longitudeOfAscendingNode = Math.toRadians(223.1);
        pluto.argumentOfPeriapsis = 0;
        pluto.meanAnomalyAtEpoch = 0;

        charon.parent = plutoBary;
        charon.semiMajorAxis = 17536;
        charon.eccentricity = 0.0;
        charon.inclination = Math.toRadians(119.6); 
        charon.longitudeOfAscendingNode = Math.toRadians(223.1);
        charon.argumentOfPeriapsis = 0;
        charon.meanAnomalyAtEpoch = 0;
        charon.longitudeAtEpoch = Math.PI; 

 
 
 
 

        CelestialBody amalthea = new CelestialBody();
        amalthea.name = "Amalthea";
        amalthea.mass = 2.08e18;
        amalthea.radius = 83.5;
        amalthea.rotationPeriod = 11.957; 
        amalthea.axialTilt = 0;
        amalthea.albedo = 0.09f;
        amalthea.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/amalthea.png");
        amalthea.tidallyLocked = true;
        amalthea.tidalLockingOffset = 0;
        amalthea.longitudeAtEpoch = 0;
        amalthea.parent = jupiter;
        amalthea.semiMajorAxis = 181366;
        amalthea.eccentricity = 0.0032;
        amalthea.inclination = Math.toRadians(0.374);
        amalthea.longitudeOfAscendingNode = 0;
        amalthea.argumentOfPeriapsis = 0;
        amalthea.meanAnomalyAtEpoch = 0;
        amalthea.epoch = 0;
        amalthea.period = 0.4982;
        registerCelestialBody(amalthea);

        CelestialBody thebe = new CelestialBody();
        thebe.name = "Thebe";
        thebe.mass = 4.3e17;
        thebe.radius = 49.3;
        thebe.rotationPeriod = 16.177;
        thebe.axialTilt = 0;
        thebe.albedo = 0.047f;
        thebe.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/thebe.png");
        thebe.tidallyLocked = true;
        thebe.tidalLockingOffset = 0;
        thebe.longitudeAtEpoch = 0;
        thebe.parent = jupiter;
        thebe.semiMajorAxis = 221895;
        thebe.eccentricity = 0.0177;
        thebe.inclination = Math.toRadians(1.076);
        thebe.longitudeOfAscendingNode = 0;
        thebe.argumentOfPeriapsis = 0;
        thebe.meanAnomalyAtEpoch = 0;
        thebe.epoch = 0;
        thebe.period = 0.6745;
        registerCelestialBody(thebe);

        CelestialBody himalia = new CelestialBody();
        himalia.name = "Himalia";
        himalia.mass = 4.2e18;
        himalia.radius = 85.0;
        himalia.rotationPeriod = 7.782;
        himalia.axialTilt = 0;
        himalia.albedo = 0.057f;
        himalia.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/himalia.png");
        himalia.tidallyLocked = false;
        himalia.tidalLockingOffset = 0;
        himalia.longitudeAtEpoch = 0;
        himalia.parent = jupiter;
        himalia.semiMajorAxis = 11460000;
        himalia.eccentricity = 0.1620;
        himalia.inclination = Math.toRadians(27.5);
        himalia.longitudeOfAscendingNode = 0;
        himalia.argumentOfPeriapsis = 0;
        himalia.meanAnomalyAtEpoch = 0;
        himalia.epoch = 0;
        himalia.period = 250.56;
        registerCelestialBody(himalia);

 
 
 
 

        CelestialBody mimas = new CelestialBody();
        mimas.name = "Mimas";
        mimas.mass = 3.75e19;
        mimas.radius = 198.2;
        mimas.rotationPeriod = 22.617; 
        mimas.axialTilt = 0;
        mimas.albedo = 0.962f;
        mimas.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/mimas.png");
        mimas.tidallyLocked = true;
        mimas.tidalLockingOffset = 0;
        mimas.longitudeAtEpoch = 0;
        mimas.parent = saturn;
        mimas.semiMajorAxis = 185520;
        mimas.eccentricity = 0.0196;
        mimas.inclination = Math.toRadians(1.574);
        mimas.longitudeOfAscendingNode = 0;
        mimas.argumentOfPeriapsis = 0;
        mimas.meanAnomalyAtEpoch = 0;
        mimas.epoch = 0;
        mimas.period = 0.9424;
        registerCelestialBody(mimas);

        CelestialBody enceladus = new CelestialBody();
        enceladus.name = "Enceladus";
        enceladus.mass = 1.08e20;
        enceladus.radius = 252.1;
        enceladus.rotationPeriod = 32.888;
        enceladus.axialTilt = 0;
        enceladus.albedo = 0.99f; 
        enceladus.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/enceladus.png");
        enceladus.tidallyLocked = true;
        enceladus.tidalLockingOffset = 0;
        enceladus.longitudeAtEpoch = 0;
        enceladus.parent = saturn;
        enceladus.semiMajorAxis = 238020;
        enceladus.eccentricity = 0.0047;
        enceladus.inclination = Math.toRadians(0.009);
        enceladus.longitudeOfAscendingNode = 0;
        enceladus.argumentOfPeriapsis = 0;
        enceladus.meanAnomalyAtEpoch = 0;
        enceladus.epoch = 0;
        enceladus.period = 1.370;
        Atmosphere enceladusAtmo = new Atmosphere();
        enceladusAtmo.surfaceDensity      = 0.000001f;
        enceladusAtmo.scaleHeight         = 0.10f;
        enceladusAtmo.outerHeightFraction = 0.06f;
        enceladusAtmo.rayleighCoeff       = new Vec3(0.8, 0.9, 1.0);
        enceladusAtmo.mieDensity          = 0.01f;
        enceladusAtmo.airglowColor        = new Vec3(0.6, 0.8, 1.0);
        enceladusAtmo.airglowIntensity    = 0.005f;
        enceladusAtmo.terminatorBandColor     = new Vec3(0.7, 0.85, 1.0);
        enceladusAtmo.terminatorBandIntensity = 0.1f;
        enceladus.atmosphere = enceladusAtmo;
        registerCelestialBody(enceladus);

        CelestialBody tethys = new CelestialBody();
        tethys.name = "Tethys";
        tethys.mass = 6.18e20;
        tethys.radius = 531.1;
        tethys.rotationPeriod = 45.307;
        tethys.axialTilt = 0;
        tethys.albedo = 0.80f;
        tethys.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/tethys.png");
        tethys.tidallyLocked = true;
        tethys.tidalLockingOffset = 0;
        tethys.longitudeAtEpoch = 0;
        tethys.parent = saturn;
        tethys.semiMajorAxis = 294619;
        tethys.eccentricity = 0.0001;
        tethys.inclination = Math.toRadians(1.091);
        tethys.longitudeOfAscendingNode = 0;
        tethys.argumentOfPeriapsis = 0;
        tethys.meanAnomalyAtEpoch = 0;
        tethys.epoch = 0;
        tethys.period = 1.888;
        registerCelestialBody(tethys);

        CelestialBody dione = new CelestialBody();
        dione.name = "Dione";
        dione.mass = 1.095e21;
        dione.radius = 561.4;
        dione.rotationPeriod = 65.686;
        dione.axialTilt = 0;
        dione.albedo = 0.998f;
        dione.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/dione.png");
        dione.tidallyLocked = true;
        dione.tidalLockingOffset = 0;
        dione.longitudeAtEpoch = 0;
        dione.parent = saturn;
        dione.semiMajorAxis = 377396;
        dione.eccentricity = 0.0022;
        dione.inclination = Math.toRadians(0.028);
        dione.longitudeOfAscendingNode = 0;
        dione.argumentOfPeriapsis = 0;
        dione.meanAnomalyAtEpoch = 0;
        dione.epoch = 0;
        dione.period = 2.737;
        registerCelestialBody(dione);

        CelestialBody rhea = new CelestialBody();
        rhea.name = "Rhea";
        rhea.mass = 2.307e21;
        rhea.radius = 763.8;
        rhea.rotationPeriod = 108.417;
        rhea.axialTilt = 0;
        rhea.albedo = 0.949f;
        rhea.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/rhea.png");
        rhea.tidallyLocked = true;
        rhea.tidalLockingOffset = 0;
        rhea.longitudeAtEpoch = 0;
        rhea.parent = saturn;
        rhea.semiMajorAxis = 527108;
        rhea.eccentricity = 0.0013;
        rhea.inclination = Math.toRadians(0.345);
        rhea.longitudeOfAscendingNode = 0;
        rhea.argumentOfPeriapsis = 0;
        rhea.meanAnomalyAtEpoch = 0;
        rhea.epoch = 0;
        rhea.period = 4.518;
        registerCelestialBody(rhea);

        CelestialBody hyperion = new CelestialBody();
        hyperion.name = "Hyperion";
        hyperion.mass = 5.62e18;
        hyperion.radius = 135.0;
        hyperion.rotationPeriod = 13.0; 
        hyperion.axialTilt = 0;
        hyperion.albedo = 0.30f;
        hyperion.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/hyperion.png");
        hyperion.tidallyLocked = false; 
        hyperion.tidalLockingOffset = 0;
        hyperion.longitudeAtEpoch = 0;
        hyperion.parent = saturn;
        hyperion.semiMajorAxis = 1481010;
        hyperion.eccentricity = 0.1230;
        hyperion.inclination = Math.toRadians(0.43);
        hyperion.longitudeOfAscendingNode = 0;
        hyperion.argumentOfPeriapsis = 0;
        hyperion.meanAnomalyAtEpoch = 0;
        hyperion.epoch = 0;
        hyperion.period = 21.277;
        registerCelestialBody(hyperion);

        CelestialBody iapetus = new CelestialBody();
        iapetus.name = "Iapetus";
        iapetus.mass = 1.806e21;
        iapetus.radius = 734.5;
        iapetus.rotationPeriod = 1903.728;
        iapetus.axialTilt = 0;
        iapetus.albedo = 0.22f; 
        iapetus.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/iapetus.png");
        iapetus.tidallyLocked = true;
        iapetus.tidalLockingOffset = 0;
        iapetus.longitudeAtEpoch = 0;
        iapetus.parent = saturn;
        iapetus.semiMajorAxis = 3560820;
        iapetus.eccentricity = 0.0283;
        iapetus.inclination = Math.toRadians(15.47); 
        iapetus.longitudeOfAscendingNode = 0;
        iapetus.argumentOfPeriapsis = 0;
        iapetus.meanAnomalyAtEpoch = 0;
        iapetus.epoch = 0;
        iapetus.period = 79.322;
        registerCelestialBody(iapetus);

 
 
 
 
 

        CelestialBody miranda = new CelestialBody();
        miranda.name = "Miranda";
        miranda.mass = 6.59e19;
        miranda.radius = 235.8;
        miranda.rotationPeriod = 33.923;
        miranda.axialTilt = 0;
        miranda.albedo = 0.32f;
        miranda.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/miranda.png");
        miranda.tidallyLocked = true;
        miranda.tidalLockingOffset = 0;
        miranda.longitudeAtEpoch = 0;
        miranda.parent = uranus;
        miranda.semiMajorAxis = 129390;
        miranda.eccentricity = 0.0013;
        miranda.inclination = Math.toRadians(4.22);
        miranda.longitudeOfAscendingNode = 0;
        miranda.argumentOfPeriapsis = 0;
        miranda.meanAnomalyAtEpoch = 0;
        miranda.epoch = 0;
        miranda.period = 1.4135;
        registerCelestialBody(miranda);

        CelestialBody ariel = new CelestialBody();
        ariel.name = "Ariel";
        ariel.mass = 1.353e21;
        ariel.radius = 578.9;
        ariel.rotationPeriod = 60.489;
        ariel.axialTilt = 0;
        ariel.albedo = 0.53f;
        ariel.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/ariel.png");
        ariel.tidallyLocked = true;
        ariel.tidalLockingOffset = 0;
        ariel.longitudeAtEpoch = 0;
        ariel.parent = uranus;
        ariel.semiMajorAxis = 191020;
        ariel.eccentricity = 0.0012;
        ariel.inclination = Math.toRadians(0.31);
        ariel.longitudeOfAscendingNode = 0;
        ariel.argumentOfPeriapsis = 0;
        ariel.meanAnomalyAtEpoch = 0;
        ariel.epoch = 0;
        ariel.period = 2.520;
        registerCelestialBody(ariel);

        CelestialBody umbriel = new CelestialBody();
        umbriel.name = "Umbriel";
        umbriel.mass = 1.172e21;
        umbriel.radius = 584.7;
        umbriel.rotationPeriod = 99.46;
        umbriel.axialTilt = 0;
        umbriel.albedo = 0.26f;
        umbriel.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/umbriel.png");
        umbriel.tidallyLocked = true;
        umbriel.tidalLockingOffset = 0;
        umbriel.longitudeAtEpoch = 0;
        umbriel.parent = uranus;
        umbriel.semiMajorAxis = 266300;
        umbriel.eccentricity = 0.0039;
        umbriel.inclination = Math.toRadians(0.36);
        umbriel.longitudeOfAscendingNode = 0;
        umbriel.argumentOfPeriapsis = 0;
        umbriel.meanAnomalyAtEpoch = 0;
        umbriel.epoch = 0;
        umbriel.period = 4.144;
        registerCelestialBody(umbriel);

        CelestialBody titania = new CelestialBody();
        titania.name = "Titania";
        titania.mass = 3.527e21;
        titania.radius = 788.9;
        titania.rotationPeriod = 208.94;
        titania.axialTilt = 0;
        titania.albedo = 0.35f;
        titania.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/titania.png");
        titania.tidallyLocked = true;
        titania.tidalLockingOffset = 0;
        titania.longitudeAtEpoch = 0;
        titania.parent = uranus;
        titania.semiMajorAxis = 435910;
        titania.eccentricity = 0.0011;
        titania.inclination = Math.toRadians(0.10);
        titania.longitudeOfAscendingNode = 0;
        titania.argumentOfPeriapsis = 0;
        titania.meanAnomalyAtEpoch = 0;
        titania.epoch = 0;
        titania.period = 8.706;
        registerCelestialBody(titania);

        CelestialBody oberon = new CelestialBody();
        oberon.name = "Oberon";
        oberon.mass = 3.014e21;
        oberon.radius = 761.4;
        oberon.rotationPeriod = 323.118;
        oberon.axialTilt = 0;
        oberon.albedo = 0.31f;
        oberon.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/oberon.png");
        oberon.tidallyLocked = true;
        oberon.tidalLockingOffset = 0;
        oberon.longitudeAtEpoch = 0;
        oberon.parent = uranus;
        oberon.semiMajorAxis = 583520;
        oberon.eccentricity = 0.0014;
        oberon.inclination = Math.toRadians(0.10);
        oberon.longitudeOfAscendingNode = 0;
        oberon.argumentOfPeriapsis = 0;
        oberon.meanAnomalyAtEpoch = 0;
        oberon.epoch = 0;
        oberon.period = 13.463;
        registerCelestialBody(oberon);

 
 
 
 

        CelestialBody proteus = new CelestialBody();
        proteus.name = "Proteus";
        proteus.mass = 4.4e19;
        proteus.radius = 210.0;
        proteus.rotationPeriod = 26.928;
        proteus.axialTilt = 0;
        proteus.albedo = 0.096f;
        proteus.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/proteus.png");
        proteus.tidallyLocked = true;
        proteus.tidalLockingOffset = 0;
        proteus.longitudeAtEpoch = 0;
        proteus.parent = neptune;
        proteus.semiMajorAxis = 117647;
        proteus.eccentricity = 0.0005;
        proteus.inclination = Math.toRadians(0.075);
        proteus.longitudeOfAscendingNode = 0;
        proteus.argumentOfPeriapsis = 0;
        proteus.meanAnomalyAtEpoch = 0;
        proteus.epoch = 0;
        proteus.period = 1.1223;
        registerCelestialBody(proteus);

        CelestialBody nereid = new CelestialBody();
        nereid.name = "Nereid";
        nereid.mass = 3.1e19;
        nereid.radius = 170.0;
        nereid.rotationPeriod = 11.594;
        nereid.axialTilt = 0;
        nereid.albedo = 0.155f;
        nereid.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/nereid.png");
        nereid.tidallyLocked = false; 
        nereid.tidalLockingOffset = 0;
        nereid.longitudeAtEpoch = 0;
        nereid.parent = neptune;
        nereid.semiMajorAxis = 5513400;
        nereid.eccentricity = 0.7512; 
        nereid.inclination = Math.toRadians(7.23);
        nereid.longitudeOfAscendingNode = 0;
        nereid.argumentOfPeriapsis = 0;
        nereid.meanAnomalyAtEpoch = 0;
        nereid.epoch = 0;
        nereid.period = 360.14;
        registerCelestialBody(nereid);

 
 
 
 
 

        CelestialBody ceres = new CelestialBody();
        ceres.name = "Ceres";
        ceres.mass = 9.39e20;
        ceres.radius = 476.2;
        ceres.rotationPeriod = 9.074;
        ceres.axialTilt = Math.toRadians(4.0);
        ceres.albedo = 0.09f;
        ceres.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/ceres.png");
        ceres.tidallyLocked = false;
        ceres.longitudeAtEpoch = 0;
        ceres.parent = sol;
        ceres.semiMajorAxis = 2.767;
        ceres.eccentricity = 0.0796;
        ceres.inclination = Math.toRadians(10.593);
        ceres.longitudeOfAscendingNode = Math.toRadians(80.33);
        ceres.argumentOfPeriapsis = Math.toRadians(73.60);
        ceres.meanAnomalyAtEpoch = 0;
        ceres.epoch = 0;
        ceres.period = 1680.5;
        registerCelestialBody(ceres);

        CelestialBody eris = new CelestialBody();
        eris.name = "Eris";
        eris.mass = 1.66e22;
        eris.radius = 1163.0;
        eris.rotationPeriod = 25.9;
        eris.axialTilt = Math.toRadians(78.0);
        eris.albedo = 0.96f; 
        eris.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/eris.png");
        eris.tidallyLocked = false;
        eris.longitudeAtEpoch = 0;
        eris.parent = sol;
        eris.semiMajorAxis = 67.66;
        eris.eccentricity = 0.4340;
        eris.inclination = Math.toRadians(44.04);
        eris.longitudeOfAscendingNode = Math.toRadians(35.96);
        eris.argumentOfPeriapsis = Math.toRadians(151.63);
        eris.meanAnomalyAtEpoch = 0;
        eris.epoch = 0;
        eris.period = 203830.0;
        registerCelestialBody(eris);

        CelestialBody makemake = new CelestialBody();
        makemake.name = "Makemake";
        makemake.mass = 3.1e21;
        makemake.radius = 715.0;
        makemake.rotationPeriod = 22.83;
        makemake.axialTilt = 0;
        makemake.albedo = 0.81f;
        makemake.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/makemake.png");
        makemake.tidallyLocked = false;
        makemake.longitudeAtEpoch = 0;
        makemake.parent = sol;
        makemake.semiMajorAxis = 45.79;
        makemake.eccentricity = 0.1591;
        makemake.inclination = Math.toRadians(28.96);
        makemake.longitudeOfAscendingNode = Math.toRadians(79.36);
        makemake.argumentOfPeriapsis = Math.toRadians(294.84);
        makemake.meanAnomalyAtEpoch = 0;
        makemake.epoch = 0;
        makemake.period = 112897.0;
        registerCelestialBody(makemake);

        CelestialBody haumea = new CelestialBody();
        haumea.name = "Haumea";
        haumea.mass = 4.006e21;
        haumea.radius = 780.0; 
        haumea.rotationPeriod = 3.915; 
        haumea.axialTilt = Math.toRadians(28.19);
        haumea.albedo = 0.66f;
        haumea.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/haumea.png");
        haumea.tidallyLocked = false;
        haumea.longitudeAtEpoch = 0;
        haumea.parent = sol;
        haumea.semiMajorAxis = 43.13;
        haumea.eccentricity = 0.1913;
        haumea.inclination = Math.toRadians(28.19);
        haumea.longitudeOfAscendingNode = Math.toRadians(122.17);
        haumea.argumentOfPeriapsis = Math.toRadians(239.04);
        haumea.meanAnomalyAtEpoch = 0;
        haumea.epoch = 0;
        haumea.period = 103774.0;
        registerCelestialBody(haumea);

        CelestialBody sedna = new CelestialBody();
        sedna.name = "Sedna";
        sedna.mass = 8.3e20;
        sedna.radius = 498.0;
        sedna.rotationPeriod = 10.273;
        sedna.axialTilt = 0;
        sedna.albedo = 0.32f;
        sedna.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/sedna.png");
        sedna.tidallyLocked = false;
        sedna.longitudeAtEpoch = 0;
        sedna.parent = sol;
        sedna.semiMajorAxis = 506.0; 
        sedna.eccentricity = 0.8432; 
        sedna.inclination = Math.toRadians(11.93);
        sedna.longitudeOfAscendingNode = Math.toRadians(144.26);
        sedna.argumentOfPeriapsis = Math.toRadians(311.09);
        sedna.meanAnomalyAtEpoch = 0;
        sedna.epoch = 0;
        sedna.period = 4015000.0; 
        registerCelestialBody(sedna);

        CelestialBody quaoar = new CelestialBody();
        quaoar.name = "Quaoar";
        quaoar.mass = 1.4e21;
        quaoar.radius = 555.0;
        quaoar.rotationPeriod = 17.6788;
        quaoar.axialTilt = 0;
        quaoar.albedo = 0.109f;
        quaoar.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/quaoar.png");
        quaoar.tidallyLocked = false;
        quaoar.longitudeAtEpoch = 0;
        quaoar.parent = sol;
        quaoar.semiMajorAxis = 43.41;
        quaoar.eccentricity = 0.0393;
        quaoar.inclination = Math.toRadians(7.99);
        quaoar.longitudeOfAscendingNode = Math.toRadians(188.94);
        quaoar.argumentOfPeriapsis = Math.toRadians(147.49);
        quaoar.meanAnomalyAtEpoch = 0;
        quaoar.epoch = 0;
        quaoar.period = 104956.0;
        registerCelestialBody(quaoar);

        CelestialBody orcus = new CelestialBody();
        orcus.name = "Orcus";
        orcus.mass = 6.4e20;
        orcus.radius = 458.0;
        orcus.rotationPeriod = 13.188;
        orcus.axialTilt = 0;
        orcus.albedo = 0.231f;
        orcus.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/orcus.png");
        orcus.tidallyLocked = false;
        orcus.longitudeAtEpoch = 0;
        orcus.parent = sol;
        orcus.semiMajorAxis = 39.17;
        orcus.eccentricity = 0.2271;
        orcus.inclination = Math.toRadians(20.57);
        orcus.longitudeOfAscendingNode = Math.toRadians(268.65);
        orcus.argumentOfPeriapsis = Math.toRadians(72.31);
        orcus.meanAnomalyAtEpoch = 0;
        orcus.epoch = 0;
        orcus.period = 89592.0;
        registerCelestialBody(orcus);

        CelestialBody SpaceOrbiter = new CelestialBody();
        SpaceOrbiter.name = "Space Orbiter";
        SpaceOrbiter.mass = 1;
        SpaceOrbiter.radius = 0.5f; 
        SpaceOrbiter.rotationPeriod = 24;
        SpaceOrbiter.axialTilt = 0;
        SpaceOrbiter.albedo = 0f;
        SpaceOrbiter.texture = new ResourceLocation("sgt", "textures/misc/celestial/bodys/earth.png");
        SpaceOrbiter.tidallyLocked = false;
        SpaceOrbiter.tidalLockingOffset = 0;
        SpaceOrbiter.longitudeAtEpoch = 0;
        SpaceOrbiter.longitudeOfAscendingNode = 0; 
        SpaceOrbiter.argumentOfPeriapsis = 0; 
        SpaceOrbiter.meanAnomalyAtEpoch = 0;
        SpaceOrbiter.epoch = 0;
        SpaceOrbiter.period = 11.0; 
 
 
        SpaceOrbiter.parent = earth;
        SpaceOrbiter.semiMajorAxis = 100000;
        SpaceOrbiter.eccentricity = 0;
        SpaceOrbiter.inclination = 0;
        registerCelestialBody(SpaceOrbiter);
        assignDimension(SpaceOrbiter, ModDimensions.SPACE_LEVEL_KEY);
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