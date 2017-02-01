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
                          const          float      shotnoise,
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
  
  
  const unsigned int noisexy1  = rnguint2(x,y);
  const unsigned int noisexy2  = rnguint1(noisexy1);
  const unsigned int noisexyt1 = rnguint3(x,y,timeindex);
  const unsigned int noisexyt2 = rnguint1(noisexyt1);
  const unsigned int noisexyt3 = rnguint1(noisexyt2);
  
  const float noisexy1f  = rngfloat(noisexy1);
  const float noisexy2f  = rngfloat(noisexy2);
  const float noisexyt1f = rngfloat(noisexyt1);
  const float noisexyt2f = rngfloat(noisexyt2);
  const float noisexyt3f = rngfloat(noisexyt3);
  
  const float fluovalue = read_imagef(imagein, intsampler, (int2){x,y}).x;
  
  const float shotnoisevalue = shotnoise*native_sqrt(fluovalue)*fast_normal(noisexyt1f);
  
  const float offsetbiasvalue = offsetbias*fast_normal(noisexy1f);
  
  const float gainbiasvalue = gainbias*fast_normal(noisexy2f);
  
  const float offsetnoisetemp  = fast_normal(noisexyt2f);
  const float offsetnoisevalue = offsetnoise*offsetnoisetemp*offsetnoisetemp*rngsign1(noisexyt1);
  
  const float gainnoisevalue   = gainnoise*fast_normal(noisexyt3f);
  
  float detectorvalue = offset + offsetbiasvalue + offsetnoisevalue + gain*(1+gainbiasvalue+gainnoisevalue)*(shotnoisevalue+fluovalue);    
  
  write_imagef (imageout, (int2){x,y}, detectorvalue);
}

