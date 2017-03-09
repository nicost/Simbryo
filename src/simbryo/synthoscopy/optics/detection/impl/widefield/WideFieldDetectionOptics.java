package simbryo.synthoscopy.optics.detection.impl.widefield;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.IOException;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import simbryo.synthoscopy.optics.detection.DetectionOpticsBase;
import simbryo.synthoscopy.optics.detection.DetectionOpticsInterface;

/**
 * Wide-field detetction optics
 *
 * @author royer
 */
public class WideFieldDetectionOptics extends DetectionOpticsBase
                                      implements
                                      DetectionOpticsInterface<ClearCLImage>
{

  protected ClearCLImage mImageTemp;

  protected ClearCLKernel mCollectPairKernel, mCollectSingleKernel,
      mDefocusBlurKernel, mScatterBlurKernel;

  private volatile float mDefocusSigma, mSmoothDefocusTransitionPoint,
      mZFocusPosition, mZFocusDepth, mScatterSigmaMin,
      mScatterSigmaMax, mScatterSamplingDeltaZ;


  /**
   * Instanciates a light sheet illumination optics class given a ClearCL
   * context, and detection image dimensions
   * 
   * @param pContext
   *          OpenCL context
   * @param pImageDimensions
   *          detection image dimensions in voxels
   * @throws IOException
   *           thrown if kernels cannot be read
   */
  public WideFieldDetectionOptics(ClearCLContext pContext,
                                  long... pImageDimensions) throws IOException
  {
    super(pContext, pImageDimensions);

    setSigma(1.0f);
    setSmoothDefocusTransitionPoint(4.0f / 512);
    setScatterSigmaMin(0.0001f);
    setScatterSigmaMax(0.04f);
    setScatterSamplingDeltaZ(0.01f);

    setupProgramAndKernels();
    mImageTemp = pContext.createImage(mImage);
    clearImages(true);
  }

 

  /**
   * Returns the sigma parameter value. The sigma parameter controls how much
   * off-focus contributing planes get defocused. The lower the sigma value, the
   * deeper the depth-of-focus.
   * 
   * @return sigma
   */
  public float getSigma()
  {
    return mDefocusSigma;
  }

  /**
   * Sets the sigma parameter value.
   * 
   * @param pDefocusSigma
   *          sigma
   */
  public void setSigma(float pDefocusSigma)
  {
    mDefocusSigma = pDefocusSigma;
  }

  /**
   * Returns the smooth defocus transition point. For small defocus, the image
   * quality does not decrease proprotionaly to the defocu distance, instead
   * there is a 2nd order 'smoothing' that can be expplained because of the
   * shape of the 3D PSF (another way to look at it is the raleigh length of a
   * Gaussian Beam). To accound for this effect, we reduce sigma for small
   * defocu distances
   * 
   * @return smooth defocus transition point
   */
  public float getSmoothDefocusTransitionPoint()
  {
    return mSmoothDefocusTransitionPoint;
  }

  /**
   * Sets the smooth defocus transition point.
   * 
   * @param pSmoothDefocusTransitionPoint
   *          smooth defocus transition points
   */
  public void setSmoothDefocusTransitionPoint(float pSmoothDefocusTransitionPoint)
  {
    mSmoothDefocusTransitionPoint = pSmoothDefocusTransitionPoint;
  }

  /**
   * Returns the z focus position in normalized coordinates relative to the
   * fluorescence phantom.
   * 
   * @return lightmap z center position relative to fluorescence phantom
   */
  public float getZFocusPosition()
  {
    return mZFocusPosition;
  }

  /**
   * Sets the z focus position in normalized coordinates relative to the
   * fluorescence phantom
   *
   * @param pZFocusPosition
   *          z focus position relative to fluorescence phantom (normalized
   *          coordinates within [0,1])
   */
  public void setZFocusPosition(float pZFocusPosition)
  {
    mZFocusPosition = pZFocusPosition;
  }

  /**
   * Return the z focus depth in normalized coordinates relative to the
   * fluorescence phantom .
   * 
   * @return z focus depth relative to fluorescence phantom
   */
  public float getZFocusDepth()
  {
    return mZFocusDepth;
  }

  /**
   * Sets the focus depth in normalized coordinates relative to the fluorescence
   * phantom .
   * 
   * @param pZFocusDepth
   *          lightmap depth relative to scatter phantom
   */
  public void setZFocusDepth(float pZFocusDepth)
  {
    mZFocusDepth = pZFocusDepth;
  }

  /**
   * Sets the default depth of the lightmap stack relative to the scatter
   * phantom in normalized coordinates. This default value is such that each
   * plane of the lightmap corresponds to a single plane of the scatter phantom.
   *
   * @param pFluorescencePhantomImage
   *          fluorescence phantom
   * @param pLightMapImage
   *          lightmap
   * 
   */
  public void setDefaultZDepth(ClearCLImage pFluorescencePhantomImage,
                               ClearCLImage pLightMapImage)
  {
    mZFocusDepth = (float) pLightMapImage.getDepth()
                   / pFluorescencePhantomImage.getDepth();
  }

  /**
   * Returns the min sigma for detection scattering.
   * 
   * @return min sigma
   */
  public float getScatterSigmaMin()
  {
    return mScatterSigmaMin;
  }

  /**
   * Sets the min sigma for detection scattering.
   * 
   * @param pScatterSigmaMin
   *          min sigma
   */
  public void setScatterSigmaMin(float pScatterSigmaMin)
  {
    mScatterSigmaMin = pScatterSigmaMin;
  }

  /**
   * Returns the max sigma for detection scattering.
   * 
   * @return max sigma
   */
  public float getScatterSigmaMax()
  {
    return mScatterSigmaMax;
  }

  /**
   * Sets the max sigma for detection scattering.
   * 
   * @param pScatterSigmaMax
   *          max sigma
   */
  public void setScatterSigmaMax(float pScatterSigmaMax)
  {
    mScatterSigmaMax = pScatterSigmaMax;
  }

  /**
   * Returns scatter Z sampling delta. this is the step size in normalized
   * coordinates by which to sample scattering on the way out to the detector.
   * 
   * @return scatter sampling delta z
   */
  public float getScatterSamplingDeltaZ()
  {
    return mScatterSamplingDeltaZ;
  }

  /**
   * Sets scatter Z sampling delta. this is the step size in normalized
   * coordinates by which to sample scattering on the way out to the detector.
   * 
   * @param pScatterSamplingDeltaZ
   *          scatter sampling delta z
   */
  public void setScatterSamplingDeltaZ(float pScatterSamplingDeltaZ)
  {
    mScatterSamplingDeltaZ = pScatterSamplingDeltaZ;
  }

  protected void setupProgramAndKernels() throws IOException
  {
    ClearCLProgram lProgram = mContext.createProgram();

    lProgram.addSource(WideFieldDetectionOptics.class,
                       "kernel/WideFieldDetection.cl");

    lProgram.addBuildOptionAllMathOpt();
    lProgram.buildAndLog();

    mCollectPairKernel = lProgram.createKernel("collectpair");
    mCollectSingleKernel = lProgram.createKernel("collectsingle");
    mDefocusBlurKernel = lProgram.createKernel("defocusblur");
    mScatterBlurKernel = lProgram.createKernel("scatterblur");
  }

  @Override
  public void render(boolean pWaitToFinish)
  {
    if (!isUpdateNeeded())
      return;

    clearImages(false);

    setInvariantKernelParameters(mFluorescencePhantomImage,
                                 mScatteringPhantomImage,
                                 mLightMapImage,
                                 mZFocusPosition,
                                 mZFocusDepth);

    int lFluorescencePhantomDepth =
                                  (int) mFluorescencePhantomImage.getDepth();
    int lLightMapDepth = (int) mLightMapImage.getDepth();
    int lLightMapHalfDepth = (lLightMapDepth - 1) / 2;

    ClearCLImage lImageA = mImageTemp;
    ClearCLImage lImageB = mImage;

    for (int zi = lLightMapHalfDepth; zi >= 1; zi--)
    {
      float lDefocusDepthInNormCoordinates = ((float) zi
                                              / lLightMapHalfDepth)
                                             * 0.5f * mZFocusDepth;

      float lPhantomZ1 = mZFocusPosition
                         - lDefocusDepthInNormCoordinates
                         + 0.5f / lFluorescencePhantomDepth;

      float lPhantomZ2 = mZFocusPosition
                         + lDefocusDepthInNormCoordinates
                         + 0.5f / lFluorescencePhantomDepth;

      float lLightMapZ1 = ((float) (lLightMapHalfDepth - zi)
                           / lLightMapDepth)
                          + 0.5f / lLightMapDepth;

      float lLightMapZ2 = ((float) (lLightMapHalfDepth + zi)
                           / lLightMapDepth)
                          + 0.5f / lLightMapDepth;

      collectPair(lImageA,
                  lImageB,
                  lPhantomZ1,
                  lPhantomZ2,
                  lLightMapZ1,
                  lLightMapZ2,
                  false);

      float lSigma = getSigma()
                     * smootherstep(0.0f,
                                    getSmoothDefocusTransitionPoint(),
                                    lDefocusDepthInNormCoordinates);/**/

      // System.out.println("SIGMA: " + lSigma);

      defocusBlur(lImageB, lImageA, lSigma, false);
    }

    float lPhantomZ = mZFocusPosition
                      + 0.5f / lFluorescencePhantomDepth;

    float lLightMapZ = 0.5f + 0.5f / lLightMapDepth;

    collectSingle(lImageA, lImageB, lPhantomZ, lLightMapZ, false);

    float zfocus = getZFocusPosition();
    float zexit = 1f;
    float dz = getScatterSamplingDeltaZ();
    for (float z = zfocus; z <= zexit; z += dz)
    {
      scatterBlur(mScatteringPhantomImage,
                  lImageB,
                  lImageA,
                  z,
                  (z + dz >= zexit) && pWaitToFinish);

      // do not swap for last loop:
      // if (z + dz <= zexit)
      {
        ClearCLImage lTemp = lImageA;
        lImageA = lImageB;
        lImageB = lTemp;
      }
    } /**/

    super.render(pWaitToFinish);
  }

  /**
   * Ken Perlin's 'smootherstep' function.
   * (https://en.wikipedia.org/wiki/Smoothstep)
   * 
   * @param edge0
   * @param edge1
   * @param x
   * @return
   */
  private float smootherstep(float edge0, float edge1, float x)
  {
    // Scale, and clamp x to 0..1 range
    x = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    // Evaluate polynomial
    return x * x * x * (x * (x * 6 - 15) + 10);
  }

  private float clamp(float pX, double pMin, double pMax)
  {
    return (float) min(max(pX, pMin), pMax);
  }

  private void setInvariantKernelParameters(ClearCLImage pFluoPhantomImage,
                                            ClearCLImage pScatterPhantomImage,
                                            ClearCLImage pLightMapImage,
                                            float pZPosition,
                                            float pZDepth)
  {
    updateTransformBuffer();
    
    mCollectPairKernel.setGlobalOffsets(0, 0);
    mCollectPairKernel.setGlobalSizes(getWidth(), getHeight());
    
    mCollectPairKernel.setArgument("fluophantom", pFluoPhantomImage);
    mCollectPairKernel.setArgument("lightmap", pLightMapImage);
    mCollectPairKernel.setArgument("intensity", getIntensity());
    mCollectPairKernel.setOptionalArgument("matrix",
                                           getTransformMatrixBuffer());

    mCollectSingleKernel.setGlobalOffsets(0, 0);
    mCollectSingleKernel.setGlobalSizes(getWidth(), getHeight());
    mCollectSingleKernel.setArgument("fluophantom",
                                     pFluoPhantomImage);
    mCollectSingleKernel.setArgument("lightmap", pLightMapImage);
    mCollectSingleKernel.setArgument("intensity", getIntensity());
    mCollectSingleKernel.setOptionalArgument("matrix",
                                             getTransformMatrixBuffer());

    mDefocusBlurKernel.setGlobalOffsets(0, 0);
    mDefocusBlurKernel.setGlobalSizes(getWidth(), getHeight());
    mDefocusBlurKernel.setOptionalArgument("matrix",
                                           getTransformMatrixBuffer());

    mScatterBlurKernel.setGlobalOffsets(0, 0);
    mScatterBlurKernel.setGlobalSizes(getWidth(), getHeight());
    mScatterBlurKernel.setArgument("scatterphantom",
                                   pScatterPhantomImage);
    mScatterBlurKernel.setOptionalArgument("matrix",
                                           getTransformMatrixBuffer());

  }

  private void clearImages(boolean pBlocking)
  {
    mImage.fill(0.0f, false, false);
    mImageTemp.fill(0.0f, pBlocking, false);
  }

  private void collectPair(ClearCLImage pImageInput,
                           ClearCLImage pImageOutput,
                           float pFluoPhantomZ1,
                           float pFluoPhantomZ2,
                           float pLightMapZ1,
                           float pLightMapZ2,
                           boolean pWaitToFinish)
  {
    mCollectPairKernel.setArgument("imagein", pImageInput);
    mCollectPairKernel.setArgument("imageout", pImageOutput);
    mCollectPairKernel.setArgument("fpz1", pFluoPhantomZ1);
    mCollectPairKernel.setArgument("fpz2", pFluoPhantomZ2);
    mCollectPairKernel.setArgument("lmz1", pLightMapZ1);
    mCollectPairKernel.setArgument("lmz2", pLightMapZ2);

    mCollectPairKernel.run(pWaitToFinish);

    // pImageOutput.notifyListenersOfChange(mContext.getDefaultQueue());
  }

  private void collectSingle(ClearCLImage pImageInput,
                             ClearCLImage pImageOutput,
                             float pFluoPhantomZ,
                             float pLightMapZ,
                             boolean pWaitToFinish)
  {
    mCollectSingleKernel.setArgument("imagein", pImageInput);
    mCollectSingleKernel.setArgument("imageout", pImageOutput);
    mCollectSingleKernel.setArgument("fpz", pFluoPhantomZ);
    mCollectSingleKernel.setArgument("lmz", pLightMapZ);

    mCollectSingleKernel.run(pWaitToFinish);

    // pImageOutput.notifyListenersOfChange(mContext.getDefaultQueue());
  }

  private void defocusBlur(ClearCLImage pImageInput,
                           ClearCLImage pImageOutput,
                           float pSigma,
                           boolean pWaitToFinish)
  {
    mDefocusBlurKernel.setArgument("imagein", pImageInput);
    mDefocusBlurKernel.setArgument("imageout", pImageOutput);
    mDefocusBlurKernel.setArgument("sigma", pSigma);

    mDefocusBlurKernel.run(pWaitToFinish);

    // pImageOutput.notifyListenersOfChange(mContext.getDefaultQueue());
  }

  private void scatterBlur(ClearCLImage pScatterPhantom,
                           ClearCLImage pImageInput,
                           ClearCLImage pImageOutput,
                           float pZ,
                           boolean pWaitToFinish)
  {
    mScatterBlurKernel.setArgument("imagein", pImageInput);
    mScatterBlurKernel.setArgument("imageout", pImageOutput);
    mScatterBlurKernel.setArgument("nz", pZ);
    mScatterBlurKernel.setArgument("sigmamin", getScatterSigmaMin());
    mScatterBlurKernel.setArgument("sigmamax", getScatterSigmaMax());

    mScatterBlurKernel.run(pWaitToFinish);

    // pImageOutput.notifyListenersOfChange(mContext.getDefaultQueue());
  }

  @Override
  public void close()
  {
    mImageTemp.close();

    super.close();
  }

}
