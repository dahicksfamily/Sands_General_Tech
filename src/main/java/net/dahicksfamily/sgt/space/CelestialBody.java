package net.dahicksfamily.sgt.space;

import net.dahicksfamily.sgt.space.atmosphere.Atmosphere;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class CelestialBody extends Orbit {
    public double mass;
    public double radius;
    public double rotationPeriod;
    public double axialTilt;
    public double longitudeAtEpoch;

    public String name;
    public ResourceLocation texture;
    public float albedo;

    public boolean tidallyLocked;
    public double tidalLockingOffset;

    public Atmosphere atmosphere = null;

    public double getSurfaceGravity() {
        return 0;
    }

    public double getEscapeVelocity() {
        return 0;
    }

    public double getApparentSize(Vec3 observerPos) {
        return 0;
    }

    public float getPhase(Vec3 observerPos, Vec3 lightSourcePos) {
        return 0;
    }

    public Vec3 getRotationAxis() {
        return null;
    }

    public double getRotationAngle(double time) {
        double timeHours = time * 24.0;
        double rotations = timeHours / rotationPeriod;
        return (rotations * 2.0 * Math.PI) + longitudeAtEpoch;
    }

    public boolean hasAtmosphere() {
        return atmosphere != null;
    }
}