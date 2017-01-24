package simbryo.synthoscopy.illumination;

import simbryo.synthoscopy.OpticsBase;

/**
 * Illumination optics base class providing common fields and methods for
 * implementations of the illumination optics interface
 *
 * @param <I>
 *          type of image used to store and process illumination-side images
 * @author royer
 */
public abstract class IlluminationOpticsBase<I> extends OpticsBase<I>
                                            implements
                                            IlluminationOpticsInterface<I>
{

  private float mLambdaInNormUnits, mLightIntensity;
  private final long[] mLightMapDimensions;

  /**
   * Instanciates illumination optics base class given light map image dimensions.
   * 
   * @param pLightMapDimensions
   *          light map image dimensions
   */
  public IlluminationOpticsBase(long... pLightMapDimensions)
  {
    super();
    mLightMapDimensions = pLightMapDimensions;
    mLambdaInNormUnits = 0.0006f;
    mLightIntensity = 1.0f;
  }

  @Override
  public float getLightLambda()
  {
    return mLambdaInNormUnits;
  }

  @Override
  public void setLightLambda(float pLambda)
  {
    mLambdaInNormUnits = pLambda;
  }

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

  @Override
  public long getWidth()
  {
    return mLightMapDimensions[0];
  }

  @Override
  public long getHeight()
  {
    return mLightMapDimensions[1];
  }

  @Override
  public long getDepth()
  {
    return mLightMapDimensions[2];
  }

}
