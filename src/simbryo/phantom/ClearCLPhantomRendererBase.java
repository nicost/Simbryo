package simbryo.phantom;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.enums.ImageChannelDataType;
import clearcl.util.Region3;
import clearcl.viewer.ClearCLImageViewer;
import coremem.ContiguousMemoryInterface;
import simbryo.dynamics.tissue.TissueDynamicsInterface;

/**
 * Base class providing common fields and methods for all classes implementing
 * the phantom renderer interface using OpenCL rendering.
 *
 * @author royer
 */
public abstract class ClearCLPhantomRendererBase extends
                                                 PhantomRendererBase<ClearCLImage>
                                                 implements
                                                 PhantomRendererInterface<ClearCLImage>,
                                                 AutoCloseable
{

  protected ClearCLContext mContext;
  protected ClearCLImage mImage;
  protected ClearCLKernel mRenderKernel;

  private final long mLocalSizeX, mLocalSizeY, mLocalSizeZ;

  /**
   * Instantiates a Phantom renderer for a given OpenCL device, tissue dynamics,
   * and stack dimensions.
   * 
   * @param pContext
   *          OpenCL context to use.
   * @param pTissueDynamics
   *          tissue dynamics object
   * @param pStackDimensions
   *          stack dimensions
   */
  public ClearCLPhantomRendererBase(final ClearCLContext pContext,
                                    final TissueDynamicsInterface pTissueDynamics,
                                    final long... pStackDimensions)
  {
    super(pTissueDynamics, pStackDimensions);

    mContext = pContext;

    mImage =
           mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                             mStackDimensions);

    final int[] lGridDimensions = pTissueDynamics.getGridDimensions();

    mLocalSizeX = mStackDimensions[0] / lGridDimensions[0];
    mLocalSizeY = mStackDimensions[1] / lGridDimensions[1];
    mLocalSizeZ = mStackDimensions[2] / lGridDimensions[2];

    mStackDimensions[0] = mLocalSizeX * lGridDimensions[0];
    mStackDimensions[1] = mLocalSizeY * lGridDimensions[1];
    mStackDimensions[2] = mLocalSizeZ * lGridDimensions[2];

    mImage.fillZero(true, false);
  }

  @Override
  public void clear()
  {
    //mImage.fillZero(true);
    super.clear();
  }

  @Override
  public void render()
  {
    render(0, (int) (getDepth() - 1));
  }

  @Override
  public boolean renderSmart(final int pZPlaneIndex)
  {
    if (!mPlaneAlreadyDrawnTable[pZPlaneIndex])
    {
      renderInternal(pZPlaneIndex, pZPlaneIndex + 1);
    }

    return super.renderSmart(pZPlaneIndex);
  }

  @Override
  public void render(int pZPlaneIndexBegin, int pZPlaneIndexEnd)
  {
    // First we snap the rendering z bounds to the grid cell boundaries:
    pZPlaneIndexBegin =
                      (int) (Math.floor(pZPlaneIndexBegin
                                        / mLocalSizeZ)
                             * mLocalSizeZ);
    pZPlaneIndexEnd = (int) (Math.ceil(pZPlaneIndexEnd / mLocalSizeZ)
                             * mLocalSizeZ);

    // Now we can render a possibly slightly larger chunck of the stack:
    renderInternal(pZPlaneIndexBegin, pZPlaneIndexEnd);
  }

  private void renderInternal(final int pZPlaneIndexBegin,
                              int pZPlaneIndexEnd)
  {
    if (pZPlaneIndexEnd == pZPlaneIndexBegin)
      pZPlaneIndexEnd += mLocalSizeZ;

    // System.out.println("pZPlaneIndexBegin=" + pZPlaneIndexBegin);
    // System.out.println("pZPlaneIndexEnd =" + pZPlaneIndexEnd);
    mRenderKernel.setGlobalOffsets(0, 0, pZPlaneIndexBegin);
    mRenderKernel.setGlobalSizes(getWidth(),
                                 getHeight(),
                                 pZPlaneIndexEnd - pZPlaneIndexBegin);
    mRenderKernel.setLocalSizes(mLocalSizeX,
                                mLocalSizeY,
                                mLocalSizeZ);
    mRenderKernel.setOptionalArgument("intensity",
                                      getSignalIntensity());
    mRenderKernel.setOptionalArgument("timeindex",
                                      (int) getTissue().getTimeStepIndex());
    mRenderKernel.run(true);
    for (int z = pZPlaneIndexBegin; z < pZPlaneIndexEnd; z++)
      mPlaneAlreadyDrawnTable[z] = true;
    mImage.notifyListenersOfChange(mContext.getDefaultQueue());
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
  public ClearCLImage getPhantomImage()
  {
    return mImage;
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

  @Override
  public void close()
  {
    mImage.close();
    mRenderKernel.close();
  }

}
