package simbryo.synthoscopy.demo;

import java.io.File;
import java.io.IOException;

import javax.vecmath.Vector3f;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.io.RawWriter;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;

import org.junit.Test;

import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.camera.impl.SCMOSCameraRenderer;
import simbryo.synthoscopy.optics.detection.impl.widefield.WideFieldDetectionOptics;
import simbryo.synthoscopy.optics.illumination.impl.lightsheet.LightSheetIllumination;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.phantom.scatter.impl.drosophila.DrosophilaScatteringPhantom;

/**
 * Light sheet illumination demo
 *
 * @author royer
 */
public class SynthoscopyDemo
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

        Drosophila lDrosophila = Drosophila.getDeveloppedEmbryo(14);

        DrosophilaHistoneFluorescence lDrosophilaFluorescencePhantom =
                                                                     new DrosophilaHistoneFluorescence(lContext,
                                                                                                       lDrosophila,
                                                                                                       lPhantomWidth,
                                                                                                       lPhantomHeight,
                                                                                                       lPhantomDepth);

        @SuppressWarnings("unused")
        ClearCLImageViewer lFluoPhantomViewer =
                                              lDrosophilaFluorescencePhantom.openViewer();/**/

        DrosophilaScatteringPhantom lDrosophilaScatteringPhantom =
                                                                 new DrosophilaScatteringPhantom(lContext,
                                                                                                 lDrosophila,
                                                                                                 lDrosophilaFluorescencePhantom,
                                                                                                 lPhantomWidth / 4,
                                                                                                 lPhantomHeight / 4,
                                                                                                 lPhantomDepth / 4);

        /*@SuppressWarnings("unused")
        ClearCLImageViewer lScatterPhantomViewer =
                                                 lDrosophilaScatteringPhantom.openViewer();/**/

        LightSheetIllumination lLightSheetIllumination =
                                                       new LightSheetIllumination(lContext,
                                                                                  lPhantomWidth
                                                                                            / 4,
                                                                                  lPhantomHeight / 4,
                                                                                  17L);
        /*ClearCLImageViewer lLightSheetViewer =
                                             lLightSheetIllumination.openViewer();/**/

        WideFieldDetectionOptics lWideFieldDetectionOptics =
                                                           new WideFieldDetectionOptics(lContext,
                                                                                        lPhantomWidth,
                                                                                        lPhantomHeight);
        /*ClearCLImageViewer lDetectionViewer =
                                            lWideFieldDetectionOptics.openViewer();/**/

        SCMOSCameraRenderer lSCMOSCameraRenderer =
                                                 new SCMOSCameraRenderer(lContext,
                                                                         lMaxCameraImageWidth,
                                                                         lMaxCameraImageHeight);

        // lSCMOSCameraRenderer.setROI(512, 3*512, 512, 2048-512);
        lSCMOSCameraRenderer.setCenteredROI(lMaxCameraImageWidth / 1,
                                            lMaxCameraImageHeight / 1);

        @SuppressWarnings("unused")
        ClearCLImageViewer lCameraImageViewer =
                                              lSCMOSCameraRenderer.openViewer();/**/

        RawWriter lRawWriter = new RawWriter();
        lRawWriter.setOverwrite(true);
        File lDesktopFolder = new File(System.getProperty("user.home")
                                       + "/Temp/data");
        lDesktopFolder.mkdirs();

        int i = 0;
        float x = 0.235f, y = 0.5f, z = 0.6f, zl = 0.35f, h = 0.55f;
        float alpha = 0, beta = 0, gamma = 0, theta = 2;
        boolean lWriteFile = true;

        Vector3f lAxisVector = new Vector3f();
        Vector3f lNormalVector = new Vector3f();

        lAxisVector.set(1.0f, 0, 0);
        lNormalVector.set(0, 0, 1.0f);

        while (lDrosophila.getTimeStepIndex() < 15000 && alpha < 6) // lCameraImageViewer.isShowing()
        // &&
        {
          System.out.println("Timepoint: "
                             + lDrosophila.getTimeStepIndex());
          File lRawFile = new File(lDesktopFolder,
                                   String.format("file%d.raw", i)); // lDrosophila.getTimeStepIndex()

          // z += 0.005f;
          // if (z >= 1f)
          // z = 0.5f;

          // if(lDrosophila.getTimeStepIndex()>12000)
          // alpha += (lDrosophila.getTimeStepIndex()-12000) * 0.0005f;
          // gamma= lDrosophila.getTimeStepIndex()*0.002f;
          // z = 0.67f;
          // if(lDrosophila.getTimeStepIndex()>5000)
          // z += (lDrosophila.getTimeStepIndex()-5000) * 0.00002f;

          zl = z;

          beta += 0.01;
          /// if (alpha >= 60)
          // alpha = -60;

          // beta += 0.05;
          // if (beta >= 10)
          // beta = -10;

          // gamma += 0.5;
          // if (gamma >= 60)
          // gamma = -60;

          // theta += 0.1;
          // if (theta >= 10)
          // theta = 0;

          lLightSheetIllumination.setIntensity(1);
          lLightSheetIllumination.setLightWavelength(lLightSheetIllumination.getLightWavelength());
          lLightSheetIllumination.setLightSheetHeigth(h);
          lLightSheetIllumination.setLightSheetPosition(x, y, zl);
          lLightSheetIllumination.setLightSheetAxisVector(lAxisVector);
          lLightSheetIllumination.setLightSheetNormalVector(lNormalVector);
          lLightSheetIllumination.setOrientationWithAnglesInDegrees(alpha,
                                                                    beta,
                                                                    gamma);
          lLightSheetIllumination.setLightSheetThetaInDeg(theta);

          lWideFieldDetectionOptics.setIntensity(1);
          lWideFieldDetectionOptics.setLightWavelength(lLightSheetIllumination.getLightWavelength());
          lWideFieldDetectionOptics.setZFocusPosition(z);

          lLightSheetIllumination.setScatteringPhantom(lDrosophilaScatteringPhantom.getImage());

          lWideFieldDetectionOptics.setFluorescencePhantomImage(lDrosophilaFluorescencePhantom.getImage());
          lWideFieldDetectionOptics.setScatteringPhantomImage(lDrosophilaScatteringPhantom.getImage());
          lWideFieldDetectionOptics.setLightMapImage(lLightSheetIllumination.getImage());

          lSCMOSCameraRenderer.setDetectionImage(lWideFieldDetectionOptics.getImage());

          /*int lZStart = max(0, (int) ((z - 0.1) * lPhantomDepth));
          int lZEnd = min(lPhantomDepth - 1,
                          (int) ((z + 0.1) * lPhantomDepth));/**/

          if (!(lWriteFile && lRawFile.exists()))
          {

            lDrosophilaFluorescencePhantom.render(false); // lZStart,lZEnd,

            lDrosophilaScatteringPhantom.render(false);

            ElapsedTime.measure("renderlightsheet",
                                () -> lLightSheetIllumination.render(false));

            ElapsedTime.measure("renderdetection",
                                () -> lWideFieldDetectionOptics.render(false));

            ElapsedTime.measure("rendercameraimage",
                                () -> lSCMOSCameraRenderer.render(true));/**/

            Thread.sleep(1);

            if (lWriteFile)
            {
              System.out.println("Writting: " + lRawFile);
              lRawWriter.write(lSCMOSCameraRenderer.getImage(),
                               lRawFile);
            }
          }

          // lDrosophila.simulationSteps(10, 1);
          // lLightSheetIllumination.requestUpdate();
          // lWideFieldDetectionOptics.requestUpdate();
          // lSCMOSCameraRenderer.requestUpdate();
          // lDrosophilaFluorescencePhantom.clear(false);
          // lDrosophilaScatteringPhantom.clear(false);

          i++;
        }

        lRawWriter.close();
        lSCMOSCameraRenderer.close();
        lWideFieldDetectionOptics.close();
        lLightSheetIllumination.close();
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
