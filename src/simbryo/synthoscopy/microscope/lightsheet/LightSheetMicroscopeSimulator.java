package simbryo.synthoscopy.microscope.lightsheet;

import static java.lang.Math.min;
import static java.lang.Math.round;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;
import coremem.ContiguousMemoryInterface;
import simbryo.synthoscopy.camera.impl.SCMOSCameraRenderer;
import simbryo.synthoscopy.microscope.MicroscopeSimulatorBase;
import simbryo.synthoscopy.microscope.lightsheet.demo.jfx.LightSheetMicroscopeSimulatorViewer;
import simbryo.synthoscopy.microscope.parameters.CameraParameter;
import simbryo.synthoscopy.microscope.parameters.DetectionParameter;
import simbryo.synthoscopy.microscope.parameters.IlluminationParameter;
import simbryo.synthoscopy.microscope.parameters.ParameterInterface;
import simbryo.synthoscopy.microscope.parameters.PhantomParameter;
import simbryo.synthoscopy.microscope.parameters.StageParameter;
import simbryo.synthoscopy.optics.detection.impl.widefield.WideFieldDetectionOptics;
import simbryo.synthoscopy.optics.illumination.impl.lightsheet.LightSheetIllumination;
import simbryo.util.geom.GeometryUtils;

/**
 * Light sheet microscope simulator
 *
 * @author royer
 */
public class LightSheetMicroscopeSimulator extends
                                           MicroscopeSimulatorBase<ClearCLImage>
                                           implements AutoCloseable
{

  private static final float cDepthOfIlluminationInNormUnits = 1f;

  private static final int cLightMapScaleFactor = 4;

  private ClearCLContext mContext;

  private ArrayList<LightSheetIllumination> mLightSheetIlluminationList =
                                                                        new ArrayList<>();
  private ArrayList<WideFieldDetectionOptics> mWideFieldDetectionOpticsList =
                                                                            new ArrayList<>();
  private ArrayList<SCMOSCameraRenderer> mCameraRendererList =
                                                             new ArrayList<>();

  private int[] mMainPhantomDimensions;

  private ConcurrentHashMap<ParameterInterface<Void>, ClearCLImage> mPhantomMap =
                                                                                new ConcurrentHashMap<>();

  private ConcurrentHashMap<ParameterInterface<Number>, ConcurrentHashMap<Integer, Number>> mParametersMap =
                                                                                                           new ConcurrentHashMap<>();

  private ConcurrentHashMap<Integer, Matrix4f> mDetectionTransformationMatrixMap =
                                                                                 new ConcurrentHashMap<>();

  /**
   * Instanciates a light sheet microscope simulator given a ClearCL context
   * 
   * @param pContext
   *          ClearCL context
   * @param pMainPhantomDimensions
   *          main phantom dimensions.
   */
  public LightSheetMicroscopeSimulator(ClearCLContext pContext,
                                       int... pMainPhantomDimensions)
  {
    mContext = pContext;
    mMainPhantomDimensions = pMainPhantomDimensions;
  }

  /**
   * Returns the main phantom width
   * 
   * @return main phantom width
   */
  public int getWidth()
  {
    return mMainPhantomDimensions[0];
  }

  /**
   * Returns the main phantom height
   * 
   * @return main phantom height
   */
  public int getHeight()
  {
    return mMainPhantomDimensions[1];
  }

  /**
   * Returns the main phantom depth
   * 
   * @return main phantom depth
   */
  public int getDepth()
  {
    return mMainPhantomDimensions[2];
  }

  /**
   * Adds a lightsheet with given axis and normal vectors.
   * 
   * @param pAxisVector
   *          axis vector
   * @param pNormalVector
   *          normal vector
   * @return lightsheet illumination
   */
  public LightSheetIllumination addLightSheet(Vector3f pAxisVector,
                                              Vector3f pNormalVector)
  {
    try
    {
      int lWidth = getWidth() / cLightMapScaleFactor;
      int lHeight = getHeight() / cLightMapScaleFactor;
      int lDepth =
                 min(getDepth(),
                     closestOddInteger(getDepth()
                                       * cDepthOfIlluminationInNormUnits));

      LightSheetIllumination lLightSheetIllumination =
                                                     new LightSheetIllumination(mContext,
                                                                                lWidth,
                                                                                lHeight,
                                                                                lDepth);

      lLightSheetIllumination.setLightSheetAxisVector(pAxisVector);
      lLightSheetIllumination.setLightSheetNormalVector(pNormalVector);

      mLightSheetIlluminationList.add(lLightSheetIllumination);
      return lLightSheetIllumination;
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  /**
   * Adds detection path. This includes widefield detection optics and a sCMOS
   * camera.
   * 
   * @param pDetectionTransformMatrix
   *          detection transform matrix
   * @param pDownUpVector
   *          updown vector
   * @param pMaxCameraWidth
   *          max camera width
   * @param pMaxCameraHeight
   *          max camera height
   */
  public void addDetectionPath(Matrix4f pDetectionTransformMatrix,
                               Vector3f pDownUpVector,
                               int pMaxCameraWidth,
                               int pMaxCameraHeight)
  {
    try
    {
      mDetectionTransformationMatrixMap.put(mWideFieldDetectionOpticsList.size(),
                                            pDetectionTransformMatrix);

      WideFieldDetectionOptics lWideFieldDetectionOptics =
                                                         new WideFieldDetectionOptics(mContext,
                                                                                      getWidth(),
                                                                                      getHeight());

      mWideFieldDetectionOpticsList.add(lWideFieldDetectionOptics);

      SCMOSCameraRenderer lSCMOSCameraRenderer =
                                               new SCMOSCameraRenderer(mContext,
                                                                       pMaxCameraWidth,
                                                                       pMaxCameraHeight);
      lSCMOSCameraRenderer.setDetectionDownUpVector(pDownUpVector);
      lWideFieldDetectionOptics.addUpdateListener(lSCMOSCameraRenderer);

      mCameraRendererList.add(lSCMOSCameraRenderer);
    }
    catch (IOException e)
    {
      throw new RuntimeException();
    }
  }

  /**
   * Must be called after all lightsheets and detection arms have been added.
   */
  public void buildMicroscope()
  {
    for (LightSheetIllumination lLightSheetIllumination : mLightSheetIlluminationList)
      for (WideFieldDetectionOptics lWideFieldDetectionOptics : mWideFieldDetectionOpticsList)
      {
        lLightSheetIllumination.addUpdateListener(lWideFieldDetectionOptics);
      }
  }

  /**
   * Returns the number of lightsheets
   * 
   * @return number of lightsheets
   */
  public int getNumberOfLightSheets()
  {
    return mLightSheetIlluminationList.size();
  }

  /**
   * Returns the number of detection paths
   * 
   * @return number of detection paths
   */
  public int getNumberOfDetectionPaths()
  {
    return mWideFieldDetectionOpticsList.size();
  }

  /**
   * Returns lightsheet for index
   * 
   * @param pIndex
   *          index
   * @return lightsheet
   */
  public LightSheetIllumination getLightSheet(int pIndex)
  {
    return mLightSheetIlluminationList.get(pIndex);
  }

  /**
   * Returns detection optics for index
   * 
   * @param pIndex
   *          index
   * @return detection optics
   */
  public WideFieldDetectionOptics getDetectionOptics(int pIndex)
  {
    return mWideFieldDetectionOpticsList.get(pIndex);
  }

  /**
   * Returns camera for index
   * 
   * @param pIndex
   *          index
   * @return camera
   */
  public SCMOSCameraRenderer getCamera(int pIndex)
  {
    return mCameraRendererList.get(pIndex);
  }

  /**
   * Sets a given Phanton.
   * 
   * @param pParameter
   *          parameter
   * @param pPhantom
   *          phantom
   */
  public void setPhantom(ParameterInterface<Void> pParameter,
                         ClearCLImage pPhantom)
  {
    mPhantomMap.put(pParameter, pPhantom);
  }

  /**
   * Sets a parameter value for a given index
   * 
   * @param pParameter
   *          parameter name
   * @param pIndex
   *          index
   * @param pValue
   *          value
   */
  public void setNumberParameter(ParameterInterface<Number> pParameter,
                                 int pIndex,
                                 Number pValue)
  {
    ConcurrentHashMap<Integer, Number> lConcurrentHashMap =
                                                          mParametersMap.get(pParameter);
    if (lConcurrentHashMap == null)
    {
      lConcurrentHashMap = new ConcurrentHashMap<Integer, Number>();
      mParametersMap.put(pParameter, lConcurrentHashMap);
    }

    lConcurrentHashMap.put(pIndex, pValue);
  }

  /**
   * Returns value for a given parameter and index
   * 
   * @param pParameter
   *          parameter name
   * @param pIndex
   *          index
   * @return value
   */
  public Number getNumberParameter(ParameterInterface<Number> pParameter,
                                   int pIndex)
  {
    ConcurrentHashMap<Integer, Number> lConcurrentHashMap =
                                                          mParametersMap.get(pParameter);
    if (lConcurrentHashMap == null)
      return pParameter.getDefaultValue();

    Number lNumber = lConcurrentHashMap.get(pIndex);
    if (lNumber == null)
      return pParameter.getDefaultValue();
    return lNumber;
  }

  /**
   * Returns value for a given parameter and index. If the parameter is not
   * defined, a overriding default value is given which is used instead of the
   * parameter's default value.
   * 
   * @param pParameter
   *          parameter name
   * @param pIndex
   *          index
   * @param pDefaultOverideValue
   *          default overide value
   * @return value
   */
  public Number getNumberParameter(ParameterInterface<Number> pParameter,
                                   int pIndex,
                                   Number pDefaultOverideValue)
  {
    ConcurrentHashMap<Integer, Number> lConcurrentHashMap =
                                                          mParametersMap.get(pParameter);
    if (lConcurrentHashMap == null)
      return pDefaultOverideValue;

    Number lNumber = lConcurrentHashMap.get(pIndex);

    if (lNumber == null)
      return pDefaultOverideValue;
    return lNumber;
  }

  /**
   * Returns a phantom image for a given parameter
   * 
   * @param pParameter
   *          phantom parameter name
   * @return phantom image
   */
  public ClearCLImage getPhantomParameter(ParameterInterface<Void> pParameter)
  {
    return mPhantomMap.get(pParameter);
  }

  private void applyParametersForLightSheet(int pLightSheetIndex,
                                            int pDetectionPathIndex)
  {

    LightSheetIllumination lLightSheetIllumination =
                                                   mLightSheetIlluminationList.get(pLightSheetIndex);

    lLightSheetIllumination.setDetectionTransformMatrix(getDetectionTransformMatrix(pDetectionPathIndex));
    lLightSheetIllumination.setPhantomTransformMatrix(getStageTransformMatrix());

    lLightSheetIllumination.setScatteringPhantom(getPhantomParameter(PhantomParameter.Scattering));

    float lIntensity =
                     getNumberParameter(IlluminationParameter.Intensity,
                                        pLightSheetIndex).floatValue();

    float lWaveLength =
                      getNumberParameter(IlluminationParameter.Wavelength,
                                         pLightSheetIndex).floatValue();

    float xl = getNumberParameter(IlluminationParameter.X,
                                  pLightSheetIndex).floatValue();
    float yl = getNumberParameter(IlluminationParameter.Y,
                                  pLightSheetIndex).floatValue();
    float zl = getNumberParameter(IlluminationParameter.Z,
                                  pLightSheetIndex).floatValue();

    float height = getNumberParameter(IlluminationParameter.Height,
                                      pLightSheetIndex).floatValue();

    float alpha = getNumberParameter(IlluminationParameter.Alpha,
                                     pLightSheetIndex).floatValue();
    float beta = getNumberParameter(IlluminationParameter.Beta,
                                    pLightSheetIndex).floatValue();
    float gamma = getNumberParameter(IlluminationParameter.Gamma,
                                     pLightSheetIndex).floatValue();
    float theta = getNumberParameter(IlluminationParameter.Theta,
                                     pLightSheetIndex).floatValue();

    lLightSheetIllumination.setIntensity(lIntensity);
    lLightSheetIllumination.setLightWavelength(lWaveLength);
    lLightSheetIllumination.setLightSheetPosition(xl, yl, zl);
    lLightSheetIllumination.setLightSheetHeigth(height);
    lLightSheetIllumination.setOrientationWithAnglesInDegrees(alpha,
                                                              beta,
                                                              gamma);
    lLightSheetIllumination.setLightSheetThetaInDeg(theta);

  }

  private void applyParametersForDetectionPath(int pDetectionPathIndex,
                                               ClearCLImage pLightMapImage)
  {
    WideFieldDetectionOptics lWideFieldDetectionOptics =
                                                       mWideFieldDetectionOpticsList.get(pDetectionPathIndex);
    SCMOSCameraRenderer lSCMOSCameraRenderer =
                                             mCameraRendererList.get(pDetectionPathIndex);

    ClearCLImage lFluorescencePhantomImage =
                                           getPhantomParameter(PhantomParameter.Fluorescence);
    ClearCLImage lScatteringPhantomImage =
                                         getPhantomParameter(PhantomParameter.Scattering);

    float lIntensity =
                     getNumberParameter(DetectionParameter.Intensity,
                                        pDetectionPathIndex).floatValue();

    float lWaveLength =
                      getNumberParameter(DetectionParameter.Wavelength,
                                         pDetectionPathIndex).floatValue();

    float lFocusZ =
                  getNumberParameter(DetectionParameter.FocusZ,
                                     pDetectionPathIndex).floatValue();

    long lDetectionImageWidth = lFluorescencePhantomImage.getWidth();
    long lDetectionImageHeight =
                               lFluorescencePhantomImage.getHeight();

    int lROIWidth =
                  getNumberParameter(CameraParameter.ROIWidth,
                                     pDetectionPathIndex,
                                     lSCMOSCameraRenderer.getMaxWidth()).intValue();
    int lROIHeight =
                   getNumberParameter(CameraParameter.ROIHeight,
                                      pDetectionPathIndex,
                                      lSCMOSCameraRenderer.getMaxHeight()).intValue();

    lWideFieldDetectionOptics.setFluorescencePhantomImage(lFluorescencePhantomImage);
    lWideFieldDetectionOptics.setScatteringPhantomImage(lScatteringPhantomImage);
    lWideFieldDetectionOptics.setLightMapImage(pLightMapImage);

    lWideFieldDetectionOptics.setIntensity(lIntensity);
    lWideFieldDetectionOptics.setLightWavelength(lWaveLength);
    lWideFieldDetectionOptics.setZFocusPosition(lFocusZ);
    lWideFieldDetectionOptics.setWidth(lDetectionImageWidth);
    lWideFieldDetectionOptics.setHeight(lDetectionImageHeight);

    lWideFieldDetectionOptics.setPhantomTransformMatrix(getStageAndDetectionTransformMatrix(pDetectionPathIndex));

    lSCMOSCameraRenderer.setDetectionImage(lWideFieldDetectionOptics.getImage());

    lSCMOSCameraRenderer.setCenteredROI(lROIWidth, lROIHeight);

  }

  private Matrix4f getStageAndDetectionTransformMatrix(int pDetectionPathIndex)
  {
    Matrix4f lCombinedTransformMatrix =
                                      GeometryUtils.multiply(getStageTransformMatrix(),
                                                             getDetectionTransformMatrix(pDetectionPathIndex));
    return lCombinedTransformMatrix;
  }

  private Matrix4f getDetectionTransformMatrix(int pDetectionPathIndex)
  {
    Matrix4f lDetectionTransformationMatrix =
                                            mDetectionTransformationMatrixMap.get(pDetectionPathIndex);
    return new Matrix4f(lDetectionTransformationMatrix);
  }

  private Matrix4f getStageTransformMatrix()
  {
    float lStageX = getNumberParameter(StageParameter.StageX,
                                       0,
                                       0).floatValue();
    float lStageY = getNumberParameter(StageParameter.StageY,
                                       0,
                                       0).floatValue();
    float lStageZ = getNumberParameter(StageParameter.StageZ,
                                       0,
                                       0).floatValue();

    float lStageRX = getNumberParameter(StageParameter.StageRX,
                                        0,
                                        0).floatValue();

    float lStageRY = getNumberParameter(StageParameter.StageRY,
                                        0,
                                        0).floatValue();
    float lStageRZ = getNumberParameter(StageParameter.StageRZ,
                                        0,
                                        0).floatValue();

    Vector3f lCenter = new Vector3f(0.5f, 0.5f, 0.5f);

    Matrix4f lMatrixRX =
                       GeometryUtils.rotX((float) Math.toRadians(lStageRX),
                                          lCenter);
    Matrix4f lMatrixRY =
                       GeometryUtils.rotY((float) Math.toRadians(lStageRY),
                                          lCenter);
    Matrix4f lMatrixRZ =
                       GeometryUtils.rotZ((float) Math.toRadians(lStageRZ),
                                          lCenter);

    Matrix4f lMatrix = GeometryUtils.multiply(lMatrixRX,
                                              lMatrixRY,
                                              lMatrixRZ);

    GeometryUtils.addTranslation(lMatrix, lStageX, lStageY, lStageZ);

    return lMatrix;
  }

  /**
   * Renders all that needs to be rendered to obtain the camera images
   * 
   * @param pWaitToFinish
   *          true -> wait for computation to finish
   */
  public void render(boolean pWaitToFinish)
  {
    int lNumberOfLightSheets = mLightSheetIlluminationList.size();
    int lNumberOfDetectionPath = mWideFieldDetectionOpticsList.size();

    for (int d = 0; d < lNumberOfDetectionPath; d++)
    {
      WideFieldDetectionOptics lWideFieldDetectionOptics =
                                                         mWideFieldDetectionOpticsList.get(d);
      SCMOSCameraRenderer lSCMOSCameraRenderer =
                                               mCameraRendererList.get(d);

      ClearCLImage lCurrentLightMap = null;
      for (int l = 0; l < lNumberOfLightSheets; l++)
      {
        LightSheetIllumination lLightSheetIllumination =
                                                       mLightSheetIlluminationList.get(l);
        applyParametersForLightSheet(l, d);
        lLightSheetIllumination.setInputImage(lCurrentLightMap);
        ElapsedTime.measure("renderlightsheet",
                            () -> lLightSheetIllumination.render(false));

        lCurrentLightMap = lLightSheetIllumination.getImage();

      }

      applyParametersForDetectionPath(d, lCurrentLightMap);

      ElapsedTime.measure("renderdetection",
                          () -> lWideFieldDetectionOptics.render(false));

      ElapsedTime.measure("rendercameraimage",
                          () -> lSCMOSCameraRenderer.render(pWaitToFinish));/**/

      lWideFieldDetectionOptics.clearUpdate();
      lSCMOSCameraRenderer.clearUpdate();
    }

  }

  /**
   * Returns camera image
   * 
   * @param pIndex
   *          camera index
   * @return camera image
   */
  public ClearCLImage getCameraImage(int pIndex)
  {
    return mCameraRendererList.get(pIndex).getImage();
  }

  /**
   * Opens viewer for the camera image of given index.
   * 
   * @param pIndex
   *          camera/detection path index
   * @return viewer
   */
  public ClearCLImageViewer openViewerForCameraImage(int pIndex)
  {
    final ClearCLImageViewer lViewImage =
                                        ClearCLImageViewer.view(getCameraImage(pIndex),
                                                                "CameraImage" + pIndex);
    return lViewImage;
  }

  /**
   * Opens viewer for the lightmap of given index.
   * 
   * @param pIndex
   *          camera/detection path index
   * @return viewer
   */
  public ClearCLImageViewer openViewerForLightMap(int pIndex)
  {
    return getLightSheet(pIndex).openViewer();
  }

  /**
   * Opens viewer for controls to the microscope parameters. Must be called
   * after the simulator is fully initialized (after all calls that add
   * lightsheets and detection paths...)
   * 
   * @return viewer
   */
  public LightSheetMicroscopeSimulatorViewer openViewerForControls()
  {
    LightSheetMicroscopeSimulatorViewer lViewer =
                                                new LightSheetMicroscopeSimulatorViewer(this,
                                                                                        "LightSheetSimulator");

    return lViewer;
  }

  /**
   * Copies the contents of the camera image to a contiguous memory object
   * 
   * @param pIndex
   *          camera/detection path index
   * @param pContiguousMemory
   *          memory object
   * @param pBlocking
   *          true -> blocking, false otherwise
   */
  public void copyTo(int pIndex,
                     ContiguousMemoryInterface pContiguousMemory,
                     boolean pBlocking)
  {
    getCameraImage(pIndex).writeTo(pContiguousMemory, pBlocking);
  }

  private int closestOddInteger(float pValue)
  {
    return round((pValue - 1) / 2) * 2 + 1;
  }

  @Override
  public void close() throws Exception
  {
    for (LightSheetIllumination lLightSheetIllumination : mLightSheetIlluminationList)
    {
      lLightSheetIllumination.close();
    }

    for (WideFieldDetectionOptics lWideFieldDetectionOptics : mWideFieldDetectionOpticsList)
    {
      lWideFieldDetectionOptics.close();
    }

    for (SCMOSCameraRenderer lScmosCameraRenderer : mCameraRendererList)
    {
      lScmosCameraRenderer.close();
    }

  }

}
