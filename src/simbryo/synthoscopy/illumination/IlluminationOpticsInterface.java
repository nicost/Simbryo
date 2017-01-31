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

  /**
   * Renders the light map for a given scattering phantom image and position and dimension along z of the lightmap. 
   * 
   * @param pScatteringPhantomImage scattering phantom
   * @return light map image (same as returned by getLightMapImage() )
   */
  I render(ClearCLImage pScatteringPhantomImage);

  /**
   * Clear the light map image
   */
  void clear();

  /**
   * Light map image width.
   * 
   * @return light map image width
   */
  long getWidth();

  /**
   * Light map image height.
   * 
   * @return light map image height
   */
  long getHeight();

  /**
   * Light map image depth.
   * 
   * @return light map image depth
   */
  long getDepth();

}
