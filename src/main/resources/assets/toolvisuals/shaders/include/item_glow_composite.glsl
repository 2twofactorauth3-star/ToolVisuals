void main() {
    vec4 scene = texture(SceneSampler, texCoord);
    vec4 glow = texture(GlowSampler, texCoord);
    float outside = 1.0 - texture(MaskSampler, texCoord).a;
    vec3 tint = clamp(glow.rgb / max(glow.a, 0.0001), 0.0, 1.0);
    float coverage = clamp(glow.a, 0.0, 1.0);
    float bloom = 1.0 - exp(-coverage * 5.0);
    float rim = smoothstep(0.12, 0.48, coverage);
    float wave = 0.5 + 0.5 * sin((texCoord.x * 1.25 + texCoord.y * 0.75 - GlowControl.w) * 6.283185307);
    float sheen = wave * wave * wave * wave;
    float brightness = max(tint.r, max(tint.g, tint.b));
    vec3 radiance = tint * (0.72 + 0.24 * bloom) + vec3(0.75 * rim * rim * brightness * (1.0 + GlowControl.z * sheen));
    float opacity = (1.0 - exp(-bloom * bloom * max(GlowControl.y, 0.0) * 3.3 * (1.0 + GlowControl.z * sheen * 0.3))) * outside;
    fragColor = vec4(clamp(mix(scene.rgb, radiance, opacity), 0.0, 1.0), scene.a);
}
