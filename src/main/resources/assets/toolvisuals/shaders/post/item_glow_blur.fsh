#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D MaskSampler;
layout(std140) uniform ItemGlowConfig { vec4 GlowControl; };
layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

layout(std140) uniform ItemBlurConfig { vec4 BlurDirection; };

#include <toolvisuals:item_glow_blur.glsl>
