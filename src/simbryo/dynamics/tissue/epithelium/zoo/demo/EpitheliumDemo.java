package simbryo.dynamics.tissue.epithelium.zoo.demo;

import javafx.scene.paint.Color;

import org.junit.Test;

import simbryo.dynamics.tissue.epithelium.zoo.TwoLayeredEpithelium;
import simbryo.particles.viewer.three.ParticleViewer3D;
import simbryo.util.timing.Timming;

/**
 * Basic demos for Embryo dynamics
 *
 * @author royer
 */
public class EpitheliumDemo
{

  /**
   * @throws InterruptedException
   *           NA
   */
  @Test
  public static void demoTwoLayeredEpithelium() throws InterruptedException
  {

    float radius = 0.005f;

    TwoLayeredEpithelium lTwoLayeredEpithelium =
                                               new TwoLayeredEpithelium(128,
                                                                        2f * radius
                                                                             - 0.00000001f,
                                                                        radius);

    ParticleViewer3D lOpen3dViewer =
                                   lTwoLayeredEpithelium.open3DViewer();

    lOpen3dViewer.setColorClosure((id) -> {
      int lCellLabel =
                     (int) lTwoLayeredEpithelium.getCellLabelProperty()
                                                .getArray()
                                                .getCurrentArray()[id];
      switch (lCellLabel) {
         case 0: return Color.BLUE;
         case 1 : return Color.RED;
         case 2 : return Color.GREEN;
         case 3 : return Color.FLORALWHITE;
      }
      return Color.BLUE;
      //return lCellLabel == 0 ? Color.BLUE : Color.RED;
    });/**/

    Timming lTimming = new Timming();

    while (lTwoLayeredEpithelium.getViewer().isShowing())
    {
      lTimming.syncAtPeriod(0);
      lTwoLayeredEpithelium.simulationSteps(1);
    }

    lTwoLayeredEpithelium.getViewer().waitWhileShowing();
  }

  public static void main(String[] args)
  {
    try
    {
      demoTwoLayeredEpithelium();
    }
    catch (InterruptedException ex)
    {
      System.out.println("Got interrupted");
    }
  }

}
