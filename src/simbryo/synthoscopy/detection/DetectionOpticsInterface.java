package simbryo.synthoscopy.detection;

import clearcl.ClearCLImage;

/**
 * Detection optics interface
 *
 * @param <I>
 *          type of image used for storing and manipulating detection-side
 *          images
 * @author royer
 */
public interface DetectionOpticsInterface<I> 
{
  /**
   * Returns the internal representation of the detection image. The type
   * depends on the actual implementation.
   * 
   * @return phantom image
   */
  I getDetectionImage();

  /**
   * Renders the light map for a given scattering phantom image and position and dimension along z of the lightmap. 
   * 
   * @param pFluorescencePhantomImage fluorescence phantom
   * @param pLightMapImage lightmap image
   * @param pZPosition z offset in normalized coordinates of the center plane of the lightmap stack relative to the phantom.
   * @param pZDepth z depth in normalized coordinates of the lightmap stack relative to the phantom.
   * @return light map image (same as returned by getLightMapImage() )
   */
  I render(ClearCLImage pFluorescencePhantomImage,
           ClearCLImage pLightMapImage,
           float pZPosition,
           float pZDepth);

  /**
   * Clears the detection image
   */
  void clear();

  /**
   * Detection image width.
   * 
   * @return light map image width
   */
  long getWidth();

  /**
   * Detection image height.
   * 
   * @return light map image height
   */
  long getHeight();


}
