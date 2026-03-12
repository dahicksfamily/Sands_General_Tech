#version 150

uniform float Time;
uniform float JetAngle;
uniform float Period;
uniform float Amplitude;
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
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p  = rot * p * 2.1 + vec2(1.7, 9.2);
        a *= 0.48;
    }
    return v;
}

// Smooth voronoi — used for accretion disc turbulent cells
float voronoi(vec2 p) {
    vec2  ip = floor(p);
    vec2  fp = fract(p);
    float md = 8.0;
    for (int y = -1; y <= 1; y++)
    for (int x = -1; x <= 1; x++) {
        vec2 nb  = vec2(float(x), float(y));
        vec2 cell = nb + vec2(hash(ip + nb), hash(ip + nb + vec2(31.41, 27.18))) - fp;
        md = min(md, dot(cell, cell));
    }
    return sqrt(md);
}

void main() {
    vec2  uv   = texCoord0 - 0.5;
    float dist = length(uv);
    if (dist > 0.5) discard;

    // Decode per-quasar seed from animData
    float seed1 = animData.x * 0.5 + 0.5;   // [0,1]
    float seed2 = animData.y * 0.5 + 0.5;

    // ── JET AXIS ──────────────────────────────────────────────────────────
    vec2 jetDir  = vec2(cos(JetAngle), sin(JetAngle));
    vec2 jetPerp = vec2(-jetDir.y, jetDir.x);
    // Disc plane is perpendicular to jet
    vec2 discDir = jetPerp;

    // Projected position in jet/disc coordinates
    float alongJet  = dot(uv, jetDir);
    float acrossJet = dot(uv, jetPerp);

    // ── MULTI-TIMESCALE VARIABILITY ────────────────────────────────────────
    // Real quasars flicker on many timescales simultaneously:
    //   Short (accretion disc thermal): period ~ seconds in game time
    //   Medium (corona oscillation): period ~ tens of seconds
    //   Long (jet precession / Eddington cycle): period ~ hundreds
    float flicker1 = sin(Time / max(Period * 0.08, 0.001) * 2.0*PI + Phase*6.28) * 0.5;
    float flicker2 = sin(Time / max(Period * 0.31, 0.001) * 2.0*PI + Phase*3.14) * 0.3;
    float flicker3 = sin(Time / max(Period,         0.001) * 2.0*PI + Phase*2.0*PI) * 0.2;
    // Random sub-flicker: shot noise from disc instabilities
    float shotNoise = noise(vec2(Time * 8.0 + seed1 * 10.0, Time * 5.3 + seed2 * 7.0));
    float shotFlicker = (shotNoise - 0.5) * 0.15;

    float pulse = 1.0 + Amplitude * (flicker1 + flicker2 + flicker3 + shotFlicker);
    pulse = max(pulse, 0.3);

    // ── ACCRETION DISC ────────────────────────────────────────────────────
    // The accretion disc lies in the plane perpendicular to the jet.
    // Seen at an angle it appears as an ellipse.
    // Key physics:
    //   • Temperature gradient: inner disc (blue-white) → outer disc (orange-red)
    //   • Doppler beaming: approaching side (one half) is brighter & bluer
    //   • Innermost stable circular orbit (ISCO) leaves a dark hole at centre
    //   • Turbulent cells from magneto-rotational instability (MRI)

    // Disc inclination: encode via seed (random viewing angle)
    float inclination = 0.25 + seed1 * 0.50;  // 0=edge-on, 1=face-on
    vec2  discUV = vec2(acrossJet / max(inclination, 0.05), alongJet * 0.3);
    float discR  = length(discUV);

    // ISCO: inner edge of disc at r_isco (no emission inside)
    float rISCO    = 0.018;
    float rOuter   = 0.22;
    float discMask = smoothstep(rISCO, rISCO * 1.8, discR)
    * smoothstep(rOuter, rOuter * 0.6, discR);

    // Radial surface brightness: I ∝ r^-3 * (1 - sqrt(r_isco/r))
    // Simplified: strong peak just outside ISCO
    float discProfile = exp(-pow((discR - rISCO * 2.5) / 0.04, 2.0)) * 1.2
    + exp(-discR / 0.06) * 0.5;
    discProfile *= discMask;

    // Temperature gradient colour: inner=blue-white, mid=yellow-white, outer=orange-red
    float discTFrac = clamp((discR - rISCO) / (rOuter - rISCO), 0.0, 1.0);
    vec3  discInner = vec3(0.82, 0.92, 1.00);  // hot blue-white (inner disc, ~10^5 K)
    vec3  discMid   = vec3(1.00, 0.98, 0.90);  // white-yellow (~10^4 K)
    vec3  discOuter = vec3(1.00, 0.62, 0.25);  // orange-red (~3000 K)
    vec3  discColor = discTFrac < 0.4
    ? mix(discInner, discMid,   discTFrac / 0.4)
    : mix(discMid,   discOuter, (discTFrac - 0.4) / 0.6);

    // Doppler beaming: one side of disc rotates toward us → brighter + bluer
    // The approaching side is the one where acrossJet > 0 (arbitrary per quasar)
    float dopplerSign  = seed1 > 0.5 ? 1.0 : -1.0;
    float dopplerPhase = acrossJet * dopplerSign / max(discR, 0.001);
    // Doppler factor D = 1/(γ(1 - β cosθ)) — approximate with smooth bias
    float dopplerFactor = 1.0 + 0.55 * dopplerPhase * discMask;
    discProfile *= max(dopplerFactor, 0.1);
    // Doppler blueshift: approaching side colour shifts bluer
    discColor = mix(discColor, discColor * vec3(0.85, 0.95, 1.10),
    clamp(dopplerPhase * 0.5 * discMask, 0.0, 1.0));

    // MRI turbulent structure: magneto-rotational instability creates
    // rotating spiral density waves in the disc
    float discAngle  = atan(discUV.y, discUV.x);
    float mriSpiral  = sin(discAngle * 3.0 - discR * 40.0 + Time * 2.5) * 0.15 + 0.85;
    float mriCell    = 1.0 - voronoi(discUV * 18.0 + vec2(Time * 0.3)) * 0.25;
    discProfile *= mriSpiral * mriCell;

    // ── X-RAY CORONA ──────────────────────────────────────────────────────
    // Hot plasma hovering above the disc — emits hard X-ray glow
    // Appears as a faint blue-white haze above the disc plane
    float coronaR    = length(uv);
    float corona     = exp(-coronaR * coronaR * 120.0) * 0.8 * pulse;
    // Corona flickers faster than disc (thermal timescale shorter)
    float coronaFlick = 1.0 + 0.3 * sin(Time * 15.0 + seed1 * 20.0)
    + 0.15 * sin(Time * 31.7 + seed2 * 13.0);
    corona *= max(coronaFlick, 0.2);
    vec3  coronaColor = vec3(0.70, 0.85, 1.00);  // hard X-ray: blue

    // ── BROAD EMISSION LINE REGION (BLR) ──────────────────────────────────
    // Clouds of gas at ~0.1 pc orbiting the black hole at high velocity.
    // Produces characteristic fuzzy glow with emission line colours.
    // Visible as a slightly extended haze with greenish/blue tints (OIII, Hβ)
    float blrR    = dist / 0.12;
    float blr     = exp(-blrR * blrR) * 0.35 * pulse;
    // BLR has turbulent structure from cloud velocities
    float blrNoise = 0.75 + 0.25 * fbm(uv * 8.0 + vec2(Time * 0.1, seed1 * 5.0));
    blr *= blrNoise;
    // Emission line mix: Hα (red), Hβ (blue-green), OIII (blue-green), MgII (UV→white)
    vec3 blrColor = mix(
    vec3(0.65, 0.90, 1.00),  // OIII/Hβ dominant (blue-green)
    vec3(1.00, 0.75, 0.55),  // Hα dominant (orange-red)
    seed2 * 0.6              // per-quasar line ratio
    );

    // ── OVEREXPOSED CENTRAL CORE ──────────────────────────────────────────
    // The central engine itself is a point source — appears saturated white
    // with a bloom halo. This is what makes quasars look stellar (quasi-stellar).
    float core = exp(-dist * dist * 2200.0) * 3.5 * pulse;
    // Inner bloom: photons scattered in the accretion disc atmosphere
    float bloom1 = exp(-dist * dist * 380.0) * 1.2 * pulse;
    // Outer bloom: dust scattering halo
    float bloom2 = exp(-dist * dist * 45.0)  * 0.45 * pulse;
    // Very faint outer halo: host galaxy + extended BLR
    float bloom3 = pow(max(0.0, 1.0 - dist * 2.0), 3.0) * 0.15 * pulse;

    vec3 coreColor = mix(vec3(1.0, 0.98, 0.92), vec3(0.90, 0.95, 1.00), seed1 * 0.3);

    // ── RELATIVISTIC JETS ─────────────────────────────────────────────────
    // Two jets launched perpendicular to the accretion disc along the spin axis.
    // Physics:
    //   • Approaching jet (jet1): Doppler boosted — dramatically brighter
    //   • Receding jet (jet2): Doppler dimmed — much fainter
    //   • Both jets have: inner spine (fast, bright) + outer sheath (slow, dim)
    //   • Knots: blobs ejected episodically, appear to move superluminally
    //   • Helical structure from magnetic field winding
    //   • Colour: pure synchrotron — blue-purple

    float jet1 = 0.0, jet2 = 0.0;
    vec3  jet1Color = vec3(0.0), jet2Color = vec3(0.0);

    // Jet geometry: along jetDir away from centre
    float along1 = dot(uv,  jetDir);
    float along2 = dot(uv, -jetDir);
    float across = acrossJet;  // same perp for both jets

    // ── Jet 1 (approaching — strongly Doppler boosted) ────────────────────
    if (along1 > 0.005) {
        float jetLen = 0.48;

        // Jet collimation: narrows from base then slowly re-expands (recollimation shocks)
        float baseWidth    = 0.018;
        float expansionR   = 0.06;   // recollimation shock at this distance
        float postExpansion = max(along1 - expansionR, 0.0) * 0.12;
        float jetRadius    = baseWidth + postExpansion;
        // Second recollimation shock (jets often have multiple)
        float recolShock   = exp(-pow((along1 - 0.18) / 0.015, 2.0)) * 0.008;
        jetRadius += recolShock;

        // Inner spine (fast, bright, narrow) + outer sheath (slow, dim, wide)
        float spine  = exp(-pow(across / (jetRadius * 0.4), 2.0));
        float sheath = exp(-pow(across / (jetRadius * 1.5), 2.0)) * 0.25;
        float profile = spine + sheath;

        // Jet length envelope
        float lenFade = pow(max(0.0, 1.0 - along1 / jetLen), 1.2);

        // Superluminal knots: discrete blobs moving along the jet
        // Multiple knots with different spacings and speeds
        float knot1 = exp(-pow(fract(along1 * 6.0 - Time * 0.8 + seed1) - 0.5, 2.0) / 0.015) * 0.8;
        float knot2 = exp(-pow(fract(along1 * 9.5 - Time * 1.3 + seed2) - 0.5, 2.0) / 0.010) * 0.6;
        float knot3 = exp(-pow(fract(along1 * 4.2 - Time * 0.5 + seed1 * 0.7) - 0.5, 2.0) / 0.020) * 0.5;
        float knotBrightness = 0.55 + 0.45 * clamp(knot1 + knot2 + knot3, 0.0, 1.0);

        // Helical magnetic structure: jet brightness oscillates helically
        float helixPhase = along1 * 22.0 - Time * 1.8 + seed1 * 6.28;
        float helix      = 0.85 + 0.15 * sin(helixPhase);

        // Recollimation shock brightening (standing shock where jet re-focuses)
        float rShock = 1.0 + 2.5 * exp(-pow((along1 - expansionR) / 0.012, 2.0));

        jet1 = profile * lenFade * knotBrightness * helix * rShock * pulse;

        // Doppler boosting: δ^(3+α) where α≈0.5 (spectral index)
        // Approximate with strong brightness boost for approaching jet
        float dopplerBoost = 2.8;
        jet1 *= dopplerBoost;

        // Synchrotron colour: blue at base (freshly accelerated), redder at tip
        // (electrons lose energy — spectral ageing)
        float ageFrac  = along1 / jetLen;
        jet1Color = mix(
        vec3(0.55, 0.70, 1.00),  // young: blue synchrotron
        vec3(0.80, 0.65, 1.00),  // aged: red-shifted synchrotron
        ageFrac * 0.7
        ) * jet1;
    }

    // ── Jet 2 (receding — Doppler dimmed) ────────────────────────────────
    if (along2 > 0.005) {
        float jetLen = 0.48;

        float baseWidth    = 0.018;
        float expansionR   = 0.06;
        float postExpansion = max(along2 - expansionR, 0.0) * 0.12;
        float jetRadius    = baseWidth + postExpansion;

        float spine  = exp(-pow(across / (jetRadius * 0.4), 2.0));
        float sheath = exp(-pow(across / (jetRadius * 1.5), 2.0)) * 0.20;
        float profile = spine + sheath;

        float lenFade = pow(max(0.0, 1.0 - along2 / jetLen), 1.4);

        // Counter-jet knots move in opposite direction (they recede)
        float knot1 = exp(-pow(fract(along2 * 6.0 + Time * 0.8 + seed1) - 0.5, 2.0) / 0.015) * 0.6;
        float knot2 = exp(-pow(fract(along2 * 9.5 + Time * 1.3 + seed2) - 0.5, 2.0) / 0.010) * 0.4;
        float knotBrightness = 0.55 + 0.45 * clamp(knot1 + knot2, 0.0, 1.0);

        float helixPhase = along2 * 22.0 + Time * 1.8 + seed2 * 6.28;
        float helix      = 0.85 + 0.15 * sin(helixPhase);

        // Doppler dimming: counter-jet is significantly fainter (δ^-(3+α))
        float dopplerDim = 0.18;
        jet2 = profile * lenFade * knotBrightness * helix * dopplerDim * pulse;

        float ageFrac  = along2 / jetLen;
        jet2Color = mix(
        vec3(0.55, 0.70, 1.00),
        vec3(0.80, 0.65, 1.00),
        ageFrac * 0.7
        ) * jet2;
    }

    // ── HOTSPOTS / LOBES ──────────────────────────────────────────────────
    // Where the jet terminates, it slams into the IGM and creates a bright
    // hotspot + radio lobe. Visible at the jet tips as a diffuse glow.
    float hotspot1 = 0.0, hotspot2 = 0.0;
    vec3  hotspotColor = vec3(0.60, 0.55, 1.00);  // optically thin: purple-blue
    {
        // Jet1 terminal hotspot
        float hsR1 = length(uv - jetDir * 0.42);
        hotspot1   = exp(-hsR1 * hsR1 / 0.0015) * 1.2 * pulse
        + exp(-hsR1 * hsR1 / 0.008)  * 0.3;
        // Jet2 hotspot (dimmer — counter-lobe)
        float hsR2 = length(uv + jetDir * 0.42);
        hotspot2   = (exp(-hsR2 * hsR2 / 0.0015) * 0.4
        +  exp(-hsR2 * hsR2 / 0.008)  * 0.12) * pulse;
    }

    // ── EXTENDED HOST GALAXY GLOW ─────────────────────────────────────────
    // Quasars live in galaxies. The quasar usually outshines it but a faint
    // underlying elliptical/spiral host is visible as a very soft glow.
    float hostR   = dist / 0.38;
    float host    = exp(-hostR * hostR * 1.5) * 0.08;
    float hostNoise = 0.7 + 0.3 * fbm(uv * 5.0 + vec2(seed1 * 3.0));
    host *= hostNoise;
    vec3  hostColor = mix(vec3(1.0, 0.85, 0.60),   // elliptical host (common)
    vec3(0.80, 0.90, 1.00),   // blue star-forming host
    seed2 * 0.4);

    // ── GRAVITATIONAL LENSING RING ─────────────────────────────────────────
    // Very rarely a background source is lensed into an Einstein ring.
    // Add a subtle partial arc around some quasars.
    float lensRing = 0.0;
    if (seed1 > 0.65) {  // only ~35% of quasars show this
        float ringR    = 0.30 + seed2 * 0.08;
        float ringW    = 0.012;
        float arc      = exp(-pow(dist - ringR, 2.0) / (ringW * ringW));
        // Arc is not complete — breaks up by fbm
        float arcMask  = smoothstep(0.2, 0.7,
        fbm(uv * 6.0 + vec2(seed1 * 4.0, seed2 * 3.0)));
        lensRing = arc * arcMask * 0.25;
    }

    // ── COMBINE ───────────────────────────────────────────────────────────
    // Build final colour from all components

    vec3 col = vec3(0.0);

    // Host galaxy (bottom layer — faintest)
    col += hostColor * host;

    // BLR haze
    col += blrColor * blr;

    // Accretion disc
    col += discColor * discProfile * 1.1;

    // X-ray corona
    col += coronaColor * corona;

    // Jets
    col += jet1Color;
    col += jet2Color;

    // Hotspots / lobes
    col += hotspotColor * (hotspot1 + hotspot2);

    // Einstein ring (faint blue-white — lensed background)
    col += vec3(0.80, 0.90, 1.00) * lensRing;

    // Core bloom layers: saturate to white at centre
    col += coreColor * bloom3;
    col += coreColor * bloom2;
    col += vec3(1.0, 0.99, 0.95) * bloom1;
    col += vec3(1.0, 1.00, 1.00) * core;

    // Modulate by vertex colour
    col *= vertexColor.rgb;

    // ── ALPHA ─────────────────────────────────────────────────────────────
    float alpha = clamp(
    host    * 0.6
    + blr     * 0.8
    + discProfile * 0.9
    + corona  * 0.7
    + (jet1 + jet2) * 0.85
    + (hotspot1 + hotspot2) * 0.7
    + lensRing * 0.5
    + bloom3  * 0.5
    + bloom2  * 1.0
    + bloom1  * 1.2
    + core    * 1.5,
    0.0, 1.0) * vertexColor.a;

    if (alpha < 0.001) discard;
    fragColor = vec4(col, alpha);
}