package simbryo.synthoscopy.detection;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcl.viewer.ClearCLImageViewer;

/**
 * Detection optics base class for detection optics computation based on CLearCL
 *
 * @author royer
 */
public abstract class ClearCLDetectionOpticsBase extends
                                                 DetectionOpticsBase<ClearCLImage>
                                                 implements
                                                 DetectionOpticsInterface<ClearCLImage>,
                                                 AutoCloseable
{

  protected ClearCLContext mContext;
  protected ClearCLImage mImage;

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
   * @param pWideFieldImageDimensions
   *          widefield map image dimensions
   */
  public ClearCLDetectionOpticsBase(final ClearCLContext pContext,
                                    float pWavelengthInNormUnits,
                                    float pLightIntensity,
                                    long... pWideFieldImageDimensions)
  {
    super(pWavelengthInNormUnits,
          pLightIntensity,
          pWideFieldImageDimensions);

    mContext = pContext;

    mImage =
           mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                             getWidth(),
                                             getHeight());

    mImage.fillZero(true);
  }

  @Override
  public ClearCLImage getDetectionImage()
  {
    return mImage;
  }

  @Override
  public ClearCLImage render(ClearCLImage pFluorescencePhantomImage,
                             ClearCLImage pLightMapImage,
                             float pZPosition,
                             float pZDepth)
  {
    // not doing anything here, derived classes must actually cmpute something
    // into mLightMapImage
    mImage.notifyListenersOfChange(mContext.getDefaultQueue());
    return mImage;
  }

  @Override
  public void clear()
  {
    mImage.fillZero(true);
  }

  @Override
  public void close()
  {
    mImage.close();
  }

  /**
   * Opens viewer for the internal image
   * 
   * @return viewer
   */
  public ClearCLImageViewer openViewer()
  {
    final ClearCLImageViewer lViewImage =
                                        ClearCLImageViewer.view(mImage);
    return lViewImage;
  }

}
