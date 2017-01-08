package simbryo.phantom.fluo.impl.drosophila;

import java.io.IOException;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.ClearCLProgram;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.KernelAccessType;
import clearcl.enums.MemAllocMode;
import clearcl.ops.Noise;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;
import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.TissueDynamicsInterface;
import simbryo.dynamics.tissue.embryo.HasSurface;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.phantom.ClearCLPhantomRendererBase;
import simbryo.phantom.PhantomRendererInterface;
import simbryo.phantom.fluo.HistoneFluorescence;
import simbryo.textures.noise.FractalNoise;
import simbryo.textures.noise.SimplexNoise;
import simbryo.util.timing.Timming;

/**
 * This renders Drosophila histone fluorescence (nuclei + yolk).
 *
 * @author royer
 */
public class DrosophilaHistoneFluorescence extends HistoneFluorescence
{

  private Drosophila mDrosophila;
  
  
  public DrosophilaHistoneFluorescence(ClearCLDevice pDevice,
                                       Drosophila pDrosophila,
                                       long... pStackDimensions) throws IOException
  {
    super(pDevice, pDrosophila, pStackDimensions);
  }


  @Override
  public void addAutoFluoFunctionSourceCode(ClearCLProgram pClearCLProgram) throws IOException
  {
    pClearCLProgram.addSource(DrosophilaHistoneFluorescence.class, "kernel/AutoFluo.cl"); 
    
    Drosophila lDrosophila = (Drosophila)getTissue();
    pClearCLProgram.addDefine("ELLIPSOIDA", lDrosophila.mEllipsoidA);
    pClearCLProgram.addDefine("ELLIPSOIDB", lDrosophila.mEllipsoidB);
    pClearCLProgram.addDefine("ELLIPSOIDC", lDrosophila.mEllipsoidC);
    pClearCLProgram.addDefine("ELLIPSOIDR", lDrosophila.mEllipsoidR);
  }


 
}
