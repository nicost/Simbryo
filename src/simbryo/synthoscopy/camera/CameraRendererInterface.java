package simbryo.synthoscopy.camera;

import clearcl.ClearCLImage;
import simbryo.synthoscopy.interfaces.ImageWidthHeightInterface;

/**
 * Camera model interface
 *
 * @param <I> type of images to store and manipulate camera images
 * @author royer
 */
public interface CameraRendererInterface<I> extends ImageWidthHeightInterface
{

  /**
   * Renders the detection image on the camera
   * @param pDetectionImage
   * @return rendered image
   */
  ClearCLImage render(ClearCLImage pDetectionImage);

}
