#version 330

uniform sampler2D Sampler0;
in vec2 texCoord0;
in vec4 vertexColor;
flat in vec2 tileCenter;
flat in vec2 glowSize;
out vec4 fragColor;

#moj_import <toolvisuals:item_glow_gui.glsl>
