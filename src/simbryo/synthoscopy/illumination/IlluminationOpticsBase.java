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


  /**
   * Instanciates illumination optics base class given wavelength of light,
   * light intensity, and light map image dimensions.
   * 
   * @param pWavelengthInNormUnits light's wavelength
   * @param pLightIntensity light's intensity
   * @param pLightMapDimensions
   *          light map image dimensions
   */
  public IlluminationOpticsBase(float pWavelengthInNormUnits,
                                float pLightIntensity,
                                long... pLightMapDimensions)
  {
    super(pWavelengthInNormUnits, pLightIntensity, pLightMapDimensions);
  }

}
