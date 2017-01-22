package simbryo.synthoscopy.demo;

import java.util.Arrays;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;
import javafx.scene.control.Slider;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.phantom.ClearCLPhantomRendererUtils;
import simbryo.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.OpticsRenderer;
import simbryo.synthoscopy.camera.SCMOSCameraModel;
import simbryo.synthoscopy.detection.WideFieldDetectionOptics;
import simbryo.synthoscopy.illumination.impl.lightsheet.LightSheetIllumination;
import simbryo.util.timing.Timming;

/**
 * Optics renderer demo
 *
 * @author royer
 */
public class OpticsRendererDemo
{

  /**
   * Demo
   * @throws Exception NA
   */
  @Test
  public void demo() throws Exception
  {
    int lPhantomWidth = 512;
    int lPhantomHeight = 512;
    int lPhantomDepth = 512;

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
                                                                                 lPhantomWidth,
                                                                                 lPhantomHeight,
                                                                                 lPhantomDepth);

      System.out.println("lGridDimensions="
                         + Arrays.toString(lGridDimensions));

      Drosophila lDrosophila = new Drosophila(16, lGridDimensions);

      System.out.println("grid size:"
                         + Arrays.toString(lDrosophila.getGridDimensions()));
      // lDrosophila.open3DViewer();
      // lDrosophila.getViewer().setDisplayRadius(false);

      DrosophilaHistoneFluorescence lDrosoFluo =
                                               new DrosophilaHistoneFluorescence(lFastestGPUDevice,
                                                                                 lDrosophila,
                                                                                 lPhantomWidth,
                                                                                 lPhantomHeight,
                                                                                 lPhantomDepth);

      ClearCLImageViewer lOpenViewer = lDrosoFluo.openViewer();
      @SuppressWarnings("unused")
      Slider lZSlider = lOpenViewer.getZSlider();

      OpticsRenderer<ClearCLImage> lOpticsRenderer =
                                                   new OpticsRenderer<>(lDrosoFluo);

      LightSheetIllumination lLightSheetIllumination =
                                                     new LightSheetIllumination(lFastestGPUDevice,lDrosoFluo);
      lOpticsRenderer.addIlluminationOptics(lLightSheetIllumination);

      WideFieldDetectionOptics lWideFieldDetectionOptics =
                                                         new WideFieldDetectionOptics(lDrosoFluo);
      lOpticsRenderer.addDetectionOptics(lWideFieldDetectionOptics);

      SCMOSCameraModel lSCMOSCameraModel = new SCMOSCameraModel(lDrosoFluo);
      lOpticsRenderer.addCameraModel(lSCMOSCameraModel);

      // lDrosophila.simulationSteps(13000, 1);

      Timming lTimming = new Timming();

      int lPeriod = 10;

      int i = 0;
      boolean lAbort = false;
      while (lOpenViewer.isShowing() && !lAbort)
      {
        // System.out.println("i=" + i);
        lTimming.syncAtPeriod(1);

        ElapsedTime.measure(i % lPeriod == 0,
                            "dynamics",
                            () -> lDrosophila.simulationSteps(1, 1));

        if (i % lPeriod == 0)
        {

          // TODO:

        }

        i++;
      }

      lOpticsRenderer.close();
      lDrosoFluo.close();

    }
  }

}
