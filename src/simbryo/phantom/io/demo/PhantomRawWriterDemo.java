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
import coremem.enums.NativeTypeEnum;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.phantom.ClearCLPhantomRendererUtils;
import simbryo.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.phantom.io.PhantomRawWriter;

public class PhantomRawWriterDemo
{

  @Test
  public void test() throws IOException
  {
    /*String lUserHome = System.getProperty("user.home");
    File lDownloadFolder = new File(lUserHome + "/Downloads/");
    File lDataFolder = new File(lDownloadFolder, "DrosoStacks");/**/
    
    File lDataFolder = new File("/Volumes/green-carpet/Simbryo/stacks");

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

      PhantomRawWriter lPhantomRawWriter = new PhantomRawWriter(100, 0);
      lPhantomRawWriter.setOverwrite(false);
      lPhantomRawWriter.setDataType(NativeTypeEnum.Byte);

      lDrosophila.simulationSteps(8500, 1);
      
      //ClearCLImageViewer lOpenViewer = lDrosoFluo.openViewer();

      int lPeriod = 10;

      while (lDrosophila.getTimeStepIndex()<15000)
      {
        lDrosophila.simulationSteps(lPeriod, 1);
        long lTimeIndex = lDrosophila.getTimeStepIndex();

        lDrosoFluo.clear();
        lDrosoFluo.render();

        File lFile =
                   new File(lDataFolder,
                            String.format("stack.%d.%d.%d.%d.%s.raw",
                                          lWidth,
                                          lHeight,
                                          lDepth,
                                          lTimeIndex,lPhantomRawWriter.getDataType()));

        if(lPhantomRawWriter.write(lDrosoFluo, lFile))
        {
          System.out.println("Writting file: "+lFile);
        }
      }

      lDrosoFluo.close();

    }

  }

}
