#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable

#include [OCLlib] "noise/noise.cl"

               
#define LAMBDA 0.09f   
#define KS 1  
#define SIGMAMIN 0.05f
#define SIGMAMOD  (1-SIGMAMIN)               
   

__kernel void initialize(
                           __write_only   image2d_t  ba,
                           __write_only   image2d_t  bb,
                           __write_only   image2d_t  sa,
                           __write_only   image2d_t  sb
                       )   
{
  const int y = get_global_id(0); 
  const int z = get_global_id(1);
  
  write_imagef (ba, (int2){y,z}, 1.0f);
  write_imagef (bb, (int2){y,z}, 1.0f);
  write_imagef (sa, (int2){y,z}, 0.0f);
  write_imagef (sb, (int2){y,z}, 0.0f);
}

__kernel void diffuseY(
                           __read_only    image2d_t  si,
                           __write_only   image2d_t  so,
                           const          int        radius 
                       )   
{
  const sampler_t intsampler  = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
  const int y = get_global_id(0); 
  const int z = get_global_id(1);
  
  float value = 0;
  for(int iy=y-radius; iy<=y+radius; iy++)
    value += read_imagef(si, intsampler, (int2){iy,z}).x;

  value *= 1.0f/(2*radius+1);

  write_imagef (so, (int2){y,z}, value);
}

__kernel void diffuseZ(
                           __read_only    image2d_t  si,
                           __write_only   image2d_t  so,
                           const          int        radius 
                       )   
{
  const sampler_t intsampler  = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
  const int y = get_global_id(0); 
  const int z = get_global_id(1);
  
  float value = 0;
  for(int iz=z-radius; iz<=z+radius; iz++)
    value += read_imagef(si, intsampler, (int2){y,iz}).x;

  value *= 1.0f/(2*radius+1);

  write_imagef (so, (int2){y,z}, value);
}
   
   
inline float lightsheetfun( const float lambda,
                            const float intensity,
                            const float theta,
                            const float height,  
                            const float3 lspvox, 
                            const float3 lsovox, 
                            const float3 posvox)
{
  const float3 relpos = posvox-lspvox;
  const float d = fabs(relpos.z);
  const float x =  relpos.x;
  
  const float wo = lambda/(((float)M_PI)*theta);
  const float xr = lambda/(((float)M_PI)*theta*theta);
  const float nx = x/xr;
  const float wx = wo*native_sqrt(1.0f+nx*nx); 
  
  const float a = wo/wx;
  const float b = d/wx;
  
  const float value = intensity*a*a*native_exp(-2*b*b);
  
  return value;
}   
   
                                
__kernel void propagate(   __read_only    image3d_t  scattermap,
                           __write_only   image3d_t  lightmap,
                           __read_only    image2d_t  binput,
                           __write_only   image2d_t  boutput,
                           __read_only    image2d_t  sinput,
                           __write_only   image2d_t  soutput,
                           const          int        x,
                           const          int        zoffset,
                           const          float      lambda,
                           const          float      intensity,
                           const          float      theta,
                           const          float      height,
                           const          float      lspx,
                           const          float      lspy,
                           const          float      lspz,
                           const          float      lsox,
                           const          float      lsoy,
                           const          float      lsoz
                       )
{
  const sampler_t normsampler = CLK_NORMALIZED_COORDS_TRUE | CLK_ADDRESS_MIRRORED_REPEAT | CLK_FILTER_NEAREST;
  const sampler_t intsampler  = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
 
  const int smwidth  = get_image_width(scattermap);
  const int smheight = get_image_height(scattermap);
  const int smdepth  = get_image_depth(scattermap);
  const float3 dim = (float3){(float)smwidth,(float)smheight,(float)smdepth};
  const float3 iaspectr = (float3){(float)smwidth,(float)smwidth,(float)smwidth}/dim;
  
  const float3 lsp    = (float3){lspx, lspy, lspz};
  const float3 lspvox = lsp*dim; //note: aspect ratio?
  const float3 lso    = (float3){lsox, lsoy, lsoz};
  const float3 lsovox = lso*dim; //note: aspect ratio?
  
  const int y = get_global_id(0); 
  const int z = get_global_id(1);
  
  const int oy = get_global_offset(0); 
  const int oz = get_global_offset(1);
   
  const float3 posvox = (float3){(float)x,(float)y,(float)(zoffset+z)};
  
  // formula for ballistic light distribution of lightsheet:
  const float ballistic0 =  lightsheetfun(lambda, intensity, theta, height, lspvox, lsovox, posvox);
  
  // [READ IMAGE] proportion of ballistic light that made it through in previous planes:
  const float oldballisticratio =  read_imagef(binput, intsampler, (int2){y,z}).x;
 
  // scattering map value at voxel: 
  const float scattermapvalue = read_imagef(scattermap, intsampler, (int4){x,y,zoffset+z,0.0f}).x;
  
  // local loss from ballistic light to scattered light: (1 -> no loss, 0 -> max loss)
  const float loss = native_exp2(-LAMBDA*scattermapvalue);
  
  // updated ballistic ratio:
  const float ballisticratio = oldballisticratio*loss;
  
  // [WRITE IMAGE] update ballistic light ratio map:
  write_imagef (boutput, (int2){y,z}, ballisticratio);
  
  // amount of ballistic light at current voxel:
  const float ballistic = ballistic0 * ballisticratio;
  
  // amount of ballistic light transfered to scattering at current voxel:
  const float transferredlight = (1-loss)*oldballisticratio*ballistic0;
  
  // scattering sigma has a min and a part modulated by density of scattering media:
  const float sigma = SIGMAMIN + (1-loss)*SIGMAMOD;
  
  // [READ IMAGE] collect light from previous plane that diffused to current voxel:
  // Note: turns out its slower to do separable-diffusion, because of the image cache - 
  // its better to touch image memory once instead of in multiple passes, might not be
  // true for bigger kernels. Strange.
  float previouslyscattered = 0.0f;
  for(  int iz=z-KS; iz<=z+KS; iz++)
    for(int iy=y-KS; iy<=y+KS; iy++)
      previouslyscattered += read_imagef(sinput, intsampler, (int2){iy,iz}).x;
  previouslyscattered *= sigma; // weight /**/
  
  // [READ IMAGE] collect scattered light from previous plane without diffusion:
  previouslyscattered +=  (1-sigma)*read_imagef(sinput, intsampler, (int2){y,z}).x;
  
  // normalize to have conservation from one plane to the next:
  previouslyscattered *= 1.0f/((2*KS+1)*(2*KS+1)*sigma+(1-sigma));
  
  // we add the light that was locally transfered from ballistic to scattered:
  const float scattered = previouslyscattered + transferredlight;
  
  // [WRITE IMAGE] update diffused light map:
  write_imagef (soutput, (int2){y,z}, scattered);

  // Light at a given point in space is the sum of the scattered and ballistic light:
  const float light = ballistic + scattered; // ballistic + scattered;

  /*
  if(y<300)
    write_imagef (lightmap, (int4){x,y,z,0.0f}, ballistic);
  else
    write_imagef (lightmap, (int4){x,y,z,0.0f}, sigma);
  /**/


  // [WRITE IMAGE] Write lightmap value:
  write_imagef (lightmap, (int4){x,y,z,0.0f}, light);

}

