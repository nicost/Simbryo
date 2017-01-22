package simbryo.synthoscopy.illumination.impl.lightsheet;

import java.io.IOException;

import javax.vecmath.Vector3f;

import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.enums.ImageChannelDataType;
import simbryo.phantom.PhantomRendererInterface;
import simbryo.phantom.fluo.HistoneFluorescence;
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
      mPropagateLightSheetKernel, mDiffuseYKernel, mDiffuseZKernel;

  protected Vector3f mLightSheetPosition, mLightSheetOrientation;

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

    mLightSheetThetaInRad = 0.006f;
    mLightSheetHeigth = 1.0f;
    
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
    mLightSheetOrientation = new Vector3f(1.0f, 0, 0);

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
   * @param pLightSheetTheta divergence in degrees
   */
  public void setLightSheetTheta(float pLightSheetTheta)
  {
    mLightSheetThetaInRad = (float) Math.toRadians(pLightSheetTheta);
  }

  /**
   * Returns lightsheet height in normalized units.
   * @return light sheet height in normalized units.
   */
  public float getLightSheetHeigth()
  {
    return mLightSheetHeigth;
  }

  /**
   * Sets the lightsheet height in normalized units.
   * @param pLightSheetHeigth light sheet height in normalized units.
   */
  public void setLightSheetHeigth(float pLightSheetHeigth)
  {
    mLightSheetHeigth = pLightSheetHeigth;
  }

  /**
   * Sets lightsheet position in normalized units.
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
   * Sets lightsheet orientation vector
   * 
   * @param pX
   *          x coordinate
   * @param pY
   *          y coordinate
   * @param pZ
   *          z coordinate
   */
  public void setLightSheetOrientation(float pX, float pY, float pZ)
  {
    mLightSheetOrientation.x = pX;
    mLightSheetOrientation.y = pY;
    mLightSheetOrientation.z = pZ;
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
    mDiffuseYKernel = lProgram.createKernel("diffuseY");
    mDiffuseZKernel = lProgram.createKernel("diffuseZ");

  }

  @Override
  public ClearCLImage render(ClearCLImage pScatteringPhantomImage,
                             int pZCenterPlaneIndex)
  {
    initializeLightSheet(mBallisticLightImageA,
                         mBallisticLightImageB,
                         mScatteredLightImageA,
                         mScatteredLightImageB);

    setInvariantKernelParameters(pZCenterPlaneIndex,
                                 pScatteringPhantomImage);

    for (int i = 0; i < getWidth(); i++)
    {
      int x;

      if (mLightSheetOrientation.x > 0)
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

    return super.render(pScatteringPhantomImage, pZCenterPlaneIndex);
  }

  private void setInvariantKernelParameters(int pZCenterPlaneIndex,
                                            ClearCLImage pScatteringPhantomImage)
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

    mPropagateLightSheetKernel.setArgument("lsox",
                                           mLightSheetOrientation.x);
    mPropagateLightSheetKernel.setArgument("lsoy",
                                           mLightSheetOrientation.y);
    mPropagateLightSheetKernel.setArgument("lsoz",
                                           mLightSheetOrientation.z);

    mPropagateLightSheetKernel.setArgument("zoffset",
                                           pZCenterPlaneIndex
                                                      - getDepth()
                                                        / 2);

    float lLambdaInVoxels = getLightLambda()*Math.max(getWidth(), getHeight());
    
    mPropagateLightSheetKernel.setArgument("lambda",
                                           lLambdaInVoxels);

    mPropagateLightSheetKernel.setArgument("intensity",
                                           getLightIntensity());

    mPropagateLightSheetKernel.setArgument("theta",
                                           getLightSheetThetaInRad());

    mPropagateLightSheetKernel.setArgument("height",
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

  private void diffuse(ClearCLImage pScatteredLightImageA,
                       ClearCLImage pScatteredLightImageB,
                       ClearCLImage pScatteredLightImageC,
                       int pRadius)
  {
    mDiffuseYKernel.setGlobalOffsets(0, 0);
    mDiffuseYKernel.setGlobalSizes(getHeight(), getDepth());

    mDiffuseYKernel.setArgument("si", pScatteredLightImageA);
    mDiffuseYKernel.setArgument("so", pScatteredLightImageB);
    mDiffuseYKernel.setArgument("radius", pRadius);

    mDiffuseZKernel.setGlobalOffsets(0, 0);
    mDiffuseZKernel.setGlobalSizes(getHeight(), getDepth());

    mDiffuseZKernel.setArgument("si", pScatteredLightImageB);
    mDiffuseZKernel.setArgument("so", pScatteredLightImageA);
    mDiffuseZKernel.setArgument("radius", pRadius);

    mDiffuseYKernel.run(false);
    mDiffuseZKernel.run(true);
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

    mInitializeLightSheetKernel.run(true);
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
