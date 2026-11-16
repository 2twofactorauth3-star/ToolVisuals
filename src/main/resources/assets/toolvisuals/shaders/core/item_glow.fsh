#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D Sampler0;
layout(location = 0) in vec2 texCoord0;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) flat in vec2 tileCenter;
layout(location = 3) flat in vec2 glowSize;
layout(location = 0) out vec4 fragColor;

#include <toolvisuals:item_glow_gui.glsl>
