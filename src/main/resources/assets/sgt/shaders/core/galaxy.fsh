#version 150

uniform vec3  GalaxyColor;
uniform float Brightness;
uniform float Aspect;
uniform float SpiralArms;
uniform float ArmTightness;
uniform float ArmOffset;
uniform float HasBar;
uniform float BarLength;
uniform float BarWidth;
uniform float BulgeSize;
uniform float BulgeColor;
uniform float HaloSize;
uniform float HaloOpacity;
uniform float AGNStrength;
uniform float DustLane;
uniform float DiscOpacity;

in vec2  texCoord0;
in vec4  vertexColor;
in vec3  animData;

out vec4 fragColor;

const float PI = 3.14159265359;

float hash(vec2 p) {
    p = fract(p * vec2(127.1, 311.7));
    p += dot(p, p + 19.19);
    return fract(p.x * p.y);
}

float hash1(float n) { return fract(sin(n) * 43758.5453123); }

float noise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f*f*(3.0-2.0*f);
    return mix(mix(hash(i),          hash(i+vec2(1,0)), f.x),
    mix(hash(i+vec2(0,1)),hash(i+vec2(1,1)), f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0, a = 0.5;
    mat2  rot = mat2(0.8, -0.6, 0.6, 0.8);
    for (int i = 0; i < 6; i++) {
        v += a * noise(p);
        p  = rot * p * 2.1 + vec2(1.7, 9.2);
        a *= 0.48;
    }
    return v;
}

float warpedFbm(vec2 p, float strength) {
    vec2 q = vec2(fbm(p + vec2(0.0, 0.0)),
    fbm(p + vec2(5.2, 1.3)));
    vec2 r = vec2(fbm(p + strength * q + vec2(1.7, 9.2)),
    fbm(p + strength * q + vec2(8.3, 2.8)));
    return fbm(p + strength * r);
}

float sdCapsule(vec2 p, float halfLen, float radius) {
    p.x = p.x - clamp(p.x, -halfLen, halfLen);
    return length(p) - radius;
}

void main() {
    vec2  uv  = texCoord0 - 0.5;
    vec2  suv = vec2(uv.x, uv.y / max(Aspect, 0.05));
    float dist = length(suv);
    if (dist > 0.62) discard;

    float r     = dist * 2.0;
    float theta = atan(suv.y, suv.x);

    float seed1 = animData.x * 0.5 + 0.5;
    float seed2 = abs(animData.y);

    // ── How much disc/arm content exists ─────────────────────────────────
    // discPresence: 0 = pure elliptical, 1 = full spiral
    // Used throughout to scale components that are absent in ellipticals.
    float discPresence = clamp(DiscOpacity * 2.0, 0.0, 1.0);

    // ── DISC WARP ─────────────────────────────────────────────────────────
    float warpAmp   = 0.04 * seed2;
    float warpPhase = seed1 * PI * 2.0;
    float discWarp  = warpAmp * r * r * sin(theta * 1.0 + warpPhase);
    vec2  warpedUV  = vec2(uv.x, uv.y - discWarp * Aspect);

    // ── HALO ──────────────────────────────────────────────────────────────
    float haloRaw = length(uv) * 2.0;
    float hR      = haloRaw / max(HaloSize, 0.01);
    // Normalized Sersic n=4 profile: peak=1 at centre
    // exp(-7.67*(r^0.25 - 1)) / exp(7.67) = exp(-7.67 * r^0.25)
    // but that drops too fast. Use softer visual profile for halo:
    float halo = exp(-2.8 * pow(max(hR, 0.0), 0.5)) * HaloOpacity;
    halo = max(halo, 0.0);

    // Globular clusters
    float gcRadius = 0.35 + seed1 * 0.25;
    float gcRing   = exp(-pow((haloRaw - gcRadius) / 0.12, 2.0));
    float gcNoise  = pow(max(0.0, noise(uv * 18.0 + vec2(seed1 * 5.0)) - 0.72), 2.0) * 8.0;
    halo += gcNoise * gcRing * 0.35 * HaloOpacity;

    vec3 haloTint = mix(vec3(1.0, 0.82, 0.50), vec3(1.0, 0.70, 0.40), seed2 * 0.5);

    // ── DISC ──────────────────────────────────────────────────────────────
    float discH      = 0.32;
    float discRadial = exp(-r / discH);
    float discZ0     = 0.04 / max(Aspect, 0.05);
    float discVert   = exp(-abs(warpedUV.y) / max(discZ0, 0.002));
    float disc       = discRadial * discVert * DiscOpacity;
    float midPlane   = exp(-abs(warpedUV.y) * 3.0 / max(discZ0, 0.002)) * 0.3;
    disc += midPlane * DiscOpacity;

    // ── BAR ───────────────────────────────────────────────────────────────
    float bar = 0.0;
    if (HasBar > 0.5) {
        float d = sdCapsule(suv, BarLength, BarWidth);
        bar = exp(-max(d, 0.0) * max(d, 0.0) / (BarWidth * BarWidth * 0.5));
        float boxiness = 1.0 - 0.3 * abs(sin(suv.y / BarWidth * PI * 0.5));
        bar *= boxiness;
        bar *= smoothstep(BarLength * 1.1, BarLength * 0.3, abs(suv.x));
        bar *= 0.90;
    }

    // ── SPIRAL ARMS ───────────────────────────────────────────────────────
    float spiralMask   = 0.0;
    float spiralArms   = 0.0;
    float armColorFrac = 0.0;

    if (SpiralArms >= 1.5) {
        float armStart = HasBar > 0.5 ? BarLength * 0.9 : BulgeSize * 0.6;
        float spacing  = 2.0 * PI / SpiralArms;

        float warpStrength = 0.12 * r;
        vec2  warpOff      = vec2(
        fbm(suv * 3.5 + vec2(seed1 * 4.0)) - 0.5,
        fbm(suv * 3.5 + vec2(seed2 * 4.0, 2.1)) - 0.5
        ) * warpStrength;
        float thetaWarped = atan(suv.y + warpOff.y, suv.x + warpOff.x);

        float phi     = thetaWarped - ArmTightness * log(max(r, 0.001)) - ArmOffset;
        float fracPhi = fract(phi / spacing + 0.5) - 0.5;
        float armDist = abs(fracPhi) * spacing;

        float armWidth = mix(0.42, 0.14, clamp((r - 0.1) / 0.9, 0.0, 1.0));
        if (HasBar > 0.5) {
            float nearBar = exp(-pow((r - BarLength * 2.0) / 0.08, 2.0));
            armWidth += nearBar * 0.12;
        }

        float rawArm    = exp(-(armDist * armDist) / (armWidth * armWidth));
        float armRadial = smoothstep(armStart, armStart + 0.18, r)
        * smoothstep(1.10, 0.55, r);

        float knot1    = noise(suv * 6.0  + vec2(ArmOffset));
        float knot2    = noise(suv * 13.0 + vec2(ArmOffset * 1.7, 3.1));
        float knotMask = smoothstep(0.6, 1.0, rawArm);
        float knotNoise = mix(1.0,
        mix(0.55 + 0.45 * knot1, 0.65 + 0.35 * knot2, 0.4),
        knotMask * 0.55);

        float leadingEdge = fracPhi > 0.0 ? 1.0 : 1.0 + fracPhi / armWidth * 0.4;
        float feather     = clamp(leadingEdge, 0.6, 1.0);

        spiralMask   = rawArm * armRadial;
        spiralArms   = spiralMask * knotNoise * feather;
        armColorFrac = smoothstep(0.3, 0.85, rawArm);
    }

    // ── INTER-ARM DUST ────────────────────────────────────────────────────
    float interArmDust = 0.0;
    if (SpiralArms >= 1.5 && DiscOpacity > 0.2) {
        float spacing  = 2.0 * PI / SpiralArms;
        float phi      = theta - ArmTightness * log(max(r, 0.001)) - ArmOffset - 0.08;
        float fracPhi  = fract(phi / spacing + 0.5) - 0.5;
        float dustDist = abs(fracPhi) * spacing;
        float dustW    = mix(0.15, 0.06, clamp(r, 0.0, 1.0));
        float dustArm  = exp(-(dustDist * dustDist) / (dustW * dustW));
        float dustRadial = smoothstep(BulgeSize, BulgeSize + 0.2, r)
        * smoothstep(1.0, 0.6, r);
        float dustPatch = warpedFbm(suv * 4.0 + vec2(seed1 * 3.0), 0.3);
        interArmDust = dustArm * dustRadial * dustPatch * 0.55;
    }

    // ── BULGE ─────────────────────────────────────────────────────────────
    //
    // FIX: the previous formula exp(-7.67*(bR^0.25 - 1)) has a peak value
    // of exp(7.67) ≈ 2148 at bR=0. Even with bulgeScale=0.45 this gives
    // ~1353 — completely overblown, producing the giant glowing oval.
    //
    // CORRECT approach: use a normalized profile where peak=1.0 at centre.
    // We use a visual Sersic approximation: exp(-k * bR^n)
    // where k and n are tuned to look like a de Vaucouleurs profile
    // without the enormous unnormalized peak.
    //
    // For n=0.5 (softer than true n=0.25): gives a smooth concentrated
    // glow that looks correct for both bulges and elliptical galaxies.
    //   bR=0.0 → 1.000 (centre)
    //   bR=0.5 → 0.208 (half-light region)
    //   bR=1.0 → 0.043 (effective radius)
    //   bR=2.0 → 0.002 (outer bulge)

    float bR    = r / max(BulgeSize, 0.01);
    float bulge = exp(-4.0 * pow(max(bR, 0.0), 0.5));  // normalized: peak=1 at bR=0
    bulge       = clamp(bulge, 0.0, 1.0);

    // Boxy/peanut shape for barred galaxies (X-shape in edge-on view)
    if (HasBar > 0.5) {
        float peanut = 1.0 + 0.25 * pow(abs(sin(theta * 2.0)), 3.0)
        * exp(-bR * bR * 2.0);
        bulge *= peanut;
        bulge  = clamp(bulge, 0.0, 1.5);
    }

    // Scale bulge down significantly for ellipticals/lenticulars.
    // When there's no disc to share the brightness, the bulge alone must
    // represent the whole galaxy — it needs to look like a soft diffuse glow,
    // not a blown-out white oval.
    // discPresence=0 (elliptical) → scale=0.30
    // discPresence=1 (spiral)     → scale=0.85
    float bulgeScale = mix(0.30, 0.85, discPresence);
    bulge *= bulgeScale;

    // Bulge tint: warm yellow-orange for old stars
    float coreBias  = exp(-bR * bR * 8.0);
    vec3  bulgeTint = mix(
    mix(GalaxyColor * 1.1, vec3(0.92, 0.95, 1.0), 0.4),
    mix(vec3(1.0, 0.90, 0.65), vec3(1.0, 0.80, 0.50), coreBias),
    clamp(BulgeColor + coreBias * 0.3, 0.0, 1.0));

    // ── NUCLEUS / AGN ─────────────────────────────────────────────────────
    float agn      = 0.0;
    vec3  agnColor = vec3(0.0);
    if (AGNStrength > 0.001) {
        float agnCore  = exp(-r * r * 1200.0) * AGNStrength * 4.0;
        float discRing = exp(-pow((r - 0.015) / 0.008, 2.0)) * AGNStrength * 1.5;

        float jetAngle = seed1 * PI;
        vec2  jetDir   = vec2(cos(jetAngle), sin(jetAngle));
        float along1   = dot(suv,  jetDir);
        float along2   = dot(suv, -jetDir);
        float across   = dot(suv, vec2(-jetDir.y, jetDir.x));
        float jetWidth = 0.008;
        float jet1 = 0.0, jet2 = 0.0;
        if (along1 > 0.01) {
            float crossFade = exp(-pow(across / jetWidth, 2.0));
            float lenFade   = exp(-along1 * 5.5);
            float knots     = 0.6 + 0.4 * noise(vec2(along1 * 25.0, seed1 * 10.0));
            jet1 = crossFade * lenFade * knots * AGNStrength * 0.9;
        }
        if (along2 > 0.01) {
            float crossFade = exp(-pow(across / jetWidth, 2.0));
            float lenFade   = exp(-along2 * 5.5);
            float knots     = 0.6 + 0.4 * noise(vec2(along2 * 25.0, seed2 * 10.0));
            jet2 = crossFade * lenFade * knots * AGNStrength * 0.55;
        }

        vec3 coreColor = mix(vec3(1.0, 0.97, 0.90), vec3(0.75, 0.88, 1.0), AGNStrength * 0.4);
        vec3 jetColor  = vec3(0.65, 0.80, 1.0);
        vec3 torusColor = vec3(1.0, 0.72, 0.35);

        agn      = agnCore + discRing + jet1 + jet2;
        agnColor = coreColor * (agnCore + discRing)
        + jetColor  * (jet1 + jet2)
        + torusColor * discRing * 0.5;
    }

    // ── EDGE-ON DUST LANE ─────────────────────────────────────────────────
    float dust = 0.0;
    if (DustLane > 0.01) {
        float dustVis = smoothstep(0.45, 0.05, Aspect);
        if (dustVis > 0.001) {
            float lane1 = exp(-pow(warpedUV.y / 0.012, 2.0))
            * smoothstep(0.5, 0.08, abs(uv.x))
            * smoothstep(BulgeSize * 0.5, BulgeSize * 0.8, abs(uv.x));
            float patchNoise = warpedFbm(uv * 12.0 + vec2(seed1 * 5.0), 0.4);
            float lane2      = exp(-pow(warpedUV.y / 0.020, 2.0))
            * smoothstep(0.4, 0.1, abs(uv.x))
            * patchNoise * 0.6;
            dust = (lane1 + lane2) * DustLane * dustVis;
            dust = clamp(dust, 0.0, 1.0);
        }
    }

    // ── STELLAR POPULATION COLOUR GRADIENT ────────────────────────────────
    float radialAge  = 1.0 - smoothstep(0.0, 0.7, r);
    vec3  warmCore   = mix(vec3(1.0, 0.88, 0.55), vec3(1.0, 0.78, 0.42), radialAge);
    vec3  baseDisc   = GalaxyColor;
    vec3  hotArm     = mix(vec3(0.72, 0.88, 1.0), vec3(0.85, 0.95, 1.0),
    noise(suv * 5.0));
    vec3  interArm   = mix(baseDisc, warmCore, 0.35);
    vec3  discColor  = mix(interArm, hotArm, clamp(armColorFrac, 0.0, 1.0));
    discColor        = mix(discColor, warmCore, radialAge * 0.45);

    // ── COMBINE ───────────────────────────────────────────────────────────
    float discComp = clamp(disc + spiralArms * 1.1 + bar * 0.8, 0.0, 2.5);
    vec3  barTint  = mix(warmCore, baseDisc, 0.55);

    vec3 col = vec3(0.0);
    col += haloTint  * halo;
    col += discColor * disc * DiscOpacity;
    col += hotArm    * spiralArms * 1.1 * DiscOpacity;
    col += barTint   * bar * 0.8;
    col += bulgeTint * bulge;
    if (AGNStrength > 0.001) col += agnColor * agn;

    // Wavelength-dependent dust extinction (blue attenuated most)
    vec3 dustExt = vec3(1.0 - dust * 0.95,
    1.0 - dust * 0.80,
    1.0 - dust * 0.55);
    col *= dustExt;

    vec3 interDustExt = vec3(1.0 - interArmDust * 0.45,
    1.0 - interArmDust * 0.30,
    1.0 - interArmDust * 0.15);
    col *= interDustExt;

    // ── ALPHA ─────────────────────────────────────────────────────────────
    // Ellipticals: only halo + bulge contribute. Both are now properly
    // scaled so the sum stays well below 1.0 for a soft diffuse appearance.
    float alpha = clamp(
    halo     * HaloOpacity * 1.6
    + discComp * DiscOpacity * 0.9
    + bulge    * mix(0.50, 0.90, discPresence)
    + agn      * 0.8,
    0.0, 1.0) * Brightness * vertexColor.a;

    alpha *= (1.0 - dust * 0.60);
    alpha *= (1.0 - interArmDust * 0.20);

    if (alpha < 0.002) discard;
    fragColor = vec4(col * Brightness, alpha);
}