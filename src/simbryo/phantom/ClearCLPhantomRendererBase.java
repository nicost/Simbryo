package simbryo.phantom;

import java.io.IOException;

import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.viewer.ClearCLImageViewer;
import simbryo.dynamics.tissue.TissueDynamics;

public abstract class ClearCLPhantomRendererBase extends PhantomRendererBase
                                          implements PhantomRendererInterface
{

  protected ClearCLContext mContext;

  protected ClearCLImage mImage;

  protected ClearCLKernel mRenderKernel;

  public ClearCLPhantomRendererBase(ClearCLDevice pDevice,
                             TissueDynamics pEmbryo,
                             long... pStackDimensions) throws IOException
  {
    super(pEmbryo, pStackDimensions);
    mStackDimensions = pStackDimensions;

    mContext = pDevice.createContext();

    mImage = mContext.createImage(ImageChannelOrder.R,
                                  ImageChannelDataType.Float,
                                  mStackDimensions);
    mImage.fillZero(true);
  }

  @Override
  public void clear()
  {
    // mImage.fillZero(true);
    super.clear();
  }

  @Override
  public boolean render(int pZPlaneIndex)
  {
    if (!mPlaneAlreadyDrawnTable[pZPlaneIndex])
    {
      mRenderKernel.setGlobalOffsets(0, 0, pZPlaneIndex);
      mRenderKernel.setGlobalSizes(getWidth(), getHeight(), 1);
      mRenderKernel.run(true);
      mImage.notifyListenersOfChange(mContext.getDefaultQueue());
    }

    return super.render(pZPlaneIndex);
  }

  @Override
  public void render(int pZPlaneIndexBegin, int pZPlaneIndexEnd)
  {
    mRenderKernel.setGlobalOffsets(0, 0, pZPlaneIndexBegin);
    //mRenderKernel.setLocalSizes(...);
    mRenderKernel.setGlobalSizes(getWidth(),
                                 getHeight(),
                                 pZPlaneIndexEnd - pZPlaneIndexBegin);
    mRenderKernel.run(true);
    mImage.notifyListenersOfChange(mContext.getDefaultQueue());
  }

  public ClearCLImageViewer openViewer()
  {
    ClearCLImageViewer lViewImage = ClearCLImageViewer.view(mImage);
    return lViewImage;
  }

}
