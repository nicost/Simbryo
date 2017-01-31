package simbryo.synthoscopy;

import simbryo.synthoscopy.camera.CameraRendererInterface;
import simbryo.synthoscopy.detection.DetectionOpticsInterface;
import simbryo.synthoscopy.illumination.IlluminationOpticsInterface;
import simbryo.synthoscopy.interfaces.ImageWidthHeightInterface;
import simbryo.synthoscopy.interfaces.LightIntensityInterface;

/**
 * Microscope Optics simulator
 *
 * @param <I> image type
 * @author royer
 */
public interface OpticsRendererInterface<I> extends ImageWidthHeightInterface, LightIntensityInterface
{

  /**
   * Adds illumination optics
   * @param pIlluminationOptics illumination optics
   */
  @SuppressWarnings("unchecked")
  void addIlluminationOptics(IlluminationOpticsInterface<I>... pIlluminationOptics);
  
  /**
   * Adds detection optics
   * @param pDetectionOptics detection optics
   */
  @SuppressWarnings("unchecked")
  void addDetectionOptics(DetectionOpticsInterface<I>... pDetectionOptics);
  
  /**
   * Adds a camera model
   * @param pCameraModel camera model
   */
  @SuppressWarnings("unchecked")
  void addCameraModel(CameraRendererInterface<I>... pCameraModel);
  

  /**
   * Renders plane at a gien coordinate 
   * @param pZ z coordinate of plane
   */
  void render(float pZ);
 
  
  
  
}
