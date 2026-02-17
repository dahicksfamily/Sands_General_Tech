#version 150

uniform sampler2D Sampler0;
uniform vec3 LightDirection;
uniform float AmbientLight;

in vec2 texCoord0;
in vec4 vertexColor;
in vec3 vertexNormal;
in vec3 vertexPos;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(Sampler0, texCoord0);

    float dotProduct = max(dot(normalize(vertexNormal), normalize(LightDirection)), 0.0);

    // Higher power = sharper terminator
    float diffuse = pow(dotProduct, 1.5);

    // Much lower ambient in shadows
    float shadowAmbient = AmbientLight * 0.05;

    // Smooth transition between shadow and light
    float lighting = mix(shadowAmbient, 1.0, diffuse);

    // Clamp to prevent over-bright
    lighting = clamp(lighting, 0.0, 1.0);

    vec4 litColor = texColor * vec4(lighting, lighting, lighting, 1.0);

    fragColor = litColor * vertexColor;
}