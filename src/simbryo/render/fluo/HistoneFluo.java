package simbryo.render.fluo;

import java.io.IOException;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLDevice;
import clearcl.ClearCLProgram;
import clearcl.enums.HostAccessType;
import clearcl.enums.KernelAccessType;
import clearcl.util.ElapsedTime;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;
import simbryo.embryo.Embryo;
import simbryo.render.ClearCLRendererBase;
import simbryo.render.RendererInterface;

public class HistoneFluo extends ClearCLRendererBase
                         implements RendererInterface
{
  private ClearCLBuffer mPositionsBuffer;
  private ClearCLBuffer mRadiiBuffer;
  private OffHeapMemory mPositionsMemory;
  private OffHeapMemory mRadiiMemory;

  public HistoneFluo(ClearCLDevice pDevice,
                     Embryo pEmbryo,
                     long... pStackDimensions) throws IOException
  {
    super(pDevice, pEmbryo, pStackDimensions);

    
    ClearCLProgram lProgram =
                            mContext.createProgram(this.getClass(),
                                                   "kernel/HistoneFluoRender.cl");
    lProgram.buildAndLog();

    mRenderKernel = lProgram.createKernel("gaussrender");

    final int lDimension = mEmbryo.getDimension();

    mPositionsBuffer =
                     mContext.createBuffer(HostAccessType.WriteOnly,
                                           KernelAccessType.ReadOnly,
                                           NativeTypeEnum.Float,
                                           lDimension * pEmbryo.getMaxNumberOfParticles());

    mRadiiBuffer =
                 mContext.createBuffer(HostAccessType.WriteOnly,
                                       KernelAccessType.ReadOnly,
                                       NativeTypeEnum.Float,
                                       pEmbryo.getMaxNumberOfParticles());

    mPositionsMemory =
                     OffHeapMemory.allocateFloats(lDimension
                                                  * pEmbryo.getMaxNumberOfParticles());
    mRadiiMemory =
                 OffHeapMemory.allocateFloats(pEmbryo.getMaxNumberOfParticles());

    mRenderKernel.setArgument("image", mImage);
    mRenderKernel.setArgument("positions", mPositionsBuffer);
    mRenderKernel.setArgument("radii", mRadiiBuffer);

  }
  
  private void updateBuffers()
  {
    final int lDimension = mEmbryo.getDimension();
    final int lNumberOfCells = mEmbryo.getMaxNumberOfParticles();
    mPositionsMemory.copyFrom(mEmbryo.getPositions()
                                     .getCurrentArray(),
                              0,
                              0,
                              lDimension * lNumberOfCells);
    mRadiiMemory.copyFrom(mEmbryo.getRadii().getCurrentArray(),
                          0,
                          0,
                          lNumberOfCells);

    mPositionsBuffer.readFrom(mPositionsMemory,
                              0,
                              lDimension * lNumberOfCells,
                              false);
    mRadiiBuffer.readFrom(mRadiiMemory, 0, lNumberOfCells, true);
  }

  @Override
  public void clear()
  {
    super.clear();
    updateBuffers();
  }

  @Override
  public boolean render(int pZPlaneIndex)
  {
    mRenderKernel.setArgument("num", mEmbryo.getNumberOfParticles());
    return super.render(pZPlaneIndex);
  }

  @Override
  public void render(int pZPlaneIndexBegin, int pZPlaneIndexEnd)
  {
    mRenderKernel.setArgument("num", mEmbryo.getNumberOfParticles());
    super.render(pZPlaneIndexBegin, pZPlaneIndexEnd);
  }



}
