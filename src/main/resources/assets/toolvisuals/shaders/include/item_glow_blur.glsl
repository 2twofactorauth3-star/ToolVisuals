const float Weights[13] = float[13](0.0999083581, 0.0968345011, 0.0881688166, 0.0754147853, 0.0605974824, 0.0457413795, 0.0324354950, 0.0216066977, 0.0135211259, 0.0079486599, 0.0043896669, 0.0022773292, 0.0011098816);

void main() {
    vec2 texel = 1.0 / vec2(textureSize(MaskSampler, 0));
    if (all(lessThan(abs(BlurDirection.xy), vec2(0.001)))) {
        fragColor = (texture(MaskSampler, texCoord + texel) + texture(MaskSampler, texCoord - texel)
            + texture(MaskSampler, texCoord + vec2(texel.x, -texel.y)) + texture(MaskSampler, texCoord + vec2(-texel.x, texel.y))) * 0.25;
        return;
    }
    vec2 stepSize = BlurDirection.xy * texel * max(GlowControl.x, 0.5) * 2.6 / 12.0;
    vec4 blurred = texture(MaskSampler, texCoord) * Weights[0];
    for (int tap = 1; tap <= 12; tap++) {
        vec2 offset = stepSize * float(tap);
        blurred += (texture(MaskSampler, texCoord + offset) + texture(MaskSampler, texCoord - offset)) * Weights[tap];
    }
    fragColor = blurred;
}
