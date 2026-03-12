#version 150

uniform float Time;

in vec2  texCoord0;
in vec4  vertexColor;
in vec3  animData;

out vec4 fragColor;

const float PI = 3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679;

void main() {
    vec2  uv   = texCoord0 - 0.5;
    float dist = length(uv);

    float phase   = animData.x * 0.5 + 0.5;
    float amp     = animData.y * 0.5 + 0.5;
    float periodN = animData.z * 0.5 + 0.5;
    float period  = periodN < 0.01 ? 0.0 : periodN * 500.0;

    float anim;
    if (period > 0.5) {
        anim = sin(Time / period * 2.0 * PI + phase * 2.0 * PI) * amp;
    } else {
        float t1 = sin(Time * 3.1  + phase * 6.28) * 0.6;
        float t2 = sin(Time * 7.3  + phase * 9.42) * 0.3;
        float t3 = sin(Time * 13.7 + phase * 3.14) * 0.1;
        anim = (t1 + t2 + t3) * amp;
    }
    float brightness = clamp(vertexColor.a + anim, 0.0, 1.0);

    float pixelSize = max(fwidth(texCoord0.x), fwidth(texCoord0.y));
    float subpixel  = clamp((pixelSize - 0.05) / 0.45, 0.0, 1.0);
    float r2        = pixelSize * pixelSize;

    float core      = exp(-dist * dist * 80.0);
    float glowWidth = mix(30.0, 8.0, amp);
    float glow      = exp(-dist * dist * glowWidth) * mix(0.15, 0.55, amp);
    float pointIntensity = core + glow;

    float coreAvg = r2 > 0.0001
    ? (1.0 - exp(-80.0 * r2)) / (80.0 * r2)
    : 1.0;
    float glowAvg = r2 > 0.0001
    ? (1.0 - exp(-glowWidth * r2)) / (glowWidth * r2) * mix(0.15, 0.55, amp)
    : mix(0.15, 0.55, amp);
    float integratedIntensity = coreAvg + glowAvg;

    float intensity = mix(pointIntensity, integratedIntensity, subpixel);


    if (amp > 0.55 && dist < 0.5 && subpixel < 0.5) {
        float spikeStr = (amp - 0.55) / 0.45 * (1.0 - subpixel * 2.0);
        float spike =
        max(0.0, 1.0 - abs(uv.x) * 40.0) * max(0.0, 1.0 - abs(uv.y) * 400.0) +
        max(0.0, 1.0 - abs(uv.y) * 40.0) * max(0.0, 1.0 - abs(uv.x) * 400.0);
        intensity += spike * spikeStr * 0.4;
    }


    // compute color + alpha
    vec3 col = vertexColor.rgb * brightness * intensity;
    float alpha = clamp(brightness * intensity, 0.0, 1.0);

    // drop tiny fragments so the background truly becomes transparent there
    if (alpha < 0.0008) discard;

    // output straight (non-premultiplied) RGBA
    fragColor = vec4(col, alpha);
}