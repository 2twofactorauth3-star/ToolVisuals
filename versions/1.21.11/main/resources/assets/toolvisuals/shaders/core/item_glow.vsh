#version 330
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;

out vec2 texCoord0;
out vec4 vertexColor;
flat out vec2 tileCenter;
flat out vec2 glowSize;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord0 = UV0;
    vertexColor = Color * ColorModulator;
    tileCenter = vec2(UV2);
    glowSize = vec2(float(UV1.x), float(UV1.y) / 256.0);
}
