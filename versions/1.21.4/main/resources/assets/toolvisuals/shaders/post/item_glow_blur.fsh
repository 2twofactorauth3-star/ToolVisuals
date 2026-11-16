#version 150

uniform sampler2D MaskSampler;
uniform vec4 GlowControl;
in vec2 texCoord;
out vec4 fragColor;

uniform vec4 BlurDirection;

#moj_import <toolvisuals:item_glow_blur.glsl>
