#version 330 core

#import <sodium:include/fog.glsl>
#import <sodium:include/chunk_vertex.glsl>
#import <sodium:include/chunk_matrices.glsl>
#import <sodium:include/chunk_material.glsl>

out vec4 v_Color;
out vec2 v_TexCoord;

out float v_MaterialMipBias;
#ifdef USE_FRAGMENT_DISCARD
out float v_MaterialAlphaCutoff;
#endif

#ifdef USE_FOG
out float v_FragDistance;
#endif

uniform int u_FogShape;
uniform vec3 u_RegionOffset;
uniform vec2 u_TexCoordShrink;

uniform sampler2D u_LightTex; 

uvec3 _get_relative_chunk_coord(uint pos) {
    
    return uvec3(pos) >> uvec3(5u, 0u, 2u) & uvec3(7u, 3u, 7u);
}

vec3 _get_draw_translation(uint pos) {
    return _get_relative_chunk_coord(pos) * vec3(16.0);
}

uniform vec4 u_ToolVisualsChunks;
uniform vec4 u_ToolVisualsTiming;
uniform float u_ToolVisualsStarts[256];
out float toolvisualsFade;

void main() {
    _vert_init();

    
    vec3 translation = u_RegionOffset + _get_draw_translation(_draw_id);
    vec3 position = _vert_position + translation;
    float started=u_ToolVisualsStarts[int(_draw_id)];
    float age=mod(u_ToolVisualsTiming.x-started+4194304.0,4194304.0);
    float fade=started<0.0?1.0:clamp(age*u_ToolVisualsTiming.y,0.0,1.0);
    float progress=u_ToolVisualsChunks.y>0.0?fade*fade*(3.0-2.0*fade):fade;
    if(u_ToolVisualsChunks.z>0.5)position.y+=u_ToolVisualsChunks.x*(1.0-progress);
    toolvisualsFade=u_ToolVisualsChunks.z>0.5&&abs(u_ToolVisualsChunks.y)>1.5?progress:1.0;

#ifdef USE_FOG
    v_FragDistance = getFragDistance(u_FogShape, position);
#endif

    
    gl_Position = u_ProjectionMatrix * u_ModelViewMatrix * vec4(position, 1.0);

    
    v_Color = _vert_color * texture(u_LightTex, _vert_tex_light_coord);
    v_TexCoord = (_vert_tex_diffuse_coord_bias * u_TexCoordShrink) + _vert_tex_diffuse_coord; 

    v_MaterialMipBias = _material_mip_bias(_material_params);
#ifdef USE_FRAGMENT_DISCARD
    v_MaterialAlphaCutoff = _material_alpha_cutoff(_material_params);
#endif
}
