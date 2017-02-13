package simbryo.phantom.scatter.impl.drosophila;

import java.io.IOException;

import clearcl.ClearCLContext;
import clearcl.ClearCLProgram;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.phantom.scatter.ScatteringPhantom;

/**
 * This renders Drosophila scattering phantom.
 *
 * @author royer
 */
public class DrosophilaScatteringPhantom extends ScatteringPhantom
{

  private float mLowEdge, mHighEdge;

  /**
   * Instanciates a Drosophila scattering phantom renderer.
   * 
   * @param pContext
   *          ClearCL context
   * @param pDrosophila
   *          drosophila embryo dynamics
   * @param pDrosophilaHistoneFluorescence
   *          drosophila histone florescence
   * @param pStackDimensions
   *          stack dimensions
   * @throws IOException
   *           thrown if OpenCL kernels cannot be read.
   */
  public DrosophilaScatteringPhantom(ClearCLContext pContext,
                                     Drosophila pDrosophila,
                                     DrosophilaHistoneFluorescence pDrosophilaHistoneFluorescence,
                                     long... pStackDimensions) throws IOException
  {
    super(pContext, pDrosophila, pStackDimensions);
    setNoiseOverSignalRatio(0.1f);
    setSignalIntensity(0.9f);

    float lNucleiRadius = pDrosophilaHistoneFluorescence.getNucleiRadius();
    
    mLowEdge = 2*lNucleiRadius;
    mHighEdge = 20*lNucleiRadius;

    setupProgramAndKernel();
  }

  protected void setupProgramAndKernel() throws IOException
  {
    ClearCLProgram lProgram = mContext.createProgram();

    lProgram.addSource(DrosophilaScatteringPhantom.class,
                       "kernel/Scattering.cl");

    Drosophila lDrosophila = (Drosophila) getTissue();
    lProgram.addDefine("ELLIPSOIDA", lDrosophila.getEllipsoidA());
    lProgram.addDefine("ELLIPSOIDB", lDrosophila.getEllipsoidB());
    lProgram.addDefine("ELLIPSOIDC", lDrosophila.getEllipsoidC());
    lProgram.addDefine("ELLIPSOIDR", lDrosophila.getEllipsoidR());

    

    lProgram.buildAndLog();

    mRenderKernel = lProgram.createKernel("scatterrender");

    mRenderKernel.setArgument("lowedge", mLowEdge);
    mRenderKernel.setArgument("highedge", mHighEdge);
    mRenderKernel.setArgument("noiseratio", getNoiseOverSignalRatio());

    mRenderKernel.setArgument("image", mImage);
  }

}
