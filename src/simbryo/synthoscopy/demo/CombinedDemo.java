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
import simbryo.phantom.ClearCLPhantomRendererUtils;
import simbryo.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.camera.impl.SCMOSCameraRenderer;
import simbryo.synthoscopy.detection.impl.widefield.WideFieldDetectionOptics;
import simbryo.synthoscopy.illumination.impl.lightsheet.LightSheetIllumination;
import simbryo.util.serialization.SerializationUtilities;

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

      int lMaxCameraImageWidth = 4 * 512;
      int lMaxCameraImageHeight = 4 * 512;

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

        /*ClearCLImageViewer lPhantomViewer = lDrosoFluo.openViewer();/**/

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
        lSCMOSCameraRenderer.setCenteredROI(800, 1600);

        ClearCLImageViewer lCameraImageViewer =
                                              lSCMOSCameraRenderer.openViewer();

        lDrosoFluo.clear();
        lDrosoFluo.render();
        
        RawWriter lRawWriter = new RawWriter();
        lRawWriter.setOverwrite(true);
        File lDesktopFolder =
                            new File(System.getProperty("user.home")
                                     + "/Desktop/data");
        lDesktopFolder.mkdirs();

        int i = 0;
        float x = 0.35f, y = 0.5f, z = 0.35f, zl = 0.35f, h = 0.99f;
        float alpha = 0, beta = 0, gamma = 0, theta = 2;

        while (/*lPhantomViewer.isShowing()
               || /*lLightSheetViewer.isShowing()
               ||/**/ /*lDetectionViewer.isShowing()
                      ||/**/ lCameraImageViewer.isShowing())
        {
          // z += 0.001f;
          // if (z >= 0.355f)
          // z = 0.345f;

          z = 0.64f;
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
          lLightSheetIllumination.setDefaultZDepth(lDrosoFluo.getPhantomImage());

          lWideFieldDetectionOptics.setZFocusPosition(z);
          lWideFieldDetectionOptics.setDefaultZDepth(lDrosoFluo.getPhantomImage(),
                                                     lLightSheetIllumination.getLightMapImage());

          ElapsedTime.measure("renderlightsheet",
                              () -> lLightSheetIllumination.render(lDrosoFluo.getPhantomImage()));

          ElapsedTime.measure("renderdetection",
                              () -> lWideFieldDetectionOptics.render(lDrosoFluo.getPhantomImage(),
                                                                     lDrosoFluo.getPhantomImage(),
                                                                     lLightSheetIllumination.getLightMapImage()));

          ElapsedTime.measure("rendercameraimage",
                              () -> lSCMOSCameraRenderer.render(lWideFieldDetectionOptics.getImage()));

          Thread.sleep(1);

          
          File lRawFile = new File(lDesktopFolder,
                                   String.format("file%d.raw", i));
          lRawWriter.write(lSCMOSCameraRenderer.getImage(),
                           lRawFile);
         

          i++;
        }

        lRawWriter.close();
        lSCMOSCameraRenderer.close();
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
