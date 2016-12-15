#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable

// A kernel to fill an image with the beautiful XOR fractal:
//default xorfractal dx=0i
//default xorfractal dy=0i
//default xorfractal u =1f
__kernel void xorfractal  (__write_only image3d_t image, 
                                        int       dx, 
                                        int       dy, 
                                        float     u 
                          )
{
	int x = get_global_id(0); 
	int y = get_global_id(1);
	int z = get_global_id(2);
	
	if(x==0 && y==0)
   printf("location: (%d,%d,%d) \n", x, y, z);
	
	write_imagef (image, (int4)(x, y, z, 0), u*((x+dx)^((y+dy)+1)^(z+2))); 
}




__kernel void gaussrender( __write_only    image3d_t image,
                           __global const  int*      neighboors,
                           __global const  float*    positions, //
                           __global const  float*    radii,
                                           int       num              
                          )
{
  __local int localneighboors[MAXNEI];
  __local float localpositions[3*MAXNEI];
  __local float localradii[MAXNEI];
  
  const uint width  = get_image_width(image);
  const uint height = get_image_height(image);
  const uint depth  = get_image_depth(image);
  
  const uint x = get_global_id(0); 
  const uint y = get_global_id(1);
  const uint z = get_global_id(2);
  
  const uint ngx = get_num_groups(0);
  const uint ngy = get_num_groups(1);
  const uint ngz = get_num_groups(2);
  
  const uint gx = get_group_id(0);
  const uint gy = get_group_id(1);
  const uint gz = get_group_id(2);

  const uint lsx = get_local_size(0);
  const uint lsy = get_local_size(1);
  const uint lsz = get_local_size(2);

  const uint lx = get_local_id(0);
  const uint ly = get_local_id(1);
  const uint lz = get_local_id(2);
  
  const float3 dim = (float3){width,height,depth};
  const float3 voxelpos = (float3){x,y,z};
  
  const uint gi = gx+gy*ngx+gz*ngx*ngy;
  const uint li = lx+ly*lsx+lz*lsx*lsy;
  
  
  if(li<MAXNEI)
  {
    const int nei = neighboors[gi*MAXNEI+li];
    localneighboors[li] = nei;
    
    if(nei!=-1)
    {
      const float3 partpos = vload3(nei,positions);
      vstore3(partpos,li,localpositions);
      localradii[li] = radii[nei];
    }
    
  }
  
  /*
  if(x==256 && y==256 && z==64)
  {
    //printf("temp=%d,  \n", temp );
    printf("ngx=%d, ngy=%d, ngz=%d,  \n", ngx, ngy, ngz );
    printf("gx=%d, gy=%d, gz=%d \n", gx, gy, gz );
    printf("lsx=%d, lsy=%d, lsz=%d,  \n", lsx, lsy, lsz );
    printf("lx=%d, ly=%d, lz=%d \n", lx, ly, lz );
    printf("gi=%d, li=%d, MAXNEI=%d  \n", gi, li, MAXNEI);
  }/**/
  
  barrier(CLK_LOCAL_MEM_FENCE);
 
   
  float value=0; 
  
  for(int k=0; k<MAXNEI; k++)
  {
    int i = localneighboors[k];
    
    /*if(x==256 && y==256 && z==64)
    {
      printf("k=%d, i=%d,  \n", k, i );
    }/**/
    
    if(i!=-1)
    {
      //const float3 partpos = vload3(i,positions)*dim;
      const float3 partpos = vload3(k,localpositions)*dim;
      const float d = fast_distance(voxelpos,partpos);
      const float radius = radii[i];
      const float sigma = 0.33*radius*width ;
      
      value += 1000*native_exp(-(d*d)/(2*sigma*sigma));
    }
  }
  /**/

  
  write_imagef (image, (int4){x,y,z,0}, value);
}

