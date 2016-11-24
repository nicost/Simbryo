package embryosim.embryo.demo;

import org.junit.Test;

import embryosim.embryo.zoo.Drosophila;
import embryosim.embryo.zoo.Organoid;
import embryosim.embryo.zoo.Spheroid;
import embryosim.util.timing.Timming;

public class EmbryoDemo
{

  @Test
  public void demoOrganoid() throws InterruptedException
  {

    Organoid lOrganoid = new Organoid(1);

    lOrganoid.open3DViewer();

    Timming lTimming = new Timming();

    while (lOrganoid.getViewer().isShowing())
    {
      lTimming.syncAtPeriod(10);

      if (lOrganoid.getTimeStepIndex() % 500 == 0)
        lOrganoid.triggerCellDivision();

      lOrganoid.simulationSteps(1);

    }

    lOrganoid.getViewer().waitWhileShowing();
  }

  @Test
  public void demoSpheroid() throws InterruptedException
  {
    Spheroid lSpheroid = new Spheroid(1);

    lSpheroid.open3DViewer();

    Timming lTimming = new Timming();
    
    while (lSpheroid.getViewer().isShowing())
    {
      lTimming.syncAtPeriod(10);

      if (lSpheroid.getTimeStepIndex() % 500 == 0)
        lSpheroid.triggerCellDivision();

      lSpheroid.simulationSteps(1);
    }

    lSpheroid.getViewer().waitWhileShowing();
  }
  
  @Test
  public void demoDrosophila() throws InterruptedException
  {
    Drosophila lDrosophila = new Drosophila(1);

    lDrosophila.open3DViewer();

    Timming lTimming = new Timming();
    
    while (lDrosophila.getViewer().isShowing())
    {
      lTimming.syncAtPeriod(10);

      if (lDrosophila.getTimeStepIndex() % 500 == 0)
        lDrosophila.triggerCellDivision();

      lDrosophila.simulationSteps(1);
    }

    lDrosophila.getViewer().waitWhileShowing();
  }

}
