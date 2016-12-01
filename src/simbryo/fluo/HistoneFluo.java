package simbryo.fluo;

import java.io.IOException;

import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.ocllib.OCLlib;
import clearcl.viewer.ClearCLImageViewer;
import clearcl.viewer.RenderMode;
import simbryo.embryo.zoo.Drosophila;

public class HistoneFluo
{

  private long[] mStackDimensions;

  private ClearCLContext mContext;

  private ClearCLImage mFluorescenceImage;

  private ClearCLKernel mRenderKernel;

  private int mDeltaX;

  public HistoneFluo(ClearCLDevice pDevice,
                     long... pStackDimensions) throws IOException
  {
    mStackDimensions = pStackDimensions;

    mContext = pDevice.createContext();

    mFluorescenceImage = mContext.createImage(ImageChannelOrder.R,
                                              ImageChannelDataType.Float,
                                              mStackDimensions);

    ClearCLProgram lProgram =
                            mContext.createProgram(this.getClass(),
                                                   "kernel/HistoneFluoRender.cl");
    lProgram.buildAndLog();

    mRenderKernel = lProgram.createKernel("xorfractal");
    mRenderKernel.setArgument("image", mFluorescenceImage);
    mRenderKernel.setGlobalSizes(mFluorescenceImage);
    mRenderKernel.run(true);
    mRenderKernel.setOptionalArgument("dx", 0);
    mRenderKernel.setOptionalArgument("dy", 0);
    mRenderKernel.setOptionalArgument("u", 1f);
     
  }

  public long getWidth()
  {
    return mStackDimensions[0];
  }

  public long getHeight()
  {
    return mStackDimensions[1];
  }

  public long getDepth()
  {
    return mStackDimensions[2];
  }

  public void render(Drosophila pDrosophila, int pZPLaneIndex)
  {
    mRenderKernel.setOptionalArgument("dx", mDeltaX++);
    mRenderKernel.run(true);
    mFluorescenceImage.notifyListenersOfChange(mContext.getDefaultQueue());
  }

  public ClearCLImageViewer openFluorescenceImageViewer()
  {
    ClearCLImageViewer lViewImage =
                                  ClearCLImageViewer.view(mFluorescenceImage);
    return lViewImage;
  }

}
