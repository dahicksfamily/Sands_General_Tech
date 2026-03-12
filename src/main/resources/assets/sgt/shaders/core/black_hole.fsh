#version 150

uniform float Time;
uniform float Phase;

in vec2  texCoord0;
in vec4  vertexColor;
in vec3  animData;

out vec4 fragColor;

const float PI = 3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679;

// ── Noise primitives ──────────────────────────────────────────────────────

float hash(vec2 p) {
    p = fract(p * vec2(127.1, 311.7));
    p += dot(p, p + 19.19);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f*f*(3.0-2.0*f);
    return mix(mix(hash(i),          hash(i+vec2(1,0)), f.x),
    mix(hash(i+vec2(0,1)),hash(i+vec2(1,1)), f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0, a = 0.5;
    mat2 rot = mat2(0.8, -0.6, 0.6, 0.8);
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p  = rot * p * 2.1 + vec2(1.7, 9.2);
        a *= 0.48;
    }
    return v;
}

float voronoi(vec2 p) {
    vec2 ip = floor(p), fp = fract(p);
    float md = 8.0;
    for (int jj = -1; jj <= 1; jj++)
    for (int ii = -1; ii <= 1; ii++) {
        vec2 nb   = vec2(float(ii), float(jj));
        vec2 cell = nb + vec2(hash(ip+nb), hash(ip+nb+vec2(31.41,27.18))) - fp;
        md = min(md, dot(cell, cell));
    }
    return sqrt(md);
}

void main() {
    vec2  uv   = texCoord0 - 0.5;
    float dist = length(uv);
    if (dist > 0.5) discard;

    // Normalised impact parameter b ∈ [0,1]
    float b = dist * 2.0;

    // ── Per-BH personality from Phase ─────────────────────────────────────
    float seed1 = fract(Phase * 3.70);
    float seed2 = fract(Phase * 7.30 + 0.5);
    float seed3 = fract(Phase * 11.1 + 0.3);

    // Kerr spin parameter a/M ∈ [0.2, 0.98]
    // Controls: ISCO size, ergosphere, jet power, photon ring asymmetry
    float spin = 0.20 + seed1 * 0.78;

    // Viewing inclination: 0=edge-on, 1=face-on
    float inclination = 0.18 + seed2 * 0.68;

    // Disc + jet orientation in screen space (random per BH)
    float orientAngle = seed3 * PI * 2.0;
    vec2  discMajor   = vec2(cos(orientAngle),  sin(orientAngle));   // disc equatorial axis
    vec2  spinAxis    = vec2(-discMajor.y, discMajor.x);             // jet / spin axis

    // Screen-space projections onto disc basis
    float discU = dot(uv, discMajor);
    float discV = dot(uv, spinAxis);

    // Deprojected disc-plane coordinates (undo inclination foreshortening)
    float invInc   = 1.0 / max(inclination, 0.05);
    float discVDep = discV * invInc;
    float discR    = sqrt(discU * discU + discVDep * discVDep);
    float discTheta = atan(discVDep, discU);  // azimuthal angle in disc plane

    // ── KERR METRIC SCALES ────────────────────────────────────────────────
    // Schwarzschild: r_photon = 3M  →  b_photon = 3√3 M ≈ 5.196M (normalised here to ≈0.265)
    // Kerr: prograde photon sphere shrinks with spin; shadow distorts (D-shape)
    float rPhoton  = 0.265 - 0.015 * spin;    // photon sphere b
    float rShadow  = rPhoton + 0.013;          // apparent shadow edge
    float rHorizon = rPhoton * 0.78;           // event horizon (inside photon sphere)
    // ISCO: Schwarzschild = 6M (b≈0.32), shrinks to 1M at a=1 (prograde Kerr)
    float rISCO    = 0.310 - 0.095 * spin;
    float rDiscOut = 0.490;

    // ── SHADOW ────────────────────────────────────────────────────────────
    // Kerr shadow is slightly D-shaped: trailing side pushed outward by frame dragging
    float shadowAngle   = atan(uv.y, uv.x);
    float dShape        = 1.0 + 0.07 * spin * sin(shadowAngle - orientAngle);
    float shadowEdge    = rShadow * dShape;

    // Soft outer edge: photon capture cross-section has a smooth profile
    float shadow  = 1.0 - smoothstep(shadowEdge - 0.008, shadowEdge + 0.020, b);
    // Hard interior: event horizon is perfectly opaque
    float horizon = 1.0 - smoothstep(rHorizon - 0.003, rHorizon + 0.008, b);
    shadow = max(shadow, horizon);

    // ── PHOTON RINGS ──────────────────────────────────────────────────────
    // n=0: direct image (brightest)
    // n=1: once around (~23x dimmer, half the width)
    // n=2: twice around (~535x dimmer, quarter the width) — barely perceptible
    //
    // Kerr: approaching-side photons are Doppler boosted; the ring is asymmetric.
    // photonDoppler > 0 = approaching disc side.
    float photonDoppler = dot(normalize(uv + vec2(0.00001)), discMajor)
    * inclination * spin;

    float rn0W  = 0.021 + 0.004 * (1.0 - spin);
    float ring0 = exp(-pow((b - rPhoton) / rn0W, 2.0)) * 3.8;
    ring0 *= 1.0 + 0.85 * photonDoppler;  // Doppler asymmetry

    float ring1 = exp(-pow((b - rPhoton * 0.932) / (rn0W * 0.28), 2.0)) * 0.17;
    ring1 *= 1.0 + 0.50 * photonDoppler;

    float ring2 = exp(-pow((b - rPhoton * 0.903) / (rn0W * 0.10), 2.0)) * 0.014;

    float totalRing = ring0 + ring1 + ring2;

    // Ring colour: sampled from disc thermal spectrum; Doppler shifts hue
    vec3  ringHot  = vec3(0.88, 0.94, 1.00);  // blue-shifted (approaching)
    vec3  ringWarm = vec3(1.00, 0.92, 0.72);  // base (orange-white)
    vec3  ringCold = vec3(1.00, 0.75, 0.38);  // red-shifted (receding)
    float ringFrac = clamp(photonDoppler * 0.65 + 0.5, 0.0, 1.0);
    vec3  ringColor = ringFrac > 0.5
    ? mix(ringWarm, ringHot,  (ringFrac - 0.5) * 2.0)
    : mix(ringCold, ringWarm,  ringFrac        * 2.0);

    // ── ACCRETION DISC — Novikov-Thorne relativistic model ────────────────
    //
    // Disc mask: annulus between ISCO and outer edge
    float discMask = smoothstep(rISCO * 0.82, rISCO * 1.18, discR)
    * smoothstep(rDiscOut, rDiscOut * 0.87, discR);

    // Vertical profile: thin disc h/r = 0.10, sech² approximated as gaussian
    float discH       = discR * 0.10;  // scale height in disc-plane units
    float hScreen     = discH * inclination + 0.003;  // projected height + min for visibility
    float discVert    = exp(-pow(discV / max(hScreen, 0.002), 2.0));
    // Mid-plane brightening (disc midplane is denser — visible edge-on)
    float midplane    = exp(-pow(discV / max(hScreen * 0.28, 0.001), 2.0)) * 0.6;
    discVert         += midplane * (1.0 - inclination);

    // Novikov-Thorne radial surface brightness: I ∝ r^{-3}[1-√(r_ISCO/r)]
    // Strong peak just outside ISCO → falls as r^{-3} outward
    float rRatio    = discR / max(rISCO, 0.001);
    float ntFactor  = (rRatio > 1.0)
    ? max(0.0, 1.0 - 1.0 / sqrt(rRatio)) * pow(rRatio, -2.5) * 3.0
    : 0.0;
    ntFactor = clamp(ntFactor, 0.0, 4.0);

    // Temperature gradient: T ∝ r^{-3/4} (Shakura-Sunyaev)
    // Inner (ISCO): X-ray soft (~10^7 K) — blue-white in Kerr (hotter ISCO)
    // Mid:  optical UV (~10^5 K) — white-yellow
    // Outer: optical red (~3000 K) — orange-red
    float tFrac  = clamp((discR - rISCO) / (rDiscOut - rISCO), 0.0, 1.0);
    vec3  tInner = mix(vec3(0.82, 0.91, 1.00), vec3(0.62, 0.78, 1.00), spin * 0.55);
    vec3  tMid   = vec3(1.00, 0.97, 0.88);
    vec3  tOuter = vec3(1.00, 0.58, 0.18);
    vec3  discThermal = tFrac < 0.35
    ? mix(tInner, tMid,   tFrac / 0.35)
    : mix(tMid,   tOuter, (tFrac - 0.35) / 0.65);

    // Relativistic Doppler beaming: I_obs = I_emit * D^{3+α}, D = 1/[γ(1-β·cosφ)]
    // Keplerian orbital velocity: β = √(r_ISCO/r) × β_ISCO ≈ 0.5 at ISCO
    float orbitBeta    = clamp(0.50 * sqrt(rISCO / max(discR, rISCO * 0.5)), 0.0, 0.95);
    float cosDiscAngle = discU / max(discR, 0.001);
    float dopplerCosPhi = cosDiscAngle * inclination;
    float dopplerD     = 1.0 / max(1.0 - orbitBeta * dopplerCosPhi, 0.04);
    float dopplerFactor = pow(dopplerD, 3.5);  // spectral index α ≈ 0.5 → 3+α = 3.5
    dopplerFactor = clamp(dopplerFactor, 0.04, 9.0);

    // Gravitational redshift: E_obs/E_emit = sqrt(1 - 2r_ISCO/r)  (Schwarzschild approx)
    // Photons climbing out of the potential well lose energy — dims + reddens inner disc
    float gFactor = sqrt(max(1.0 - 2.0 * rISCO / max(discR, rISCO * 0.5), 0.01));
    vec3  gravTint = vec3(1.25, 0.82, 0.50);  // gravitational red-shift tint
    discThermal    = mix(discThermal * gravTint, discThermal, gFactor);

    // Doppler colour shift: approaching → bluer; receding → redder
    vec3 discColor = discThermal;
    float blueShift = clamp((dopplerFactor - 1.0) * 0.22, 0.0, 1.0);
    float redShift  = clamp((1.0 - dopplerFactor) * 0.45, 0.0, 1.0);
    discColor = mix(discColor, discColor * vec3(0.80, 0.92, 1.15), blueShift);
    discColor = mix(discColor, discColor * vec3(1.25, 0.85, 0.55), redShift);

    // MRI turbulence: magnetorotational instability — rotating spiral density waves
    // Inner disc rotates faster → differential rotation encoded in angular velocity
    float omega     = 2.5 / max(sqrt(discR / rISCO), 1.0);  // Keplerian ω
    float mriTheta  = discTheta - Time * omega;
    float mriSpiral = sin(mriTheta * 3.0 - discR * 32.0) * 0.14 + 0.86;
    float mriCell   = 1.0 - voronoi(vec2(discU * 16.0, discV * 16.0 + Time * 0.9)) * 0.22;
    float mri       = mriSpiral * mriCell;

    // Multi-timescale variability (accretion rate fluctuations)
    float var1      = 1.0 + 0.13 * sin(Time * 1.7 + Phase * 6.28);
    float var2      = 1.0 + 0.07 * sin(Time * 5.9 + Phase * 3.14);
    float var3      = 1.0 + 0.04 * sin(Time *15.3 + Phase * 9.42);
    float varShot   = 1.0 + 0.04 * (noise(vec2(Time * 5.5 + seed1 * 8.0,
    Time * 3.8 + seed2 * 6.0)) * 2.0 - 1.0);
    float variability = var1 * var2 * var3 * varShot;

    // Orbiting hot spots: magnetic flux rope brightenings in the disc
    float hs = 0.0;
    // HS1: just outside ISCO — fastest (innermost), highest energy
    float hs1R = rISCO * 1.22;
    float hs1A = Time * (2.5 + seed1 * 1.8) + seed1 * 6.28;
    float hs1Ang = mod(discTheta - hs1A + PI, 2.0*PI) - PI;
    float hs1 = exp(-pow(discR - hs1R, 2.0) / (rISCO * rISCO * 0.018))
    * exp(-hs1Ang * hs1Ang / 0.12) * 2.8;
    // HS2: mid-disc — intermediate period
    float hs2R = rISCO * 2.10;
    float hs2A = Time * (1.4 + seed2 * 1.0) + seed2 * 6.28;
    float hs2Ang = mod(discTheta - hs2A + PI, 2.0*PI) - PI;
    float hs2 = exp(-pow(discR - hs2R, 2.0) / (rISCO * rISCO * 0.030))
    * exp(-hs2Ang * hs2Ang / 0.16) * 1.8;
    // HS3: outer disc — slow, faint, diffuse
    float hs3R = rISCO * 3.20;
    float hs3A = Time * (0.7 + seed3 * 0.5) + seed3 * 6.28;
    float hs3Ang = mod(discTheta - hs3A + PI, 2.0*PI) - PI;
    float hs3 = exp(-pow(discR - hs3R, 2.0) / (rISCO * rISCO * 0.055))
    * exp(-hs3Ang * hs3Ang / 0.22) * 1.0;
    hs = (hs1 + hs2 + hs3) * discMask * discVert;

    float discBright = ntFactor * discMask * discVert
    * dopplerFactor * gFactor * mri * variability;
    vec3  hsColor    = mix(vec3(1.0, 0.88, 0.65), vec3(1.0, 0.72, 0.35), seed2);
    float hsBright   = hs * variability;

    // ── SECONDARY LENSED DISC IMAGE ───────────────────────────────────────
    // Photons looping once around the BH create a thin compressed copy of
    // the disc just outside the photon ring. Appears as a thin bright arc.
    float secondBand = smoothstep(rPhoton + 0.002, rPhoton + 0.010, b)
    * smoothstep(rPhoton + 0.030, rPhoton + 0.016, b);
    // Secondary image maps disc at mirrored azimuth (opposite side)
    float secTheta  = discTheta + PI;  // mirrored in disc plane
    float secDiscR  = discR;           // same radius, other side
    float secRRatio = secDiscR / max(rISCO, 0.001);
    float secNT     = (secRRatio > 1.0)
    ? max(0.0, 1.0 - 1.0/sqrt(secRRatio)) * pow(secRRatio, -2.5) * 0.8
    : 0.0;
    float secondDisc = clamp(secNT, 0.0, 1.2) * secondBand * variability * 0.65;
    // Secondary image: slightly reddened (additional gravitational redshift on loop)
    float sTFrac    = clamp((secDiscR - rISCO) / (rDiscOut - rISCO), 0.0, 1.0);
    vec3  secondCol = sTFrac < 0.35
    ? mix(tInner, tMid,   sTFrac / 0.35)
    : mix(tMid,   tOuter, (sTFrac - 0.35) / 0.65);
    secondCol *= vec3(1.05, 0.88, 0.70);  // net redshift on secondary path

    // ── X-RAY CORONA ──────────────────────────────────────────────────────
    // Hot magnetised plasma hovering above the disc, inverse-Compton scattering
    // disc photons to X-ray energies. Oblate spheroid shape.
    float corona  = exp(-dist * dist * 90.0) * 0.65;
    float coronaOblate = exp(-pow(discU / 0.10, 2.0) - pow(discV / 0.06, 2.0)) * 0.28;
    corona = max(corona, coronaOblate);
    // Magnetic reconnection flares: fast multi-frequency flickering
    float cf1 = 0.88 + 0.12 * sin(Time * 23.0 + seed1 * 20.0);
    float cf2 = 0.88 + 0.12 * sin(Time * 41.7 + seed2 * 13.0);
    float cf3 = 0.88 + 0.12 * noise(vec2(Time * 8.5 + seed1 * 5.0, seed2 * 3.0));
    corona *= cf1 * cf2 * cf3;
    vec3 coronaColor = vec3(0.55, 0.76, 1.00);

    // ── ERGOSPHERE + FRAME DRAGGING (Kerr) ────────────────────────────────
    // Region outside the event horizon where spacetime rotation cannot be resisted.
    // Oblate shape: equatorial extent > polar (cos²θ term).
    // Frame dragging twists the emission — spinning light paths create a twisted glow.
    float ergoStr    = spin * spin;
    float ergoEq     = 0.248 + 0.010 * (1.0 - spin);  // equatorial ergosphere edge
    // Oblate: larger at equator (discMajor direction), smaller toward poles (spinAxis)
    float ergoR      = ergoEq * (1.0 + 0.28 * abs(cosDiscAngle) * inclination);
    float ergosphere = exp(-pow(b - ergoR, 2.0) / 0.00090) * ergoStr * 1.0;
    // Frame dragging: emission rotates with spin, creating a swirl
    float ergoPhase  = atan(uv.y, uv.x) - Time * spin * 2.5 + orientAngle;
    float ergoTwist  = 0.65 + 0.35 * sin(ergoPhase * 4.0);
    ergosphere      *= ergoTwist;
    vec3  ergoColor  = mix(vec3(0.88, 0.60, 1.00), vec3(1.00, 0.78, 0.42), spin * 0.6);

    // ── RELATIVISTIC JETS (Blandford-Znajek) ─────────────────────────────
    // Jet power ∝ a² (spin²). Extract rotational energy via magnetic field threading horizon.
    // Structure: narrow spine (fast, bright) + wide sheath (slow, dimmer)
    // Parabolic collimation from magnetic pressure.
    float jet1V = 0.0, jet2V = 0.0;
    vec3  jet1C = vec3(0.0), jet2C = vec3(0.0);
    float jetPow = spin * spin;

    if (jetPow > 0.04) {
        float along1 = dot(uv,  spinAxis);
        float along2 = dot(uv, -spinAxis);
        float across = dot(uv,  discMajor);
        float jetLen = 0.46;

        // ── Jet 1 (approaching — Doppler boosted ~2.5x) ───────────────────
        if (along1 > 0.006) {
            // Parabolic collimation: width ∝ sqrt(distance)
            float jW    = 0.008 + 0.038 * sqrt(along1);
            float spine = exp(-pow(across / (jW * 0.28), 2.0));
            float sheath= exp(-pow(across / (jW * 2.20), 2.0)) * 0.20;
            float prof  = spine + sheath;
            float lFade = pow(max(0.0, 1.0 - along1 / jetLen), 1.15);

            // Superluminal knots at 3 scales (different ejection episodes)
            float k1 = exp(-pow(fract(along1 *  7.5 - Time * 1.4 + seed1      ) - 0.5, 2.0) / 0.013) * 1.0;
            float k2 = exp(-pow(fract(along1 * 13.0 - Time * 2.5 + seed2      ) - 0.5, 2.0) / 0.008) * 0.75;
            float k3 = exp(-pow(fract(along1 *  4.8 - Time * 0.8 + seed1*0.5 ) - 0.5, 2.0) / 0.022) * 0.55;
            float knots = 0.48 + 0.52 * clamp(k1 + k2 + k3, 0.0, 1.0);

            // Helical magnetic winding (Lorentz force accelerates/confines)
            float helix = 0.78 + 0.22 * sin(along1 * 32.0 - Time * 4.5 + seed1 * 6.28);

            // Recollimation shock: standing shock where ambient pressure re-focuses jet
            float rShock = 1.0 + 2.2 * exp(-pow((along1 - 0.11) / 0.010, 2.0))
            + 1.0 * exp(-pow((along1 - 0.24) / 0.008, 2.0));

            jet1V = prof * lFade * knots * helix * rShock * jetPow * 2.5;

            // Spectral ageing: fresh = blue synchrotron, aged = purple-red (energy loss)
            float age = along1 / jetLen;
            jet1C = mix(vec3(0.42, 0.60, 1.00), vec3(0.85, 0.52, 1.00), age * 0.72) * jet1V;
        }

        // ── Jet 2 (receding — Doppler dimmed ~0.14x) ──────────────────────
        if (along2 > 0.006) {
            float jW    = 0.008 + 0.038 * sqrt(along2);
            float spine = exp(-pow(across / (jW * 0.28), 2.0));
            float sheath= exp(-pow(across / (jW * 2.20), 2.0)) * 0.16;
            float prof  = spine + sheath;
            float lFade = pow(max(0.0, 1.0 - along2 / jetLen), 1.40);

            float k1 = exp(-pow(fract(along2 *  7.5 + Time * 1.4 + seed1      ) - 0.5, 2.0) / 0.013) * 0.70;
            float k2 = exp(-pow(fract(along2 * 13.0 + Time * 2.5 + seed2      ) - 0.5, 2.0) / 0.008) * 0.50;
            float knots = 0.48 + 0.52 * clamp(k1 + k2, 0.0, 1.0);
            float helix = 0.78 + 0.22 * sin(along2 * 32.0 + Time * 4.5 + seed2 * 6.28);

            jet2V = prof * lFade * knots * helix * jetPow * 0.14;

            float age = along2 / jetLen;
            jet2C = mix(vec3(0.42, 0.60, 1.00), vec3(0.85, 0.52, 1.00), age * 0.72) * jet2V;
        }
    }

    // ── GRAVITATIONAL LENSING ARCS ────────────────────────────────────────
    // Background stars lensed into partial arcs at the Einstein radius.
    // θ_E randomised per BH (depends on mass + distance + source geometry).
    float einsteinR = 0.285 + seed3 * 0.14;
    float lensArc   = exp(-pow(b - einsteinR, 2.0) / 0.00045);
    float arcMask   = smoothstep(0.22, 0.72, fbm(uv * 7.5 + vec2(seed1 * 4.2, seed2 * 3.1)));
    lensArc *= arcMask * 0.38;
    vec3  lensColor = mix(vec3(0.78, 0.90, 1.00),   // blue main sequence
    vec3(1.00, 0.83, 0.58),    // K-star orange
    seed2 * 0.55);

    // ── HAWKING RADIATION ─────────────────────────────────────────────────
    // Quantum thermal radiation at T_H ∝ 1/M. Negligible for stellar BHs
    // but artistically rendered as a faint violet haze at the event horizon edge.
    // Reduced for higher spin (superradiance reduces thermal emission).
    float hawkR    = rHorizon + 0.004;
    float hawking  = exp(-pow(b - hawkR, 2.0) / 0.00012) * 0.18 * (1.0 - spin * 0.6);
    vec3  hawkCol  = vec3(0.72, 0.50, 1.00);

    // ── TOROIDAL MAGNETIC FIELD LINES ─────────────────────────────────────
    // The magnetosphere rotates with the BH ergosphere.
    // Visible as faint arc-shaped brightening just outside the shadow.
    float fieldAng  = atan(uv.y, uv.x) - Time * spin * 2.8;
    float fieldStr  = sin(fieldAng * 6.0 + dist * 22.0) * 0.10 + 0.10;
    float fieldMask = smoothstep(rShadow + 0.010, rShadow + 0.055, b)
    * smoothstep(0.38, rShadow + 0.015, b);
    float magField  = fieldStr * fieldMask * spin * spin * 0.9;

    // ── COMBINE ───────────────────────────────────────────────────────────
    vec3 col = vec3(0.0);

    // Layer from back to front:

    // 1. Lensed background sources + Hawking glow
    col += lensColor  * lensArc;
    col += hawkCol    * hawking;

    // 2. Primary accretion disc
    col += discColor  * discBright * 1.35;
    col += hsColor    * hsBright;

    // 3. Secondary lensed disc image (thin arc near photon ring)
    col += secondCol  * secondDisc;

    // 4. X-ray corona
    col += coronaColor * corona * 0.52;

    // 5. Toroidal magnetic structure
    col += vec3(0.65, 0.50, 1.00) * magField;

    // 6. Ergosphere frame-drag glow
    col += ergoColor  * ergosphere * 0.75;

    // 7. Photon rings (bright edge of shadow)
    col += ringColor  * totalRing;

    // 8. Jets (over everything)
    col += jet1C;
    col += jet2C;

    // ── SHADOW APPLICATION ────────────────────────────────────────────────
    // Shadow absorbs ALL photons. But the front face of the accretion disc
    // passes THROUGH the shadow region (it's between us and the BH).
    // Front disc = side with positive Doppler (approaching us).
    // We partially preserve the front disc emission through the soft shadow.
    float frontDisc   = clamp(discBright * dopplerFactor * inclination * 0.25, 0.0, 0.90);
    float frontHS     = clamp(hsBright   * inclination * 0.18,                 0.0, 0.80);
    float frontTotal  = clamp(frontDisc + frontHS, 0.0, 0.95);

    // Soft shadow: absorbs most light, front-disc passes through partially
    float shadowStr   = shadow * (1.0 - frontTotal * 0.45);
    col = mix(col, vec3(0.0), shadowStr);

    // Hard event horizon: absolutely opaque
    col = mix(col, vec3(0.0), horizon * 0.998);

    col *= vertexColor.rgb;

    // ── ALPHA ─────────────────────────────────────────────────────────────
    float alpha = clamp(
    shadow      * 0.98
    + discBright  * 0.88
    + hsBright    * 0.70
    + totalRing   * 0.58
    + secondDisc  * 0.55
    + corona      * 0.42
    + ergosphere  * 0.55
    + (jet1V + jet2V) * 0.78
    + lensArc     * 0.48
    + hawking     * 0.42
    + magField    * 0.35,
    0.0, 1.0) * vertexColor.a;

    if (alpha < 0.001) discard;
    fragColor = vec4(col, alpha);
}