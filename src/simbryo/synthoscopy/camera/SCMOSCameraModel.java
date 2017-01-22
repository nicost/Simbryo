package simbryo.synthoscopy.camera;

import clearcl.ClearCLImage;
import simbryo.phantom.PhantomRendererInterface;

/**
 * Generic camera model for sCMOS cameras
 *
 * @author royer
 */
public class SCMOSCameraModel extends ClearCLCameraModelBase implements CameraModelInterface<ClearCLImage>
{

  public SCMOSCameraModel()
  {
    super();
  }

}
