
float rngfloat1(uint x);



#define AUTOSHARPNESS 10.0f
#define AUTOBACKGROUND 0.5f
#define AUTOYOLK 0.25f 
//0.30f

inline float autofluo(float3 dim, float3 voxelpos, sampler_t sampler, __read_only image3d_t  perlin, int timeindex )
{
  const float3 normpos = voxelpos/dim;
  const float3 centnormpos = normpos - 0.5f;
  const float3 axis = (float3){ELLIPSOIDA, ELLIPSOIDB, ELLIPSOIDC};
  const float3 scaledcentnormpos = centnormpos/axis;
  const float distance = fast_length(scaledcentnormpos)-(ELLIPSOIDR+2*NUCLEIRADIUS);
  
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



