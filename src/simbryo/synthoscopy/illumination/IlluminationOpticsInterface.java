package simbryo.synthoscopy.illumination;

import simbryo.synthoscopy.interfaces.HasPhantom;
import simbryo.synthoscopy.interfaces.LightIntensityInterface;

/**
 * Illumination optics interface
 *
 * @param <I> type of images used to store and process illumination-side images
 * @author royer
 */
public interface IlluminationOpticsInterface<I> extends HasPhantom<I>, LightIntensityInterface
{

  /**
   * Returns the internal representation of the illumination image. The type depends on the actual implementation.
   * @return phantom image
   */
  I getIlluminationImage();

}
