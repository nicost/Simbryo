package simbryo.synthoscopy.camera.impl;

import java.io.IOException;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.enums.ImageChannelDataType;
import simbryo.synthoscopy.camera.CameraRendererInterface;
import simbryo.synthoscopy.camera.ClearCLCameraRendererBase;

/**
 * Generic camera model for sCMOS cameras
 *
 * @author royer
 */
public class SCMOSCameraRenderer extends ClearCLCameraRendererBase
                                 implements
                                 CameraRendererInterface<ClearCLImage>
{

  protected ClearCLImage mImageTemp;

  protected ClearCLKernel mUpscaleKernel, mNoiseKernel;

  private int mTimeIndex = 0;

  private int mXMin, mXMax, mYMin, mYMax;

  private float mShotNoise, mOffset, mGain, mOffsetNoise, mGainNoise,
      mOffsetBias, mGainBias;

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
   * @param pMaxCameraImageDimensions
   *          max camera image dimensions in voxels
   * @throws IOException
   *           thrown if kernels cannot be read
   */
  public SCMOSCameraRenderer(ClearCLContext pContext,
                             float pWavelengthInNormUnits,
                             float pLightIntensity,
                             long... pMaxCameraImageDimensions) throws IOException
  {
    super(pContext,
          pWavelengthInNormUnits,
          pLightIntensity,
          pMaxCameraImageDimensions);

    setShotNoise(0.03f);
    setOffset(97.0f);
    setGain(200.0f);
    setOffsetBias(1.0f);
    setGainBias(0.05f);
    setOffsetNoise(1.0f);
    setGainNoise(0.04f);

    mXMin = 0;
    mXMax = (int) pMaxCameraImageDimensions[0];
    mYMin = 0;
    mYMax = (int) pMaxCameraImageDimensions[1];

    ensureImagesAllocated();
    setupProgramAndKernels();
    clearImages(true);
  }

  /**
   * Returns shot noise parameter.
   * 
   * @return shot noise parameter
   */
  public float getShotNoise()
  {
    return mShotNoise;
  }

  /**
   * Sets shot noise parameter. This parameter should be a floating point number
   * within [0,1].
   * 
   * @param pShotNoise
   *          shot noise parameter within [0,1]
   */
  public void setShotNoise(float pShotNoise)
  {
    mShotNoise = pShotNoise;
  }

  /**
   * Returns signal amplification offset.
   * 
   * @return signal amplification offset.
   */
  public float getOffset()
  {
    return mOffset;
  }

  /**
   * Sets signal amplification offset. This is a in y=ax+b where x is the
   * fluorescence signal and y is the resulting electronic-amplified signal.
   * 
   * @param pOffset
   *          signal amplification offset.
   */
  public void setOffset(float pOffset)
  {
    mOffset = pOffset;
  }

  /**
   * Returns signal amplification gain.
   * 
   * @return signal amplification gain.
   */
  public float getGain()
  {
    return mGain;
  }

  /**
   * Sets signal amplification gain. This is a in y=ax+b where x is the
   * fluorescence signal and y is the resulting electronic-amplified signal.
   * 
   * @param pGain
   *          signal amplification gain.
   */
  public void setGain(float pGain)
  {
    mGain = pGain;
  }

  /**
   * Returns signal amplification offset noise amplitude.
   * 
   * @return signal amplification offset noise amplitude.
   */
  public float getOffsetNoise()
  {
    return mOffsetNoise;
  }

  /**
   * Sets signal amplification offset noise amplitude. Offset noise is modeled
   * as a χ^2(k=1) (Chi-square distribution with 1 degree of freedom)
   * 
   * @param pOffsetNoise
   *          signal amplification offset noise amplitude
   */
  public void setOffsetNoise(float pOffsetNoise)
  {
    mOffsetNoise = pOffsetNoise;
  }

  /**
   * Returns signal amplification gain noise amplitude.
   * 
   * @return signal amplification gain noise amplitude.
   */
  public float getGainNoise()
  {
    return mGainNoise;
  }

  /**
   * Sets signal amplification gain noise amplitude. Offset gain is as a
   * zero-mean Gaussian distribution of given amplitude. The gain is modulated
   * additively by the noise.
   * 
   * @param pGainNoise
   *          signal amplification offset noise amplitude
   */
  public void setGainNoise(float pGainNoise)
  {
    mGainNoise = pGainNoise;
  }

  /**
   * Returns the offset bias amplitude
   * 
   * @return offset bias amplitude
   */
  public float getOffsetBias()
  {
    return mOffsetBias;
  }

  /**
   * Sets the offset bias amplitude. Each pixel of the detector has a
   * non-time-varying noise component for the electronic offset which only
   * varies across pixels. This noise is modeled as a zero-mean Gaussian of
   * given amplitude.
   * 
   * @param pOffsetBias
   *          offset bias amplitude
   */
  public void setOffsetBias(float pOffsetBias)
  {
    mOffsetBias = pOffsetBias;
  }

  /**
   * Returns the offset bias amplitude. Each pixel of the detector has a
   * non-time-varying noise component for the electronic gain which only varies
   * across pixels. This noise is modeled as a zero-mean Gaussian of given
   * amplitude.
   * 
   * @return offset bias amplitude
   */
  public float getGainBias()
  {
    return mGainBias;
  }

  /**
   * Sets the gain bias amplitude
   * 
   * @param pGainBias
   *          gain bias amplitude
   */
  public void setGainBias(float pGainBias)
  {
    mGainBias = pGainBias;
  }

  /**
   * Increment time index.
   */
  public void incrementTimeIndex()
  {
    mTimeIndex++;
  }

  /**
   * Sets Region-Of-Interest [xmin,xmax]x[ymin,ymax]
   * 
   * @param pXMin
   *          x min
   * @param pXMax
   *          x max
   * @param pYMin
   *          y min
   * @param pYMax
   *          y max
   */
  public void setROI(int pXMin, int pXMax, int pYMin, int pYMax)
  {
    mXMin = pXMin;
    mXMax = pXMax;
    mYMin = pYMin;
    mYMax = pYMax;

    ensureImagesAllocated();
  }

  /**
   * Sets a centered Region-Of-Interest (center of ROI coincides with center of
   * detector)
   * 
   * @param pWidth
   *          width of ROI
   * @param pHeight
   *          height of ROI
   */
  public void setCenteredROI(int pWidth, int pHeight)
  {
    int lMarginX = (int) ((getMaxWidth() - pWidth) / 2);
    int lMarginY = (int) ((getMaxHeight() - pHeight) / 2);
    mXMin = lMarginX;
    mXMax = (int) (getMaxWidth() - lMarginX);
    mYMin = lMarginY;
    mYMax = (int) (getMaxHeight() - lMarginY);

    ensureImagesAllocated();
  }

  @Override
  public long getWidth()
  {
    return mXMax - mXMin;
  }

  @Override
  public long getHeight()
  {
    return mYMax - mYMin;
  }

  /**
   * Returns the camera pixel width
   * 
   * @return camera pixel width
   */
  public long getMaxWidth()
  {
    return super.getWidth();
  }

  /**
   * Returns the camera pixel height
   * 
   * @return camera pixel height
   */
  public long getMaxHeight()
  {
    return super.getHeight();
  }

  private void ensureImagesAllocated()
  {
    if (mImage != null)
      mImage.close();
    if (mImageTemp != null)
      mImageTemp.close();

    mImage =
           mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                             getWidth(),
                                             getHeight());

    mImageTemp = mContext.createImage(mImage);
  }

  protected void setupProgramAndKernels() throws IOException
  {
    ClearCLProgram lProgram = mContext.createProgram();

    lProgram.addSource(SCMOSCameraRenderer.class,
                       "kernel/CameraImage.cl");

    lProgram.buildAndLog();

    mUpscaleKernel = lProgram.createKernel("upscale");
    mNoiseKernel = lProgram.createKernel("camnoise");
  }

  @Override
  public ClearCLImage render(ClearCLImage pDetectionImage)
  {
    clearImages(false);
    setInvariantKernelParameters(pDetectionImage);
    upscale(pDetectionImage, mImageTemp, false);
    noise(mImageTemp, mImage, true);
    incrementTimeIndex();

    return super.render(pDetectionImage);
  }

  private void setInvariantKernelParameters(ClearCLImage pDetectionImage)
  {
    mUpscaleKernel.setGlobalOffsets(0, 0);
    mUpscaleKernel.setGlobalSizes(getWidth(), getHeight());

    mNoiseKernel.setGlobalOffsets(0, 0);
    mNoiseKernel.setGlobalSizes(getWidth(), getHeight());

  }

  private void clearImages(boolean pBlocking)
  {
    mImage.fill(0.0f, false, false);
    mImageTemp.fill(0.0f, pBlocking, false);
  }

  private void upscale(ClearCLImage pImageInput,
                       ClearCLImage pImageOutput,
                       boolean pWaitToFinish)
  {
    mUpscaleKernel.setArgument("imagein", pImageInput);
    mUpscaleKernel.setArgument("imageout", pImageOutput);

    float lNormalizedXMin = (float) mXMin / getMaxWidth();
    float lNormalizedXScale = (float) getWidth() / getMaxWidth();
    float lNormalizedYMin = (float) mYMin / getMaxHeight();
    float lNormalizedYScale = (float) getHeight() / getMaxHeight();

    mUpscaleKernel.setArgument("nxmin", lNormalizedXMin);
    mUpscaleKernel.setArgument("nxscale", lNormalizedXScale);
    mUpscaleKernel.setArgument("nymin", lNormalizedYMin);
    mUpscaleKernel.setArgument("nyscale", lNormalizedYScale);

    mUpscaleKernel.run(pWaitToFinish);
  }

  private void noise(ClearCLImage pImageInput,
                     ClearCLImage pImageOutput,
                     boolean pWaitToFinish)
  {
    mNoiseKernel.setArgument("imagein", pImageInput);
    mNoiseKernel.setArgument("imageout", pImageOutput);
    mNoiseKernel.setArgument("timeindex", mTimeIndex);
    mNoiseKernel.setArgument("shotnoise", getShotNoise());
    mNoiseKernel.setArgument("offset", getOffset());
    mNoiseKernel.setArgument("gain", getGain());
    mNoiseKernel.setArgument("offsetbias", getOffsetBias());
    mNoiseKernel.setArgument("gainbias", getGainBias());
    mNoiseKernel.setArgument("offsetnoise", getOffsetNoise());
    mNoiseKernel.setArgument("gainnoise", getGainNoise());

    mNoiseKernel.run(pWaitToFinish);
  }

  @Override
  public void close()
  {
    mImageTemp.close();
    super.close();
  }

}
