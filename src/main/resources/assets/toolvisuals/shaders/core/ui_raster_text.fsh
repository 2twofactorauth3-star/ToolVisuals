#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;
layout(location = 0) in vec2 texCoord0;
layout(location = 1) in vec4 vertexColor;
layout(location = 0) out vec4 fragColor;

#define TEXT_COVERAGE_CHANNEL a
#include <toolvisuals:ui_text_coverage.glsl>

void main() {
    vec4 color = vertexColor * ColorModulator;
    color.a *= uiTextCoverage(texCoord0);
    fragColor = color;
}
