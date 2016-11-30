package embryosim.embryo.demo;

import org.junit.Test;

import embryosim.embryo.zooo.Drosophila;
import embryosim.embryo.zooo.Organoid;
import embryosim.embryo.zooo.Spheroid;
import embryosim.util.timing.Timming;

public class EmbryoDemo
{

  @Test
  public void demoOrganoid() throws InterruptedException
  {

    Organoid lOrganoid = new Organoid();

    lOrganoid.open3DViewer();

    Timming lTimming = new Timming();

    while (lOrganoid.getViewer().isShowing())
    {
      lTimming.syncAtPeriod(10);     
      lOrganoid.simulationSteps(1,1);
    }

    lOrganoid.getViewer().waitWhileShowing();
  }

  @Test
  public void demoSpheroid() throws InterruptedException
  {
    Spheroid lSpheroid = new Spheroid();

    lSpheroid.open3DViewer();

    Timming lTimming = new Timming();
    
    while (lSpheroid.getViewer().isShowing())
    {
      lTimming.syncAtPeriod(10);
      lSpheroid.simulationSteps(1,1);
    }

    lSpheroid.getViewer().waitWhileShowing();
  }
  
  @Test
  public void demoDrosophila() throws InterruptedException
  {
    Drosophila lDrosophila = new Drosophila();

    lDrosophila.open3DViewer();
    
    lDrosophila.getViewer().setDisplayRadius(false);

    Timming lTimming = new Timming();
    
    while (lDrosophila.getViewer().isShowing())
    {
      lTimming.syncAtPeriod(10);
      lDrosophila.simulationSteps(1,1);
    }

    lDrosophila.getViewer().waitWhileShowing();
  }

}
