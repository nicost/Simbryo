package simbryo.synthoscopy.camera;

import clearcl.ClearCLImage;
import simbryo.phantom.PhantomRendererInterface;

/**
 * Camera model base class for camera models computation based on CLearCL
 *
 * @author royer
 */
public class ClearCLCameraModelBase extends
                                    CameraModelBase<ClearCLImage>
                                    implements
                                    CameraModelInterface<ClearCLImage>
{

  public ClearCLCameraModelBase()
  {
    super();
  }

}
