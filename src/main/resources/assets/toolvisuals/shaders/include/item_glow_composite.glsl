void main() {
    vec4 scene = texture(SceneSampler, texCoord);
    vec4 glow = texture(GlowSampler, texCoord);
    float outside = 1.0 - texture(MaskSampler, texCoord).a;
    vec3 tint = clamp(glow.rgb / max(glow.a, 0.0001), 0.0, 1.0);
    float coverage = clamp(glow.a, 0.0, 1.0);
    float wave = 0.5 + 0.5 * sin((texCoord.x * 1.25 + texCoord.y * 0.75 - GlowControl.w) * 6.283185307);
    float sheen = wave * wave * wave * wave;
    float energy = coverage * max(GlowControl.y, 0.0) * 5.2 * (1.0 + GlowControl.z * sheen);
    float brightness = max(tint.r, max(tint.g, tint.b));
    vec3 light = vec3(1.0) - exp(-(tint + vec3(coverage * coverage * brightness * 0.2)) * energy);
    float shade = (1.0 - brightness) * (1.0 - exp(-energy)) * outside;
    vec3 base = scene.rgb * (1.0 - shade);
    fragColor = vec4(clamp(base + (vec3(1.0) - base) * light * outside, 0.0, 1.0), scene.a);
}
