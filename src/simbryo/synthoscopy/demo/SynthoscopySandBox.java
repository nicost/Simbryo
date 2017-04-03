package simbryo.synthoscopy.demo;

import java.io.File;
import java.io.IOException;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.io.RawWriter;
import clearcl.viewer.ClearCLImageViewer;

import org.junit.Test;

import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulatorOrtho;
import simbryo.synthoscopy.microscope.parameters.DetectionParameter;
import simbryo.synthoscopy.microscope.parameters.IlluminationParameter;
import simbryo.synthoscopy.microscope.parameters.PhantomParameter;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.phantom.scatter.impl.drosophila.DrosophilaScatteringPhantom;

/**
 * Light sheet illumination demo
 *
 * @author royer
 */
public class SynthoscopySandBox
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

      int lNumberOfDetectionArms = 1;
      int lNumberOfIlluminationArms = 2;

      int lMaxCameraResolution = 1024;

      int lPhantomWidth = 320;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      boolean lWriteFile = false;

      RawWriter lRawWriter = new RawWriter();
      lRawWriter.setOverwrite(true);
      File lDesktopFolder = new File(System.getProperty("user.home")
                                     + "/Temp/data");
      lDesktopFolder.mkdirs();

      // ElapsedTime.sStandardOutput = true;

      ClearCLBackendInterface lBestBackend =
                                           ClearCLBackends.getBestBackend();

      try (ClearCL lClearCL = new ClearCL(lBestBackend);
          ClearCLDevice lFastestGPUDevice =
                                          lClearCL.getFastestGPUDeviceForImages();
          ClearCLContext lContext = lFastestGPUDevice.createContext())
      {

        Drosophila lDrosophila = Drosophila.getDeveloppedEmbryo(11);

        DrosophilaHistoneFluorescence lDrosophilaFluorescencePhantom =
                                                                     new DrosophilaHistoneFluorescence(lContext,
                                                                                                       lDrosophila,
                                                                                                       lPhantomWidth,
                                                                                                       lPhantomHeight,
                                                                                                       lPhantomDepth);
        lDrosophilaFluorescencePhantom.render(true);

        // @SuppressWarnings("unused")

        /*ClearCLImageViewer lFluoPhantomViewer = lDrosophilaFluorescencePhantom.openViewer();/**/

        DrosophilaScatteringPhantom lDrosophilaScatteringPhantom =
                                                                 new DrosophilaScatteringPhantom(lContext,
                                                                                                 lDrosophila,
                                                                                                 lDrosophilaFluorescencePhantom,
                                                                                                 lPhantomWidth / 2,
                                                                                                 lPhantomHeight / 2,
                                                                                                 lPhantomDepth / 2);

        lDrosophilaScatteringPhantom.render(true);

        // @SuppressWarnings("unused")
        /*ClearCLImageViewer lScatterPhantomViewer =
                                                 lDrosophilaScatteringPhantom.openViewer();/**/

        LightSheetMicroscopeSimulatorOrtho lSimulator =
                                                      new LightSheetMicroscopeSimulatorOrtho(lContext,
                                                                                             lNumberOfDetectionArms,
                                                                                             lNumberOfIlluminationArms,
                                                                                             lMaxCameraResolution,
                                                                                             lPhantomWidth,
                                                                                             lPhantomHeight,
                                                                                             lPhantomDepth);

        lSimulator.setPhantomParameter(PhantomParameter.Fluorescence,
                                       lDrosophilaFluorescencePhantom.getImage());
        lSimulator.setPhantomParameter(PhantomParameter.Scattering,
                                       lDrosophilaScatteringPhantom.getImage());

        lSimulator.openViewerForControls();

        ClearCLImageViewer lCameraImageViewer =
                                              lSimulator.openViewerForCameraImage(0);
        for (int i = 1; i < lNumberOfDetectionArms; i++)
          lCameraImageViewer = lSimulator.openViewerForCameraImage(i);

        // for (int i = 0; i < lNumberOfIlluminationArms; i++)
        // lSimulator.openViewerForLightMap(i);

        lSimulator.setNumberParameter(IlluminationParameter.Height,
                                      0,
                                      1f);
        lSimulator.setNumberParameter(IlluminationParameter.Height,
                                      1,
                                      0.2f);

        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      0,
                                      50f);
        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      1,
                                      0f);

        lSimulator.setNumberParameter(IlluminationParameter.Gamma,
                                      0,
                                      0f);
        lSimulator.setNumberParameter(IlluminationParameter.Gamma,
                                      1,
                                      20f);

        int i = 0;

        for (float z =
                     -0.0f; z < 0.3
                            && lCameraImageViewer.isShowing(); z +=
                                                                 0.001)
        {

          lSimulator.setNumberParameter(IlluminationParameter.Z,
                                        0,
                                        z);
          lSimulator.setNumberParameter(IlluminationParameter.Z,
                                        1,
                                        z);

          lSimulator.setNumberParameter(DetectionParameter.Z, 0, z);

          // lDrosophila.simulationSteps(10, 1);
          // lDrosophilaFluorescencePhantom.clear(false);
          lDrosophilaFluorescencePhantom.render(false);

          lSimulator.render(true);

          if (lWriteFile)
          {
            File lRawFile =
                          new File(lDesktopFolder,
                                   String.format("file%d.raw", i++)); // lDrosophila.getTimeStepIndex()

            System.out.println("Writting: " + lRawFile);
            lRawWriter.write(lSimulator.getCameraImage(0), lRawFile);
          }
        }

        lSimulator.close();
        lDrosophilaScatteringPhantom.close();
        lDrosophilaFluorescencePhantom.close();

      }

      lRawWriter.close();
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }

  }

}
