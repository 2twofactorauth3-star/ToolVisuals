#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D MaskSampler;
layout(std140) uniform ItemGlowConfig { vec4 GlowControl; };
layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    vec2 texel = 1.0 / vec2(textureSize(MaskSampler, 0));
    vec4 blurred = texture(MaskSampler, texCoord) * 2.0;
    float total = 2.0;
    for (int tap = 0; tap < 32; tap++) {
        float t = (float(tap) + 0.5) / 32.0;
        float angle = float(tap) * 2.39996323;
        float weight = exp(-t * 2.5);
        vec2 offset = vec2(cos(angle), sin(angle)) * sqrt(t) * max(GlowControl.x, 0.5) * texel;
        blurred += texture(MaskSampler, texCoord + offset) * weight;
        total += weight;
    }
    fragColor = blurred / total;
}
