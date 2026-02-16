package net.dahicksfamily.sgt.space;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class CelestialBody extends Orbit {
    // Physical properties
    public double mass;                 // kg
    public double radius;               // km
    public double rotationPeriod;       // sidereal day (hours)
    public double axialTilt;            // obliquity (radians)
    public double longitudeAtEpoch;     // rotation position at epoch

    // Appearance
    public String name;
    public ResourceLocation texture;    // for rendering
    public float albedo;                // reflectivity (0-1)

    // Tidal locking
    public boolean tidallyLocked;       // If true, always shows same face to parent
    public double tidalLockingOffset;   // Radians - which longitude faces parent (0 = front of texture)

    // Functions
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

    /**
     * Get rotation angle at a given time
     * For tidally locked bodies, this ensures the same face points at parent
     * @param time Current time in days
     * @return Rotation angle in radians
     */
    public double getRotationAngle(double time) {

        if (tidallyLocked) {

            double meanMotion = (2.0 * Math.PI) / rotationPeriod;

            double timeDays = time / 24000.0;

            double orbitalAngle = meanMotion * timeDays;

            return orbitalAngle + tidalLockingOffset;

        } else {

            double timeHours = time * 24.0;
            double rotations = timeHours / rotationPeriod;
            return (rotations * 2.0 * Math.PI) + longitudeAtEpoch;
        }
    }

}