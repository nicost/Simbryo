package simbryo.synthoscopy.microscope.lightsheet;

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

/**
 * Light sheet microscope simulator
 *
 * @author royer
 */
public class LightSheetMicroscopeSimulator extends
                                           MicroscopeSimulatorBase<ClearCLImage>
                                           implements AutoCloseable
{

  private static final float cDepthOfIlluminationInNormUnits = 0.05f;

  private static final int cLightMapScaleFactor = 4;

  private ClearCLContext mContext;

  protected ConcurrentHashMap<ParameterInterface<Void>, ClearCLImage> mPhantomMap =
                                                                                  new ConcurrentHashMap<>();

  protected ArrayList<LightSheetIllumination> mLightSheetIlluminationList =
                                                                          new ArrayList<>();
  protected ArrayList<WideFieldDetectionOptics> mWideFieldDetectionOpticsList =
                                                                              new ArrayList<>();
  protected ArrayList<SCMOSCameraRenderer> mCameraRendererList =
                                                               new ArrayList<>();

  private int[] mMainPhantomDimensions;

  private ConcurrentHashMap<ParameterInterface<Number>, ConcurrentHashMap<Integer, Number>> mParametersMap =
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
                 closestOddInteger(getDepth()
                                   * cDepthOfIlluminationInNormUnits);

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
   * camera
   * 
   * @param pDownUpVector
   *          updown vector
   * @param pMaxCameraWidth
   *          max camera width
   * @param pMaxCameraHeight
   *          max camera height
   */
  public void addDetectionPath(Vector3f pDownUpVector,
                               int pMaxCameraWidth,
                               int pMaxCameraHeight)
  {
    try
    {
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

      mCameraRendererList.add(lSCMOSCameraRenderer);
    }
    catch (IOException e)
    {
      throw new RuntimeException();
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

    lLightSheetIllumination.setDefaultZDepth(getPhantomParameter(PhantomParameter.Scattering));
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

    float lDetectionZ =
                      getNumberParameter(DetectionParameter.FocusZ,
                                         pDetectionPathIndex).floatValue();

    lLightSheetIllumination.setIntensity(lIntensity);
    lLightSheetIllumination.setLightWavelength(lWaveLength);
    lLightSheetIllumination.setLightSheetPosition(xl, yl, zl);
    lLightSheetIllumination.setZCenterOffset(lDetectionZ);
    lLightSheetIllumination.setLightSheetHeigth(height);
    lLightSheetIllumination.setOrientationWithAnglesInDegrees(alpha,
                                                              beta,
                                                              gamma);
    lLightSheetIllumination.setLightSheetThetaInDeg(theta);

    lLightSheetIllumination.setTransformMatrix(getStageTransformMatrix());

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

    lWideFieldDetectionOptics.setDefaultZDepth(lFluorescencePhantomImage,
                                               pLightMapImage);

    lWideFieldDetectionOptics.setFluorescencePhantomImage(lFluorescencePhantomImage);
    lWideFieldDetectionOptics.setScatteringPhantomImage(lScatteringPhantomImage);
    lWideFieldDetectionOptics.setLightMapImage(pLightMapImage);

    lWideFieldDetectionOptics.setIntensity(lIntensity);
    lWideFieldDetectionOptics.setLightWavelength(lWaveLength);
    lWideFieldDetectionOptics.setZFocusPosition(lFocusZ);
    lWideFieldDetectionOptics.setWidth(lDetectionImageWidth);
    lWideFieldDetectionOptics.setHeight(lDetectionImageHeight);

    lWideFieldDetectionOptics.setTransformMatrix(getStageTransformMatrix());

    lSCMOSCameraRenderer.setDetectionImage(lWideFieldDetectionOptics.getImage());

    lSCMOSCameraRenderer.setCenteredROI(lROIWidth, lROIHeight);

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
    float lStageR = getNumberParameter(StageParameter.StageR,
                                       0,
                                       0).floatValue();

    Matrix4f lMatrix = new Matrix4f();
    lMatrix.setIdentity();
    lMatrix.rotY((float) Math.toRadians(lStageR));
    lMatrix.setTranslation(new Vector3f(lStageX, lStageY, lStageZ));

    return lMatrix;
  }

  /**
   * Renders all that needs to be rendered to obtain the camera images
   */
  public void render()
  {
    int lNumberOfLightSheets = mLightSheetIlluminationList.size();
    int lNumberOfDetectionPath = mWideFieldDetectionOpticsList.size();

    for (int d = 0; d < lNumberOfDetectionPath; d++)
    {

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

      WideFieldDetectionOptics lWideFieldDetectionOptics =
                                                         mWideFieldDetectionOpticsList.get(d);
      SCMOSCameraRenderer lSCMOSCameraRenderer =
                                               mCameraRendererList.get(d);

      applyParametersForDetectionPath(d, lCurrentLightMap);

      ElapsedTime.measure("renderdetection",
                          () -> lWideFieldDetectionOptics.render(false));

      ElapsedTime.measure("rendercameraimage",
                          () -> lSCMOSCameraRenderer.render(true));/**/
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
