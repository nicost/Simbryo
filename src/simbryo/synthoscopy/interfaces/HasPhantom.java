package simbryo.synthoscopy.interfaces;

import simbryo.phantom.PhantomRendererInterface;

/**
 * Classes implementing this interface have an internal Phantom renderer that
 * can be set externally.
 *
 * @param <I> image type
 * @author royer
 */
public interface HasPhantom<I>
{
  /**
   * Sets the phantom renderer
   * @param pPhantomRenderer phantom renderer
   */
  void setPhantom(PhantomRendererInterface<I> pPhantomRenderer);
}
