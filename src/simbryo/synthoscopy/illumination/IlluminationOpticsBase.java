package simbryo.synthoscopy.illumination;

import simbryo.synthoscopy.OpticsBase;

/**
 * Illumination optics base class providing common fields and methods for
 * implementations of the illumination optics interface
 *
 * @param <I> type of image used to store and process illumination-side images
 * @author royer
 */
public abstract class IlluminationOpticsBase<I> extends OpticsBase<I>
                                            implements
                                            IlluminationOpticsInterface<I>
{

  private float mLightIntensity;

  @Override
  public float getLightIntensity()
  {
    return mLightIntensity;
  }

  @Override
  public void setLightIntensity(float pLightIntensity)
  {
    mLightIntensity = pLightIntensity;
  }

}
