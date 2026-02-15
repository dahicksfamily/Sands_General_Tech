package net.dahicksfamily.sgt.space;

import net.minecraft.world.phys.Vec3;

public class Orbit {
    public double semiMajorAxis;        // a - size of orbit (AU or km)
    protected double eccentricity;         // e - shape (0 = circle, <1 = ellipse)
    protected double inclination;          // i - tilt relative to reference plane (radians)
    protected double longitudeOfAscendingNode;  // Omega - where orbit crosses reference plane
    protected double argumentOfPeriapsis;  // omega - where closest approach is
    protected double meanAnomalyAtEpoch;   // M0 - position at reference time
    protected double epoch;                // reference time (ticks or seconds)

    protected double period;               // orbital period (calculated from semi-major axis)
    public CelestialBody parent;        // what this orbits around (null for barycenter)

    // Gravitational parameter (GM) - can be set based on parent mass
    protected static final double G = 6.67430e-11; // Gravitational constant m^2/(kg*s^2)

    public Vec3 getPositionAtTime(double time) {
        if (parent == null || semiMajorAxis == 0) {
            return Vec3.ZERO;
        }

        double M = getMeanAnomaly(time);
        double E = solveKeplersEquation(M);
        double trueAnomaly = eccentricToTrueAnomaly(E);
        double r = semiMajorAxis * (1 - eccentricity * Math.cos(E));

        double x_orbital = r * Math.cos(trueAnomaly);
        double y_orbital = r * Math.sin(trueAnomaly);

        Vec3 position = orbitalToCartesian(x_orbital, y_orbital);

        if (semiMajorAxis < 100) {
            position = position.scale(149597870.7);
        }

        return position;
    }

    public Vec3 getVelocityAtTime(double time) {
        double M = getMeanAnomaly(time);
        double E = solveKeplersEquation(M);
        double trueAnomaly = eccentricToTrueAnomaly(E);

        double p = semiMajorAxis * (1 - eccentricity * eccentricity);

        double mu = G * (parent != null ? parent.mass : 1.989e30);

        double h = Math.sqrt(mu * p);

        double vx_orbital = -(mu / h) * Math.sin(trueAnomaly);
        double vy_orbital = (mu / h) * (eccentricity + Math.cos(trueAnomaly));

        return orbitalToCartesian(vx_orbital, vy_orbital);
    }

    protected double getMeanAnomaly(double time) {
        double n = 2 * Math.PI / period;
        double M = meanAnomalyAtEpoch + n * (time - epoch);

        M = M % (2 * Math.PI);
        if (M < 0) M += 2 * Math.PI;

        return M;
    }

    protected double solveKeplersEquation(double M) {
        double E = M;

        int maxIterations = 100;
        double tolerance = 1e-10;

        for (int i = 0; i < maxIterations; i++) {
            double f = E - eccentricity * Math.sin(E) - M;
            double fPrime = 1 - eccentricity * Math.cos(E);

            double deltaE = f / fPrime;
            E = E - deltaE;

            if (Math.abs(deltaE) < tolerance) {
                break;
            }
        }

        return E;
    }

    protected double eccentricToTrueAnomaly(double E) {
        // tan(ν/2) = sqrt((1+e)/(1-e)) * tan(E/2)
        double beta = eccentricity / (1 + Math.sqrt(1 - eccentricity * eccentricity));
        double trueAnomaly = E + 2 * Math.atan(beta * Math.sin(E) / (1 - beta * Math.cos(E)));

        return trueAnomaly;
    }

    protected Vec3 orbitalToCartesian(double x_orbital, double y_orbital) {
        double w = argumentOfPeriapsis;
        double omega = longitudeOfAscendingNode;
        double i = inclination;

        double x1 = x_orbital * Math.cos(w) - y_orbital * Math.sin(w);
        double y1 = x_orbital * Math.sin(w) + y_orbital * Math.cos(w);
        double z1 = 0;

        double y2 = y1 * Math.cos(i) - z1 * Math.sin(i);
        double z2 = y1 * Math.sin(i) + z1 * Math.cos(i);

        double x3 = x1 * Math.cos(omega) - y2 * Math.sin(omega);
        double y3 = x1 * Math.sin(omega) + y2 * Math.cos(omega);

        return new Vec3(x3, y3, z2);
    }

    public void calculatePeriod() {
        if (parent != null) {
            double mu = G * parent.mass;
            // Convert semi-major axis to meters if in AU (1 AU = 1.496e11 m)
            double a_meters = semiMajorAxis * 1.496e11; // Assuming AU
            period = 2 * Math.PI * Math.sqrt(Math.pow(a_meters, 3) / mu);
            // Convert period from seconds to days
            period = period / 86400.0;
        }
    }

    public double getDistanceFromParent(double time) {
        double M = getMeanAnomaly(time);
        double E = solveKeplersEquation(M);
        return semiMajorAxis * (1 - eccentricity * Math.cos(E));
    }

    public double getPeriapsis() {
        return semiMajorAxis * (1 - eccentricity);
    }

    public double getApoapsis() {
        return semiMajorAxis * (1 + eccentricity);
    }
}