package simbryo.synthoscopy;

import simbryo.phantom.PhantomRendererInterface;

/**
 * Optics Renderer
 *
 * @param <I> image type used to store and process images during the optics rendering
 * @author royer
 */
public class OpticsRenderer<I> extends OpticsRendererBase<I>
                                     implements
                                     OpticsRendererInterface<I>,
                                     AutoCloseable
{

  /**
   * Instanciates an optics renderer given a given phantom renderer.
   * @param pPhantomRenderer phantom renderer
   */
  public OpticsRenderer(PhantomRendererInterface<I> pPhantomRenderer)
  {
    super(pPhantomRenderer);
  }

  @Override
  public long getWidth()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getHeight()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public float getLightIntensity()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void setLightIntensity(float pLightIntensity)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void close() 
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void render(float pZ)
  {
    // TODO Auto-generated method stub
    
  }

}
