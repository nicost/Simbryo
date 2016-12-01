package simbryo.fluo.demo;

import java.io.IOException;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackends;
import simbryo.embryo.zoo.Drosophila;
import simbryo.fluo.HistoneFluo;
import simbryo.util.timing.Timming;

public class HistoneFluoDemo
{

  @Test
  public void demo() throws IOException, InterruptedException
  {
    try (
        ClearCL lClearCL =
                         new ClearCL(ClearCLBackends.getBestBackend()))
    {
      ClearCLDevice lFastestGPUDevice =
                                      lClearCL.getFastestGPUDevice();

      HistoneFluo lHistoneFluo = new HistoneFluo(lFastestGPUDevice,
                                                 512,
                                                 512,
                                                 100);

      lHistoneFluo.openFluorescenceImageViewer();

      Drosophila lDrosophila = new Drosophila();

      lDrosophila.open3DViewer();

      lDrosophila.getViewer().setDisplayRadius(false);

      Timming lTimming = new Timming();

      int i = 0;
      while (lDrosophila.getViewer().isShowing())
      {
        lTimming.syncAtPeriod(10);
        lDrosophila.simulationSteps(1, 1);

        lHistoneFluo.render(lDrosophila, 30);

        i++;
        Thread.sleep(10);
      }

      lDrosophila.getViewer().waitWhileShowing();

    }

  }

}
