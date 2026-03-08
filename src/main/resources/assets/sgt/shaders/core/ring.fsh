#version 150

uniform sampler2D Sampler0;
uniform float RingOpacity;

in vec2  texCoord0;
in vec4  vertexColor;
in float ringLight;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(Sampler0, texCoord0);


    if (texColor.a * RingOpacity < 0.001) discard;

    vec3  finalColor = texColor.rgb * ringLight;
    float finalAlpha = texColor.a * RingOpacity;

    fragColor = vec4(finalColor, finalAlpha) * vertexColor;
}