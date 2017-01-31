package simbryo.synthoscopy.illumination.impl.lightsheet;

import static java.lang.Math.toRadians;

import java.io.IOException;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.enums.ImageChannelDataType;
import simbryo.synthoscopy.illumination.ClearCLIlluminationOpticsBase;
import simbryo.synthoscopy.illumination.IlluminationOpticsInterface;

/**
 * Lightsheet illumination.
 *
 * @author royer
 */
public class LightSheetIllumination extends
                                    ClearCLIlluminationOpticsBase
                                    implements
                                    IlluminationOpticsInterface<ClearCLImage>,
                                    AutoCloseable
{

  protected ClearCLImage mBallisticLightImageA, mBallisticLightImageB,
      mScatteredLightImageA, mScatteredLightImageB;
  protected ClearCLKernel mPropagateLightSheetKernel;

  protected Vector3f mLightSheetPosition, mLightSheetAxisVector,
      mLightSheetNormalVector;

  private float mLightSheetThetaInRad, mLightSheetHeigth,
      mScatterConstant, mScatterLoss, mSigmaMin, mSigmaMax,
      mZCenterOffset, mZDepth;

  /**
   * Instanciates a light sheet illumination optics class given a ClearCL
   * context, the wavelength of light, the light intensity, and light map image
   * dimensions
   * 
   * @param pContext
   *          OpenCL context
   * @param pWavelengthInNormUnits
   *          light's wavelength
   * @param pLightIntensity
   *          light's intensity
   * @param pLightMapDimensions
   *          light map dimensions in voxels
   * @throws IOException
   *           thrown if kernels cannot be read
   */
  public LightSheetIllumination(ClearCLContext pContext,
                                float pWavelengthInNormUnits,
                                float pLightIntensity,
                                long... pLightMapDimensions) throws IOException
  {
    super(pContext,
          pWavelengthInNormUnits,
          pLightIntensity,
          pLightMapDimensions);

    setLightSheetThetaInDeg(2);
    setLightSheetHeigth(0.5f);
    setScatterConstant(100.0f);
    setScatterLoss(0.01f);
    setSigmaMin(0.5f);
    setSigmaMax(1.0f);

    setupProgramAndKernels();

    mScatteredLightImageA =
                          mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                            getHeight(),
                                                            getDepth());

    mScatteredLightImageB =
                          mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                            getHeight(),
                                                            getDepth());

    mBallisticLightImageA =
                          mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                            getHeight(),
                                                            getDepth());

    mBallisticLightImageB =
                          mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                            getHeight(),
                                                            getDepth());

    mLightSheetPosition = new Vector3f(0f, 0f, 0.5f);
    mLightSheetAxisVector = new Vector3f(1.0f, 0, 0);
    mLightSheetNormalVector = new Vector3f(0, 0, 1.0f);
  }

  /**
   * Returns scattering constant. The bigger the constant the more light gets
   * transfered from ballistic to scattering.
   * 
   * @return scattering constant
   */
  public float getScatterConstant()
  {
    return mScatterConstant;
  }

  /**
   * Setseturns scattering constant. The bigger the constant the more light gets
   * transfered from ballistic to scattering.
   * 
   * @param pScatterConstant
   *          new scattering constant
   */
  public void setScatterConstant(float pScatterConstant)
  {
    mScatterConstant = pScatterConstant;
  }

  /**
   * Returns the scattering loss. This is the proportion of scattered light
   * (value within [0,1]) that gets lost per voxel.
   * 
   * @return scattering loss
   */
  public float getScatterLoss()
  {
    return mScatterLoss;
  }

  /**
   * Sets the scattering loss. This is the proportion of scattered light (value
   * within [0,1]) that gets lost per voxel.
   * 
   * @param pScatterLoss
   *          scattering loss
   */
  public void setScatterLoss(float pScatterLoss)
  {
    mScatterLoss = pScatterLoss;
  }

  /**
   * Returns the minimal sigma value. The min sigma value represents the
   * dispersion of already scattered light as it propagates _even_ in the
   * absence of scattering material.
   * 
   * @return current min sigma value
   */
  public float getSigmaMin()
  {
    return mSigmaMin;
  }

  /**
   * Sets the minimal sigma value. The min sigma value represents the dispersion
   * of already scattered light as it propagates _even_ in the absence of
   * scattering material.
   * 
   * @param pSigmaMin
   *          new min sigma value
   */
  public void setSigmaMin(float pSigmaMin)
  {
    mSigmaMin = pSigmaMin;
  }

  /**
   * Returns the maximal sigma value. The actual sigma value per voxel will be
   * modulated by (maxsigma-minsigm) basd on the actual scattering phantom value
   * (which are values within [0,1], 0-> min scattering, 1 -> max scattering)
   * 
   * @return current max sigma value
   */
  public float getSigmaMax()
  {
    return mSigmaMax;
  }

  /**
   * Sets the maximal sigma value. The actual sigma value per vocel will be
   * modulated by (maxsigma-minsigm) basd on the actual scattering phantom value
   * (which are values within [0,1], 0-> min scattering, 1 -> max scattering)
   * 
   * @param pSigmaMax
   *          new max sigma value
   */
  public void setSigmaMax(float pSigmaMax)
  {
    mSigmaMax = pSigmaMax;
  }

  /**
   * Returns lightsheet height in normalized units.
   * 
   * @return light sheet height in normalized units.
   */
  public float getLightSheetHeigth()
  {
    return mLightSheetHeigth;
  }

  /**
   * Sets the lightsheet height in normalized units.
   * 
   * @param pLightSheetHeigth
   *          light sheet height in normalized units.
   */
  public void setLightSheetHeigth(float pLightSheetHeigth)
  {
    mLightSheetHeigth = pLightSheetHeigth;
  }

  /**
   * Returns the lightsheet divergence angle (half of the angle between the
   * bounding planes)
   * 
   * @return divergence in degrees
   */
  public float getLightSheetThetaInDeg()
  {
    return (float) Math.toDegrees(mLightSheetThetaInRad);
  }

  /**
   * Returns the lightsheet divergence angle (half of the angle between the
   * bounding planes)
   * 
   * @return divergence in radians
   */
  public float getLightSheetThetaInRad()
  {
    return mLightSheetThetaInRad;
  }

  /**
   * Sets the lightsheet divergence angle (half of the angle between the
   * bounding planes)
   * 
   * @param pLightSheetThetaInDeg
   *          divergence in degrees
   */
  public void setLightSheetThetaInDeg(float pLightSheetThetaInDeg)
  {
    mLightSheetThetaInRad =
                          (float) Math.toRadians(pLightSheetThetaInDeg);
  }

  private float getSpotSizeAtNeck()
  {
    return (float) (getLightWavelength()
                    / (Math.PI * getLightSheetThetaInRad()));
  }

  /**
   * Sets lightsheet position in normalized units. N
   * 
   * @param pX
   *          x coordinate within [0,1]
   * @param pY
   *          y coordinate within [0,1]
   * @param pZ
   *          z coordinate within [0,1]
   */
  public void setLightSheetPosition(float pX, float pY, float pZ)
  {
    mLightSheetPosition.x = pX;
    mLightSheetPosition.y = pY;
    mLightSheetPosition.z = pZ;
  }

  /**
   * Returns lightsheet center position vector.
   * 
   * @return axis vector
   */
  public Vector3f getLightSheetPositionVector()
  {
    return mLightSheetPosition;
  }

  /**
   * Sets lightsheet axis vector (light propagation direction). Inputs are
   * automatically normalized.
   * 
   * @param pX
   *          x coordinate
   * @param pY
   *          y coordinate
   * @param pZ
   *          z coordinate
   */
  public void setLightSheetAxisVector(float pX, float pY, float pZ)
  {
    mLightSheetAxisVector.x = pX;
    mLightSheetAxisVector.y = pY;
    mLightSheetAxisVector.z = pZ;

    mLightSheetAxisVector.normalize();
    float lProjection =
                      mLightSheetAxisVector.dot(mLightSheetNormalVector);

    mLightSheetNormalVector.scaleAdd(-lProjection,
                                     mLightSheetAxisVector,
                                     mLightSheetNormalVector);
    mLightSheetNormalVector.normalize();
  }

  /**
   * Returns lightsheet axis vector (light propagation direction).
   * 
   * @return axis vector
   */
  public Vector3f getLightSheetAxisVector()
  {
    return mLightSheetAxisVector;
  }

  /**
   * Sets lightsheet normal vector (perpendicular to the lightsheet plane).
   * Inputs are automatically normalized.
   * 
   * @param pX
   *          x coordinate
   * @param pY
   *          y coordinate
   * @param pZ
   *          z coordinate
   */
  public void setLightSheetNormalVector(float pX, float pY, float pZ)
  {
    mLightSheetNormalVector.x = pX;
    mLightSheetNormalVector.y = pY;
    mLightSheetNormalVector.z = pZ;

    mLightSheetNormalVector.normalize();

    float lProjection =
                      mLightSheetNormalVector.dot(mLightSheetAxisVector);

    mLightSheetAxisVector.scaleAdd(-lProjection,
                                   mLightSheetNormalVector,
                                   mLightSheetAxisVector);

    mLightSheetAxisVector.normalize();
  }

  /**
   * Returns lightsheet normal vector.
   * 
   * @return normal vector
   */
  public Vector3f getLightSheetNormalVector()
  {
    return mLightSheetNormalVector;
  }

  /**
   * Sets the axis and normal lightsheet vectors from the three angles (alpha,
   * beta, gamma) in degrees. the alpha angle rotates along x, the beta angle
   * along y, and the gamma angle around z.
   * 
   * @param pAlpha
   *          alpha in degrees
   * @param pBeta
   *          beta in degrees
   * @param pGamma
   *          gamma in degrees
   */
  public void setOrientationWithAnglesInDegrees(float pAlpha,
                                                float pBeta,
                                                float pGamma)
  {
    setOrientationWithAnglesInRadians((float) toRadians(pAlpha),
                                      (float) toRadians(pBeta),
                                      (float) toRadians(pGamma));
  }

  /**
   * Returns the z center position of the lightmap relative to the scatter
   * phantom in normalized coordinates.
   * 
   * @return lightmap z center position relative to scatter phantom
   */
  public float getZCenterOffset()
  {
    return mZCenterOffset;
  }

  /**
   * Sets the z center position of the lightmap relative to the scatter phantom
   * in normalized coordinates.
   * 
   * @param pZCenterOffset
   *          lightmap z center position relative to scatter phantom
   */
  public void setZCenterOffset(float pZCenterOffset)
  {
    mZCenterOffset = pZCenterOffset;
  }

  /**
   * Return the depth of the lightmap stack relative to the scatter phantom in
   * normalized coordinates.
   * 
   * @return lightmap depth relative to scatter phantom
   */
  public float getZDepth()
  {
    return mZDepth;
  }

  /**
   * Sets the depth of the lightmap stack relative to the scatter phantom in
   * normalized coordinates.
   * 
   * @param pZDepth
   *          lightmap depth relative to scatter phantom
   */
  public void setZDepth(float pZDepth)
  {
    mZDepth = pZDepth;
  }

  /**
   * Sets the default depth of the lightmap stack relative to the scatter
   * phantom in normalized coordinates. This default value is such that each
   * plane of the lightmap corresponds to a single plane of the scatter phantom.
   * 
   * @param pScatteringPhantomImage
   *          scattering phantom
   */
  public void setDefaultZDepth(ClearCLImage pScatteringPhantomImage)
  {
    mZDepth = (float) getDepth() / pScatteringPhantomImage.getDepth();
  }

  /**
   * Sets the axis and normal lightsheet vectors from the three angles (alpha,
   * beta, gamma) in radians. the alpha angle rotates along x, the beta angle
   * along y, and the gamma angle around z.
   * 
   * @param pAlpha
   *          alpha in radians
   * @param pBeta
   *          beta in radians
   * @param pGamma
   *          gamma in radians
   */
  public void setOrientationWithAnglesInRadians(float pAlpha,
                                                float pBeta,
                                                float pGamma)
  {
    Matrix3f lMatrix = new Matrix3f();
    Matrix3f lRotX = new Matrix3f();
    Matrix3f lRotY = new Matrix3f();
    Matrix3f lRotZ = new Matrix3f();

    lMatrix.setIdentity();
    lRotX.rotX(pAlpha);
    lRotY.rotY(pBeta);
    lRotZ.rotZ(pGamma);

    lMatrix.mul(lRotX);
    lMatrix.mul(lRotY);
    lMatrix.mul(lRotZ);

    lMatrix.getColumn(0, mLightSheetAxisVector);
    lMatrix.getColumn(2, mLightSheetNormalVector);
  }

  protected void setupProgramAndKernels() throws IOException
  {
    ClearCLProgram lProgram = mContext.createProgram();

    lProgram.addSource(LightSheetIllumination.class,
                       "kernel/LightSheetIllumination.cl");

    lProgram.buildAndLog();

    mPropagateLightSheetKernel = lProgram.createKernel("propagate");
  }

  @Override
  public ClearCLImage render(ClearCLImage pScatteringPhantomImage)
  {
    initializeLightSheet(mBallisticLightImageA,
                         mBallisticLightImageB,
                         mScatteredLightImageA,
                         mScatteredLightImageB);

    setInvariantKernelParameters(pScatteringPhantomImage,
                                 getZCenterOffset(),
                                 getZDepth());

    for (int i = 0; i < getWidth(); i++)
    {
      int x;

      if (mLightSheetAxisVector.x > 0)
        x = i;
      else
        x = (int) (getWidth() - 1 - i);

      propagate(x,
                mBallisticLightImageA,
                mScatteredLightImageA,
                mBallisticLightImageB,
                mScatteredLightImageB,
                i == getWidth() - 1);

      /*diffuse(mScatteredLightImageB,
              mScatteredLightImageA,
              mScatteredLightImageB,
              5);/**/
      swapLightImages();
    }

    return super.render(pScatteringPhantomImage);
  }

  private void setInvariantKernelParameters(ClearCLImage pScatteringPhantomImage,
                                            float pZCenterOffset,
                                            float pZDepth)
  {
    mPropagateLightSheetKernel.setGlobalOffsets(0, 0);
    mPropagateLightSheetKernel.setGlobalSizes(getHeight(),
                                              getDepth());

    mPropagateLightSheetKernel.setArgument("scatterphantom",
                                           pScatteringPhantomImage);
    mPropagateLightSheetKernel.setArgument("lightmap",
                                           getLightMapImage());
    mPropagateLightSheetKernel.setArgument("lspx",
                                           mLightSheetPosition.x);
    mPropagateLightSheetKernel.setArgument("lspy",
                                           mLightSheetPosition.y);
    mPropagateLightSheetKernel.setArgument("lspz",
                                           mLightSheetPosition.z);

    mPropagateLightSheetKernel.setArgument("lsax",
                                           mLightSheetAxisVector.x);
    mPropagateLightSheetKernel.setArgument("lsay",
                                           mLightSheetAxisVector.y);
    mPropagateLightSheetKernel.setArgument("lsaz",
                                           mLightSheetAxisVector.z);

    mPropagateLightSheetKernel.setArgument("lsnx",
                                           mLightSheetNormalVector.x);
    mPropagateLightSheetKernel.setArgument("lsny",
                                           mLightSheetNormalVector.y);
    mPropagateLightSheetKernel.setArgument("lsnz",
                                           mLightSheetNormalVector.z);

    mPropagateLightSheetKernel.setArgument("zdepth", pZDepth);

    mPropagateLightSheetKernel.setArgument("zoffset",
                                           pZCenterOffset
                                                      - pZDepth / 2);

    mPropagateLightSheetKernel.setArgument("lambda",
                                           getLightWavelength());

    mPropagateLightSheetKernel.setArgument("intensity",
                                           getLightIntensity());

    mPropagateLightSheetKernel.setArgument("scatterconstant",
                                           getScatterConstant());

    mPropagateLightSheetKernel.setArgument("scatterloss",
                                           1.0f - getScatterLoss());

    mPropagateLightSheetKernel.setArgument("sigmamin", getSigmaMin());

    mPropagateLightSheetKernel.setArgument("sigmamax", getSigmaMax());

    mPropagateLightSheetKernel.setArgument("w0", getSpotSizeAtNeck());

    mPropagateLightSheetKernel.setArgument("lsheight",
                                           getLightSheetHeigth());
  }

  private void propagate(int pXPosition,
                         ClearCLImage pBallisticLightImageA,
                         ClearCLImage pScatteredLightImageA,
                         ClearCLImage pBallisticLightImageB,
                         ClearCLImage pScatteredLightImageB,
                         boolean pWaitToFinish)
  {

    mPropagateLightSheetKernel.setArgument("binput",
                                           pBallisticLightImageA);
    mPropagateLightSheetKernel.setArgument("boutput",
                                           pBallisticLightImageB);
    mPropagateLightSheetKernel.setArgument("sinput",
                                           pScatteredLightImageA);
    mPropagateLightSheetKernel.setArgument("soutput",
                                           pScatteredLightImageB);

    mPropagateLightSheetKernel.setArgument("x", pXPosition);

    mPropagateLightSheetKernel.run(pWaitToFinish);

    // pBallisticLightImageB.notifyListenersOfChange(mContext.getDefaultQueue());
    // pScatteredLightImageB.notifyListenersOfChange(mContext.getDefaultQueue());

  }

  private void initializeLightSheet(ClearCLImage pBallisticLightImageA,
                                    ClearCLImage pBallisticLightImageB,
                                    ClearCLImage pScatteredLightImageA,
                                    ClearCLImage pScatteredLightImageB)
  {

    pBallisticLightImageA.fill(1.0f, false, false);
    pBallisticLightImageB.fill(1.0f, false, false);
    pScatteredLightImageA.fill(0.0f, false, false);
    pScatteredLightImageB.fill(0.0f, true, false);
  }

  private void swapLightImages()
  {
    ClearCLImage lTemp;

    lTemp = mScatteredLightImageA;
    mScatteredLightImageA = mScatteredLightImageB;
    mScatteredLightImageB = lTemp;

    lTemp = mBallisticLightImageA;
    mBallisticLightImageA = mBallisticLightImageB;
    mBallisticLightImageB = lTemp;
  }

  @Override
  public void close()
  {
    mBallisticLightImageA.close();
    mBallisticLightImageB.close();
    mScatteredLightImageA.close();
    mScatteredLightImageB.close();

    mPropagateLightSheetKernel.close();

    super.close();
  }

}
