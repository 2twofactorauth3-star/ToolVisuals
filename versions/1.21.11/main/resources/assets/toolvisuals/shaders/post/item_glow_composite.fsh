#version 330

uniform sampler2D SceneSampler;
uniform sampler2D MaskSampler;
uniform sampler2D GlowSampler;
layout(std140) uniform ItemGlowConfig { vec4 GlowControl; };
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 scene = texture(SceneSampler, texCoord);
    vec4 glow = texture(GlowSampler, texCoord);
    float outside = 1.0 - texture(MaskSampler, texCoord).a;
    vec3 light = clamp(glow.rgb * outside * GlowControl.y * 1.8, 0.0, 1.0);
    vec3 tint = glow.rgb / max(glow.a, 0.0001);
    float darkness = 1.0 - max(tint.r, max(tint.g, tint.b));
    float shade = clamp(glow.a * outside * GlowControl.y * darkness * 1.4, 0.0, 0.9);
    fragColor = vec4((1.0 - (1.0 - scene.rgb) * (1.0 - light)) * (1.0 - shade), scene.a);
}
