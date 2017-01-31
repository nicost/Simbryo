package simbryo.synthoscopy;

import java.util.ArrayList;

import simbryo.phantom.PhantomRendererInterface;
import simbryo.synthoscopy.camera.CameraRendererInterface;
import simbryo.synthoscopy.detection.DetectionOpticsInterface;
import simbryo.synthoscopy.illumination.IlluminationOpticsInterface;

/**
 * Optics renderer base class
 *
 * @param <I> type used to store and manipulate images during optics rendering
 * @author royer
 */
public abstract class OpticsRendererBase<I> implements
                                         OpticsRendererInterface<I>
{

  private PhantomRendererInterface<I> mPhantomRenderer;
  
  private float mLambda;

  private ArrayList<IlluminationOpticsInterface<I>> mIlluminationOpticsList =
                                                                         new ArrayList<>();
  private ArrayList<DetectionOpticsInterface<I>> mDetectionOpticsList =
                                                                   new ArrayList<>();
  private ArrayList<CameraRendererInterface<I>> mCameraModelList =
                                                           new ArrayList<>();

  /**
   * Instanciates a optics renderer base class given a phantom renderer
   * @param pPhantomRenderer phantom renderer
   */
  public OpticsRendererBase(PhantomRendererInterface<I> pPhantomRenderer)
  {
    super();
    mPhantomRenderer = pPhantomRenderer;
  }
  
  @Override
  public float getLightWavelength()
  {
    return mLambda;
  }

  @Override
  public void setLightLambda(float pLambda)
  {
    mLambda = pLambda;
  }

  @SafeVarargs
  @Override
  public final void addIlluminationOptics(IlluminationOpticsInterface<I>... pIlluminationOptics)
  {
    for (IlluminationOpticsInterface<I> lIlluminationOptics : pIlluminationOptics)
    {
      mIlluminationOpticsList.add(lIlluminationOptics);
    }
  }

  @SafeVarargs
  @Override
  public final void addDetectionOptics(DetectionOpticsInterface<I>... pDetectionOptics)
  {
    for (DetectionOpticsInterface<I> lDetectionOptics : pDetectionOptics)
    {
      mDetectionOpticsList.add(lDetectionOptics);
    }
  }

  @SafeVarargs
  @Override
  public final void addCameraModel(CameraRendererInterface<I>... pCameraModels)
  {
    for (CameraRendererInterface<I> lCameraModel : pCameraModels)
    {
      mCameraModelList.add(lCameraModel);
    }
  }

}
