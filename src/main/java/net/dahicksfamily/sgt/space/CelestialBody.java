package net.dahicksfamily.sgt.space;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class CelestialBody extends Orbit {
    public double mass;                 // kg
    public double radius;               // km
    public double rotationPeriod;       // sidereal day (hours or ticks)
    public double axialTilt;            // obliquity (radians)

    public String name;
    public ResourceLocation texture;    // for rendering
    public float albedo;                // reflectivity (0-1)

    public double getSurfaceGravity() {
        return 0;
    }

    public double getEscapeVelocity() {
        return 0;
    }

    public float getPhase(Vec3 observerPos, Vec3 lightSourcePos) {
        return 0;
    }

    public Vec3 getRotationAxis() {
        return null;
    }

    public double getRotationAngle(double time) {
        return time;
    }
}
