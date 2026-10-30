#version 150

#moj_import <minecraft:fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

in vec4 toolvisualsTint;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
#ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
#endif
    float peak = max(max(toolvisualsTint.r, toolvisualsTint.g), toolvisualsTint.b);
    float shade = peak > 0.001 ? max(max(vertexColor.r, vertexColor.g), vertexColor.b) / peak : 1.0;
#ifdef TOOLVISUALS_SOLID_FILL
    color.rgb = toolvisualsTint.rgb * shade * ColorModulator.rgb;
#else
    float luma = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 tint = toolvisualsTint.rgb * (0.35 + 0.65 * luma);
    color.rgb = mix(color.rgb, tint, toolvisualsTint.a) * shade * ColorModulator.rgb;
#endif
    color.a *= ColorModulator.a;
#ifndef NO_OVERLAY
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
#endif
#ifndef EMISSIVE
    color *= lightMapColor;
#endif
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
