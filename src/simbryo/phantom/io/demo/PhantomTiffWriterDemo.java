package simbryo.phantom.io.demo;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.phantom.ClearCLPhantomRendererUtils;
import simbryo.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.phantom.io.PhantomTiffWriter;

public class PhantomTiffWriterDemo
{

  @Test
  public void test() throws Throwable
  {
    String lUserHome = System.getProperty("user.home");
    File lDownloadFolder = new File(lUserHome+"/Downloads/"); 
    File lDataFolder = new File(lDownloadFolder,"DrosoStacks"); 
    
    int lWidth = 512;
    int lHeight = 512;
    int lDepth = 512;

    ElapsedTime.sStandardOutput = true;

    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend);
        ClearCLDevice lFastestGPUDevice =
                                        lClearCL.getFastestGPUDeviceForImages();)
    {

      int[] lGridDimensions =
                            ClearCLPhantomRendererUtils.getOptimalGridDimensions(lFastestGPUDevice,
                                                                                 lWidth,
                                                                                 lHeight,
                                                                                 lDepth);

      Drosophila lDrosophila = new Drosophila(16, lGridDimensions);

      DrosophilaHistoneFluorescence lDrosoFluo =
                                               new DrosophilaHistoneFluorescence(lFastestGPUDevice,
                                                                                 lDrosophila,
                                                                                 lWidth,
                                                                                 lHeight,
                                                                                 lDepth);
      
      PhantomTiffWriter lPhantomTiffWriter = new PhantomTiffWriter(1,0);

      ClearCLImageViewer lOpenViewer = lDrosoFluo.openViewer();

      int lPeriod = 50;

      while (lOpenViewer.isShowing())
      {
        lDrosophila.simulationSteps(lPeriod, 1);
        long lTimeIndex = lDrosophila.getTimeStepIndex();

        lDrosoFluo.clear();
        lDrosoFluo.render();
        
        File lFile = new File(lDataFolder,String.format("stack%d.tiff",lTimeIndex));
        lPhantomTiffWriter.write(lDrosoFluo,lFile);
      }

      lDrosoFluo.close();

    }

  }

}
