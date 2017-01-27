package simbryo.synthoscopy.detection;

import simbryo.synthoscopy.OpticsBase;

/**
 * Detection optics base class providing commonfields and methods nescessary for
 * all implementions of the detection optics interface
 *
 * @param <I>
 *          type of images used to store and process detection-side images
 * @author royer
 */
public abstract class DetectionOpticsBase<I> extends OpticsBase<I>
                                         implements
                                         DetectionOpticsInterface<I>
{

  /**
   * Instanciates illumination optics base class given the light's wavelength,
   * intensity, and detection image dimensions.
   * 
   * @param pWavelengthInNormUnits light's wavelength in normalized units (within [0,1])
   * @param pLightIntensity light intensity
   * @param pDetectionImageDimensions
   *          detection image dimensions
   */
  public DetectionOpticsBase(float pWavelengthInNormUnits,
                             float pLightIntensity,
                             long... pDetectionImageDimensions)
  {
    super(pWavelengthInNormUnits,
          pLightIntensity,
          pDetectionImageDimensions);
  }

}
