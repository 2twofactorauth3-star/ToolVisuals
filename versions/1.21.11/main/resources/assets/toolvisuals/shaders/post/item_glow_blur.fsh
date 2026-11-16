#version 330

uniform sampler2D MaskSampler;
layout(std140) uniform ItemGlowConfig { vec4 GlowControl; };
in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform ItemBlurConfig { vec4 BlurDirection; };

#moj_import <toolvisuals:item_glow_blur.glsl>
