package simbryo.phantom.scatter;

import java.io.IOException;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.TissueDynamicsInterface;
import simbryo.phantom.ClearCLPhantomRendererBase;
import simbryo.phantom.PhantomRendererInterface;

/**
 * This renders a scattering phantom.
 *
 * @author royer
 */
public abstract class ScatteringPhantom extends
                                        ClearCLPhantomRendererBase
                                        implements
                                        PhantomRendererInterface<ClearCLImage>
{

  /**
   * Instantiates a scattering phantom renderer for a given OpenCL context,
   * tissue dynamics, and stack dimensions.
   * 
   * @param pContext
   *          OpenCL context
   * @param pTissueDynamics
   *          tissue dynamics
   * @param pStackDimensions
   *          stack dimensions
   * @throws IOException
   *           thrown in case kernel code cannot be read.
   */
  public ScatteringPhantom(ClearCLContext pContext,
                           TissueDynamics pTissueDynamics,
                           long... pStackDimensions) throws IOException
  {
    this(pContext,
         pTissueDynamics,
         1e-2f,
         pStackDimensions);
  }

  /**
   * Instantiates a scattering phantom renderer for a given OpenCL context,
   * tissue dynamics, and stack dimensions.
   * 
   * @param pContext
   *          OpenCL context
   * @param pTissueDynamics
   *          tissue dynamics
   * @param pNoiseOverSignalRatio
   *          noise over signal ratio
   * @param pStackDimensions
   *          stack dimensions
   * @throws IOException
   *           thrown in OpenCL kernels cannot be read.
   */
  public ScatteringPhantom(ClearCLContext pContext,
                           TissueDynamicsInterface pTissueDynamics,
                           float pNoiseOverSignalRatio,
                           long... pStackDimensions) throws IOException
  {
    super(pContext, pTissueDynamics, pStackDimensions);

    setNoiseOverSignalRatio(pNoiseOverSignalRatio);
  }

 
  @Override
  public void close()
  {
    super.close();
  }

}
