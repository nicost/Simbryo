#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable

                                
__kernel void collectpair( __read_only    image3d_t  fluophantom,
                           __read_only    image3d_t  lightmap,
                           __read_only    image2d_t  imagein,
                           __write_only   image2d_t  imageout,
                           const          float      fpz1,
                           const          float      fpz2,
                           const          float      lmz1,
                           const          float      lmz2
                       )
{
  const sampler_t normsampler = CLK_NORMALIZED_COORDS_TRUE  | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
  const sampler_t intsampler  = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
 
  const int x = get_global_id(0); 
  const int y = get_global_id(1);
  
  const int width   = get_global_size(0); 
  const int height  = get_global_size(1);
  
  const float nx = ((float)x+0.5f)/width;
  const float ny = ((float)y+0.5f)/height;

  
  const float oldvalue = read_imagef(imagein, intsampler, (int2){x,y}).x;
  
  const float fluovalue1   = read_imagef(fluophantom, normsampler, (float4){nx,ny,fpz1,0.0f}).x;
  const float fluovalue2   = read_imagef(fluophantom, normsampler, (float4){nx,ny,fpz2,0.0f}).x;
  
  const float lightvalue1  = read_imagef(lightmap,    normsampler, (float4){nx,ny,lmz1,0.0f}).x;
  const float lightvalue2  = read_imagef(lightmap,    normsampler, (float4){nx,ny,lmz2,0.0f}).x;
  
  const float newvalue     = oldvalue+fluovalue1*lightvalue1+fluovalue2*lightvalue2;
  
  write_imagef (imageout, (int2){x,y}, newvalue);
}

__kernel void collectsingle(   __read_only    image3d_t  fluophantom,
                               __read_only    image3d_t  lightmap,
                               __read_only    image2d_t  imagein,
                               __write_only   image2d_t  imageout,
                                 const        float      fpz,
                                 const        float      lmz
                             )
{
  const sampler_t normsampler = CLK_NORMALIZED_COORDS_TRUE  | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
  const sampler_t intsampler  = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
 
  const int x = get_global_id(0); 
  const int y = get_global_id(1);
  
  const int width = get_global_size(0); 
  const int height  = get_global_size(1);
  
  const float nx = ((float)x+0.5f)/width;
  const float ny = ((float)y+0.5f)/height;

  
  const float oldvalue    = read_imagef(imagein, intsampler, (int2){x,y}).x;
  
  const float fluovalue   = read_imagef(fluophantom, normsampler,  (float4){nx,ny,fpz,0.0f}).x;
  
  const float lightvalue  = read_imagef(lightmap,    normsampler,  (float4){nx,ny,lmz,0.0f}).x;
  
  const float newvalue    = oldvalue+fluovalue*lightvalue;
  
  write_imagef (imageout, (int2){x,y}, newvalue);
}


                                
__kernel void defocusblur( __read_only    image2d_t  imagein,
                           __write_only   image2d_t  imageout,
                           const          float      sigma
                         )
{
  const sampler_t normsampler = CLK_NORMALIZED_COORDS_TRUE  | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
  const sampler_t intsampler  = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
 
  const int x = get_global_id(0); 
  const int y = get_global_id(1);
  
  const int width = get_global_size(0); 
  const int height  = get_global_size(1);
  
  const float nx = ((float)x+0.5f)/width;
  const float ny = ((float)y+0.5f)/height;
  
  const float dx = 1.0f/width;
  const float dy = 1.0f/height;

  
  const float a =       read_imagef(imagein, normsampler, (float2){nx-dx, ny-dy}).x;
  const float b =       read_imagef(imagein, normsampler, (float2){nx   , ny-dy}).x;
  const float c =       read_imagef(imagein, normsampler, (float2){nx+dx, ny-dy}).x;
  
  const float d =       read_imagef(imagein, normsampler, (float2){nx-dx, ny   }).x;
  const float e =       read_imagef(imagein, normsampler, (float2){nx   , ny   }).x;
  const float f =       read_imagef(imagein, normsampler, (float2){nx+dx, ny   }).x;
  
  const float g =       read_imagef(imagein, normsampler, (float2){nx-dx, ny+dy}).x;
  const float h =       read_imagef(imagein, normsampler, (float2){nx   , ny+dy}).x;
  const float i =       read_imagef(imagein, normsampler, (float2){nx+dx, ny+dy}).x;
  
  const float4 sum4 = (float4){a,b,c,d}+(float4){f,g,h,i} ;
  const float  sum = sigma*((sum4.x+sum4.y)+(sum4.z+sum4.w)+e) + (1-sigma)*e;
  
  const float value = sum*(1.0f/(9*sigma+(1-sigma)));
  
  write_imagef (imageout, (int2){x,y}, value);
}

__kernel void scatterblur( __read_only    image3d_t  scatterphantom,
                           __read_only    image2d_t  imagein,
                           __write_only   image2d_t  imageout,
                           const          float      nz,
                           const          float      sigmamin,
                           const          float      sigmamax
                         )
{
  const sampler_t normsampler = CLK_NORMALIZED_COORDS_TRUE  | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
  const sampler_t intsampler  = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
 
  const int x = get_global_id(0); 
  const int y = get_global_id(1);
  
  const int width   = get_global_size(0); 
  const int height  = get_global_size(1);
  
  const float nx = ((float)x+0.5f)/width;
  const float ny = ((float)y+0.5f)/height;
  
  const float dx = 1.0f/width;
  const float dy = 1.0f/height;


  const float scattervalue = read_imagef(scatterphantom, normsampler, (float4){nx, ny, nz, 0.0f}).x;

  const float sigma = sigmamin + (sigmamax-sigmamin)*scattervalue;

  const float a =       read_imagef(imagein, normsampler, (float2){nx-dx, ny-dy}).x;
  const float b =       read_imagef(imagein, normsampler, (float2){nx   , ny-dy}).x;
  const float c =       read_imagef(imagein, normsampler, (float2){nx+dx, ny-dy}).x;
  
  const float d =       read_imagef(imagein, normsampler, (float2){nx-dx, ny   }).x;
  const float e =       read_imagef(imagein, normsampler, (float2){nx   , ny   }).x;
  const float f =       read_imagef(imagein, normsampler, (float2){nx+dx, ny   }).x;
  
  const float g =       read_imagef(imagein, normsampler, (float2){nx-dx, ny+dy}).x;
  const float h =       read_imagef(imagein, normsampler, (float2){nx   , ny+dy}).x;
  const float i =       read_imagef(imagein, normsampler, (float2){nx+dx, ny+dy}).x;
  
  const float4 sum4 = (float4){a,b,c,d}+(float4){f,g,h,i} ;
  const float  sum  = sigma*((sum4.x+sum4.y)+(sum4.z+sum4.w)+e) + (1-sigma)*e;
  
  const float value = sum*(1.0f/(9*sigma+(1-sigma)));

  write_imagef (imageout, (int2){x,y}, value);
  
}

