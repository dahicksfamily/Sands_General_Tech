#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord0;
out vec4 vertexColor;
out vec3 vertexNormal;
out vec3 vertexPos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexColor = Color;

    // Transform normal to world space
    vertexNormal = normalize(mat3(ModelViewMat) * Normal);

    // Pass position in view space
    vertexPos = (ModelViewMat * vec4(Position, 1.0)).xyz;
}