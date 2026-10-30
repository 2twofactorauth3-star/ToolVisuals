#version 330 core

#import <sodium:include/fog.glsl>
#import <sodium:include/chunk_material.glsl>

in vec4 v_Color; 
in vec2 v_TexCoord; 
in vec2 v_FragDistance; 
in float fadeFactor;

flat in uint v_Material;

uniform sampler2D u_BlockTex; 

uniform vec4 u_FogColor; 
uniform vec2 u_EnvironmentFog; 
uniform vec2 u_RenderFog; 
uniform vec2 u_TexelSize;
uniform bool u_UseRGSS;
uniform vec4 u_ToolVisualsShader;

out vec4 fragColor; 

vec4 sampleNearest(sampler2D sampler, vec2 uv, vec2 pixelSize, vec2 du, vec2 dv, vec2 texelScreenSize) {
    
    vec2 uvTexelCoords = uv / pixelSize;
    vec2 texelCenter = round(uvTexelCoords) - 0.5f;
    vec2 texelOffset = uvTexelCoords - texelCenter;

    
    texelOffset = (texelOffset - 0.5f) * pixelSize / texelScreenSize + 0.5f;
    texelOffset = clamp(texelOffset, 0.0f, 1.0f);

    uv = (texelCenter + texelOffset) * pixelSize;
    return textureGrad(sampler, uv, du, dv);
}

vec4 sampleNearest(sampler2D source, vec2 uv, vec2 pixelSize) {
    vec2 du = dFdx(uv);
    vec2 dv = dFdy(uv);
    vec2 texelScreenSize = sqrt(du * du + dv * dv);
    return sampleNearest(source, uv, pixelSize, du, dv, texelScreenSize);
}


vec4 sampleRGSS(sampler2D source, vec2 uv, vec2 pixelSize) {
    vec2 du = dFdx(uv);
    vec2 dv = dFdy(uv);

    vec2 texelScreenSize = sqrt(du * du + dv * dv);
    float maxTexelSize = max(texelScreenSize.x, texelScreenSize.y);

    float minPixelSize = min(pixelSize.x, pixelSize.y);

    float transitionStart = minPixelSize * 1.0;
    float transitionEnd = minPixelSize * 2.0;
    float blendFactor = smoothstep(transitionStart, transitionEnd, maxTexelSize);

    float duLength = length(du);
    float dvLength = length(dv);
    float minDerivative = min(duLength, dvLength);
    float maxDerivative = max(duLength, dvLength);

    float effectiveDerivative = sqrt(minDerivative * maxDerivative);

    float mipLevelExact = max(0.0, log2(effectiveDerivative / minPixelSize));

    const vec2 offsets[4] = vec2[](
    vec2(0.125, 0.375),
    vec2(-0.125, -0.375),
    vec2(0.375, -0.125),
    vec2(-0.375, 0.125)
    );

    vec4 rgssColor = vec4(0.0);
    for (int i = 0; i < 4; ++i) {
        vec2 sampleUV = uv + offsets[i] * pixelSize;
        rgssColor += textureLod(source, sampleUV, mipLevelExact);
    }
    rgssColor *= 0.25;

    vec4 nearestColor = sampleNearest(source, uv, pixelSize, du, dv, texelScreenSize);

    return mix(nearestColor, rgssColor, blendFactor);
}

void main() {
    int flags=int(u_ToolVisualsShader.x);
    vec4 color = (flags&1)!=0?texture(u_BlockTex,v_TexCoord):u_UseRGSS ? sampleRGSS(u_BlockTex, v_TexCoord, u_TexelSize) : sampleNearest(u_BlockTex, v_TexCoord, u_TexelSize);
    color *= v_Color;
    if((flags&2)!=0){
        float luma=dot(color.rgb,vec3(0.2126,0.7152,0.0722));
        color.rgb=mix(vec3(luma),color.rgb,u_ToolVisualsShader.y);
        color.rgb=(color.rgb-0.5)*u_ToolVisualsShader.z+0.5;
        color.rgb=clamp(color.rgb+vec3(0.025,0.006,-0.025)*u_ToolVisualsShader.w,0.0,1.0);
    }
 

#ifdef USE_FRAGMENT_DISCARD
    if (color.a < _material_alpha_cutoff(v_Material)) {
        discard;
    }
#endif

    fragColor = _linearFog(color, v_FragDistance, u_FogColor, u_EnvironmentFog, u_RenderFog, fadeFactor);
}
