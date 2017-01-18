package simbryo.synthoscopy.illumination.demo;

import java.io.IOException;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.util.ElapsedTime;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.phantom.ClearCLPhantomRendererUtils;
import simbryo.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.illumination.LightSheetIllumination;

/**
 * Light sheet illumination demo
 *
 * @author royer
 */
public class LightSheetIlluminationDemo
{

  /**
   * Demo
   * @throws IOException NA
   */
  @Test
  public void demo() throws IOException
  {
    
    int lPhantomWidth = 512;
    int lPhantomHeight = 512;
    int lPhantomDepth = 512;

    ElapsedTime.sStandardOutput = true;

    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();

    try (ClearCL lClearCL = new ClearCL(lBestBackend);
        ClearCLDevice lFastestGPUDevice =
                                        lClearCL.getFastestGPUDeviceForImages();)
    {

      int[] lGridDimensions =
                            ClearCLPhantomRendererUtils.getOptimalGridDimensions(lFastestGPUDevice,
                                                                                 lPhantomWidth,
                                                                                 lPhantomHeight,
                                                                                 lPhantomDepth);


      Drosophila lDrosophila = new Drosophila(16,lGridDimensions);

      DrosophilaHistoneFluorescence lDrosoFluo =
                                       new DrosophilaHistoneFluorescence(lFastestGPUDevice,
                                                               lDrosophila,
                                                               lPhantomWidth,
                                                               lPhantomHeight,
                                                               lPhantomDepth);
      
      
      lDrosophila.simulationSteps(1000, 1);
      
      
      
      LightSheetIllumination lLightSheetIllumination = new LightSheetIllumination();
      
      
      lLightSheetIllumination.setPhantom(lDrosoFluo);
      
     

      lDrosoFluo.close();

    }
    
  }

}
