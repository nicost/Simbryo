package simbryo.phantom.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import simbryo.dynamics.tissue.zoo.Drosophila;
import simbryo.phantom.PhantomRendererBase;

public class RendererBaseTests
{

  @Test
  public void testSmartness()
  {
    Drosophila lDrosophila = new Drosophila(64,64,16);
    PhantomRendererBase lRenderer = new PhantomRendererBase(lDrosophila,
                                              512,
                                              512,
                                              128)
    {
      @Override
      public void render(int pZPlaneIndexBegin, int pZPlaneIndexEnd)
      {
        //do nothing
      }
    };


    assertEquals(75, lRenderer.renderSmart(0, 75));
    assertEquals(25, lRenderer.renderSmart(25, 100));

  }

}
