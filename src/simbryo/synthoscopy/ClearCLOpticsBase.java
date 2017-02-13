package simbryo.synthoscopy;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcl.viewer.ClearCLImageViewer;

/**
 * Ilumination optics base class for illumination optics computation based on
 * CLearCL
 *
 * @author royer
 */
public abstract class ClearCLOpticsBase extends
                                        OpticsBase<ClearCLImage>
                                        implements AutoCloseable
{

  /**
   * Instanciates a ClearCL powered illumination optics base class given the
   * wavelength of light, the light intensity, ClearCL context, and the light
   * map image dimensions.
   * 
   * @param pContext
   *          ClearCL context
   * @param pWavelengthInNormUnits
   *          light's wavelength
   * @param pLightIntensity
   *          light's intensity
   * @param pImageDimensions
   *          image dimensions
   */
  public ClearCLOpticsBase(final ClearCLContext pContext,
                           float pWavelengthInNormUnits,
                           float pLightIntensity,
                           long... pImageDimensions)
  {
    super(pWavelengthInNormUnits, pLightIntensity, pImageDimensions);

    mContext = pContext;

    mImage =
           mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                             pImageDimensions);

    mImage.fillZero(true, false);
  }

  protected ClearCLContext mContext;
  protected ClearCLImage mImage;

  /**
   * Returns image
   * @return image
   */
  public ClearCLImage getImage()
  {
    return mImage;
  }

  /**
   * Clears image
   */
  public void clear()
  {
    mImage.fillZero(true, false);
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
