package simbryo.synthoscopy.demo;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.io.RawWriter;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.phantom.scatter.impl.drosophila.DrosophilaScatteringPhantom;
import simbryo.synthoscopy.camera.impl.SCMOSCameraRenderer;
import simbryo.synthoscopy.detection.impl.widefield.WideFieldDetectionOptics;
import simbryo.synthoscopy.illumination.impl.lightsheet.LightSheetIllumination;

/**
 * Light sheet illumination demo
 *
 * @author royer
 */
public class CombinedDemo
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
      float lWavelengthInNormUnits = 0.0005f;
      float lLightIntensity = 1.0f;

      int lPhantomWidth = 512;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      int lMaxCameraImageWidth = 2 * 512;
      int lMaxCameraImageHeight = 2 * 512;

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

        /*ClearCLImageViewer lFluoPhantomViewer = lDrosoFluo.openViewer();/**/

        DrosophilaScatteringPhantom lDrosophilaScatteringPhantom =
                                                                 new DrosophilaScatteringPhantom(lContext,
                                                                                                 lDrosophila,
                                                                                                 lDrosophilaFluorescencePhantom,
                                                                                                 lPhantomWidth / 4,
                                                                                                 lPhantomHeight / 4,
                                                                                                 lPhantomDepth / 4);

        @SuppressWarnings("unused")
        ClearCLImageViewer lScatterPhantomViewer =
                                                 lDrosophilaScatteringPhantom.openViewer();/**/

        LightSheetIllumination lLightSheetIllumination =
                                                       new LightSheetIllumination(lContext,
                                                                                  lWavelengthInNormUnits,
                                                                                  lLightIntensity,
                                                                                  lPhantomWidth / 4,
                                                                                  lPhantomHeight / 4,
                                                                                  17L);
        /*ClearCLImageViewer lLightSheetViewer =
                                             lLightSheetIllumination.openViewer();/**/

        WideFieldDetectionOptics lWideFieldDetectionOptics =
                                                           new WideFieldDetectionOptics(lContext,
                                                                                        lWavelengthInNormUnits,
                                                                                        lLightIntensity,
                                                                                        lPhantomWidth,
                                                                                        lPhantomHeight);
        /*ClearCLImageViewer lDetectionViewer =
                                            lWideFieldDetectionOptics.openViewer();/**/

        SCMOSCameraRenderer lSCMOSCameraRenderer =
                                                 new SCMOSCameraRenderer(lContext,
                                                                         lWavelengthInNormUnits,
                                                                         lLightIntensity,
                                                                         lMaxCameraImageWidth,
                                                                         lMaxCameraImageHeight);

        // lSCMOSCameraRenderer.setROI(512, 3*512, 512, 2048-512);
        lSCMOSCameraRenderer.setCenteredROI(lMaxCameraImageWidth / 1,
                                            lMaxCameraImageHeight / 1);

        ClearCLImageViewer lCameraImageViewer =
                                              lSCMOSCameraRenderer.openViewer();

        lDrosophilaFluorescencePhantom.clear();
        lDrosophilaFluorescencePhantom.render();

        lDrosophilaScatteringPhantom.clear();
        lDrosophilaScatteringPhantom.render();

        RawWriter lRawWriter = new RawWriter();
        lRawWriter.setOverwrite(true);
        File lDesktopFolder = new File(System.getProperty("user.home")
                                       + "/Desktop/data");
        lDesktopFolder.mkdirs();

        int i = 0;
        float x = 0.35f, y = 0.5f, z = 0.35f, zl = 0.35f, h = 0.3f;
        float alpha = 0, beta = 0, gamma = 0, theta = 2;
        boolean lWriteFile = false;

        while (/*lPhantomViewer.isShowing()
               || /*lLightSheetViewer.isShowing()
               ||/**/ /*lDetectionViewer.isShowing()
                      ||/**/ lCameraImageViewer.isShowing())
        {
          y += 0.001f;
          if (y >= 1f)
            y = 0.0f;

          z = 0.5f;
          zl = z;

          // alpha += 0.1;
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

          lLightSheetIllumination.setLightSheetHeigth(h);
          lLightSheetIllumination.setLightSheetPosition(x, y, zl);
          lLightSheetIllumination.setOrientationWithAnglesInDegrees(alpha,
                                                                    beta,
                                                                    gamma);
          lLightSheetIllumination.setLightSheetThetaInDeg(theta);
          lLightSheetIllumination.setZCenterOffset(z);
          lLightSheetIllumination.setDefaultZDepth(lDrosophilaFluorescencePhantom.getPhantomImage());

          lWideFieldDetectionOptics.setZFocusPosition(z);
          lWideFieldDetectionOptics.setDefaultZDepth(lDrosophilaFluorescencePhantom.getPhantomImage(),
                                                     lLightSheetIllumination.getLightMapImage());

          ElapsedTime.measure("renderlightsheet",
                              () -> lLightSheetIllumination.render(lDrosophilaScatteringPhantom.getPhantomImage()));

          ElapsedTime.measure("renderdetection",
                              () -> lWideFieldDetectionOptics.render(lDrosophilaFluorescencePhantom.getPhantomImage(),
                                                                     lDrosophilaScatteringPhantom.getPhantomImage(),
                                                                     lLightSheetIllumination.getLightMapImage()));

          ElapsedTime.measure("rendercameraimage",
                              () -> lSCMOSCameraRenderer.render(lWideFieldDetectionOptics.getImage()));

          Thread.sleep(1);

          if (lWriteFile)
          {
            File lRawFile = new File(lDesktopFolder,
                                     String.format("file%d.raw", i));
            lRawWriter.write(lSCMOSCameraRenderer.getImage(),
                             lRawFile);
          }

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
