#version 330

uniform sampler2D Sampler0;
in vec2 texCoord0;
in vec4 vertexColor;
flat in vec2 tileCenter;
flat in vec2 glowSize;
out vec4 fragColor;

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
    float alpha = min(1.0, blurred / 30.0) * (1.0 - mask) * vertexColor.a;
    if (alpha < 0.003) discard;
    fragColor = vec4(vertexColor.rgb, alpha);
}
