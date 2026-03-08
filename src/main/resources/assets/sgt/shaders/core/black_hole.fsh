#version 150

uniform float Time;
uniform float Phase;

in vec2  texCoord0;
in vec4  vertexColor;
in vec3  animData;

out vec4 fragColor;

const float PI = 3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679;

void main() {
    vec2  uv   = texCoord0 - 0.5;
    float dist = length(uv);
    if (dist > 0.5) discard;


    float b = dist * 2.0;




    float shadow    = smoothstep(0.28, 0.20, b);


    float ringPeak  = 0.30;
    float ringWidth = 0.025;
    float photonRing = exp(-pow((b - ringPeak) / ringWidth, 2.0)) * 2.5;




    float diskInner = 0.32;
    float diskOuter = 0.50;
    float diskY     = abs(uv.y) / max(dist, 0.001);
    float inDisk    = smoothstep(diskInner, diskInner + 0.04, b) *
    smoothstep(diskOuter, diskOuter - 0.04, b);

    float diskEquator = pow(1.0 - diskY, 4.0);
    float diskBright  = inDisk * diskEquator;



    float doppler   = uv.x * sin(Phase * PI * 2.0);
    vec3  diskColor = mix(
    vec3(1.0, 0.50, 0.10),
    vec3(1.0, 0.90, 0.60),
    clamp(doppler * 1.5 + 0.5, 0.0, 1.0));


    float flicker = 0.85 + 0.15 * sin(Time * 2.3 + Phase * 6.0);
    diskBright *= flicker;



    float lensGlow = smoothstep(0.30, 0.45, b) * smoothstep(0.50, 0.38, b) * 0.35;
    vec3  lensCol  = vec3(0.8, 0.7, 0.5);


    vec3 bh = vec3(0.0);
    vec3 ring_col = vec3(1.0, 0.85, 0.55) * photonRing;
    vec3 disk_col = diskColor * diskBright * 1.5;
    vec3 lens_col = lensCol   * lensGlow;

    vec3  finalColor = mix(disk_col + ring_col + lens_col, bh, shadow);

    float alpha = max(shadow, max(diskBright * 0.9, max(photonRing * 0.5, lensGlow)));
    alpha = clamp(alpha, 0.0, 1.0) * vertexColor.a;

    if (alpha < 0.001) discard;
    fragColor = vec4(finalColor, alpha);
}