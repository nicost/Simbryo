package simbryo.phantom.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import coremem.ContiguousMemoryInterface;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.phantom.PhantomRendererBase;

/**
 * Basic tests for Renderer base class
 *
 * @author royer
 */
public class RendererBaseTests
{

  /**
   * Basic test checking the smart cache-aware rendering.
   */
  @Test
  public void testSmartness()
  {
    Drosophila lDrosophila = new Drosophila(64,64,16);
    PhantomRendererBase<Object> lRenderer = new PhantomRendererBase<Object>(lDrosophila,
                                              512,
                                              512,
                                              128)
    {
      @Override
      public void render(int pZPlaneIndexBegin, int pZPlaneIndexEnd)
      {
        //do nothing
      }

      @Override
      public void render()
      {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void copyTo(ContiguousMemoryInterface pMemory,
                         boolean pBlocking)
      {
        // TODO Auto-generated method stub
        
      }

      @Override
      public Object getPhantomImage()
      {
        // TODO Auto-generated method stub
        return null;
      }
    };


    assertEquals(75, lRenderer.renderSmart(0, 75));
    assertEquals(25, lRenderer.renderSmart(25, 100));

  }

}
