float coverage(vec2 point, vec2 dimensions) {
    vec2 distanceFromCenter = abs(point - tileCenter);
    if (any(greaterThan(distanceFromCenter, vec2(glowSize.x * 0.5 - 0.5)))) return 0.0;
    return texture(Sampler0, point / dimensions).a;
}

void main() {
    vec2 dimensions = vec2(textureSize(Sampler0, 0));
    vec2 point = texCoord0 * dimensions;
    float mask = coverage(point, dimensions);
    float blurred = 0.0;
    for (int ring = 1; ring <= 3; ring++) {
        float radius = glowSize.y * float(ring) / 3.0;
        float weight = 4.0 - float(ring);
        for (int tap = 0; tap < 8; tap++) {
            float angle = (float(tap) + float(ring) * 0.5) * 0.7853981634;
            blurred += coverage(point + vec2(cos(angle), sin(angle)) * radius, dimensions) * weight;
        }
    }
    float density = blurred / 48.0;
    float alpha = (1.0 - exp(-density * 5.0)) * (1.0 - mask) * vertexColor.a;
    if (alpha < 0.003) discard;
    float rim = smoothstep(0.18, 0.55, density);
    float brightness = max(vertexColor.r, max(vertexColor.g, vertexColor.b));
    fragColor = vec4(mix(vertexColor.rgb, vec3(1.0), rim * 0.55 * brightness), alpha);
}
