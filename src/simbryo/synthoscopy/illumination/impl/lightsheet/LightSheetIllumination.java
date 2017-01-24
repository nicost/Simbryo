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
  protected ClearCLKernel mInitializeLightSheetKernel,
      mPropagateLightSheetKernel;

  protected Vector3f mLightSheetPosition, mLightSheetAxisVector,
      mLightSheetNormalVector;

  private float mLightSheetThetaInRad, mLightSheetHeigth;

  /**
   * Instanciates a light sheet illumination optics class given a ClearCL
   * context, and light map image dimensions
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

    mLightSheetThetaInRad = 0.04f;
    mLightSheetHeigth = 0.5f;

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
    return (float) (getLightLambda()
                    / (Math.PI * getLightSheetThetaInRad()));
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

    lProgram.addDefine("MAXNEI", 0);

    lProgram.buildAndLog();

    mInitializeLightSheetKernel = lProgram.createKernel("initialize");
    mPropagateLightSheetKernel = lProgram.createKernel("propagate");
  }

  /**
   * Renders the light map for a given scattering phantom image and the position in z (normalized coordinates) of the light map.
   * 
   * @param pScatteringPhantomImage scattering phantom
   * @param pZCenterOffset z offset in normalized coordinates of the center plane of the lightmap stack relative to the phantom.
   * @return light map image (same as returned by getLightMapImage() )
   */
  public ClearCLImage render(ClearCLImage pScatteringPhantomImage,
                             float pZCenterOffset)
  {
    float lZDepth = (float) getDepth()
                    / pScatteringPhantomImage.getDepth();
    return render(pScatteringPhantomImage, pZCenterOffset, lZDepth);
  }

  @Override
  public ClearCLImage render(ClearCLImage pScatteringPhantomImage,
                             float pZCenterOffset,
                             float pZDepth)
  {
    initializeLightSheet(mBallisticLightImageA,
                         mBallisticLightImageB,
                         mScatteredLightImageA,
                         mScatteredLightImageB);

    setInvariantKernelParameters(pScatteringPhantomImage,
                                 pZCenterOffset,
                                 pZDepth);

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

    return super.render(pScatteringPhantomImage,
                        pZCenterOffset,
                        pZDepth);
  }

  private void setInvariantKernelParameters(ClearCLImage pScatteringPhantomImage,
                                            float pZCenterOffset,
                                            float pZDepth)
  {
    mPropagateLightSheetKernel.setGlobalOffsets(0, 0);
    mPropagateLightSheetKernel.setGlobalSizes(getHeight(),
                                              getDepth());

    mPropagateLightSheetKernel.setArgument("scattermap",
                                           pScatteringPhantomImage);
    mPropagateLightSheetKernel.setArgument("lightmap",
                                           mLightMapImage);
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
                                           getLightLambda());

    mPropagateLightSheetKernel.setArgument("intensity",
                                           getLightIntensity());

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
    mLightMapImage.notifyListenersOfChange(mContext.getDefaultQueue());
  }

  private void initializeLightSheet(ClearCLImage pBallisticLightImageA,
                                    ClearCLImage pBallisticLightImageB,
                                    ClearCLImage pScatteredLightImageA,
                                    ClearCLImage pScatteredLightImageB)
  {
    /*
    pBallisticLightImageA.fill(1.0f, false);
    pBallisticLightImageB.fill(1.0f, false);
    pScatteredLightImageA.fill(0.0f, true);
    pScatteredLightImageB.fill(0.0f, true);
    /**/

    mInitializeLightSheetKernel.setGlobalOffsets(0, 0);
    mInitializeLightSheetKernel.setGlobalSizes(getHeight(),
                                               getDepth());

    mInitializeLightSheetKernel.setArgument("ba",
                                            pBallisticLightImageA);
    mInitializeLightSheetKernel.setArgument("bb",
                                            pBallisticLightImageB);
    mInitializeLightSheetKernel.setArgument("sa",
                                            pScatteredLightImageA);
    mInitializeLightSheetKernel.setArgument("sb",
                                            pScatteredLightImageB);

    mInitializeLightSheetKernel.run(false);
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
