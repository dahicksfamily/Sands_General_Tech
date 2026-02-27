#version 150

uniform float PlanetRadiusFraction;
uniform float SurfaceDensity;
uniform float ScaleHeight;
uniform vec3  RayleighCoeff;
uniform vec3  MieCoeff;
uniform float MieDensity;
uniform float MieAnisotropy;
uniform vec3  AirglowColor;
uniform float AirglowIntensity;
uniform float ShadowSoftness;        // terminator blur width
uniform vec3  TerminatorBandColor;    // colour of shadow-edge scatter band
uniform float TerminatorBandIntensity; // 0=disabled, 1=strong (Earth)

// Same uniforms as celestial_body.fsh — used for identical shadow calculation
uniform vec3  LightDirection;
uniform float AmbientLight;

in vec2  texCoord0;
in vec4  vertexColor;
in vec3  vertexNormal;   // view space
in vec3  vertexPos;      // view space

out vec4 fragColor;

const float PI = 3.14159265359;

float chapmanApprox(float mu, float zenithDeg) {
    float sz = clamp(zenithDeg, 0.01, 93.0);
    return 1.0 / max(mu + 0.15 * pow(93.885 - sz, -1.253), 0.001);
}

float rayleighPhase(float cosTheta) {
    return (3.0 / (16.0 * PI)) * (1.0 + cosTheta * cosTheta);
}

float miePhaseHG(float cosTheta, float g) {
    float g2 = g * g;
    return (1.0 - g2) / (4.0 * PI * pow(max(1.0 + g2 - 2.0 * g * cosTheta, 0.0001), 1.5));
}

void main() {

    vec3 n        = normalize(vertexNormal);
    vec3 viewDir  = normalize(-vertexPos);
    // Use LightDirection raw — it is already in the correct space from Java,
    // same as the celestial_body shader does. Do NOT re-transform it.
    vec3 lightDir = normalize(LightDirection);

    // ── Shadow — EXACTLY matching celestial_body.fsh ──────────────────────────
    // dotProduct is the raw N·L value in [-1, 1].
    // We keep the signed version (muLight) for terminator band placement.
    float dotProduct = dot(n, lightDir);               // signed, same as cel body
    float muLight    = clamp(dotProduct, -1.0, 1.0);
    float muView     = clamp(dot(n, viewDir), -1.0, 1.0);

    // Diffuse matches celestial_body.fsh: max(dot,0) then pow(x,1)
    float diffuse       = max(dotProduct, 0.0);
    float shadowAmbient = AmbientLight * 0.05;
    // 'lighting' is the same value the surface shader computes — [shadowAmbient, 1.0]
    float lighting   = clamp(mix(shadowAmbient, 1.0, diffuse), 0.0, 1.0);
    // Convert to a clean [0,1] shadow mask with soft terminator
    float shadow     = smoothstep(0.0, ShadowSoftness, diffuse);

    // ── Optical depth ──────────────────────────────────────────────────────────
    float h       = clamp(1.0 - PlanetRadiusFraction, 0.001, 1.0);
    float density = SurfaceDensity * exp(-h / max(ScaleHeight, 0.001));
    float optBase = density * ScaleHeight;

    float zenithViewDeg  = degrees(acos(clamp(muView,  -1.0, 1.0)));
    float zenithLightDeg = degrees(acos(clamp(muLight, -1.0, 1.0)));
    float chapV = chapmanApprox(max(muView,  0.001), abs(zenithViewDeg));
    float chapL = chapmanApprox(max(muLight, 0.001), clamp(zenithLightDeg, 0.0, 93.0));

    float tauView  = optBase * chapV;

    // ── Scatter magnitude — raw tau, no phase weighting ───────────────────────
    float scatterMag = 1.0 - exp(-tauView);

    // ── Limb factor ───────────────────────────────────────────────────────────
    float limb      = max(1.0 - abs(muView), 0.0);
    float thinness  = 1.0 / (1.0 + SurfaceDensity);
    float limbPow   = mix(0.4, 4.0, thinness);
    float baseLimb  = pow(limb, limbPow);
    // pow(1-thinness,2.5): Venus=0.973(opaque), Earth=0.177(pale blue), Mars=0(limb only)
    float discFloor = pow(clamp(1.0 - thinness, 0.0, 1.0), 2.5);
    float limbFactor = max(baseLimb, discFloor);
    float limbColor  = pow(limb, 0.6);   // wide spread for terminator band
    float limbRing   = pow(limb, 6.0);   // tight ring for airglow only

    // ── Gas colour tint — normalised, never zero or white ─────────────────────
    float rMax    = max(RayleighCoeff.r, max(RayleighCoeff.g, RayleighCoeff.b));
    float mMax    = max(MieCoeff.r,      max(MieCoeff.g,      MieCoeff.b));
    vec3 gasTint  = RayleighCoeff / max(rMax, 0.001);
    vec3 hazeTint = MieCoeff      / max(mMax, 0.001);
    float mieW    = clamp(MieDensity / (1.0 + MieDensity), 0.0, 1.0);
    vec3 baseTint = mix(gasTint, hazeTint, mieW);

    // Gentle hue shift for sunset reddening (capped so it never kills colour)
    float tauLightCapped = min(optBase * chapL, 2.0);
    vec3 sunHueShift     = exp(-gasTint * tauLightCapped * 0.25);
    vec3 litTint         = baseTint * mix(vec3(1.0), sunHueShift, shadow);

    // Mie forward-scatter brightening toward the sun
    float cosAngle = dot(-viewDir, lightDir);
    float mPhase   = miePhaseHG(cosAngle, clamp(MieAnisotropy, 0.0, 0.99));
    float mieBoost = mPhase * MieDensity * 0.3;

    // ── Colour magnitude — boosted for thin atmospheres ────────────────────────
    // Earth's optBase≈0.054 makes scatterMag≈0.05 — nearly invisible as colour.
    // colourBoost = 1/optBase (clamped) normalises thin atmospheres so they still
    // show their characteristic tint across the disc (pale blue for Earth etc.)
    // Venus (optBase≈2.8) gets boost=1.0 — no change, already saturated.
    float colourBoost = clamp(1.0 / max(optBase, 0.001), 1.0, 20.0);
    float colourMag   = tanh(tauView * colourBoost);

    // ── Scatter colour and alpha ───────────────────────────────────────────────
    // COLOUR uses colourMag (boosted) — ensures pale blue tint is visible on Earth.
    // ALPHA  uses colourMag * limbFactor so thick atmospheres stay opaque.
    float alphaScale  = mix(0.92, 2.5, 1.0 - thinness);
    vec3  scatterColor = litTint * (colourMag + mieBoost) * lighting * limbFactor;
    float alpha        = colourMag * limbFactor * shadow * alphaScale;

    // ── Terminator scatter band ────────────────────────────────────────────────
    // The warm orange/red crescent visible at the day-night boundary.
    // Uses dotProduct (= N·L, same as the shadow calc) so the band tilts exactly
    // with the light direction and matches the shadow on the planet body.
    //
    // FIX: was using limbRing (pow 6) → only showed at the outer edge.
    // Now uses limbColor (pow 0.6) → fills the whole shadow crescent correctly.
    //
    // TerminatorBandIntensity = 0 disables it entirely (Venus, gas giants, etc.)
    float bandWidth   = max(ShadowSoftness, 0.08);
    // Gaussian centred on terminator (dotProduct=0), slightly asymmetric:
    // extend a bit more into shadow (negative dot) than into daylight.
    float dotShifted  = dotProduct + bandWidth * 0.3;  // shift peak slightly to shadow side
    float bandGauss   = exp(-pow(dotShifted / bandWidth, 2.0));
    // Band spans the FULL disc at the terminator — do NOT multiply by limbColor.
    // limbColor=0 at disc centre was making the entire crescent invisible.
    // No * lighting here — band is atmospheric scatter, not surface reflection.
    // lighting=0.0075 at the terminator (diffuse=0) was making the band invisible.
    vec3  scatterBand = TerminatorBandColor * bandGauss * TerminatorBandIntensity * shadow * 0.6;
    float bandAlpha   = bandGauss * TerminatorBandIntensity * 0.7;

    // ── Airglow ────────────────────────────────────────────────────────────────
    float airglowShadow = max(shadow, 0.12);
    float airLimb  = limbRing * AirglowIntensity * airglowShadow;
    float termGlow = exp(-pow(dotProduct / max(ShadowSoftness, 0.05), 2.0))
    * AirglowIntensity * 0.4 * limbRing;
    float nightGlo = (1.0 - shadow) * AirglowIntensity * 0.08 * limbRing;
    vec3  airglow  = AirglowColor * (airLimb + termGlow + nightGlo);

    // ── Combine ────────────────────────────────────────────────────────────────
    vec3 finalColor = scatterColor + scatterBand + airglow;

    alpha = max(alpha, bandAlpha);
    alpha = max(alpha, length(airglow) * 1.8);
    alpha = clamp(alpha, 0.0, 1.0);

    fragColor = vec4(finalColor, alpha) * vertexColor;
}
