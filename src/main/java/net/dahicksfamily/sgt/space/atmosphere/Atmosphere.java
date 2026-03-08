package net.dahicksfamily.sgt.space.atmosphere;

import net.minecraft.world.phys.Vec3;

public class Atmosphere {
    public float surfaceDensity = 1.0f; 
    public float scaleHeight = 0.085f;
    public float outerHeightFraction = 0.15f;
    public Vec3 rayleighCoeff = new Vec3(0.58, 0.82, 1.0); 
    public Vec3 mieCoeff = new Vec3(0.9, 0.9, 0.9);
    public float mieDensity = 0.05f;
    public float mieAnisotropy = 0.76f;
    public Vec3 airglowColor = new Vec3(0.1, 0.95, 0.25);
    public float airglowIntensity = 0.015f;
    public float shadowSoftness = 0.15f;
    public Vec3 terminatorBandColor = new Vec3(1.0, 0.45, 0.05);
    public float terminatorBandIntensity = 0.0f;

    public static Atmosphere earthLike() {
        Atmosphere a = new Atmosphere();
        a.surfaceDensity      = 1.0f;
        a.scaleHeight         = 0.085f;
        a.outerHeightFraction = 0.06f;
        a.rayleighCoeff       = new Vec3(0.48, 0.72, 1.0);
        a.mieCoeff            = new Vec3(0.88, 0.88, 0.88);
        a.mieDensity          = 0.06f;
        a.mieAnisotropy       = 0.76f;
        a.airglowColor        = new Vec3(0.08, 0.95, 0.28);
        a.airglowIntensity    = 0.016f;
        a.shadowSoftness      = 0.04f; 
        a.terminatorBandColor     = new Vec3(1.0, 0.45, 0.05);
        a.terminatorBandIntensity = 0.4f; 
        return a;
    }

    public static Atmosphere venusLike() {
        Atmosphere a = new Atmosphere();
        a.surfaceDensity      = 60.0f; 
        a.scaleHeight         = 0.25f; 
        a.outerHeightFraction = 0.18f; 
        a.rayleighCoeff       = new Vec3(1.0, 0.92, 0.70); 
        a.mieCoeff            = new Vec3(1.0, 0.97, 0.88); 
        a.mieDensity          = 2.5f; 
        a.mieAnisotropy       = 0.82f;
        a.airglowColor        = new Vec3(1.0, 0.85, 0.4);
        a.airglowIntensity    = 0.0f; 
        a.shadowSoftness      = 0.35f; 
        a.terminatorBandColor     = new Vec3(1.0, 0.6, 0.2);
        a.terminatorBandIntensity = 0.0f; 
        return a;
    }

    public static Atmosphere marsLike() {
        Atmosphere a = new Atmosphere();
        a.surfaceDensity      = 0.006f;
        a.scaleHeight         = 0.12f;
        a.outerHeightFraction = 0.045f;
        a.rayleighCoeff       = new Vec3(1.0, 0.72, 0.52);
        a.mieCoeff            = new Vec3(1.0, 0.58, 0.32);
        a.mieDensity          = 0.55f;
        a.mieAnisotropy       = 0.70f;
        a.airglowColor        = new Vec3(0.05, 0.45, 1.0);
        a.airglowIntensity    = 0.006f;
        a.shadowSoftness      = 0.12f;
        a.terminatorBandColor     = new Vec3(1.0, 0.35, 0.08); 
        a.terminatorBandIntensity = 0.4f;
        return a;
    }

    public static Atmosphere titanLike() {
        Atmosphere a = new Atmosphere();
        a.surfaceDensity      = 1.5f;
        a.scaleHeight         = 0.12f;
        a.outerHeightFraction = 0.09f; 
        a.rayleighCoeff       = new Vec3(0.62, 0.74, 1.0);
        a.mieCoeff            = new Vec3(1.0, 0.58, 0.28);
        a.mieDensity          = 0.92f;
        a.mieAnisotropy       = 0.80f;
        a.airglowColor        = new Vec3(0.9, 0.55, 0.1);
        a.airglowIntensity    = 0.03f;
        a.shadowSoftness      = 0.20f;
        a.terminatorBandColor     = new Vec3(0.9, 0.5, 0.1); 
        a.terminatorBandIntensity = 0.6f;
        return a;
    }

    public static Atmosphere hydrogenThin() {
        Atmosphere a = new Atmosphere();
        a.surfaceDensity      = 0.1f;
        a.scaleHeight         = 0.20f;
        a.outerHeightFraction = 0.06f;
        a.rayleighCoeff       = new Vec3(0.88, 0.90, 1.0);
        a.mieCoeff            = new Vec3(0.7, 0.7, 0.8);
        a.mieDensity          = 0.02f;
        a.mieAnisotropy       = 0.60f;
        a.airglowColor        = new Vec3(0.3, 0.6, 1.0);
        a.airglowIntensity    = 0.01f;
        a.shadowSoftness      = 0.20f;
        a.terminatorBandIntensity = 0.0f; 
        return a;
    }
}