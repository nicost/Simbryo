package simbryo.synthoscopy.camera;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import simbryo.synthoscopy.ClearCLOpticsBase;

/**
 * Camera model base class for camera models computation based on CLearCL
 *
 * @author royer
 */
public class ClearCLCameraRendererBase extends ClearCLOpticsBase
                                    implements
                                    CameraRendererInterface<ClearCLImage>
{

  /**
   * Instanciates a ClearCL powered detection optics base class given a ClearCL
   * context, and idefield image dimensions (2D).
   * 
   * @param pContext
   *          ClearCL context
   * @param pWavelengthInNormUnits
   *          light's wavelength
   * @param pLightIntensity
   *          light's intesnity
   * @param pMaxCameraImageDimensions
   *          max camera image dimensions
   */
  public ClearCLCameraRendererBase(final ClearCLContext pContext,
                                float pWavelengthInNormUnits,
                                float pLightIntensity,
                                long... pMaxCameraImageDimensions)
  {
    super(pContext,
          pWavelengthInNormUnits,
          pLightIntensity,
          pMaxCameraImageDimensions);

    mContext = pContext;

    mImage =
           mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                             pMaxCameraImageDimensions[0],
                                             pMaxCameraImageDimensions[1]);

    mImage.fillZero(true, false);
  }

  @Override
  public ClearCLImage render(ClearCLImage pDetectionImage)
  {
    // not doing anything here, derived classes must actually cmpute something
    // into mLightMapImage
    mImage.notifyListenersOfChange(mContext.getDefaultQueue());
    return mImage;
  }

}
