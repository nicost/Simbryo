package simbryo.render.fluo;

import java.io.IOException;

import clearcl.ClearCLDevice;
import clearcl.ClearCLProgram;
import simbryo.embryo.Embryo;
import simbryo.render.ClearCLRendererBase;
import simbryo.render.RendererInterface;

public class HistoneFluo extends ClearCLRendererBase
                         implements RendererInterface
{
   public HistoneFluo(ClearCLDevice pDevice,
                     Embryo pEmbryo,
                     long... pStackDimensions) throws IOException
  {
    super(pDevice, pEmbryo, pStackDimensions);

    ClearCLProgram lProgram =
                            mContext.createProgram(this.getClass(),
                                                   "kernel/HistoneFluoRender.cl");
    lProgram.buildAndLog();

    mRenderKernel = lProgram.createKernel("xorfractal");
    mRenderKernel.setArgument("image", mImage);

  }

  @Override
  public boolean render(int pZPLaneIndex)
  {
    mRenderKernel.setOptionalArgument("dx", mEmbryo.getTimeStepIndex());
    return super.render(pZPLaneIndex);
  }

}
