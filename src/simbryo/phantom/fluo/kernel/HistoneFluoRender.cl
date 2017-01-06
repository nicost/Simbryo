#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable

#include [OCLlib] "noise/noise.cl"


#define AUTOSHARPNESS 10.0f
#define AUTOBACKGROUND 0.5f
#define AUTOYOLK 0.25f 
//0.30f

inline float renderauto(float3 dim, float3 voxelpos, sampler_t sampler, __read_only image3d_t  perlin, int timeindex )
{
  const float3 normpos = voxelpos/dim;
  const float3 centnormpos = normpos - 0.5f;
  const float3 axis = (float3){1.0f, 0.43f, 0.43f};
  const float3 scaledcentnormpos = centnormpos/axis;
  const float distance = fast_length(scaledcentnormpos)-(0.48f+2*NUCLEIRADIUS);
  
  const float insdistance = fmax(0.0f,-distance);
  
  const float insmask = native_recip(1.0f+native_exp2(100.0f*(-insdistance)))-0.5f ;

  const float npnx = rngfloat1((2654435789*1)^timeindex);
  const float npny = rngfloat1((2654435789*2)^timeindex);
  const float npnz = rngfloat1((2654435789*3)^timeindex);
  const float4 npn = (float4){npnx,npny,npnz,0.0f};

  const float4 noisepos       = (float4){5.0f*normpos.x+7.0f*normpos.y-3.0f*normpos.z*npnx,
                                         5.0f*normpos.y-7.0f*normpos.z+3.0f*normpos.x*npny,
                                         5.0f*normpos.z+7.0f*normpos.x-3.0f*normpos.y*npnz, 0.0f};
  const float noiseval        = read_imagef(perlin, sampler, noisepos).x;

  const float autoyolk1       = native_recip(1.0f+native_exp2(100.0f*(0.1f-insdistance)));
  const float autoyolk2       = native_recip(1.0f+fabs(pown(25.0f*(insdistance-0.12f),3)));
  const float autoyolk        = autoyolk1+autoyolk2;
  
  const float autofluo = insmask * (AUTOBACKGROUND*(1.0f+0.3f*noiseval) + AUTOYOLK*autoyolk);

  return autofluo;
}





#define INOISEDIM 1.0f/NOISEDIM
                                    
__kernel void hisrender(   __write_only    image3d_t  image,
                           __global const  int*       neighboors,
                           __global const  float*     positions, //
                           __global const  float*     radii,
                                    const  float      intensity,
                                    const  int        timeindex,
                           __read_only     image3d_t  perlin                            
                          )
{
  const sampler_t sampler = CLK_NORMALIZED_COORDS_TRUE | CLK_ADDRESS_MIRRORED_REPEAT | CLK_FILTER_NEAREST;
 
  __local int first;
  __local int localneighboors[MAXNEI];
  __local float localpositions[3*MAXNEI];
  
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
  
  const uint ngx = get_num_groups(0);
  const uint ngy = get_num_groups(1);
  const uint ngz = get_num_groups(2);
  
  const uint lsx = get_local_size(0);
  const uint lsy = get_local_size(1);
  const uint lsz = get_local_size(2);
  
  const uint gx = get_group_id(0) + ox/lsx;
  const uint gy = get_group_id(1) + oy/lsy;
  const uint gz = get_group_id(2) + oz/lsz;

  const uint lx = get_local_id(0);
  const uint ly = get_local_id(1);
  const uint lz = get_local_id(2);
  
  const uint gi = gx+gy*ngx+gz*ngx*ngy;
  const uint li = lx+ly*lsx+lz*lsx*lsy;
 
  if(li<MAXNEI)
  {
    const int nei = neighboors[gi*MAXNEI+li];
    localneighboors[li] = nei;
    
    if(nei!=-1)
    {
      const float3 partpos = vload3(nei,positions)*dim;
      vstore3(partpos,li,localpositions);
    }
    
  }
  
  barrier(CLK_LOCAL_MEM_FENCE);
 
  float value=0; 
 
  value += renderauto(dim, voxelpos, sampler, perlin, timeindex);
  value += NOISERATIO*rngfloat3(x+timeindex,y+timeindex,z+timeindex);    
  
  if(localneighboors[0]==-1)
  {
    write_imagef (image, (int4){x,y,z,0.0f}, intensity*value);
    return;
  }
  
  const float nucleiradiusvoxels = NUCLEIRADIUS*width ;
  
  for(int k=0; k<MAXNEI; k++)
  {
    const uint nei = localneighboors[k]; 
    if(nei!=-1)
    {
      const float3 partpos     = vload3(k,localpositions);
      const float3 relvoxpos   = voxelpos-partpos;
      const float3 relvoxposac = relvoxpos*iaspectr;
      
      const float npnx = rngfloat1((2654435789*1)^nei);
      const float npny = rngfloat1((2654435789*2)^nei);
      const float npnz = rngfloat1((2654435789*3)^nei);
      const float4 npn = (float4){npnx,npny,npnz,0.0f};
      
      const float4 normrelvoxposac  = (float4){(relvoxposac*INOISEDIM).xyz,0.0f};
      const float4 noisepos         =   0.5f+normrelvoxposac+npn;
      const float  levelnoise       =   2.0f*read_imagef(perlin, sampler, noisepos).x-1.0f;
      
      const float d      = fast_length(relvoxposac); 
      const float noisyd = d + NUCLEIROUGHNESS*levelnoise;
      
      const float  level      =  native_recip(1.0f+native_exp2(NUCLEISHARPNESS*(noisyd-nucleiradiusvoxels)));
      const float  noisylevel =  (1.0f+NUCLEITEXTURECONTRAST*levelnoise)*level;
       
      value += noisylevel;
    }
  }
    
  write_imagef (image, (int4){x,y,z,0.0f}, intensity*value);

}

