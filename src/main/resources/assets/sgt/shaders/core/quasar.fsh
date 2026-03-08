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

void main() {
    vec2  uv   = texCoord0 - 0.5;
    float dist = length(uv);


    float pulse = 1.0 + Amplitude * sin(Time / Period * 2.0 * PI + Phase * 2.0 * PI);



    float core = pow(max(0.0, 1.0 - dist * 6.0), 0.7) * pulse;
    float halo = pow(max(0.0, 1.0 - dist * 2.2), 2.0) * 0.5 * pulse;




    vec2 jetDir  = vec2(cos(JetAngle), sin(JetAngle));
    vec2 jetPerp = vec2(-jetDir.y, jetDir.x);

    float along1  = dot(uv,  jetDir);
    float across1 = dot(uv,  jetPerp);
    float along2  = dot(uv, -jetDir);



    float jetWidth = 0.025;
    float jet1 = 0.0, jet2 = 0.0;
    if (along1 > 0.02 && along1 < 0.50) {
        float crossFade  = exp(-pow(across1 / jetWidth, 2.0));
        float lengthFade = pow(1.0 - along1 / 0.50, 1.5);

        float knot = 0.7 + 0.3 * sin(along1 * 40.0 + Time * 3.0);
        jet1 = crossFade * lengthFade * knot * pulse;
    }
    if (along2 > 0.02 && along2 < 0.50) {
        float crossFade  = exp(-pow(dot(uv, jetPerp) / jetWidth, 2.0));
        float lengthFade = pow(1.0 - along2 / 0.50, 1.5);
        float knot = 0.7 + 0.3 * sin(along2 * 40.0 - Time * 3.0 + 1.5);

        jet2 = crossFade * lengthFade * knot * pulse * 0.5;
    }

    float jets = max(jet1, jet2);



    vec3 coreCol = vec3(1.0, 0.95, 0.80) * (core + halo);

    vec3 jetCol  = vec3(0.70, 0.80, 1.00) * jets * 1.2;

    vec3  finalColor = (coreCol + jetCol) * vertexColor.rgb;
    float alpha      = clamp(core*1.5 + halo + jets, 0.0, 1.0) * vertexColor.a;

    if (alpha < 0.001) discard;
    fragColor = vec4(finalColor, alpha);
}