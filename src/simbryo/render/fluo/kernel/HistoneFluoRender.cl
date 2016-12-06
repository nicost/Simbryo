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
                           __global const  float*    positions, //
                           __global const  float*    radii,
                                           int       num
                          )
{
  const int width  = get_image_width(image);
  const int height = get_image_height(image);
  const int depth  = get_image_depth(image);
  
  const int x = get_global_id(0); 
  const int y = get_global_id(1);
  const int z = get_global_id(2);
  
  const float3 dim = (float3){width,height,depth};
  const float3 voxelpos = (float3){x,y,z};
  
  float value=0;
  
  for(int i=0; i<num; i++)
  {
    const float3 partpos = vload3(i,positions)*dim;
    const float d = fast_distance(voxelpos,partpos);
    const float radius = radii[i];
    const float sigma = 0.33*radius*width ;
    
    value += 1000*native_exp(-(d*d)/(2*sigma*sigma));
  }
  
//  if(x==0 && y==0 && z==0)
//    printf("end: (%d,%d,%d) ->  n=%d,  v=%f \n", x, y, z, num, value);
  
  write_imagef (image, (int4){x,y,z,0}, value);
}

