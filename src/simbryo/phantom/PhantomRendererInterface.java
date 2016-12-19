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
   * Return intensity
   * 
   * @return
   */
  float getIntensity();

  /**
   * Sets the phantom intensity
   * 
   * @param pIntensity
   */
  void setIntensity(float pIntensity);

}
