package simbryo.synthoscopy.phantom.fluo.impl.drosophila.demo;

import java.io.IOException;
import java.util.Arrays;
import javafx.scene.control.Slider;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;

import org.junit.Test;

import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.util.timing.Timming;

/**
 * Histone fluorescence drosophila demo
 *
 * @author royer
 */
public class HistoneFluorescenceDrosophilaDemo
{

  private static final boolean cRenderFullStack = true;
  private static final int cWidth = 512;
  private static final int cHeight = 512;
  private static final int cDepth = 512;
  private static final int cStartSimulationStep = 0;
  private static final int cDisplayPeriod = 10;

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

    ElapsedTime.sStandardOutput = true;

    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend);
        ClearCLDevice lFastestGPUDevice =
                                        lClearCL.getFastestGPUDeviceForImages();
        ClearCLContext lContext = lFastestGPUDevice.createContext())
    {

      Drosophila lDrosophila =
                             Drosophila.getDeveloppedEmbryo(14,
                                                            cWidth,
                                                            cHeight,
                                                            cDepth,
                                                            lFastestGPUDevice);

      System.out.println("grid size:"
                         + Arrays.toString(lDrosophila.getGridDimensions()));
      // lDrosophila.open3DViewer();
      // lDrosophila.getViewer().setDisplayRadius(false);

      DrosophilaHistoneFluorescence lDrosoFluo =
                                               new DrosophilaHistoneFluorescence(lContext,
                                                                                 lDrosophila,
                                                                                 cWidth,
                                                                                 cHeight,
                                                                                 cDepth);

      ClearCLImageViewer lOpenViewer = lDrosoFluo.openViewer();
      Slider lZSlider = lOpenViewer.getZSlider();

      lDrosophila.simulationSteps(cStartSimulationStep, 1);

      Timming lTimming = new Timming();

      int i = 0;
      boolean lAbort = false;
      while (lOpenViewer.isShowing() && !lAbort)
      {
        // System.out.println("i=" + i);
        lTimming.syncAtPeriod(5000);

        ElapsedTime.measure(i % cDisplayPeriod == 0,
                            "dynamics",
                            () -> lDrosophila.simulationSteps(1, 1));

        if (i % cDisplayPeriod == 0)
        {
          lDrosoFluo.clear(true);

          if (cRenderFullStack)
            lDrosoFluo.render(0, (int) lDrosoFluo.getDepth(), true);
          else
            lDrosoFluo.render((int) lZSlider.valueProperty().get()
                              - 1,
                              (int) lZSlider.valueProperty().get()
                                   + 1,
                              true);

          lAbort = checkMaxGridCellOccupancy(lDrosophila, lAbort);
        }

        i++;
      }

      lDrosoFluo.close();

    }

  }

  private boolean checkMaxGridCellOccupancy(Drosophila lDrosophila,
                                            boolean lAbort)
  {
    float lMaximalCellOccupancy =
                                lDrosophila.getNeighborhoodGrid()
                                           .getMaximalCellOccupancy();

    System.out.println("lMaximalCellOccupancy="
                       + lMaximalCellOccupancy);

    if (lMaximalCellOccupancy >= 0.99f)
    {
      System.err.println("MAXIMAL OCCUPANCY REACHED!!!");
      lAbort = true;
    }
    return lAbort;
  }

}
