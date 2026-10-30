#version 330 core

#import <sodium:include/fog.glsl>
#import <sodium:include/chunk_vertex.glsl>
#import <sodium:include/chunk_matrices.glsl>

out vec4 v_Color;
out vec2 v_TexCoord;

flat out uint v_Material;

#ifdef USE_FOG
out vec2 v_FragDistance;
out float fadeFactor;
#endif

uniform vec3 u_RegionOffset;
uniform vec2 u_TexCoordShrink;

uniform sampler2D u_LightTex; 

uniform vec4 u_ToolVisualsChunks;
uniform int u_CurrentTime;
uniform float u_FadePeriodInv;

layout(std140) uniform ChunkData {
    ivec4 u_chunkFades[64]; 
};

uvec3 _get_relative_chunk_coord(uint pos) {
    
    return uvec3(pos) >> uvec3(5u, 0u, 2u) & uvec3(7u, 3u, 7u);
}

vec3 _get_draw_translation(uint pos) {
    return _get_relative_chunk_coord(pos) * vec3(16.0);
}

void main() {
    _vert_init();

    
    vec3 translation = u_RegionOffset + _get_draw_translation(_draw_id);
    vec3 position = _vert_position + translation;

    int chunkId=int(_draw_id);
    int chunkFade=u_chunkFades[chunkId>>2][chunkId&3];
    float fade=chunkFade<0?1.0:clamp(float(u_CurrentTime-chunkFade)*u_FadePeriodInv,0.0,1.0);
    float progress=u_ToolVisualsChunks.y>0.0?fade*fade*(3.0-2.0*fade):fade;
    if(u_ToolVisualsChunks.z>0.5)position.y+=u_ToolVisualsChunks.x*(1.0-progress);
#ifdef USE_FOG
    v_FragDistance=getFragDistance(position);
    fadeFactor=u_ToolVisualsChunks.z>0.5?(abs(u_ToolVisualsChunks.y)>1.5?progress:1.0):fade;
#endif

    
    gl_Position = u_ProjectionMatrix * u_ModelViewMatrix * vec4(position, 1.0);

    
    v_Color = _vert_color * texture(u_LightTex, _vert_tex_light_coord);
    v_TexCoord = (_vert_tex_diffuse_coord_bias * u_TexCoordShrink) + _vert_tex_diffuse_coord; 

    v_Material = _material_params;
}
