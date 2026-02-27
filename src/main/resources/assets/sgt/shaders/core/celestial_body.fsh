#version 150

uniform sampler2D Sampler0;
uniform vec3  LightDirection;  // view-space: from body toward star
uniform float AmbientLight;
uniform float StarRadius;      // angular radius of star in radians

// ── Shadow casters (4 slots, radius=0 means disabled) ─────────────────────────
// No int count uniform — variable-bound GLSL loops are unreliable across drivers.
// Instead: always 4 slots, ShadowCasterRadius[i] == 0.0 means "slot unused".
// All directions are in view-space (transformed by skyRot in Java).
uniform vec3  ShadowCasterDir0;    uniform float ShadowCasterRadius0;
uniform vec3  ShadowCasterDir1;    uniform float ShadowCasterRadius1;
uniform vec3  ShadowCasterDir2;    uniform float ShadowCasterRadius2;
uniform vec3  ShadowCasterDir3;    uniform float ShadowCasterRadius3;

// ── Reflected / secondary light (4 slots, color=black means disabled) ─────────
uniform vec3  ReflectedLightDir0;  uniform vec3 ReflectedLightColor0;
uniform vec3  ReflectedLightDir1;  uniform vec3 ReflectedLightColor1;
uniform vec3  ReflectedLightDir2;  uniform vec3 ReflectedLightColor2;
uniform vec3  ReflectedLightDir3;  uniform vec3 ReflectedLightColor3;

in vec2  texCoord0;
in vec4  vertexColor;
in vec3  vertexNormal;
in vec3  vertexPos;

out vec4 fragColor;

// Returns the shadow contribution [0,1] from one caster.
// radius==0 → returns 0 (slot disabled).
float shadowTerm(vec3 n, vec3 lightDir, vec3 casterDir, float casterRadius) {
    if (casterRadius <= 0.0) return 0.0;
    vec3  cDir    = normalize(casterDir);
    // Only cast shadow on the lit hemisphere (occluder must be between body and star)
    float onLit   = step(0.0, dot(cDir, normalize(lightDir)));
    float fragAng = acos(clamp(dot(n, cDir), -1.0, 1.0));
    float umbraEdge    = max(0.0, casterRadius - StarRadius);
    float penumbraEdge = casterRadius + StarRadius;
    return (1.0 - smoothstep(umbraEdge, penumbraEdge, fragAng)) * onLit;
}

// Returns reflected light contribution on one side.
// color==black → returns 0 (slot disabled).
vec3 reflectedTerm(vec3 n, float dotPrimary, vec3 rDir, vec3 rColor) {
    if (dot(rColor, rColor) <= 0.0) return vec3(0.0);
    float rDot    = max(dot(n, normalize(rDir)), 0.0);
    float darkSide = 1.0 - dotPrimary;  // only fill the shadow side
    return rColor * rDot * darkSide;
}

void main() {
    vec4 texColor = texture(Sampler0, texCoord0);
    vec3 n        = normalize(vertexNormal);
    vec3 lightDir = normalize(LightDirection);

    // ── Primary (star) lighting ────────────────────────────────────────────────
    float dotPrimary    = max(dot(n, lightDir), 0.0);
    float shadowAmbient = AmbientLight * 0.05;
    float lighting      = clamp(mix(shadowAmbient, 1.0, dotPrimary), 0.0, 1.0);

    // ── Shadow casters (unrolled — 4 slots, no loop) ──────────────────────────
    float s0 = shadowTerm(n, lightDir, ShadowCasterDir0, ShadowCasterRadius0);
    float s1 = shadowTerm(n, lightDir, ShadowCasterDir1, ShadowCasterRadius1);
    float s2 = shadowTerm(n, lightDir, ShadowCasterDir2, ShadowCasterRadius2);
    float s3 = shadowTerm(n, lightDir, ShadowCasterDir3, ShadowCasterRadius3);
    // max() so overlapping shadows don't double-darken
    float totalShadow = max(max(s0, s1), max(s2, s3));
    // 0.03 residual = solar corona during totality
    lighting *= 1.0 - totalShadow * 0.97;

    // ── Reflected light (unrolled — 4 slots, no loop) ─────────────────────────
    vec3 reflected = vec3(0.0);
    reflected += reflectedTerm(n, dotPrimary, ReflectedLightDir0, ReflectedLightColor0);
    reflected += reflectedTerm(n, dotPrimary, ReflectedLightDir1, ReflectedLightColor1);
    reflected += reflectedTerm(n, dotPrimary, ReflectedLightDir2, ReflectedLightColor2);
    reflected += reflectedTerm(n, dotPrimary, ReflectedLightDir3, ReflectedLightColor3);

    vec3 finalRGB = texColor.rgb * vec3(lighting) + texColor.rgb * reflected;
    fragColor = vec4(finalRGB, texColor.a) * vertexColor;
}
