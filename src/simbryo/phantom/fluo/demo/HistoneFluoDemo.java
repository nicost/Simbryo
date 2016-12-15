package simbryo.phantom.fluo.demo;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.util.ElapsedTime;
import simbryo.dynamics.tissue.zoo.Drosophila;
import simbryo.phantom.ClearCLPhantomRendererUtils;
import simbryo.phantom.fluo.HistoneFluorescence;
import simbryo.util.timing.Timming;

public class HistoneFluoDemo
{

  @Test
  public void demo() throws IOException, InterruptedException
  {
    int lWidth = 512;
    int lHeight = 512;
    int lDepth = 128;

    ElapsedTime.sStandardOutput = true;

    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend))
    {
      ClearCLDevice lFastestGPUDevice =
                                      lClearCL.getFastestGPUDeviceForImages();

      int[] lGridDimensions =
                            ClearCLPhantomRendererUtils.getOptimalGridDimensions(lFastestGPUDevice,
                                                                                 lWidth,
                                                                                 lHeight,
                                                                                 lDepth);

      System.out.println("lGridDimensions="
                         + Arrays.toString(lGridDimensions));

      Drosophila lDrosophila = new Drosophila(16, lGridDimensions);

      System.out.println("grid size:"
                         + lDrosophila.getGridDimensions());
      lDrosophila.open3DViewer();
      lDrosophila.getViewer().setDisplayRadius(false);

      HistoneFluorescence lHistoneFluo =
                                       new HistoneFluorescence(lFastestGPUDevice,
                                                               lDrosophila,
                                                               lWidth,
                                                               lHeight,
                                                               lDepth);

      lHistoneFluo.openViewer();

      Timming lTimming = new Timming();

      int i = 0;
      while (lDrosophila.getViewer().isShowing())
      {
        lTimming.syncAtPeriod(1);
        lDrosophila.simulationSteps(1, 1);

        if (i % 10 == 0)
        {
          System.out.format("avg part per cell : %g \n",
                            lDrosophila.getNeighborhoodGrid()
                                       .getAverageNumberOfParticlesPerGridCell());
          System.out.format("max part per cell : %d \n",
                            lDrosophila.getNeighborhoodGrid()
                                       .getMaximalEffectiveNumberOfParticlesPerGridCell());
          System.out.format("occupancy : %g \n",
                            lDrosophila.getNeighborhoodGrid()
                                       .getAverageCellOccupancy());
          lHistoneFluo.clear();
          lHistoneFluo.renderSmart(0, (int) lHistoneFluo.getDepth());
        }

        // int z = (int) (i%100);
        // lHistoneFluo.invalidate(z);

        /*
        if (i % 100 == 0)
        {
          lHistoneFluo.clear();
          for (int zi = 0; zi < 100; zi++)
            lHistoneFluo.render(zi);
        }/**/

        i++;
      }

      lDrosophila.getViewer().waitWhileShowing();

    }

  }

}
