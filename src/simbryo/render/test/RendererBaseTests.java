package simbryo.render.test;

import static org.junit.Assert.*;

import org.junit.Test;

import clearcl.viewer.ClearCLImageViewer;
import simbryo.embryo.zoo.Drosophila;
import simbryo.render.RendererBase;

public class RendererBaseTests
{

  @Test
  public void testSmartness()
  {
    Drosophila lDrosophila = new Drosophila();
    RendererBase lRenderer = new RendererBase(lDrosophila,
                                              100,
                                              100,
                                              100)
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
