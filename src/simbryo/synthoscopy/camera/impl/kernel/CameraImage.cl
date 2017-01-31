#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable

#include [OCLlib] "noise/noise.cl"
          
                                
__kernel void upscale(     __read_only    image2d_t  imagein,
                           __write_only   image2d_t  imageout,
                           const          float        nxmin,
                           const          float        nxscale,
                           const          float        nymin,
                           const          float        nyscale
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

  const float value = read_imagef(imagein, normsampler, (float2){nxmin+nx*nxscale,nymin+ny*nyscale}).x;
  
  write_imagef (imageout, (int2){x,y}, value);
}



__kernel void camnoise(   __read_only    image2d_t  imagein,
                          __write_only   image2d_t  imageout,
                          const          int        timeindex,
                          const          float      photonnoise,
                          const          float      offset,
                          const          float      gain,
                          const          float      offsetbias,
                          const          float      gainbias,
                          const          float      offsetnoise,
                          const          float      gainnoise
                      )
{
  const sampler_t normsampler = CLK_NORMALIZED_COORDS_TRUE  | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
  const sampler_t intsampler  = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
 
  const int x = get_global_id(0); 
  const int y = get_global_id(1);
  
  const float fluovalue = read_imagef(imagein, intsampler, (int2){x,y}).x;
  
  const float photonnoisevalue = photonnoise*normal3(x, y, timeindex);
  
  const float offsetbiasvalue = offsetbias*normal2(x,y);
  
  const float gainbiasvalue = gainbias*normal2(~y,~x);
  
  const float offsetnoisetemp  = normal3(x,y,timeindex);
  const float offsetnoisevalue = offsetnoise*offsetnoisetemp*offsetnoisetemp;
  
  const float gainnoisevalue   = gainnoise*normal3((~x)^timeindex, y^timeindex, timeindex);
  
  float detectorvalue = offset + offsetbiasvalue + offsetnoisevalue + gain*(1+gainbiasvalue+gainnoisevalue)*(1+photonnoisevalue)*fluovalue;    
  
  write_imagef (imageout, (int2){x,y}, detectorvalue);
}

