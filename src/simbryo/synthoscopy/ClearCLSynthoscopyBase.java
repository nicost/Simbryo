package simbryo.synthoscopy;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcl.util.Region3;
import clearcl.viewer.ClearCLImageViewer;
import coremem.ContiguousMemoryInterface;

/**
 * Ilumination optics base class for illumination optics computation based on
 * CLearCL
 *
 * @author royer
 */
public abstract class ClearCLSynthoscopyBase extends
                                             SynthoscopyBase<ClearCLImage>
                                             implements AutoCloseable
{

  protected ClearCLContext mContext;
  protected ClearCLImage mImage;

  /**
   * Instanciates a ClearCL powered illumination optics base class given the
   * wavelength of light, the light intensity, ClearCL context, and the light
   * map image dimensions.
   * 
   * @param pContext
   *          ClearCL context
   * @param pImageDimensions
   *          image dimensions
   */
  public ClearCLSynthoscopyBase(final ClearCLContext pContext,
                                long... pImageDimensions)
  {
    super(pImageDimensions);

    mContext = pContext;

    mImage =
           mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                             pImageDimensions);

    mImage.fillZero(true, false);
  }

  @Override
  public void clear(boolean pWaitToFinish)
  {
    mImage.fillZero(pWaitToFinish, false);
    super.clear(pWaitToFinish);
  }

  @Override
  public void render(boolean pWaitToFinish)
  {
    mImage.notifyListenersOfChange(mContext.getDefaultQueue());
    super.render(pWaitToFinish);
  }

  @Override
  public ClearCLImage getImage()
  {
    return mImage;
  }

  @Override
  public void copyTo(final ContiguousMemoryInterface pMemory,
                     final boolean pBlocking)
  {
    mImage.writeTo(pMemory,
                   Region3.originZero(),
                   Region3.region(getWidth(),
                                  getHeight(),
                                  getDepth()),
                   pBlocking);
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
                                        ClearCLImageViewer.view(mImage, this.getClass().getSimpleName());
    return lViewImage;
  }

}
