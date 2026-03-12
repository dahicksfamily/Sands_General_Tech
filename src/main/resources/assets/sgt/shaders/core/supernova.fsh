#version 150

uniform float Time;
uniform float Progress;
uniform float Phase;

in vec2  texCoord0;
in vec4  vertexColor;
in vec3  animData;

out vec4 fragColor;

const float PI  = 3.14159265359;
const float TAU = 6.28318530718;

float hash(vec2 p) {
    p = fract(p * vec2(127.1, 311.7));
    p += dot(p, p + 19.19);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i),             hash(i + vec2(1,0)), f.x),
    mix(hash(i + vec2(0,1)), hash(i + vec2(1,1)), f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0, a = 0.5;
    mat2 rot = mat2(0.80,-0.60, 0.60,0.80);
    for (int i = 0; i < 6; i++) {
        v += a * noise(p);
        p  = rot * p * 2.07 + vec2(1.7, 9.2);
        a *= 0.50;
    }
    return v;
}

float wfbm(vec2 p, float warp) {
    vec2 q = vec2(fbm(p), fbm(p + vec2(5.2, 1.3)));
    vec2 r2 = vec2(fbm(p + warp * q + vec2(1.7, 9.2)),
    fbm(p + warp * q + vec2(8.3, 2.8)));
    return fbm(p + warp * r2);
}

float ridge(vec2 p) {
    float f = wfbm(p, 0.9);
    float r = 0.0;
    r += exp(-pow((f - 0.58) / 0.06, 2.0)) * 1.0;
    r += exp(-pow((f - 0.72) / 0.04, 2.0)) * 0.7;
    r += exp(-pow((f - 0.44) / 0.05, 2.0)) * 0.5;
    return r;
}

void main() {
    vec2  uv    = texCoord0 - 0.5;
    float r     = length(uv);
    float theta = atan(uv.y, uv.x);

    if (r > 0.5) discard;

    float p = clamp(Progress, 0.0, 1.0);
    float t = Time * 0.06;

    float s1 = fract(Phase * 5.731);
    float s2 = fract(Phase * 9.173 + 0.41);
    float s3 = fract(Phase * 3.444 + 0.73);
    float s4 = fract(Phase * 7.891 + 0.22);
    float s5 = fract(Phase * 2.114 + 0.87);

    // ── PHASE SPLIT ───────────────────────────────────────────────────────
    // p < FLASH_END  : initial blinding star flash, expanding rapidly
    // p >= FLASH_END : nebula / remnant phase, slowly fading
    // The Java side is responsible for making the 0→FLASH_END range
    // correspond to the first few real-time seconds.
    const float FLASH_END = 0.12;

    // How far through each phase we are [0,1]
    float flashT  = clamp(p / FLASH_END, 0.0, 1.0);
    float nebT    = clamp((p - FLASH_END) / (1.0 - FLASH_END), 0.0, 1.0);
    float inFlash = smoothstep(FLASH_END, FLASH_END * 0.5, p);  // 1=flash, 0=nebula
    float inNeb   = smoothstep(FLASH_END * 0.5, FLASH_END, p);  // 0=flash, 1=nebula

    // ── FLASH PHASE: STAR POINT ───────────────────────────────────────────
    // At p=0 the quad is tiny (Java scales it to star size).
    // Visually it should be a blinding white-blue point with a diffraction cross
    // and a short bloom. As flashT increases it blooms outward and turns yellow.

    // Core point — tight Gaussian, max brightness at t=0
    float starCore   = exp(-r * r / 0.0006) * 8.0;
    // Bloom halo — wider, fades as flash progresses (the initial shockwave flash)
    float bloomWidth = mix(0.0015, 0.025, flashT * flashT);
    float starBloom  = exp(-r * r / bloomWidth) * mix(5.0, 0.6, flashT);
    // Diffraction spikes (4-pointed cross, classic star look)
    float spike1 = exp(-pow(abs(uv.x) / 0.006, 1.5)) * exp(-r * r / 0.04);
    float spike2 = exp(-pow(abs(uv.y) / 0.006, 1.5)) * exp(-r * r / 0.04);
    float spikes = (spike1 + spike2) * mix(3.0, 0.0, flashT * 1.5);
    // Outer shock ring expanding during flash
    float shockFlash = exp(-pow(r - mix(0.02, 0.45, flashT * flashT), 2.0) / 0.0012)
    * mix(3.0, 0.2, flashT);

    float flashTotal = starCore + starBloom + spikes + shockFlash;
    // Color: blue-white at peak → warm yellow-white as it expands
    vec3  flashColor = mix(vec3(0.80, 0.90, 1.00),   // hot shock: blue-white
    vec3(1.00, 0.92, 0.70),   // expanding: warm yellow
    flashT * flashT);

    // ── NEBULA PHASE ──────────────────────────────────────────────────────
    // Same as before but brightness is calm — it's large and colorful, not blinding.

    float outerPert = 0.0;
    outerPert += sin(theta *  5.0 + s1 * TAU) * 0.028;
    outerPert += sin(theta *  9.0 + s2 * TAU) * 0.018;
    outerPert += sin(theta * 15.0 + s3 * TAU) * 0.012;
    outerPert += sin(theta * 23.0 + s4 * TAU) * 0.007;
    outerPert += sin(theta * 37.0 + s5 * TAU) * 0.004;
    outerPert += (fbm(vec2(cos(theta)*2.8 + s1*3.0, sin(theta)*2.8 + s2*2.5))*2.0-1.0)*0.038;
    float rOuter = 0.830 + outerPert;

    float innerPert = 0.0;
    innerPert += sin(theta * 3.0 + s3 * TAU) * 0.025;
    innerPert += sin(theta * 7.0 + s4 * TAU) * 0.018;
    innerPert += sin(theta * 11.0 + s1 * TAU) * 0.012;
    innerPert += (fbm(vec2(cos(theta)*2.2 + s5*4.0, sin(theta)*2.2 + s3*3.0))*2.0-1.0)*0.028;
    float rInner = rOuter * (0.38 + 0.06 * s2) + innerPert;

    float shellT   = clamp((r - rInner) / max(rOuter - rInner, 0.001), 0.0, 1.0);
    float inShell  = smoothstep(rOuter + 0.008, rOuter - 0.005, r)
    * smoothstep(rInner - 0.018, rInner + 0.010, r);
    float inCavity = smoothstep(rInner + 0.012, rInner - 0.005, r);

    // Filaments
    float shellU = theta * 3.0;
    float shellV = shellT * 4.5;
    vec2 fp1 = vec2(shellU * 1.0 + s1*6.0 + t*0.3,  shellV * 1.0 + s2*5.0);
    vec2 fp2 = vec2(shellU * 2.5 + s3*4.0 - t*0.2,  shellV * 2.5 + s4*3.0);
    vec2 fp3 = vec2(shellU * 5.5 + s5*3.0 + t*0.5,  shellV * 5.5 + s1*4.0);
    float fil1 = ridge(fp1);
    float fil2 = ridge(fp2) * 0.7;
    float fil3 = ridge(fp3) * 0.4;
    float filament = clamp((fil1*0.55 + fil2*0.30 + fil3*0.15) * inShell, 0.0, 1.0);
    float knot = clamp(fil1 * fil2 * 2.5, 0.0, 1.0) * inShell;

    vec3 cInnerFil = vec3(0.22, 0.75, 0.88);
    vec3 cMidFil   = vec3(0.72, 0.42, 0.90);
    vec3 cOuterFil = vec3(1.00, 0.40, 0.14);
    vec3 cHotKnot  = vec3(1.00, 0.95, 0.80);
    vec3 cCoolWisp = vec3(0.88, 0.50, 0.22);

    vec3 filBaseColor = shellT < 0.35
    ? mix(cInnerFil, cMidFil,   shellT / 0.35)
    : mix(cMidFil,   cOuterFil, (shellT - 0.35) / 0.65);
    filBaseColor = mix(filBaseColor, cCoolWisp, fbm(uv*7.0 + vec2(s4*4.0, s5*3.0)) * 0.40);
    filBaseColor = mix(filBaseColor, cHotKnot,  knot * 0.60);

    float shellGlow = inShell * (0.25 + 0.75 * wfbm(uv*4.5 + vec2(s1*3.0, t*0.2), 0.6));
    shellGlow *= smoothstep(0.0, 0.20, shellT) * smoothstep(1.0, 0.80, shellT);
    vec3 shellGlowColor = mix(vec3(1.00, 0.35, 0.10), vec3(0.30, 0.60, 0.80),
    fbm(uv*3.0 + vec2(s2*4.0)) * 0.65);

    float synchroBase = inCavity * (0.20 + 0.80*fbm(uv*6.0 + vec2(t*0.15 + s3*4.0)))
    * smoothstep(0.0, rInner*0.8, r);
    float wispR       = fract(r / max(rInner,0.001) * 2.2 - t*0.25 + s1);
    float cavWisps    = smoothstep(0.3,0.55,wispR)*smoothstep(0.85,0.55,wispR)
    * fbm(vec2(theta*4.0+s2*5.0, r*8.0)) * 0.25 * inCavity * 0.60;
    vec3 cavColor = mix(vec3(0.28,0.42,1.00), vec3(0.60,0.28,1.00),
    fbm(uv*3.5 + vec2(s4*3.0)) * 0.7);

    float limbArg   = max(1e-4, 1.0-(r/max(rOuter,0.001))*(r/max(rOuter,0.001)));
    float shockRing = exp(-pow((r-rOuter)/(0.012+0.008*nebT), 2.0))
    * clamp(0.18/sqrt(limbArg), 0.0, 3.5)
    * (0.65 + 0.35*fbm(vec2(theta*12.0+s1*5.0, t*0.3)));

    float ejBlob = smoothstep(rOuter-0.01, rOuter+0.06, r)
    * smoothstep(0.499, rOuter+0.04, r)
    * wfbm(uv*6.0+vec2(s5*4.0), 0.5);
    ejBlob *= ejBlob * 1.8;

    float psrPP    = fract(t / (0.3 + s1*8.0));
    float psrPulse = exp(-psrPP*psrPP*120.0) + exp(-(psrPP-0.5)*(psrPP-0.5)*200.0)*0.35;
    float psr      = (exp(-r*r/(0.004*0.004))*4.0 + exp(-r*r/(0.025*0.025))*0.6)
    * (0.6 + 2.5*psrPulse);

    // Nebula composite
    vec3  nebCol   = vec3(0.0);
    float nebAlpha = 0.0;
    nebCol   += vec3(0.95,0.30,0.10) * ejBlob * 1.3;    nebAlpha += ejBlob * 0.55;
    nebCol   += vec3(0.90,0.70,1.00) * shockRing * 2.0; nebAlpha += shockRing * 0.70;
    nebCol   += shellGlowColor * shellGlow * 0.9;        nebAlpha += shellGlow * 0.55;
    nebCol   += filBaseColor * filament * 2.2;           nebAlpha += filament * 0.90;
    nebCol   += cHotKnot * knot * 1.5;                  nebAlpha += knot * 0.80;
    nebCol   += cavColor * synchroBase * 0.85;           nebAlpha += synchroBase * 0.55;
    nebCol   += cavColor * cavWisps * 0.60;              nebAlpha += cavWisps * 0.35;
    nebCol   += vec3(0.80,0.92,1.00) * psr;             nebAlpha += psr * 0.75;

    // Nebula brightness: calm — it's big and colorful but NOT blinding
    // Fades gently over the full nebular lifetime
    float nebBright = mix(0.70, 0.06, nebT * nebT);

    // ── BLEND FLASH + NEBULA ──────────────────────────────────────────────
    vec3  col   = mix(nebCol   * nebBright, flashColor * flashTotal, inFlash);
    float alpha = mix(nebAlpha * nebBright, clamp(flashTotal * 0.9, 0.0, 1.0), inFlash);

    col   *= vertexColor.rgb;
    alpha  = clamp(alpha * vertexColor.a * 1.20, 0.0, 1.0);

    if (alpha < 0.001) discard;
    fragColor = vec4(col, alpha);
}
