#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable


// Wang Hash based RNG
//  Has at least 20 separate cycles, shortest cycle is < 7500 long.  
//  But it yields random looking 2D noise when fed OpenCL work item IDs, 
//  and that short cycle should only be hit for one work item in about 500K.
unsigned int parallelRNG( unsigned int x )
{
  unsigned int value = x;

  value = (value ^ 61) ^ (value>>16);
  value *= 9;
  value ^= value << 4;
  value *= 0x27d4eb2d;
  value ^= value >> 15;

  return value;
}



__kernel void gaussrender( __write_only    image3d_t image,
                           __global const  int*      neighboors,
                           __global const  float*    positions, //
                           __global const  float*    radii,
                                           float     intensity,
                                           int       timeindex            
                          )
{
  __local int localneighboors[MAXNEI];
  __local float localpositions[3*MAXNEI];
  __local float localradii[3*MAXNEI];
  
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
  
  if(x==256 && y==256 && z==64)
  {
    printf("gx=%d, gy=%d, gz=%d \n", gx, gy, gz);
    printf("lx=%d, ly=%d, lz=%d \n", lx, ly, lz);
  }
  
  if(li<MAXNEI)
  {
    const int nei = neighboors[gi*MAXNEI+li];
    localneighboors[li] = nei;
    
    if(nei!=-1)
    {
      const float3 partpos = vload3(nei,positions)*dim;
      vstore3(partpos,li,localpositions);
      const float radius = radii[nei];
      localradii[li] = radius; 
    }
    
  }
  
  barrier(CLK_LOCAL_MEM_FENCE);
 
  float value=0; 
  
  if(localneighboors[0]!=-1)
    for(int k=0; k<MAXNEI; k++)
      if(localneighboors[k]!=-1)
      {
        const float3 partpos = vload3(k,localpositions);
        const float d = fast_distance(voxelpos,partpos);
        const float radius = localradii[k];
        const float sigma = 0.33f*radius*width ;
        
        value += 1000.0f*native_exp(-(d*d)/(2.0f*sigma*sigma));
      }
      
  value += 0.00000001f*parallelRNG(x+17*z+997*timeindex); //y+293*    
  value += 0.0000000001f*parallelRNG(y+997*timeindex);
       
  write_imagef (image, (int4){x,y,z,0}, intensity*value);

}

