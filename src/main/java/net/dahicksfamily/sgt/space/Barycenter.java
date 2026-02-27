package net.dahicksfamily.sgt.space;

public class Barycenter extends CelestialBody {

    public Barycenter(String name) {
        this.name   = name;
        this.mass   = 0;
        this.radius = 0;
        this.rotationPeriod = 1.0;  // arbitrary, never used
        this.axialTilt      = 0;
        this.albedo         = 0;
        this.atmosphere     = null;
        this.texture        = null;
    }

    public static Barycenter of(String name, CelestialBody parent,
                                double semiMajorAxis, double period,
                                double eccentricity, double inclination,
                                double longitudeAtEpoch) {
        Barycenter b = new Barycenter(name);
        b.parent           = parent;
        b.semiMajorAxis    = semiMajorAxis;
        b.period           = period;
        b.eccentricity     = eccentricity;
        b.inclination      = inclination;
        b.longitudeAtEpoch = longitudeAtEpoch;
        return b;
    }

    public boolean isRenderable() {
        return false;
    }
}