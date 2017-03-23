package simbryo.synthoscopy.camera;

import clearcl.ClearCLBuffer;
import simbryo.synthoscopy.SynthoscopyInterface;
import simbryo.synthoscopy.interfaces.ImageWidthHeightInterface;

/**
 * Camera model interface
 *
 * @param <I>
 *          type of images to store and manipulate camera images
 * @author royer
 */
public interface CameraRendererInterface<I> extends
                                        SynthoscopyInterface<I>,
                                        ImageWidthHeightInterface
{

  /**
   * Returns the camera image but as a ClearCL buffer of 16bit unsigned
   * integers.
   * 
   * @return camera image buffer (16bit unsigned ints)
   */
  ClearCLBuffer getCameraImageBuffer();

}
