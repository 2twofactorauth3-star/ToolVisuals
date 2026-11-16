const float Weights[9] = float[9](0.1498048578, 0.1396334813, 0.1130786392, 0.0795607594, 0.0486345167, 0.0258295969, 0.0119184009, 0.0047779950, 0.0016641816);

void main() {
    vec2 texel = 1.0 / vec2(textureSize(MaskSampler, 0));
    if (all(lessThan(abs(BlurDirection.xy), vec2(0.001)))) {
        fragColor = (texture(MaskSampler, texCoord + texel) + texture(MaskSampler, texCoord - texel)
            + texture(MaskSampler, texCoord + vec2(texel.x, -texel.y)) + texture(MaskSampler, texCoord + vec2(-texel.x, texel.y))) * 0.25;
        return;
    }
    vec2 stepSize = BlurDirection.xy * texel * max(GlowControl.x, 0.5) * 1.35 / 8.0;
    vec4 blurred = texture(MaskSampler, texCoord) * Weights[0];
    for (int tap = 1; tap <= 8; tap++) {
        vec2 offset = stepSize * float(tap);
        blurred += (texture(MaskSampler, texCoord + offset) + texture(MaskSampler, texCoord - offset)) * Weights[tap];
    }
    fragColor = blurred;
}
