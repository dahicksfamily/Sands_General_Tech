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
uniform float ShadowSoftness;
uniform vec3  TerminatorBandColor;
uniform float TerminatorBandIntensity;
uniform vec3  LightDirection;
uniform float AmbientLight;

in vec2  texCoord0;
in vec4  vertexColor;
in vec3  vertexNormal;
in vec3  vertexPos;

out vec4 fragColor;

const float PI = 3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679;

float chapmanApprox(float mu, float zenithDeg) {
    float sz = clamp(zenithDeg, 0.01, 93.0);
    return 1.0 / max(mu + 0.15 * pow(93.885 - sz, -1.253), 0.001);
}

float miePhaseHG(float cosTheta, float g) {
    float g2 = g * g;
    return (1.0 - g2) / (4.0 * PI * pow(max(1.0 + g2 - 2.0 * g * cosTheta, 0.0001), 1.5));
}

void main() {
    vec3 n        = normalize(vertexNormal);
    vec3 viewDir  = normalize(-vertexPos);
    vec3 lightDir = normalize(LightDirection);

    float muView  = dot(n, viewDir);
    float muLight = dot(n, lightDir);

    if (muView <= 0.0) discard;


    float diffuse  = max(muLight, 0.0);
    float ambient  = AmbientLight * 0.05;
    float lighting = clamp(ambient + (1.0 - ambient) * diffuse, 0.0, 1.0);
    float shadow   = smoothstep(-ShadowSoftness * 0.3, ShadowSoftness, muLight);


    float h       = clamp(1.0 - PlanetRadiusFraction, 0.001, 1.0);
    float density = SurfaceDensity * exp(-h / max(ScaleHeight, 0.001));
    float optBase = density * ScaleHeight;

    float zenithViewDeg  = degrees(acos(clamp(muView,  0.0, 1.0)));
    float zenithLightDeg = degrees(acos(clamp(muLight, -1.0, 1.0)));
    float chapV = chapmanApprox(max(muView,  0.001), abs(zenithViewDeg));
    float chapL = chapmanApprox(max(muLight, 0.001), clamp(zenithLightDeg, 0.0, 93.0));

    float tauView  = optBase * chapV;
    float tauLight = min(optBase * chapL, 3.0);


    float rMax = max(RayleighCoeff.r, max(RayleighCoeff.g, RayleighCoeff.b));
    float mMax = max(MieCoeff.r,      max(MieCoeff.g,      MieCoeff.b));
    vec3 rayleighTint = RayleighCoeff / max(rMax, 0.001);
    vec3 mieTint      = MieCoeff      / max(mMax, 0.001);
    float mieWeight   = clamp(MieDensity / (1.0 + MieDensity), 0.0, 1.0);
    vec3 baseTint = mix(rayleighTint, mieTint, mieWeight);


    vec3 sunHueShift = exp(-rayleighTint * tauLight * 0.3);
    vec3 litTint     = baseTint * mix(vec3(1.0), sunHueShift, shadow);


    float cosAngle = dot(-viewDir, lightDir);
    float mPhase   = miePhaseHG(cosAngle, clamp(MieAnisotropy, 0.0, 0.99));
    float mieBoost = mPhase * MieDensity * 0.3;


    float limb     = max(1.0 - muView, 0.0);
    float thinness = 1.0 / (1.0 + SurfaceDensity);

    float colourBoost = clamp(1.0 / max(optBase, 0.001), 1.0, 20.0);
    float tauBoosted  = tanh(tauView * colourBoost);


    float shellA_peak  = 0.91;
    float shellA_width = 0.09;
    float shellA = max(exp(-pow((limb - shellA_peak) / shellA_width, 2.0)), 0.0);


    float shellB_peak  = 0.972;
    float shellB_width = 0.032;
    float shellB = max(exp(-pow((limb - shellB_peak) / shellB_width, 2.0)), 0.0);
    vec3 shellBTint = rayleighTint;


    float discBase     = pow(muView, 0.3);
    float discFloor    = 1.0 - exp(-SurfaceDensity * 0.2);
    float discThick    = discFloor * (1.0 - limb * limb);
    float discStrength = clamp(SurfaceDensity * 1.2, 0.0, 1.0);


    vec3 colA = litTint * (tauBoosted + mieBoost) * shellA * lighting * 1.4;

    float shellBLight = clamp(shadow + 0.15, 0.0, 1.0);
    vec3 colB = shellBTint * tauBoosted * shellB * shellBLight * 1.3;




    vec3 colDisc = rayleighTint * tauBoosted
    * (discBase * discStrength + discThick * 0.7)
    * lighting;

    vec3 scatterColor = colA + colB + colDisc;


    float thinScale = mix(0.92, 2.5, 1.0 - thinness);
    float alphaA    = tauBoosted * shellA * shadow * thinScale * 1.4;
    float alphaB    = tauBoosted * shellB * shellBLight * 1.4;




    float alphaDisc = tauBoosted * (discBase * discStrength * 0.7 + discThick * 0.8);

    float alpha = alphaA + alphaB + alphaDisc;


    float bandWidth  = max(ShadowSoftness, 0.08);
    float dotShifted = muLight + bandWidth * 0.3;
    float bandGauss  = exp(-pow(dotShifted / bandWidth, 2.0));
    float bandGate   = smoothstep(0.97, 0.70, limb);
    vec3  scatterBand = TerminatorBandColor * bandGauss * TerminatorBandIntensity
    * bandGate * 0.25;
    float bandAlpha   = bandGauss * TerminatorBandIntensity * bandGate * 0.35;


    float limbRing      = pow(limb, 6.0);
    float airglowShadow = max(shadow, 0.12);
    float termGlow      = exp(-pow(muLight / max(ShadowSoftness, 0.05), 2.0))
    * AirglowIntensity * 0.4 * limbRing;
    float nightGlo      = (1.0 - shadow) * AirglowIntensity * 0.08 * limbRing;
    vec3  airglow       = AirglowColor * ((limbRing * AirglowIntensity * airglowShadow)
    + termGlow + nightGlo);


    vec3  finalColor = scatterColor + scatterBand + airglow;
    float finalAlpha = max(max(alpha, bandAlpha), length(airglow) * 1.8);
    finalAlpha = clamp(finalAlpha, 0.0, 1.0);

    fragColor = vec4(finalColor, finalAlpha) * vertexColor;
}