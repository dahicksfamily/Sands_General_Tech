#version 150

uniform vec3  GalaxyColor;
uniform float Brightness;
uniform float Aspect;

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
    return mix(mix(hash(i),         hash(i+vec2(1,0)), f.x),
    mix(hash(i+vec2(0,1)),hash(i+vec2(1,1)), f.x), f.y);
}

void main() {
    vec2  uv   = texCoord0 - 0.5;



    vec2  suv  = vec2(uv.x, uv.y / max(Aspect, 0.05));
    float dist = length(suv);
    if (dist > 0.5) discard;




    bool  isSpiral = animData.z > 0.0;
    float seed     = animData.x * 0.5 + 0.5;

    float r     = dist * 2.0;
    float theta = atan(suv.y, suv.x);

    float spiralArms = 0.0;
    if (isSpiral) {

        float armPitch  = mix(0.5, 1.4, seed);
        float arm1 = cos(2.0 * (theta - armPitch * log(max(r, 0.001))));
        float arm2 = cos(2.0 * (theta - armPitch * log(max(r, 0.001)) + PI));

        float armRadial = smoothstep(0.0, 0.25, r) * smoothstep(1.0, 0.45, r);
        spiralArms = max(arm1, arm2) * armRadial;
        spiralArms = pow(max(spiralArms, 0.0), 1.8);
    }



    float bulge = exp(-pow(r * 5.5, 0.5)) * 1.4;



    float disc  = exp(-r * 3.5) * 0.6;



    float dust = 0.0;
    if (Aspect < 0.30) {

        dust = smoothstep(0.04, 0.0, abs(uv.y)) * smoothstep(0.45, 0.1, abs(uv.x));
    }



    float halo = pow(max(0.0, 1.0 - r), 3.0) * 0.15;


    float shape = (bulge + disc + spiralArms * 0.8 + halo) * (1.0 - dust * 0.85);


    vec3  bulgeCol = GalaxyColor * vec3(1.10, 1.00, 0.80);
    vec3  armCol   = GalaxyColor * vec3(0.85, 0.92, 1.10);
    float armFrac  = isSpiral ? spiralArms * (1.0 - bulge * 0.7) : 0.0;
    vec3  finalCol = mix(bulgeCol, armCol, clamp(armFrac, 0.0, 1.0));

    float alpha = clamp(shape * Brightness, 0.0, 1.0) * vertexColor.a;
    if (alpha < 0.002) discard;

    fragColor = vec4(finalCol * shape, alpha);
}