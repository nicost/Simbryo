package simbryo.phantom;

import coremem.ContiguousMemoryInterface;
import simbryo.dynamics.tissue.TissueDynamicsInterface;

/**
 * Phantom Renderers implement this interface
 *
 * @param <I>
 *          type of image used to store phantom
 * @author royer
 */
public interface PhantomRendererInterface<I>
{

  /**
   * Returns the tissue dynamics for this renderer.
   * 
   * @return tissue dynamics
   */
  TissueDynamicsInterface getTissue();

  /**
   * Returns the rendered image width
   * 
   * @return image width
   */
  long getWidth();

  /**
   * Returns the rendered image height
   * 
   * @return image height
   */
  long getHeight();

  /**
   * Returns the rendered image depth
   * 
   * @return image depth
   */
  long getDepth();

  /**
   * Clears the render cache for all planes
   */
  void clear();

  /**
   * Renders whole phantom stack. This method is not cache-aware, it will re-render already rendered planes
   */
  void render();

  /**
   * Renders a range of z plane indices. This method is not cache-aware, it will re-render already rendered planes
   * 
   * @param pZPlaneIndexBegin
   *          begin of z plane index range
   * @param pZPlaneIndexEnd
   *          end of z plane index range
   */
  void render(int pZPlaneIndexBegin, int pZPlaneIndexEnd);

  /**
   * Renders a single plane (possibly more if renderer cannot render single
   * planes but a whole chunk). This method is smart in the sense that it is
   * cache-aware - it will not render planes previously rendered.
   * 
   * @param pZPlaneIndex
   *          z plane index
   * @return true if plane rendered (not in cache)
   */
  boolean renderSmart(int pZPlaneIndex);

  /**
   * Renders a range of z plane indices. this method is aware of the cache and
   * only renders planes that have not been rendered previously (since last call
   * to clear)
   * 
   * @param pZPlaneIndexBegin
   *          begin of z plane index range
   * @param pZPlaneIndexEnd
   *          end of z plane index range
   * @return number of rendered planes
   */
  int renderSmart(int pZPlaneIndexBegin, int pZPlaneIndexEnd);

  /**
   * Invalidates a given rendered plane (will be rendered again if needed).
   * 
   * @param pZPlaneIndex
   */
  void invalidate(int pZPlaneIndex);

  /**
   * Returns the internal representation of the phantom image. The type depends
   * on the actual implementation.
   * 
   * @return phantom image
   */
  I getPhantomImage();

  /**
   * Copies rendered stack data into memory region.
   * 
   * @param pMemory
   *          memory
   * @param pBlocking
   *          true blocks call until copy done, false for asynch copy. Note:
   *          Some implementations might not be capable of asynch copy.
   */
  void copyTo(ContiguousMemoryInterface pMemory, boolean pBlocking);

  /**
   * Return phantom signal intensity
   * 
   * @return signal intensity
   */
  float getSignalIntensity();

  /**
   * Sets the phantom signal intensity
   * 
   * @param pSignalIntensity
   */
  void setSignalIntensity(float pSignalIntensity);

  /**
   * Return phantom noise over signal intensity ratio.
   * 
   * @return noise over signal ratio
   */
  float getNoiseOverSignalRatio();

  /**
   * Sets the phantom noise intensity
   * 
   * @param pNoiseIntensity
   */
  void setNoiseOverSignalRatio(float pNoiseIntensity);

}
