#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D CurrentSampler;
uniform sampler2D DepthSampler;

layout(std140) uniform MotionConfig {
    mat4 ClipToPrevious;
    vec4 BlurControl;
};

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    vec4 current = texture(CurrentSampler, texCoord);
    if (BlurControl.x <= 0.0) {
        fragColor = current;
        return;
    }
    float depth = texture(DepthSampler, texCoord).r;
    vec4 previous = ClipToPrevious * vec4(texCoord * 2.0 - 1.0, depth * BlurControl.z + BlurControl.w, 1.0);
    if (previous.w <= 0.000001 || any(isnan(previous)) || any(isinf(previous))) {
        fragColor = current;
        return;
    }
    vec2 dimensions = vec2(textureSize(CurrentSampler, 0));
    vec2 velocity = (texCoord - (previous.xy / previous.w * 0.5 + 0.5)) * dimensions * BlurControl.x;
    float pixels = length(velocity);
    if (pixels < 0.35) {
        fragColor = current;
        return;
    }
    velocity *= min(1.0, BlurControl.y / pixels);
    vec2 stepSize = velocity / dimensions * 0.5;
    vec2 halfTexel = 0.5 / dimensions;
    vec3 total = current.rgb;
    float weights = 1.0;
    for (int tap = 1; tap <= 4; tap++) {
        float position = float(tap) * 0.25;
        float kernel = exp(-2.0 * position * position);
        for (int side = -1; side <= 1; side += 2) {
            vec2 uv = texCoord + stepSize * position * float(side);
            if (any(lessThan(uv, halfTexel)) || any(greaterThan(uv, 1.0 - halfTexel))) continue;
            float neighborDepth = texture(DepthSampler, uv).r;
            float difference = abs(neighborDepth - depth) / max(depth, 0.00001);
            float weight = kernel * (1.0 - smoothstep(0.02, 0.12, difference));
            total += texture(CurrentSampler, uv).rgb * weight;
            weights += weight;
        }
    }
    float blend = 0.8 * smoothstep(0.35, 1.5, min(pixels, BlurControl.y));
    fragColor = vec4(mix(current.rgb, total / weights, blend), current.a);
}
