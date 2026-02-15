package net.dahicksfamily.sgt.space;

import net.minecraft.world.phys.Vec3;

public class Star extends CelestialBody {
    // Stellar classification
    public double luminosity;           // Solar luminosities (L☉)
    public double temperature;          // Kelvin (surface temp)
    protected String spectralType;         // O, B, A, F, G, K, M + number + luminosity class

    // Functions for calculated properties
    public Vec3 getColor() {
        // Calculate RGB from temperature (blackbody radiation)
        // Use Wien's displacement law & Planck's law
        return blackbodyColorFromTemp(temperature);
    }

    public float getBrightness(Vec3 observerPos) {
        // observerPos is now in km!
        double distanceKm = observerPos.length();

        if (distanceKm == 0) return 1.0f;

        // Convert km to meters
        double distanceMeters = distanceKm * 1000.0;

        // Solar luminosity in watts
        double solarLuminosity = 3.828e26;
        double absoluteLuminosity = luminosity * solarLuminosity;

        // Apparent brightness (flux) = L / (4π * d²)
        double brightness = absoluteLuminosity / (4.0 * Math.PI * distanceMeters * distanceMeters);

        // Normalize to Sun brightness at 1 AU
        double sunBrightnessAt1AU = solarLuminosity / (4.0 * Math.PI * Math.pow(149597870700.0, 2));

        return (float)(brightness / sunBrightnessAt1AU);
    }

    public double getAbsoluteMagnitude() {
        // M = 4.83 - 2.5 * log10(L/L☉)
        // Sun's absolute magnitude is 4.83
        if (luminosity <= 0) return 99.0; // Very dim/invalid

        return 4.83 - 2.5 * Math.log10(luminosity);
    }

    public double getApparentMagnitude(Vec3 observerPos) {
        // m = M + 5 * log10(d/10)
        // where d is distance in parsecs

        double absoluteMag = getAbsoluteMagnitude();
        double distance = observerPos.length(); // in AU

        if (distance <= 0) return -99.0; // Extremely bright/invalid

        // Convert AU to parsecs (1 parsec = 206265 AU)
        double distanceParsecs = distance / 206265.0;

        // Apparent magnitude formula
        // m = M + 5 * log10(d) - 5
        // or m = M + 5 * log10(d/10)
        return absoluteMag + 5.0 * Math.log10(distanceParsecs / 10.0);
    }

    private Vec3 blackbodyColorFromTemp(double temp) {
        // Convert Kelvin to RGB using color temperature formulas
        // Based on Tanner Helland's algorithm and CIE color matching

        // Clamp temperature to reasonable range
        temp = Math.max(1000, Math.min(temp, 40000));

        // Divide by 100 for calculation
        double t = temp / 100.0;

        double r, g, b;

        // Calculate Red
        if (t <= 66) {
            r = 255;
        } else {
            r = t - 60;
            r = 329.698727446 * Math.pow(r, -0.1332047592);
            r = Math.max(0, Math.min(255, r));
        }

        // Calculate Green
        if (t <= 66) {
            g = t;
            g = 99.4708025861 * Math.log(g) - 161.1195681661;
            g = Math.max(0, Math.min(255, g));
        } else {
            g = t - 60;
            g = 288.1221695283 * Math.pow(g, -0.0755148492);
            g = Math.max(0, Math.min(255, g));
        }

        // Calculate Blue
        if (t >= 66) {
            b = 255;
        } else if (t <= 19) {
            b = 0;
        } else {
            b = t - 10;
            b = 138.5177312231 * Math.log(b) - 305.0447927307;
            b = Math.max(0, Math.min(255, b));
        }

        // Normalize to 0-1 range
        return new Vec3(r / 255.0, g / 255.0, b / 255.0);
    }

    /**
     * Alternative blackbody color method using Planck's law (more accurate but slower)
     */
    private Vec3 blackbodyColorFromTempAccurate(double temp) {
        // CIE 1931 color matching functions approximation
        // This gives more accurate colors but is computationally heavier

        double x = 0, y = 0, z = 0;

        // Sample wavelengths from 380nm to 780nm (visible spectrum)
        for (int wavelength = 380; wavelength <= 780; wavelength += 5) {
            double lambda = wavelength * 1e-9; // Convert to meters

            // Planck's law: spectral radiance
            double h = 6.62607015e-34; // Planck constant
            double c = 299792458; // Speed of light
            double k = 1.380649e-23; // Boltzmann constant

            double intensity = (2 * h * c * c) / (Math.pow(lambda, 5) * (Math.exp((h * c) / (lambda * k * temp)) - 1));

            // CIE color matching functions (approximated)
            double xBar = colorMatchingX(wavelength);
            double yBar = colorMatchingY(wavelength);
            double zBar = colorMatchingZ(wavelength);

            x += intensity * xBar;
            y += intensity * yBar;
            z += intensity * zBar;
        }

        // Normalize
        double sum = x + y + z;
        if (sum > 0) {
            x /= sum;
            y /= sum;
            z /= sum;
        }

        // Convert XYZ to RGB (sRGB color space)
        double r = 3.2406 * x - 1.5372 * y - 0.4986 * z;
        double g = -0.9689 * x + 1.8758 * y + 0.0415 * z;
        double b = 0.0557 * x - 0.2040 * y + 1.0570 * z;

        // Normalize to 0-1 and apply gamma correction
        r = gammaCorrect(Math.max(0, r));
        g = gammaCorrect(Math.max(0, g));
        b = gammaCorrect(Math.max(0, b));

        // Normalize to brightest component
        double max = Math.max(r, Math.max(g, b));
        if (max > 1.0) {
            r /= max;
            g /= max;
            b /= max;
        }

        return new Vec3(r, g, b);
    }

    // Helper functions for accurate color matching
    private double colorMatchingX(int wavelength) {
        // Simplified CIE 1931 X color matching function
        if (wavelength < 380 || wavelength > 780) return 0;
        if (wavelength < 440) return 0.014 * Math.exp(-0.5 * Math.pow((double) (wavelength - 435) / 20, 2));
        if (wavelength < 600) return Math.exp(-0.5 * Math.pow((double) (wavelength - 550) / 60, 2));
        return 0.3 * Math.exp(-0.5 * Math.pow((double) (wavelength - 600) / 40, 2));
    }

    private double colorMatchingY(int wavelength) {
        // Simplified CIE 1931 Y color matching function (luminosity)
        if (wavelength < 380 || wavelength > 780) return 0;
        return Math.exp(-0.5 * Math.pow((double) (wavelength - 555) / 70, 2));
    }

    private double colorMatchingZ(int wavelength) {
        // Simplified CIE 1931 Z color matching function
        if (wavelength < 380 || wavelength > 780) return 0;
        if (wavelength > 550) return 0;
        return 1.5 * Math.exp(-0.5 * Math.pow((double) (wavelength - 445) / 40, 2));
    }

    private double gammaCorrect(double value) {
        // sRGB gamma correction
        if (value <= 0.0031308) {
            return 12.92 * value;
        } else {
            return 1.055 * Math.pow(value, 1.0 / 2.4) - 0.055;
        }
    }
}
