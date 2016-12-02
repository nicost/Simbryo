package simbryo.render.fluo.demo;

import java.io.IOException;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackends;
import simbryo.embryo.zoo.Drosophila;
import simbryo.render.fluo.HistoneFluo;
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

      Drosophila lDrosophila = new Drosophila();
      lDrosophila.open3DViewer();
      lDrosophila.getViewer().setDisplayRadius(false);

      HistoneFluo lHistoneFluo = new HistoneFluo(lFastestGPUDevice,
                                                 lDrosophila,
                                                 512,
                                                 512,
                                                 100);

      lHistoneFluo.openFluorescenceImageViewer();

      Timming lTimming = new Timming();

      int i = 0;
      while (lDrosophila.getViewer().isShowing())
      {
        lTimming.syncAtPeriod(10);
        lDrosophila.simulationSteps(1, 1);

        lHistoneFluo.render((int) (i%lHistoneFluo.getDepth()));

        i++;
        Thread.sleep(10);
      }

      lDrosophila.getViewer().waitWhileShowing();

    }

  }

}
