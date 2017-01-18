package simbryo.synthoscopy;

import simbryo.phantom.PhantomRendererInterface;
import simbryo.synthoscopy.interfaces.HasPhantom;

/**
 * Optics base class providing common firlds and methods for optics related classes
 *
 * @param <I> image type used to store and process images
 * @author royer
 */
public class OpticsBase<I> implements HasPhantom<I>
{
  
  PhantomRendererInterface<I> mPhantomRenderer;
  
  @Override
  public void setPhantom(PhantomRendererInterface<I> pPhantomRenderer)
  {
    mPhantomRenderer = pPhantomRenderer;
  }
}
