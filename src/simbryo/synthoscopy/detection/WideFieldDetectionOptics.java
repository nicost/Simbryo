package simbryo.synthoscopy.detection;

import clearcl.ClearCLImage;
import simbryo.phantom.PhantomRendererInterface;

/**
 * Wide-field detetction optics
 *
 * @author royer
 */
public class WideFieldDetectionOptics extends ClearCLDetectionOpticsBase
                                      implements
                                      DetectionOpticsInterface<ClearCLImage>
{

  public WideFieldDetectionOptics()
  {
    super();
  }

}
