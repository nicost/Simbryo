#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable


#include [OCLlib] "noise/noise.cl"

                                    
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
 

  __local int localneighboors[MAXNEI];
  __local float localpositions[3*MAXNEI];
  
  const uint width  = get_image_width(image);
  const uint height = get_image_height(image);
  const uint depth  = get_image_depth(image);
  const float3 dim = (float3){width,height,depth};
  
  const uint x = get_global_id(0); 
  const uint y = get_global_id(1);
  const uint z = get_global_id(2);
  
  const uint ox = get_global_offset(0); 
  const uint oy = get_global_offset(1);
  const uint oz = get_global_offset(2);
  
  const float3 voxelpos = (float3){x,y,z};
  
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
  
  value += NOISERATIO*rngfloat3(x+timeindex,y+timeindex,z+timeindex);    
  
  if(localneighboors[0]!=-1)
  {
    const float invnoisedim = 1.0f/NOISEDIM;
    const float nucleiradiusvoxels = NUCLEIRADIUS*width ;
    
    for(int k=0; k<MAXNEI; k++)
    {
      const uint nei = localneighboors[k]; 
      if(nei!=-1)
      {
        const float3 partpos = vload3(k,localpositions);
        const float3 relvoxpos  = voxelpos-partpos;
        
        const int npnx = (int) rnguint1((2654435789*1)^nei) & (NOISEDIM-1);
        const int npny = (int) rnguint1((2654435789*2)^nei) & (NOISEDIM-1);
        const int npnz = (int) rnguint1((2654435789*3)^nei) & (NOISEDIM-1);
        
        const float4 noisepos   =   ((float4){relvoxpos+(float3){npnx, npny, npnz},0}-NOISEDIM/2)*invnoisedim;
        const float  levelnoise =   read_imagef(perlin, sampler, noisepos).x;
        
        const float d = fast_length(relvoxpos); 
        const float noisyd = d + NUCLEIROUGHNESS*(2*levelnoise-1);
        
        const float  level      =  1/(1+native_exp(NUCLEISHARPNESS*(noisyd-nucleiradiusvoxels)));
        const float  noisylevel =  levelnoise*level;
         
        value += noisylevel;
      }
    }
  }
       
  write_imagef (image, (int4){x,y,z,0}, intensity*value);

}

