#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 LightDirection;
uniform vec3 LightDirectionModel;

out vec2  texCoord0;
out vec4  vertexColor;
out float ringLight;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord0   = UV0;
    vertexColor = Color;




    vec3  ringNormalView = normalize(mat3(ModelViewMat) * vec3(0.0, 1.0, 0.0));
    float normalDotLight = abs(dot(ringNormalView, normalize(LightDirection)));

    float diffuse = 0.25 + 0.75 * sqrt(normalDotLight);















    vec3  P     = Position;
    vec3  L     = normalize(LightDirectionModel);
    float dotPL = dot(P, L);
    float disc  = dotPL * dotPL - (dot(P, P) - 1.0);

    float inShadow = 0.0;
    if (dotPL < 0.0) {

        inShadow = smoothstep(-0.01, 0.04, disc);
    }
    float shadowFactor = 1.0 - inShadow * 0.9;

    ringLight = diffuse * shadowFactor;
}