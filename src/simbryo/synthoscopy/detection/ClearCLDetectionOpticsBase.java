package simbryo.synthoscopy.detection;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import simbryo.synthoscopy.ClearCLOpticsBase;

/**
 * Detection optics base class for detection optics computation based on CLearCL
 *
 * @author royer
 */
public abstract class ClearCLDetectionOpticsBase extends
                                                 ClearCLOpticsBase
                                                 implements
                                                 DetectionOpticsInterface<ClearCLImage>,
                                                 AutoCloseable
{

  /**
   * Instanciates a ClearCL powered detection optics base class given a ClearCL
   * context, and widefield image dimensions (2D).
   * 
   * @param pContext
   *          ClearCL context
   * @param pWavelengthInNormUnits
   *          light's wavelength
   * @param pLightIntensity
   *          light's intesnity
   * @param pWideFieldImageDimensions
   *          widefield map image dimensions
   */
  public ClearCLDetectionOpticsBase(final ClearCLContext pContext,
                                    float pWavelengthInNormUnits,
                                    float pLightIntensity,
                                    long... pWideFieldImageDimensions)
  {
    super(pContext,
          pWavelengthInNormUnits,
          pLightIntensity,
          pWideFieldImageDimensions);

    mContext = pContext;

    mImage =
           mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                             getWidth(),
                                             getHeight());

    mImage.fillZero(true, false);
  }

  @Override
  public ClearCLImage getDetectionImage()
  {
    return mImage;
  }

  @Override
  public ClearCLImage render(ClearCLImage pFluorescencePhantomImage,
                             ClearCLImage pScatteringPhantomImage,
                             ClearCLImage pLightMapImage)
  {
    // not doing anything here, derived classes must actually cmpute something
    // into mLightMapImage
    mImage.notifyListenersOfChange(mContext.getDefaultQueue());
    return mImage;
  }

}
