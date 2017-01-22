package simbryo.synthoscopy.interfaces;

/**
 * Light intensity interface
 *
 * @author royer
 */
public interface LightIntensityInterface
{
  
  /**
   * Returns wavelength used for calculations. Normalized units (within [0,1]) are used.
   * @return returns wavelength in normalizd coordinates.
   */
  float getLightLambda();

  /**
   * Sets wavelength in normalized coordinates.
   * @param pLambda normalized coordinates
   */
  void setLightLambda(float pLambda);
  
  /**
   * Return light intensity
   * 
   * @return light intensity
   */
  float getLightIntensity();

  /**
   * Sets the light intensity
   * 
   * @param pLightIntensity
   */
  void setLightIntensity(float pLightIntensity);


}
