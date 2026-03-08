#version 150

uniform vec3  LayerColor;
uniform float LayerAlpha;
uniform float LayerRot;

in vec2  texCoord0;
in vec4  vertexColor;
in vec3  animData;

out vec4 fragColor;

const float PI = 3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679;


float hash(vec2 p) {
    p = fract(p * vec2(127.1, 311.7));
    p += dot(p, p + 19.19);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f*f*(3.0-2.0*f);
    float a = hash(i);
    float b = hash(i + vec2(1,0));
    float c = hash(i + vec2(0,1));
    float d = hash(i + vec2(1,1));
    return mix(mix(a,b,f.x), mix(c,d,f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0, a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p  = p * 2.1 + vec2(1.7, 9.2);
        a *= 0.5;
    }
    return v;
}

void main() {
    vec2  uv   = texCoord0 - 0.5;
    float dist = length(uv);
    if (dist > 0.5) discard;


    float cosR = cos(LayerRot), sinR = sin(LayerRot);
    vec2  ruv  = vec2(uv.x*cosR - uv.y*sinR,
    uv.x*sinR + uv.y*cosR);






    vec2 warp = vec2(
    fbm(ruv * 2.5 + vec2(0.0,  0.0)),
    fbm(ruv * 2.5 + vec2(5.2,  1.3))
    );
    float warped = fbm(ruv * 3.0 + warp * 0.6);


    vec2 warp2 = vec2(
    fbm(ruv * 4.0 + warp * 1.2 + vec2(1.7, 9.2)),
    fbm(ruv * 4.0 + warp * 1.2 + vec2(8.3, 2.8))
    );
    float detail = fbm(ruv * 6.0 + warp2 * 0.4);


    float shape = warped * 0.65 + detail * 0.35;


    float radFade = pow(max(0.0, 1.0 - dist * 2.0), 0.6);


    float core = exp(-dist * dist * 18.0) * 0.5;



    float lane     = exp(-pow(ruv.y * 12.0, 2.0)) * 0.35;
    float darkness = 1.0 - lane;

    float intensity = (shape * radFade + core) * darkness;



    float edgeFade  = clamp(dist * 2.5, 0.0, 1.0);

    vec3  edgeTint  = LayerColor * vec3(0.75, 0.85, 1.0);
    vec3  coreTint  = LayerColor * vec3(1.10, 1.00, 0.90);
    vec3  finalCol  = mix(coreTint, edgeTint, edgeFade);

    float alpha = intensity * LayerAlpha * vertexColor.a;
    if (alpha < 0.002) discard;

    fragColor = vec4(finalCol * intensity, alpha);
}