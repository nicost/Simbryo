package simbryo.synthoscopy.optics.illumination;

import clearcl.ClearCLImage;
import simbryo.synthoscopy.SynthoscopyInterface;
import simbryo.synthoscopy.interfaces.LightIntensityInterface;
import simbryo.synthoscopy.interfaces.LightWavelengthInterface;

/**
 * Illumination optics interface
 *
 * @param <I>
 *          type of images used to store and process illumination-side images
 * @author royer
 */
public interface IlluminationOpticsInterface<I> extends
                                            SynthoscopyInterface<I>,
                                            LightIntensityInterface,
                                            LightWavelengthInterface
{

  /**
   * Returns this lightmap image.
   * 
   * @return input lightmap image
   */
  ClearCLImage getInputImage();

  /**
   * Sets the input lightmap image. If Null is provided then the lightmap is
   * initialized to zero. If an input lightmap is provided, the values in this
   * provided input lightmap will be used and any additional light will be
   * added.
   * 
   * @param pInputImage
   *          input lightmap image
   */
  void setInputImage(ClearCLImage pInputImage);

}
