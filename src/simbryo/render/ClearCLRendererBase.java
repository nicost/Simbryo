package simbryo.render;

import java.io.IOException;

import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.viewer.ClearCLImageViewer;
import simbryo.embryo.Embryo;

public abstract class ClearCLRendererBase extends RendererBase
                                          implements RendererInterface
{

  protected ClearCLContext mContext;

  protected ClearCLImage mImage;

  protected ClearCLKernel mRenderKernel;

  public ClearCLRendererBase(ClearCLDevice pDevice,
                             Embryo pEmbryo,
                             long... pStackDimensions) throws IOException
  {
    super(pEmbryo, pStackDimensions);
    mStackDimensions = pStackDimensions;

    mContext = pDevice.createContext();

    mImage = mContext.createImage(ImageChannelOrder.R,
                                  ImageChannelDataType.Float,
                                  mStackDimensions);
  }

  @Override
  public void clear()
  {
    mImage.fillZero(true);
  }

  @Override
  public boolean render(int pZPLaneIndex)
  {
    if (!mCacheTable[pZPLaneIndex])
    {
      mRenderKernel.setGlobalOffsets(0,0,pZPLaneIndex);
      mRenderKernel.setGlobalSizes(getWidth(),getHeight(),1);
      mRenderKernel.run(true);
      mImage.notifyListenersOfChange(mContext.getDefaultQueue());
    }
    
    return super.render(pZPLaneIndex);
  }

  @Override
  public ClearCLImageViewer openFluorescenceImageViewer()
  {
    ClearCLImageViewer lViewImage = ClearCLImageViewer.view(mImage);
    return lViewImage;
  }

}
