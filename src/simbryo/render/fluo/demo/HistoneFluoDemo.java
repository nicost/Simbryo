package simbryo.render.fluo.demo;

import java.io.IOException;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackends;
import clearcl.util.ElapsedTime;
import simbryo.embryo.zoo.Drosophila;
import simbryo.render.fluo.HistoneFluo;
import simbryo.util.timing.Timming;

public class HistoneFluoDemo
{

  @Test
  public void demo() throws IOException, InterruptedException
  {
    ElapsedTime.sStandardOutput = true;
    
    try (
        ClearCL lClearCL =
                         new ClearCL(ClearCLBackends.getBestBackend()))
    {
      ClearCLDevice lFastestGPUDevice =
                                      lClearCL.getFastestGPUDevice();

      Drosophila lDrosophila = new Drosophila();

      System.out.println("grid size:"+lDrosophila.getGridSize());
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
        lTimming.syncAtPeriod(1);
        lDrosophila.simulationSteps(1, 1);
        
        

        if (i % 10 == 0)
        {
          System.out.format("avg part per cell : %g \n",lDrosophila.getNeighborhoodGrid().getAverageNumberOfParticlesPerGridCell());
          System.out.format("max part per cell : %d \n",lDrosophila.getNeighborhoodGrid().getMaximalEffectiveNumberOfParticlesPerGridCell());
          System.out.format("occupancy : %g \n",lDrosophila.getNeighborhoodGrid().getAverageCellOccupancy());
          lHistoneFluo.clear();
          lHistoneFluo.renderSmart(0,100);
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
