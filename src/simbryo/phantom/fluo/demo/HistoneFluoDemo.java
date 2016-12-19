package simbryo.phantom.fluo.demo;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;
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
                         + Arrays.toString(lDrosophila.getGridDimensions()));
      // lDrosophila.open3DViewer();
      // lDrosophila.getViewer().setDisplayRadius(false);

      HistoneFluorescence lHistoneFluo =
                                       new HistoneFluorescence(lFastestGPUDevice,
                                                               lDrosophila,
                                                               lWidth,
                                                               lHeight,
                                                               lDepth);

      ClearCLImageViewer lOpenViewer = lHistoneFluo.openViewer();

      Timming lTimming = new Timming();

      int i = 0;
      while (lOpenViewer.isShowing())
      {
        System.out.println("i=" + i);
        lTimming.syncAtPeriod(1);

        ElapsedTime.measure(i % 10 == 0,
                            "dynamics",
                            () -> lDrosophila.simulationSteps(1, 1));

        if (i % 5 == 0)
        {

          /*System.out.format("avg part per cell : %g \n",
                            lDrosophila.getNeighborhoodGrid()
                                       .getAverageNumberOfParticlesPerGridCell());
          System.out.format("max part per cell : %d \n",
                            lDrosophila.getNeighborhoodGrid()
                                       .getMaximalEffectiveNumberOfParticlesPerGridCell());
          System.out.format("occupancy : %g \n",
                            lDrosophila.getNeighborhoodGrid()
                                       .getAverageCellOccupancy());/**/
          lHistoneFluo.clear();
          lHistoneFluo.renderSmart(0, (int) lHistoneFluo.getDepth());
          // Thread.sleep(5000);
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

      // lDrosophila.getViewer().waitWhileShowing();

    }

  }

}
