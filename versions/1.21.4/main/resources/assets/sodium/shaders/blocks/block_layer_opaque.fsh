#version 330 core

#import <sodium:include/fog.glsl>

in vec4 v_Color; 
in vec2 v_TexCoord; 
in float v_FragDistance; 

in float v_MaterialMipBias;
in float v_MaterialAlphaCutoff;

uniform sampler2D u_BlockTex; 

uniform vec4 u_FogColor; 
uniform float u_FogStart; 
uniform float u_FogEnd; 

out vec4 fragColor; 

uniform vec4 u_ToolVisualsShader;
in float toolvisualsFade;

void main() {
    vec4 diffuseColor = texture(u_BlockTex, v_TexCoord, v_MaterialMipBias);

    
    diffuseColor *= v_Color;
    int flags=int(u_ToolVisualsShader.x);
    if((flags&2)!=0){float luma=dot(diffuseColor.rgb,vec3(0.2126,0.7152,0.0722));diffuseColor.rgb=mix(vec3(luma),diffuseColor.rgb,u_ToolVisualsShader.y);diffuseColor.rgb=(diffuseColor.rgb-0.5)*u_ToolVisualsShader.z+0.5;diffuseColor.rgb=clamp(diffuseColor.rgb+vec3(0.025,0.006,-0.025)*u_ToolVisualsShader.w,0.0,1.0);}


#ifdef USE_FRAGMENT_DISCARD
    if (diffuseColor.a < v_MaterialAlphaCutoff) {
        discard;
    }
#endif

    fragColor = mix(u_FogColor,_linearFog(diffuseColor, v_FragDistance, u_FogColor, u_FogStart, u_FogEnd),toolvisualsFade);
}