#version 330
#extension GL_ARB_separate_shader_objects : require

uniform vec4 ColorModulator;

uniform sampler2D Sampler0;
in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

#define TEXT_COVERAGE_CHANNEL a
#moj_import <toolvisuals:ui_text_coverage.glsl>

void main() {
    vec4 color = vertexColor * ColorModulator;
    color.a *= uiTextCoverage(texCoord0);
    fragColor = color;
}
