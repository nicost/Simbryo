package simbryo.synthoscopy.microscope.lightsheet.demo;

import java.io.IOException;

import javax.vecmath.Vector3f;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.viewer.ClearCLImageViewer;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulator;
import simbryo.synthoscopy.microscope.parameters.PhantomParameter;
import simbryo.synthoscopy.optics.illumination.impl.lightsheet.LightSheetIllumination;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.phantom.scatter.impl.drosophila.DrosophilaScatteringPhantom;

/**
 * Demo for lightsheet microscope simulator
 *
 * @author royer
 */
public class LightSheetMicroscopeSimulatorDemo
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

      int lPhantomWidth = 320;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      int lMaxCameraImageWidth = 2 * lPhantomWidth;
      int lMaxCameraImageHeight = 2 * lPhantomHeight;

      // ElapsedTime.sStandardOutput = true;

      ClearCLBackendInterface lBestBackend =
                                           ClearCLBackends.getBestBackend();

      try (ClearCL lClearCL = new ClearCL(lBestBackend);
          ClearCLDevice lFastestGPUDevice =
                                          lClearCL.getFastestGPUDeviceForImages();
          ClearCLContext lContext = lFastestGPUDevice.createContext())
      {

        Drosophila lDrosophila =
                               Drosophila.getDeveloppedEmbryo(14,
                                                              lPhantomWidth,
                                                              lPhantomHeight,
                                                              lPhantomDepth,
                                                              lFastestGPUDevice);

        DrosophilaHistoneFluorescence lDrosophilaFluorescencePhantom =
                                                                     new DrosophilaHistoneFluorescence(lContext,
                                                                                                       lDrosophila,
                                                                                                       lPhantomWidth,
                                                                                                       lPhantomHeight,
                                                                                                       lPhantomDepth);
        lDrosophilaFluorescencePhantom.render(true);

        @SuppressWarnings("unused")
        /*ClearCLImageViewer lFluoPhantomViewer = lDrosophilaFluorescencePhantom.openViewer();/**/

        DrosophilaScatteringPhantom lDrosophilaScatteringPhantom =
                                                                 new DrosophilaScatteringPhantom(lContext,
                                                                                                 lDrosophila,
                                                                                                 lDrosophilaFluorescencePhantom,
                                                                                                 lPhantomWidth / 2,
                                                                                                 lPhantomHeight / 2,
                                                                                                 lPhantomDepth / 2);

        lDrosophilaScatteringPhantom.render(true);

        @SuppressWarnings("unused")
        /*ClearCLImageViewer lScatterPhantomViewer =
                                                 lDrosophilaScatteringPhantom.openViewer();/**/

        LightSheetMicroscopeSimulator lSimulator =
                                                 new LightSheetMicroscopeSimulator(lContext,
                                                                                   lPhantomWidth,
                                                                                   lPhantomHeight,
                                                                                   lPhantomDepth);

        lSimulator.setPhantom(PhantomParameter.Fluorescence,
                              lDrosophilaFluorescencePhantom.getImage());
        lSimulator.setPhantom(PhantomParameter.Scattering,
                              lDrosophilaScatteringPhantom.getImage());

        Vector3f lIlluminationAxisVector = new Vector3f(1, 0, 0);
        Vector3f lIlluminationNormalVector = new Vector3f(0, 0, 1);

        LightSheetIllumination lLightSheet =
                                           lSimulator.addLightSheet(lIlluminationAxisVector,
                                                                    lIlluminationNormalVector);

        lLightSheet.openViewer();

        Vector3f lDetectionUpDownVector = new Vector3f(0, 1, 0);

        lSimulator.addDetectionPath(lDetectionUpDownVector,
                                    lMaxCameraImageWidth,
                                    lMaxCameraImageHeight);

        lSimulator.openViewerForControls();

        ClearCLImageViewer lCameraImageViewer =
                                              lSimulator.openViewerForCameraImage(0);

        //float y = 0;

        while (lCameraImageViewer.isShowing())
        {
          /*lSimulator.setNumberParameter(IlluminationParameter.Height,
                                        0,
                                        0.01f);
          lSimulator.setNumberParameter(IlluminationParameter.Y,
                                        0,
                                        y);
          
          y += 0.01f;
          if (y > 1)
            y = 0;
            /**/

          lSimulator.render();
        }

        lSimulator.close();
        lDrosophilaScatteringPhantom.close();
        lDrosophilaFluorescencePhantom.close();

      }
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }
  }

}
