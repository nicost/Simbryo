package simbryo.phantom;

import coremem.ContiguousMemoryInterface;
import simbryo.dynamics.tissue.TissueDynamicsInterface;

/**
 * Phantom Renderers implement this interface
 *
 * @author royer
 */
public interface PhantomRendererInterface
{

  /**
   * Returns the tissue dynamics for this renderer.
   * 
   * @return
   */
  TissueDynamicsInterface getTissue();

  /**
   * Returns the rendered stack width
   * 
   * @return
   */
  long getWidth();

  /**
   * Returns the rendered stack height
   * 
   * @return
   */
  long getHeight();

  /**
   * Returns the rendered stack depth
   * 
   * @return
   */
  long getDepth();

  /**
   * Clears the render cache for all planes
   */
  void clear();

  /**
   * Renders whole stack.
   */
  void render();

  /**
   * @param pZPlaneIndex
   * @return
   */
  boolean render(int pZPlaneIndex);

  /**
   * @param pZPlaneIndexBegin
   * @param pZPlaneIndexEnd
   */
  void render(int pZPlaneIndexBegin, int pZPlaneIndexEnd);

  /**
   * @param pZPlaneIndexBegin
   * @param pZPlaneIndexEnd
   * @return
   */
  int renderSmart(int pZPlaneIndexBegin, int pZPlaneIndexEnd);

  /**
   * Invalidates a given rendered plane (will be rendered again if needed).
   * 
   * @param pZPlaneIndex
   */
  void invalidate(int pZPlaneIndex);

  /**
   * Copies rendered stack data into memory region.
   * 
   * @param pMemory
   *          memory
   * @param pBlocking
   *          true blocks call until copy done, false for asynch copy. Note: Some
   *          implementations might not be capable of asynch copy.
   */
  void copyTo(ContiguousMemoryInterface pMemory, boolean pBlocking);

  /**
   * Return phantom signal intensity
   * 
   * @return
   */
  float getSignalIntensity();

  /**
   * Sets the phantom signal intensity
   * 
   * @param pSignalIntensity
   */
  void setSignalIntensity(float pSignalIntensity);

  /**
   * Return phantom noise intensity
   * 
   * @return
   */
  float getNoiseOverSignalRatio();

  /**
   * Sets the phantom noise intensity
   * 
   * @param pNoiseIntensity
   */
  void setNoiseOverSignalRatio(float pNoiseIntensity);

}
