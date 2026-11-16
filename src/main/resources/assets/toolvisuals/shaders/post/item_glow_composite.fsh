#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D SceneSampler;
uniform sampler2D MaskSampler;
uniform sampler2D GlowSampler;
layout(std140) uniform ItemGlowConfig { vec4 GlowControl; };
layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

#include <toolvisuals:item_glow_composite.glsl>
