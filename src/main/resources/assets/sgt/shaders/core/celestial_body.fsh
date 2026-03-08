#version 150

uniform sampler2D Sampler0;

uniform vec3  LightDirection;
uniform float AmbientLight;
uniform float StarRadius;



uniform vec3  ShadowCasterDir0;  uniform float ShadowCasterRadius0;
uniform vec3  ShadowCasterDir1;  uniform float ShadowCasterRadius1;
uniform vec3  ShadowCasterDir2;  uniform float ShadowCasterRadius2;
uniform vec3  ShadowCasterDir3;  uniform float ShadowCasterRadius3;
uniform vec3  ShadowCasterDir4;  uniform float ShadowCasterRadius4;
uniform vec3  ShadowCasterDir5;  uniform float ShadowCasterRadius5;
uniform vec3  ShadowCasterDir6;  uniform float ShadowCasterRadius6;
uniform vec3  ShadowCasterDir7;  uniform float ShadowCasterRadius7;

uniform vec3  ReflectedLightDir0;  uniform vec3 ReflectedLightColor0;
uniform vec3  ReflectedLightDir1;  uniform vec3 ReflectedLightColor1;
uniform vec3  ReflectedLightDir2;  uniform vec3 ReflectedLightColor2;
uniform vec3  ReflectedLightDir3;  uniform vec3 ReflectedLightColor3;

in vec2  texCoord0;
in vec4  vertexColor;
in vec3  vertexNormal;
in vec3  vertexPos;

out vec4 fragColor;



















float shadowTerm(vec3 n, vec3 lDir, vec3 casterDir, float casterRadius) {
    if (casterRadius <= 0.0) return 0.0;

    vec3  cDir   = normalize(casterDir);
    float cosSep = dot(cDir, lDir);
    if (cosSep <= 0.0) return 0.0;

    float separation   = acos(clamp(cosSep, -1.0, 1.0));
    float combinedDisk = casterRadius + StarRadius;


    if (separation >= combinedDisk) return 0.0;









    float ingress = 1.0 - smoothstep(combinedDisk * 0.7, combinedDisk, separation);





    float umbraEdge    = max(0.0, casterRadius - StarRadius);
    float penumbraEdge = casterRadius + max(StarRadius, 1e-5);
    float fragAng      = acos(clamp(dot(n, cDir), -1.0, 1.0));

    float rawShadow = 1.0 - smoothstep(umbraEdge, penumbraEdge, fragAng);
    return rawShadow * ingress;
}

vec3 reflectedTerm(vec3 n, float dotStar, vec3 rDir, vec3 rColor) {
    if (dot(rColor, rColor) == 0.0) return vec3(0.0);
    float rDot        = max(dot(n, normalize(rDir)), 0.0);
    float nightFactor = clamp(1.0 - dotStar * 4.0, 0.0, 1.0);
    return rColor * rDot * nightFactor;
}

void main() {
    vec4 texColor = texture(Sampler0, texCoord0);
    vec3 n        = normalize(vertexNormal);
    vec3 lDir     = normalize(LightDirection);

    float dotStar = max(dot(n, lDir), 0.0);




    float s0 = shadowTerm(n, lDir, ShadowCasterDir0, ShadowCasterRadius0);
    float s1 = shadowTerm(n, lDir, ShadowCasterDir1, ShadowCasterRadius1);
    float s2 = shadowTerm(n, lDir, ShadowCasterDir2, ShadowCasterRadius2);
    float s3 = shadowTerm(n, lDir, ShadowCasterDir3, ShadowCasterRadius3);
    float s4 = shadowTerm(n, lDir, ShadowCasterDir4, ShadowCasterRadius4);
    float s5 = shadowTerm(n, lDir, ShadowCasterDir5, ShadowCasterRadius5);
    float s6 = shadowTerm(n, lDir, ShadowCasterDir6, ShadowCasterRadius6);
    float s7 = shadowTerm(n, lDir, ShadowCasterDir7, ShadowCasterRadius7);
    float totalShadow = max(max(max(s0, s1), max(s2, s3)),
    max(max(s4, s5), max(s6, s7)));


    float diffuse = dotStar * (1.0 - totalShadow);
    float ambient = AmbientLight * 0.05;
    float lighting = clamp(ambient + (1.0 - ambient) * diffuse, 0.0, 1.0);


    vec3 reflected = vec3(0.0);
    reflected += reflectedTerm(n, dotStar, ReflectedLightDir0, ReflectedLightColor0);
    reflected += reflectedTerm(n, dotStar, ReflectedLightDir1, ReflectedLightColor1);
    reflected += reflectedTerm(n, dotStar, ReflectedLightDir2, ReflectedLightColor2);
    reflected += reflectedTerm(n, dotStar, ReflectedLightDir3, ReflectedLightColor3);

    vec3 finalRGB = texColor.rgb * (lighting + reflected);
    fragColor = vec4(clamp(finalRGB, 0.0, 1.0), texColor.a) * vertexColor;
}