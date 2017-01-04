package simbryo.phantom;

/**
 * Phantom Renderers implement this interface
 *
 * @author royer
 */
public interface PhantomRendererInterface
{

  /**
   * Returns the rendered stack width
   * 
   * @return
   */
  long getWidth();

  /**
   * Returns the rendered stack height
   * @return
   */
  long getHeight();

  /**
   * Returns the rendered stack depth
   * @return
   */
  long getDepth();

  /**
   * Clears the render cache for all planes
   */
  void clear();

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
