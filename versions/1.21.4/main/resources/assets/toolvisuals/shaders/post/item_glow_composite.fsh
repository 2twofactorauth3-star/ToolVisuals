#version 150

uniform sampler2D SceneSampler;
uniform sampler2D MaskSampler;
uniform sampler2D GlowSampler;
uniform vec4 GlowControl;
in vec2 texCoord;
out vec4 fragColor;

#moj_import <toolvisuals:item_glow_composite.glsl>
