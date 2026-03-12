#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec3 vertPos;

void main() {
    // Pass raw position to fragment shader for use as the view direction.
    // The sky dome is a sphere centred at the origin, so position IS direction.
    vertPos = Position;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
