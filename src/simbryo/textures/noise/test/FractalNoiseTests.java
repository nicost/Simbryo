package simbryo.textures.noise.test;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.enums.ImageChannelDataType;
import clearcl.viewer.ClearCLImageViewer;
import simbryo.textures.noise.BSplineNoise;
import simbryo.textures.noise.FractalNoise;
import simbryo.textures.noise.SimplexNoise;
import simbryo.textures.noise.UniformNoise;

public class FractalNoiseTests
{

  @Test
  public void testWithSimplexNoise()
  {
    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend))
    {
      ClearCLDevice lFastestGPUDevice =
          lClearCL.getFastestGPUDeviceForImages();
      
      ClearCLContext lContext = lFastestGPUDevice.createContext();
      
      SimplexNoise lSimplexNoise = new SimplexNoise(2);
      FractalNoise lFractalNoise = new FractalNoise(lSimplexNoise, 0.1f, 0.05f, 0.025f, 0.0125f, 0.00625f);

      float[] lTexture = lFractalNoise.generateTexture(128, 128);

      ClearCLImage lClearCLImage = lContext.createSingleChannelImage(ImageChannelDataType.Float, 128, 128);
      
      lClearCLImage.readFrom(lTexture, true);
      
      ClearCLImageViewer lView = ClearCLImageViewer.view(lClearCLImage);
      
      lView.waitWhileShowing();
      
    }

  }
  
  @Test
  public void testWithBSplineNoise()
  {
    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend))
    {
      ClearCLDevice lFastestGPUDevice =
          lClearCL.getFastestGPUDeviceForImages();
      
      ClearCLContext lContext = lFastestGPUDevice.createContext();
      
      BSplineNoise lBSplineNoise = new BSplineNoise(2);
      FractalNoise lFractalNoise = new FractalNoise(lBSplineNoise, 0.1f, 0.05f, 0.025f, 0.0125f, 0.00625f);
      
      float[] lTexture = lFractalNoise.generateTexture(128, 128);

      ClearCLImage lClearCLImage = lContext.createSingleChannelImage(ImageChannelDataType.Float, 128, 128);
      
      lClearCLImage.readFrom(lTexture, true);
      
      ClearCLImageViewer lView = ClearCLImageViewer.view(lClearCLImage);
      
      lView.waitWhileShowing();
      
    }

  }

}
