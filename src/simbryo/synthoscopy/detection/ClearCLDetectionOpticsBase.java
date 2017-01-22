package simbryo.synthoscopy.detection;

import clearcl.ClearCLImage;
import simbryo.phantom.PhantomRendererInterface;

/**
 *  Detection optics base class for detection optics computation based on CLearCL
 *
 * @author royer
 */
public abstract class ClearCLDetectionOpticsBase extends
                                             DetectionOpticsBase<ClearCLImage>
                                             implements
                                             DetectionOpticsInterface<ClearCLImage>
{

  public ClearCLDetectionOpticsBase()
  {
    super();
  }

}
