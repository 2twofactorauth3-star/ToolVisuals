#version 330
#extension GL_ARB_separate_shader_objects : require

uniform vec4 ColorModulator;

uniform sampler2D Sampler0;
in vec4 vertexColor;
in vec2 texCoord0;
out vec4 fragColor;

#define TEXT_COVERAGE_CHANNEL r
#define TEXT_GLYPH_ATLAS
#moj_import <toolvisuals:ui_text_coverage.glsl>

void main() {
    vec4 color = vertexColor * ColorModulator;
    color.a *= uiTextCoverage(texCoord0);
    fragColor = color;
}
