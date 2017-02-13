
#include [OCLlib] "noise/noise.cl"
                                    
__kernel void scatterrender(   __write_only    image3d_t  image,
                                        const  float      lowedge,
                                        const  float      highedge,
                                        const  float      noiseratio,
                                        const  float      intensity,
                                        const  int        timeindex                         
                           )
{
  const sampler_t sampler = CLK_NORMALIZED_COORDS_TRUE | CLK_ADDRESS_MIRRORED_REPEAT | CLK_FILTER_NEAREST;
  
  const uint width  = get_image_width(image);
  const uint height = get_image_height(image);
  const uint depth  = get_image_depth(image);
  const float3 dim = (float3){(float)width,(float)height,(float)depth};
  const float3 iaspectr = (float3){(float)width,(float)width,(float)width}/dim;
  
  const uint x = get_global_id(0); 
  const uint y = get_global_id(1);
  const uint z = get_global_id(2);
  
  const uint ox = get_global_offset(0); 
  const uint oy = get_global_offset(1);
  const uint oz = get_global_offset(2);
  
  const float3 voxelpos = (float3){(float)x,(float)y,(float)z};
  

  float value=0; 
  value += noiseratio*rngfloat3(x+timeindex,y+timeindex,z+timeindex);
 
  const float3 normpos = voxelpos/dim;
  const float3 centnormpos = normpos - 0.5f;
  const float3 axis = (float3){1.0f/ELLIPSOIDA, 1.0f/ELLIPSOIDB, 1.0f/ELLIPSOIDC};
  const float3 scaledcentnormpos = centnormpos*axis;
  const float distance = fast_length(scaledcentnormpos)-(ELLIPSOIDR);  
  const float insdistance = fmax(0.0f,-distance);
  value +=  smoothstep(lowedge, highedge, insdistance);

  value = clamp(intensity*value, 0.0f, 1.0f);
  
  write_imagef (image, (int4){x,y,z,0.0f}, value);
}




