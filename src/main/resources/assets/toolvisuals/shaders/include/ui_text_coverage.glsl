float uiTextSample(vec2 uv) {
    return textureLod(Sampler0, uv, 0.0).TEXT_COVERAGE_CHANNEL;
}

float uiTextCoverage(vec2 uv) {
    vec2 footprint = fwidth(uv);
    vec2 size = vec2(textureSize(Sampler0, 0));
#ifdef TEXT_GLYPH_ATLAS
    footprint = min(footprint, vec2(2.0) / size);
#endif
    vec2 texels = footprint * size;
    float span = max(texels.x, texels.y);
    if (span <= 1.1) {
        return uiTextSample(uv);
    }
    if (span <= 2.5) {
        vec2 offset = footprint * 0.25;
        return (uiTextSample(uv + vec2(-offset.x, -offset.y))
              + uiTextSample(uv + vec2( offset.x, -offset.y))
              + uiTextSample(uv + vec2(-offset.x,  offset.y))
              + uiTextSample(uv + vec2( offset.x,  offset.y))) * 0.25;
    }
    float coverage = 0.0;
    for (int y = 0; y < 4; y++) {
        for (int x = 0; x < 4; x++) {
            vec2 offset = (vec2(x, y) - vec2(1.5)) * footprint * 0.25;
            coverage += uiTextSample(uv + offset);
        }
    }
    return coverage * 0.0625;
}
