package simbryo.synthoscopy.optics.illumination.impl.lightsheet;

import static java.lang.Math.toRadians;

import java.io.IOException;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.enums.ImageChannelDataType;
import simbryo.synthoscopy.optics.illumination.IlluminationOpticsBase;
import simbryo.synthoscopy.optics.illumination.IlluminationOpticsInterface;

/**
 * Lightsheet illumination.
 *
 * @author royer
 */
public class LightSheetIllumination extends IlluminationOpticsBase
                                    implements
                                    IlluminationOpticsInterface<ClearCLImage>,
                                    AutoCloseable
{

  protected ClearCLImage mNullImage, mInputImage,
      mBallisticLightImageA, mBallisticLightImageB,
      mScatteredLightImageA, mScatteredLightImageB;
  protected ClearCLKernel mPropagateLightSheetKernel;

  protected Vector3f mLightSheetPosition, mLightSheetAxisVector,
      mLightSheetNormalVector, mLightSheetEffectiveAxisVector,
      mLightSheetEffectiveNormalVector;

  private volatile float mLightSheetAlphaInRad, mLightSheetBetaInRad,
      mLightSheetGammaInRad, mLightSheetThetaInRad, mLightSheetHeigth,
      mScatterConstant, mScatterLoss, mSigmaMin, mSigmaMax,
      mZCenterOffset, mZDepth;

  /**
   * Instanciates a light sheet illumination optics class given a ClearCL
   * context, the wavelength of light, the light intensity, and light map image
   * dimensions
   * 
   * @param pContext
   *          OpenCL context
   * @param pLightMapDimensions
   *          light map dimensions in voxels
   * @throws IOException
   *           thrown if kernels cannot be read
   */
  public LightSheetIllumination(ClearCLContext pContext,
                                long... pLightMapDimensions) throws IOException
  {
    super(pContext, pLightMapDimensions);

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

    mNullImage =
               mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                 1,
                                                 1);
    mNullImage.fillZero(false, false);
    
    mInputImage = null;

    mLightSheetPosition = new Vector3f(0f, 0f, 0.5f);
    mLightSheetAxisVector = new Vector3f(1.0f, 0, 0);
    mLightSheetNormalVector = new Vector3f(0, 0, 1.0f);
    mLightSheetEffectiveAxisVector = new Vector3f(1.0f, 0, 0);
    mLightSheetEffectiveNormalVector = new Vector3f(0, 0, 1.0f);
  }

  @Override
  public void setInputImage(ClearCLImage pInputImage)
  {
    mInputImage = pInputImage;
  }

  @Override
  public ClearCLImage getInputImage()
  {
    return mInputImage;
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
    if (mScatterConstant != pScatterConstant)
    {
      mScatterConstant = pScatterConstant;
      requestUpdate();
    }
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
    if (mScatterLoss != pScatterLoss)
    {
      mScatterLoss = pScatterLoss;
      requestUpdate();
    }
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
    if (mSigmaMin != pSigmaMin)
    {
      mSigmaMin = pSigmaMin;
      requestUpdate();
    }
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
    if (mSigmaMax != pSigmaMax)
    {
      mSigmaMax = pSigmaMax;
      requestUpdate();
    }
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
    if (mLightSheetHeigth != pLightSheetHeigth)
    {
      mLightSheetHeigth = pLightSheetHeigth;
      requestUpdate();
    }
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
    float lNewLightSheetThetaInDeg =
                                   (float) Math.toRadians(pLightSheetThetaInDeg);
    if (mLightSheetThetaInRad != lNewLightSheetThetaInDeg)
    {
      mLightSheetThetaInRad = lNewLightSheetThetaInDeg;
      requestUpdate();
    }
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
    if (mLightSheetPosition.x != pX || mLightSheetPosition.y != pY
        || mLightSheetPosition.z != pZ)
    {
      mLightSheetPosition.x = pX;
      mLightSheetPosition.y = pY;
      mLightSheetPosition.z = pZ;
      requestUpdate();
    }
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
   * @param pAxisVector
   *          axis vector
   * 
   */
  public void setLightSheetAxisVector(Vector3f pAxisVector)
  {
    setLightSheetAxisVector(pAxisVector.x,
                            pAxisVector.y,
                            pAxisVector.z);
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
    if (mLightSheetAxisVector.x != pX || mLightSheetAxisVector.y != pY
        || mLightSheetAxisVector.z != pZ)
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

      updateEffectiveAxisAndNormalVectors();
      requestUpdate();
    }
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
   * @param pNormalVector
   *          normal vector
   * 
   */
  public void setLightSheetNormalVector(Vector3f pNormalVector)
  {
    setLightSheetNormalVector(pNormalVector.x,
                              pNormalVector.y,
                              pNormalVector.z);
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
    if (mLightSheetNormalVector.x != pX
        || mLightSheetNormalVector.y != pY
        || mLightSheetNormalVector.z != pZ)
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
      updateEffectiveAxisAndNormalVectors();
      requestUpdate();
    }
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
    if (mZCenterOffset != pZCenterOffset)
    {
      mZCenterOffset = pZCenterOffset;
      requestUpdate();
    }
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
    if (mZDepth != pZDepth)
    {
      mZDepth = pZDepth;
      requestUpdate();
    }
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
    setZDepth((float) getDepth()
              / pScatteringPhantomImage.getDepth());
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
   * Rotates the axis and normal lightsheet vectors along the three directions
   * (x,y,z) by the angles (alpha, beta, gamma) in radians. the alpha angle
   * rotates along x, the beta angle along y, and the gamma angle around z.
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

    if (mLightSheetAlphaInRad != pAlpha
        || mLightSheetBetaInRad != pBeta
        || mLightSheetGammaInRad != pGamma)
    {
      mLightSheetAlphaInRad = pAlpha;
      mLightSheetBetaInRad = pBeta;
      mLightSheetGammaInRad = pGamma;

      updateEffectiveAxisAndNormalVectors();
      requestUpdate();
    }

  }

  private void updateEffectiveAxisAndNormalVectors()
  {
    Matrix3f lMatrix = new Matrix3f();
    Matrix3f lRotX = new Matrix3f();
    Matrix3f lRotY = new Matrix3f();
    Matrix3f lRotZ = new Matrix3f();

    getLightSheetAxisVector().normalize();
    getLightSheetNormalVector().normalize();
    Vector3f lOrthoVector = new Vector3f();
    lOrthoVector.cross(getLightSheetAxisVector(),
                       getLightSheetNormalVector());

    lMatrix.setColumn(0, getLightSheetAxisVector());
    lMatrix.setColumn(1, lOrthoVector);
    lMatrix.setColumn(2, getLightSheetNormalVector());
    lRotX.rotX(mLightSheetAlphaInRad);
    lRotY.rotY(mLightSheetBetaInRad);
    lRotZ.rotZ(mLightSheetGammaInRad);

    lMatrix.mul(lRotZ); 
    lMatrix.mul(lRotX);
    lMatrix.mul(lRotY);

    lMatrix.getColumn(0, mLightSheetEffectiveAxisVector);
    lMatrix.getColumn(2, mLightSheetEffectiveNormalVector);
  }

  /**
   * Returns the light sheet's effective axis vector after rotation by (alpha,
   * beta, gamma)
   * 
   * @return effective axis vector
   */
  public Vector3f getLightSheetEffectiveAxisVector()
  {
    return mLightSheetEffectiveAxisVector;
  }

  /**
   * Returns the light sheet's effective normal vector after rotation by (alpha,
   * beta, gamma)
   * 
   * @return normal axis vector
   */
  public Vector3f getLightSheetEffectiveNormalVector()
  {
    return mLightSheetEffectiveNormalVector;
  }

  protected void setupProgramAndKernels() throws IOException
  {
    ClearCLProgram lProgram = mContext.createProgram();

    lProgram.addSource(LightSheetIllumination.class,
                       "kernel/LightSheetIllumination.cl");

    lProgram.addBuildOptionAllMathOpt();
    lProgram.buildAndLog();

    mPropagateLightSheetKernel = lProgram.createKernel("propagate");
  }

  @Override
  public void render(boolean pWaitToFinish)
  {
    if (!isUpdateNeeded())
      return;

    initializeLightSheet(mBallisticLightImageA,
                         mBallisticLightImageB,
                         mScatteredLightImageA,
                         mScatteredLightImageB);

    setInvariantKernelParameters(mScatteringPhantomImage,
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
                (i == getWidth() - 1) && pWaitToFinish);

      /*diffuse(mScatteredLightImageB,
              mScatteredLightImageA,
              mScatteredLightImageB,
              5);/**/
      swapLightImages();
    }

    super.render(pWaitToFinish);
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
    mPropagateLightSheetKernel.setArgument("lightmapin",
                                           getInputImage() == null ? mNullImage
                                                                   : getInputImage());
    mPropagateLightSheetKernel.setArgument("lightmapout", getImage());

    mPropagateLightSheetKernel.setArgument("lspx",
                                           mLightSheetPosition.x);
    mPropagateLightSheetKernel.setArgument("lspy",
                                           mLightSheetPosition.y);
    mPropagateLightSheetKernel.setArgument("lspz",
                                           mLightSheetPosition.z);

    mPropagateLightSheetKernel.setArgument("lsax",
                                           mLightSheetEffectiveAxisVector.x);
    mPropagateLightSheetKernel.setArgument("lsay",
                                           mLightSheetEffectiveAxisVector.y);
    mPropagateLightSheetKernel.setArgument("lsaz",
                                           mLightSheetEffectiveAxisVector.z);

    mPropagateLightSheetKernel.setArgument("lsnx",
                                           mLightSheetEffectiveNormalVector.x);
    mPropagateLightSheetKernel.setArgument("lsny",
                                           mLightSheetEffectiveNormalVector.y);
    mPropagateLightSheetKernel.setArgument("lsnz",
                                           mLightSheetEffectiveNormalVector.z);

    mPropagateLightSheetKernel.setArgument("zdepth", pZDepth);

    mPropagateLightSheetKernel.setArgument("zoffset",
                                           pZCenterOffset
                                                      - pZDepth / 2);

    mPropagateLightSheetKernel.setArgument("lambda",
                                           getLightWavelength());

    mPropagateLightSheetKernel.setArgument("intensity",
                                           getIntensity());

    mPropagateLightSheetKernel.setArgument("scatterconstant",
                                           getScatterConstant());

    mPropagateLightSheetKernel.setArgument("scatterloss",
                                           1.0f - getScatterLoss());

    mPropagateLightSheetKernel.setArgument("sigmamin", getSigmaMin());

    mPropagateLightSheetKernel.setArgument("sigmamax", getSigmaMax());

    mPropagateLightSheetKernel.setArgument("w0", getSpotSizeAtNeck());

    mPropagateLightSheetKernel.setArgument("lsheight",
                                           getLightSheetHeigth());
    
    updateTransformBuffer();
    mPropagateLightSheetKernel.setOptionalArgument("matrix",
                                                   getTransformMatrixBuffer());
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
