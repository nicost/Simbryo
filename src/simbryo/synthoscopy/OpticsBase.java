package simbryo.synthoscopy;

/**
 * Optics base class providing common firlds and methods for optics related
 * classes
 *
 * @param <I>
 *          image type used to store and process images
 * @author royer
 */
public class OpticsBase<I>
{

  private float mWavelengthInNormUnits, mLightIntensity;
  private final long[] mImageDimensions;

  /**
   * Instanciates a optics base class with basic optics related fields.
   * 
   * @param pWavelengthInNormUnits
   *          light wavelength in normalized (within [0,1]) units.
   * @param pLightIntensity
   *          light intensity
   * @param pImageDimensions image dimensions
   */
  public OpticsBase(float pWavelengthInNormUnits, float pLightIntensity, long... pImageDimensions)
  {
    mWavelengthInNormUnits = pWavelengthInNormUnits;
    mLightIntensity = pLightIntensity;
    mImageDimensions = pImageDimensions;
  }

  /**
   * Returns light wavelength
   * 
   * @return wavelength in normalized units (within [0,1])
   */
  public float getLightWavelength()
  {
    return mWavelengthInNormUnits;
  }

  /**
   * Sets light wavelength in normalized units.
   * 
   * @param pWavelengthInNormUnits wavelength in normalized units
   */
  public void setLightLambda(float pWavelengthInNormUnits)
  {
    mWavelengthInNormUnits = pWavelengthInNormUnits;
  }

  /**
   * Returns light intensity
   * @return light intensity
   */
  public float getLightIntensity()
  {
    return mLightIntensity;
  }

  /**
   * Sets light intensity
   * @param pLightIntensity light intensity
   */
  public void setLightIntensity(float pLightIntensity)
  {
    mLightIntensity = pLightIntensity;
  }
  

  /**
   * Returns image width
   * @return width
   */
  public long getWidth()
  {
    return mImageDimensions[0];
  }


  /**
   * Returns image height
   * @return height
   */
  public long getHeight()
  {
    return mImageDimensions[1];
  }


  /**
   * Returns image depth
   * @return depth
   */
  public long getDepth()
  {
    return mImageDimensions[2];
  }

}
