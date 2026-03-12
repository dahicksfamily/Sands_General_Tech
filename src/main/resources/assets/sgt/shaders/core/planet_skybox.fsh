#version 150

in vec3 vertPos;

uniform vec3  SunDir;
uniform vec3  SunColor;
uniform float SunAngularRadius;

uniform vec3  RayleighCoeff;
uniform vec3  MieCoeff;
uniform float MieDensity;
uniform float MieAnisotropy;
uniform float SurfaceDensity;
uniform float ScaleHeight;

uniform vec3  AirglowColor;
uniform float AirglowIntensity;
uniform vec3  TerminatorColor;
uniform float TerminatorIntensity;

uniform float Exposure;

// ── NEW: vanilla sky tint ─────────────────────────────────────────────────
// RGB sky colour from level.getSkyColor() — biome tint, weather, vanilla ToD.
// Already in linear [0,1] range as returned by Minecraft.
// VanillaTintStrength: 0 = pure physical scatter, 1 = fully vanilla-tinted.
uniform vec3  VanillaSkyTint;
uniform float VanillaTintStrength;   // recommend 0.25–0.55 for a nice blend

out vec4 fragColor;

const float PI            = 3.14159265359;
// SOLAR_IRRAD drives the raw scatter brightness before tone-mapping.
// At 80.0 the Reinhard curve (exp=-skyColor) maps the zenith noon sky to
// approximately #87CEEB (sky blue) and the horizon to near-white, matching
// real Earth sky photographs.  The previous value of 22 was too dark because
// it sat below the Reinhard knee, producing the washed-out grey-blue #677792.
const float SOLAR_IRRAD   = 160.0;
const float GROUND_ALBEDO = 0.22;

float chapmanPath(float cosZenith) {
    float c = max(cosZenith, 0.0);
    return 1.0 / (c + 0.04 * exp(-8.5 * c));
}
float rayleighPhase(float cosT) {
    return (3.0 / (16.0 * PI)) * (1.0 + cosT * cosT);
}
float miePhase(float cosT, float g) {
    float g2    = g * g;
    float denom = max(1.0 + g2 - 2.0 * g * cosT, 1e-5);
    return (1.0 - g2) / (4.0 * PI * pow(denom, 1.5));
}

void main() {
    vec3  viewDir = normalize(vertPos);
    float viewY   = viewDir.y;

    if (viewY < -0.015) {
        vec3 gnd = RayleighCoeff * SunColor * 0.018 * SurfaceDensity
        * smoothstep(-0.25, -0.015, viewY);
        fragColor = vec4(pow(max(gnd, vec3(0.0)), vec3(1.0/2.2)), 1.0);
        return;
    }

    vec3  sunDir3   = normalize(SunDir);
    float cosTheta  = dot(viewDir, sunDir3);
    float viewCosZ  = max(viewY, 0.0);
    float sunCosZ   = sunDir3.y;

    float sunAbove  = smoothstep(-0.06, 0.05, sunCosZ);
    float sunCivil  = smoothstep(-0.20, 0.10, sunCosZ);

    float tauBase   = SurfaceDensity * ScaleHeight;
    float pathView  = chapmanPath(viewCosZ);
    float pathSun   = chapmanPath(max(sunCosZ, 0.0));

    vec3  odViewR   = RayleighCoeff * tauBase * pathView;
    vec3  odSunR    = RayleighCoeff * tauBase * pathSun;
    float odViewM   = MieDensity    * tauBase * pathView;
    float odSunM    = MieDensity    * tauBase * pathSun;

    vec3  extinctView = odViewR + MieCoeff * odViewM;
    vec3  extinctSun  = odSunR  + MieCoeff * odSunM;
    vec3  Tv = exp(-extinctView);
    vec3  Ts = exp(-extinctSun) * sunAbove;

    float rPhase = rayleighPhase(cosTheta);
    float mPhase = miePhase(cosTheta, MieAnisotropy) * 4.0 * PI;

    vec3 inscatterR = RayleighCoeff * tauBase * pathView * rPhase * Ts;
    vec3 inscatterM = MieCoeff      * MieDensity * tauBase * pathView * mPhase * Ts;
    vec3 skyColor   = (inscatterR + inscatterM) * SunColor * SOLAR_IRRAD;

    // Ground bounce
    vec3  downwellT    = exp(-(RayleighCoeff + MieCoeff * MieDensity) * tauBase * pathSun);
    float groundFactor = (1.0 - viewCosZ) * GROUND_ALBEDO * 0.25;
    skyColor += RayleighCoeff * tauBase * groundFactor * downwellT * sunAbove
    * SunColor * SOLAR_IRRAD * 0.18;

    // Horizon haze
    float horizonMask = pow(1.0 - viewCosZ, 8.0);
    skyColor += MieCoeff * MieDensity * horizonMask * Ts * SunColor * SOLAR_IRRAD * 0.6;

    // Twilight arch
    float antiSolar      = max(0.0, -cosTheta);
    float twilightZone   = smoothstep(-0.15, 0.0, sunCosZ) * (1.0 - smoothstep(0.0, 0.20, sunCosZ));
    float twilightHorizon= pow(1.0 - viewCosZ, 4.0) * antiSolar;
    skyColor += RayleighCoeff * twilightZone * twilightHorizon * SunColor * SOLAR_IRRAD * 0.4;

    // Terminator band
    float termSun  = smoothstep(-0.22, 0.02, sunCosZ) * (1.0 - smoothstep(0.02, 0.22, sunCosZ));
    float termView = pow(1.0 - viewCosZ, 3.0);
    skyColor += TerminatorColor * TerminatorIntensity * termSun * termView * SOLAR_IRRAD * 0.28;

    // Sun disk
    float sunRad  = max(SunAngularRadius, 0.002);
    float cosDisk = cos(sunRad), cosLimb = cos(sunRad * 1.5), cosHalo = cos(sunRad * 14.0);
    float sunCore = smoothstep(cosDisk, cosDisk + 1e-4, cosTheta);
    float sunLimb = smoothstep(cosLimb, cosDisk, cosTheta) * (1.0 - sunCore);
    float sunHalo = max(0.0, smoothstep(cosHalo, cosLimb, cosTheta)) * MieDensity;
    vec3  sunDiskColor = SunColor * Ts;
    skyColor += sunDiskColor * (sunCore * 200.0 + sunLimb * 10.0 + sunHalo * 3.0);

    // Airglow
    float nightFrac  = 1.0 - sunAbove;
    float airglowH   = exp(-pow((viewCosZ - 0.09) / 0.06, 2.0));
    skyColor += AirglowColor * AirglowIntensity * airglowH * nightFrac * SOLAR_IRRAD * 0.04;

    // Night ambient
    skyColor += RayleighCoeff * (0.0012 * nightFrac + 0.0004);

    // ── Tone-map & colour correction ──────────────────────────────────────
    // The problem with applying Reinhard to the full skyColor is that it
    // compresses ALL channels toward 1.0, collapsing the blue/red ratio and
    // producing a grey-white instead of a saturated blue sky.
    //
    // Fix: separate the sun-disk HDR contribution from the diffuse scatter,
    // tone-map them independently, then recombine.
    //
    // "diffuse" = scatter only (everything except the bright sun disk).
    // "sunContrib" = the overexposed sun area, mapped with a much higher knee.

    // Reconstruct diffuse scatter without sun disk
    vec3 sunContribRaw = sunDiskColor * (sunCore * 200.0 + sunLimb * 10.0 + sunHalo * 3.0);
    vec3 diffuseRaw    = max(skyColor - sunContribRaw, vec3(0.0));

    // Tone-map diffuse: moderate exposure → preserves blue saturation
    float exp_v      = max(Exposure * 0.6, 0.05);
    vec3  diffMapped = vec3(1.0) - exp(-diffuseRaw * exp_v);
    diffMapped = pow(max(diffMapped, vec3(0.0)), vec3(1.0 / 2.2));

    // Vanilla tint applied to diffuse only, using mix() instead of multiply.
    // mix() pulls the colour TOWARD the vanilla target rather than scaling it,
    // so the result is the correct hue even if our scatter is slightly off.
    // At VanillaTintStrength=0.65 (Earth) the output colour closely matches
    // level.getSkyColor() while our scatter still contributes sunset/twilight shape.
    diffMapped = mix(diffMapped, VanillaSkyTint, VanillaTintStrength);

    // Tone-map sun disk with a very high knee (it should be blindingly bright).
    vec3  sunMapped = vec3(1.0) - exp(-sunContribRaw * max(Exposure, 0.05) * 0.04);
    sunMapped = pow(max(sunMapped, vec3(0.0)), vec3(1.0 / 2.2));

    // Combine: sun disk additively on top of the tinted diffuse sky.
    vec3 mapped = clamp(diffMapped + sunMapped * sunAbove, 0.0, 1.0);

    // ── Star occlusion — hard daytime veil ───────────────────────────────
    // If the sun is above the horizon at all, output alpha = 1.0 with an
    // early return.  No smoothstep, no gradual fade — the sky is simply
    // opaque and the star layer underneath is fully covered.
    //
    // Stars only begin to appear once the sun is below the horizon and the
    // scattered-light budget drops (civil → nautical → astronomical twilight).
    if (sunCosZ >= 0.0) {
        fragColor = vec4(mapped, 1.0);
        return;
    }

    // Sun is below horizon: fade from opaque (just-set) to transparent (full night).
    // sunCosZ ranges from 0 (just set) to ~ -0.30 (deep night).
    // Stars are fully visible once we reach astronomical twilight (~-0.18).
    float twilightAlpha = smoothstep(-0.20, -0.01, sunCosZ);  // 1 at horizon, 0 at astro-night

    // Dense atmospheres (Venus, Titan) keep a residual glow even at night
    float atmoFloor = clamp(SurfaceDensity * 0.10, 0.0, 0.85);
    float alpha     = clamp(max(twilightAlpha, atmoFloor), 0.0, 1.0);

    fragColor = vec4(mapped, alpha);
}
