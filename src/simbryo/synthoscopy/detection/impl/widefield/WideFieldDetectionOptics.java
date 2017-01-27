package simbryo.synthoscopy.detection.impl.widefield;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.IOException;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import simbryo.synthoscopy.detection.ClearCLDetectionOpticsBase;
import simbryo.synthoscopy.detection.DetectionOpticsInterface;

/**
 * Wide-field detetction optics
 *
 * @author royer
 */
public class WideFieldDetectionOptics extends
                                      ClearCLDetectionOpticsBase
                                      implements
                                      DetectionOpticsInterface<ClearCLImage>
{

  protected ClearCLImage mImageTemp;

  protected ClearCLKernel mCollectPairKernel, mCollectSingleKernel,
      mDefocusBlurKernel, mDetectionScatteringKernel;

  private float mSigma, mSmoothDefocusTransitionPoint;

  /**
   * Instanciates a light sheet illumination optics class given a ClearCL
   * context, and light map image dimensions
   * 
   * @param pContext
   *          OpenCL context
   * @param pWavelengthInNormUnits
   *          lights' wavelength
   * @param pLightIntensity
   *          light's intensity
   * @param pLightMapDimensions
   *          light map dimensions in voxels
   * @throws IOException
   *           thrown if kernels cannot be read
   */
  public WideFieldDetectionOptics(ClearCLContext pContext,
                                  float pWavelengthInNormUnits,
                                  float pLightIntensity,
                                  long... pLightMapDimensions) throws IOException
  {
    super(pContext,
          pWavelengthInNormUnits,
          pLightIntensity,
          pLightMapDimensions);

    setSigma(1.0f);
    setSmoothDefocusTransitionPoint(4.0f/512);

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
    return mSigma;
  }

  /**
   * Sets the sigma parameter value.
   * 
   * @param pSigma
   *          sigma
   */
  public void setSigma(float pSigma)
  {
    mSigma = pSigma;
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

  protected void setupProgramAndKernels() throws IOException
  {
    ClearCLProgram lProgram = mContext.createProgram();

    lProgram.addSource(WideFieldDetectionOptics.class,
                       "kernel/WideFieldDetection.cl");

    lProgram.buildAndLog();

    mCollectPairKernel = lProgram.createKernel("collectpair");
    mCollectSingleKernel = lProgram.createKernel("collectsingle");
    mDefocusBlurKernel = lProgram.createKernel("defocusblur");
    // mDetectionScatteringKernel = lProgram.createKernel("scatter");
  }

  /**
   * Renders the light map for a given scattering phantom image and the position
   * in z (normalized coordinates) of the light map.
   * 
   * @param pFluorescencePhantomImage
   *          fluorescent phantom
   * @param pLightMapImage
   *          light map
   * @param pZCenterOffset
   *          z offset in normalized coordinates of the center plane of the
   *          lightmap stack relative to the phantom.
   * @return light map image (same as returned by getLightMapImage() )
   */
  public ClearCLImage render(ClearCLImage pFluorescencePhantomImage,
                             ClearCLImage pLightMapImage,
                             float pZCenterOffset)
  {
    float lZDepth = (float) pLightMapImage.getDepth()
                    / pFluorescencePhantomImage.getDepth();
    return render(pFluorescencePhantomImage,
                  pLightMapImage,
                  pZCenterOffset,
                  lZDepth);
  }

  @Override
  public ClearCLImage render(ClearCLImage pFluorescencePhantomImage,
                             ClearCLImage pLightMapImage,
                             float pZPosition,
                             float pZDepth)
  {
    clearImages(false);

    setInvariantKernelParameters(pFluorescencePhantomImage,
                                 pLightMapImage,
                                 pZPosition,
                                 pZDepth);

    int lFluorescencePhantomDepth =
                                  (int) pFluorescencePhantomImage.getDepth();
    int lLightMapDepth = (int) pLightMapImage.getDepth();
    int lLightMapHalfDepth = (lLightMapDepth - 1) / 2;

    ClearCLImage lImageA = mImageTemp;
    ClearCLImage lImageB = mImage;

    for (int zi = lLightMapHalfDepth; zi >= 1; zi--)
    {
      float lDefocusDepthInNormCoordinates = ((float) zi
                                              / lLightMapHalfDepth)
                                             * 0.5f * pZDepth;

      float lPhantomZ1 = pZPosition - lDefocusDepthInNormCoordinates
                         + 0.5f / lFluorescencePhantomDepth;

      float lPhantomZ2 = pZPosition + lDefocusDepthInNormCoordinates
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
      
      System.out.println("SIGMA: "+lSigma);

      defocusBlur(lImageB, lImageA, lSigma, false);
    }

    float lPhantomZ = pZPosition + 0.5f / lFluorescencePhantomDepth;

    float lLightMapZ = 0.5f + 0.5f / lLightMapDepth;

    collectSingle(lImageA, lImageB, lPhantomZ, lLightMapZ, true);

    return getDetectionImage();
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
                                            ClearCLImage pLightMapImage,
                                            float pZPosition,
                                            float pZDepth)
  {
    mCollectPairKernel.setGlobalOffsets(0, 0);
    mCollectPairKernel.setGlobalSizes(getWidth(), getHeight());
    mCollectPairKernel.setArgument("fluophantom", pFluoPhantomImage);
    mCollectPairKernel.setArgument("lightmap", pLightMapImage);

    mCollectSingleKernel.setGlobalOffsets(0, 0);
    mCollectSingleKernel.setGlobalSizes(getWidth(), getHeight());
    mCollectSingleKernel.setArgument("fluophantom",
                                     pFluoPhantomImage);
    mCollectSingleKernel.setArgument("lightmap", pLightMapImage);

    mDefocusBlurKernel.setGlobalOffsets(0, 0);
    mDefocusBlurKernel.setGlobalSizes(getWidth(), getHeight());
  }

  private void clearImages(boolean pBlocking)
  {
    mImage.fill(0.0f, false);
    mImageTemp.fill(0.0f, pBlocking);
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

    pImageOutput.notifyListenersOfChange(mContext.getDefaultQueue());
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

    pImageOutput.notifyListenersOfChange(mContext.getDefaultQueue());
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

    pImageOutput.notifyListenersOfChange(mContext.getDefaultQueue());
  }

  @Override
  public void close()
  {
    mImageTemp.close();

    super.close();
  }

}
