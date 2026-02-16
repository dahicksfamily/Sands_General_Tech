#version 150

uniform sampler2D Sampler0;
uniform vec3 LightDirection; // Direction TO the sun (normalized)
uniform float AmbientLight;  // Ambient lighting (0.0 - 1.0)

in vec2 texCoord0;
in vec4 vertexColor;
in vec3 vertexNormal;
in vec3 vertexPos;

out vec4 fragColor;

void main() {
    // Sample the texture
    vec4 texColor = texture(Sampler0, texCoord0);

    // Calculate diffuse lighting (Lambertian)
    float diffuse = max(dot(normalize(vertexNormal), normalize(LightDirection)), 0.0);

    // Combine ambient and diffuse
    float lighting = clamp(AmbientLight + diffuse, 0.0, 1.0);

    // Apply lighting to texture
    vec4 litColor = texColor * vec4(lighting, lighting, lighting, 1.0);

    // Apply vertex color (for brightness/tint)
    fragColor = litColor * vertexColor;
}