package simbryo.synthoscopy.detection.impl.widefield.demo;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.phantom.ClearCLPhantomRendererUtils;
import simbryo.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.detection.impl.widefield.WideFieldDetectionOptics;
import simbryo.synthoscopy.illumination.impl.lightsheet.LightSheetIllumination;
import simbryo.util.serialization.SerializationUtilities;

/**
 * Light sheet illumination demo
 *
 * @author royer
 */
public class WideFieldDetectionOpticsDemo
{

  /**
   * Demo
   * 
   * @throws IOException
   *           NA
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void demo() throws IOException, InterruptedException
  {

    try
    {
      float lWaveelngthInNormUnits = 0.0005f;
      float lLightIntensity = 1.0f;

      int lPhantomWidth = 512;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      ElapsedTime.sStandardOutput = true;

      ClearCLBackendInterface lBestBackend =
                                           ClearCLBackends.getBestBackend();

      try (ClearCL lClearCL = new ClearCL(lBestBackend);
          ClearCLDevice lFastestGPUDevice =
                                          lClearCL.getFastestGPUDeviceForImages();
          ClearCLContext lContext = lFastestGPUDevice.createContext())
      {

        File lTempDirectory =
                            new File(System.getProperty("java.io.tmpdir"));
        File lCachedEmbryoDynamicsFile =
                                       new File(lTempDirectory,
                                                this.getClass()
                                                    .getSimpleName());
        Drosophila lDrosophila =
                               SerializationUtilities.loadFromFile(Drosophila.class,
                                                                   lCachedEmbryoDynamicsFile);

        if (lDrosophila == null)
        {
          int[] lGridDimensions =
                                ClearCLPhantomRendererUtils.getOptimalGridDimensions(lFastestGPUDevice,
                                                                                     lPhantomWidth,
                                                                                     lPhantomHeight,
                                                                                     lPhantomDepth);

          lDrosophila = new Drosophila(16, lGridDimensions);
          lDrosophila.simulationSteps(14000, 1);
          SerializationUtilities.saveToFile(lDrosophila,
                                            lCachedEmbryoDynamicsFile);
        }

        DrosophilaHistoneFluorescence lDrosoFluo =
                                                 new DrosophilaHistoneFluorescence(lContext,
                                                                                   lDrosophila,
                                                                                   lPhantomWidth,
                                                                                   lPhantomHeight,
                                                                                   lPhantomDepth);
        lDrosoFluo.clear();
        lDrosoFluo.render();
        ClearCLImageViewer lPhantomViewer = lDrosoFluo.openViewer();

        LightSheetIllumination lLightSheetIllumination =
                                                       new LightSheetIllumination(lContext,
                                                                                  lWaveelngthInNormUnits,
                                                                                  lLightIntensity,
                                                                                  lPhantomWidth / 4,
                                                                                  lPhantomHeight / 4,
                                                                                  17L);

        lLightSheetIllumination.setLightSheetHeigth(0.999f);
        lLightSheetIllumination.setLightSheetPosition(0.35f,
                                                      0.5f,
                                                      0.680f);

        lLightSheetIllumination.setOrientationWithAnglesInDegrees(3,
                                                                  0,
                                                                  0);
        lLightSheetIllumination.setLightSheetThetaInDeg(1.0f);

        ElapsedTime.measure("renderlightsheet",
                            () -> lLightSheetIllumination.render(lDrosoFluo.getPhantomImage(),
                                                                 0.68f));

        ClearCLImageViewer lLightSheetViewer =
                                             lLightSheetIllumination.openViewer();

        WideFieldDetectionOptics lWideFieldDetectionOptics =
                                                           new WideFieldDetectionOptics(lContext,
                                                                                        lWaveelngthInNormUnits,
                                                                                        lLightIntensity,
                                                                                        lPhantomWidth,
                                                                                        lPhantomHeight);
        ElapsedTime.measure("renderdetection",
                            () -> lWideFieldDetectionOptics.render(lDrosoFluo.getPhantomImage(),
                                                                   lLightSheetIllumination.getLightMapImage(),
                                                                   0.68f));

        ClearCLImageViewer lDetectionViewer =
                                            lWideFieldDetectionOptics.openViewer();

        Thread.sleep(500);

        while (lPhantomViewer.isShowing()
               || lLightSheetViewer.isShowing()
               || lDetectionViewer.isShowing())
        {
          Thread.sleep(10);
        }

        lWideFieldDetectionOptics.close();
        lLightSheetIllumination.close();
        lDrosoFluo.close();

      }
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }

  }

}
