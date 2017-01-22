package simbryo.synthoscopy.illumination;

import clearcl.ClearCLImage;
import simbryo.synthoscopy.interfaces.LightIntensityInterface;

/**
 * Illumination optics interface
 *
 * @param <I>
 *          type of images used to store and process illumination-side images
 * @author royer
 */
public interface IlluminationOpticsInterface<I> extends
                                            LightIntensityInterface
{

  /**
   * Returns the internal representation of the illumination image. The type
   * depends on the actual implementation.
   * 
   * @return phantom image
   */
  I getLightMapImage();

  long getWidth();

  long getHeight();

  long getDepth();

  I render(ClearCLImage pScatteringPhantomImage,
           int pZCenterPlaneIndex);

  void clear();



}
