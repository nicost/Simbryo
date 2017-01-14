package simbryo.phantom.fluo.impl.drosophila.demo;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;
import javafx.scene.control.Slider;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.phantom.ClearCLPhantomRendererUtils;
import simbryo.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.util.timing.Timming;

public class HistoneFluoDrosophilaDemo
{

  @Test
  public void demo() throws IOException, InterruptedException
  {
    int lWidth = 512;
    int lHeight = 512;
    int lDepth = 512;

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
                                                                                 lWidth,
                                                                                 lHeight,
                                                                                 lDepth);

      System.out.println("lGridDimensions="
                         + Arrays.toString(lGridDimensions));

      Drosophila lDrosophila = new Drosophila(16,lGridDimensions);

      System.out.println("grid size:"
                         + Arrays.toString(lDrosophila.getGridDimensions()));
      // lDrosophila.open3DViewer();
      // lDrosophila.getViewer().setDisplayRadius(false);

      DrosophilaHistoneFluorescence lDrosoFluo =
                                       new DrosophilaHistoneFluorescence(lFastestGPUDevice,
                                                               lDrosophila,
                                                               lWidth,
                                                               lHeight,
                                                               lDepth);
      
      
      ClearCLImageViewer lOpenViewer = lDrosoFluo.openViewer();
      Slider lZSlider = lOpenViewer.getZSlider();

      lDrosophila.simulationSteps(13000, 1);

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
          lDrosoFluo.clear();
          
          
          //lDrosoFluo.renderSmart(0, (int) lDrosoFluo.getDepth());
          lDrosoFluo.render((int)lZSlider.valueProperty().get()-1,(int)lZSlider.valueProperty().get()+1);

          /*float lMaximalCellOccupancy =
                                      lDrosophila.getNeighborhoodGrid()
                                                 .getMaximalCellOccupancy();

          System.out.println("lMaximalCellOccupancy="
                             + lMaximalCellOccupancy);

          if (lMaximalCellOccupancy >= 0.99f)
          {
            System.err.println("MAXIMAL OCCUPANCY REACHED!!!");
            lAbort = true;
          }/**/
          //Thread.sleep(5);
        }

        i++;
      }

      lDrosoFluo.close();

    }

  }

}
