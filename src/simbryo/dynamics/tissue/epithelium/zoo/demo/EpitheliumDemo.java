package simbryo.dynamics.tissue.epithelium.zoo.demo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.geometry.Point3D;
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
public class EpitheliumDemo {

   private static final Color[] COLORS = {Color.RED, Color.GREEN, Color.FLORALWHITE,
      Color.BROWN, Color.CYAN, Color.ORANGE, Color.HOTPINK, Color.YELLOW, 
      Color.DARKGOLDENROD, Color.DARKMAGENTA, Color.TEAL, Color.DARKORANGE};
   private final static int STARTNRPARTICLES = 128;
   private final static int ENDNRPARTICLES = STARTNRPARTICLES << 6; // avg. # of cell divisions 

   /**
    * @throws InterruptedException NA
    */
   @Test
   public static void demoTwoLayeredEpithelium() throws InterruptedException {
      
      long startTime = System.currentTimeMillis();

      float radius = 0.005f;
      final float diameter = 2 * radius;
      final float layerDistance =   diameter - 0.00000001f;

      TwoLayeredEpithelium lTwoLayeredEpithelium
              = new TwoLayeredEpithelium(STARTNRPARTICLES,
                      layerDistance,
                      radius);

      ParticleViewer3D lOpen3dViewer
              = lTwoLayeredEpithelium.open3DViewer();

      lOpen3dViewer.setColorClosure((id) -> {
         int lCellLabel
                 = (int) lTwoLayeredEpithelium.getCellLabelProperty()
                         .getArray()
                         .getCurrentArray()[id];
         if (lCellLabel <= COLORS.length) {
            return COLORS[lCellLabel - 1];
         }
         return Color.BLUE;
      });

      // Actually run the simulation
      Timming lTimming = new Timming();
      long counter = 0;
      boolean done = false;
      while (lTwoLayeredEpithelium.getViewer().isShowing() && !done) {
         lTimming.syncAtPeriod(0);
         lTwoLayeredEpithelium.simulationSteps(1);
         if (counter % 100 == 0) {
            lTwoLayeredEpithelium.getViewer().updateDisplay(true);
         }
         counter++;
         if (lTwoLayeredEpithelium.getNumberOfParticles() > ENDNRPARTICLES) {
            done = true;
         }
      }
      
      if (lTwoLayeredEpithelium.getViewer().isShowing()) {
         lTwoLayeredEpithelium.getViewer().updateDisplay(true);
      }
      
      // Analyze the result - only look at the colored nuclei
      List<List<Point3D>> pois = new ArrayList<>();
      for (int testLabel = 1; testLabel <= COLORS.length; testLabel++) {
         List<Point3D> points = new ArrayList<>();
         pois.add(points);
         for (int id = 0; id < lTwoLayeredEpithelium.getNumberOfParticles(); id++) {
            int lCellLabel = (int) lTwoLayeredEpithelium.
                    getCellLabelProperty().getArray().getCurrentArray()[id];
            if (lCellLabel == testLabel) {
               float[] point = lTwoLayeredEpithelium.getPosition(id);
               points.add(new Point3D(point[0], point[1], point[2]));
            }
         }
      }
      
      System.out.println("Simulation, starting particles: " + STARTNRPARTICLES + 
              " final particles: " + 
              lTwoLayeredEpithelium.getNumberOfParticles() + 
              ", Layer distance: " + 
              layerDistance / diameter + 
              " Simulation steps: " + 
              counter);
      System.out.println("#\tNeighbors\tAvg. Distance\tMaxDistance");
      int dataSetId = 0;
      // express distances as diameters 
      for (List<Point3D> points : pois) {
         int nrNeighbors = getNrNeighbors(points, 2.5f * radius);
         float avgPairWiseDistance = getAvgPairWiseDistance(points) / diameter;
         float maxDistance = getMaxDistance(points) / diameter ;
         System.out.println("" + dataSetId + "\t" + nrNeighbors + "\t" + 
                 avgPairWiseDistance + "\t" + maxDistance);
         dataSetId++;
      }
      System.out.println("Simulation took: " + 
              ((System.currentTimeMillis() - startTime) / 1000) + " seconds");

      lTwoLayeredEpithelium.getViewer().waitWhileShowing();
   }
   

   
   public static int getNrNeighbors(List<Point3D> points, float maxDistance) {
      int nrNeighbors = 0;
      Iterator<Point3D> tester = points.iterator();
      while (tester.hasNext()) {
         Point3D testPoint = tester.next();
         for (Point3D target : points) {
            if (testPoint != target) {
               double distance = testPoint.distance(target);
               if (distance < maxDistance) {
                  nrNeighbors++;
               }
            }
         }
      }
      return nrNeighbors;
   }
   
   
   public static float getAvgPairWiseDistance(List<Point3D> points) {
      float totalDistance = 0f;
      int counter = 0;
      Iterator<Point3D> tester = points.iterator();
      while (tester.hasNext()) {
         Point3D testPoint = tester.next();
         for (Point3D target : points) {
            if (testPoint != target) {
               totalDistance += testPoint.distance(target);
               counter++;
            }
         }
      }
      return totalDistance / counter;
   }
   
   
   public static float getMaxDistance(List<Point3D> points) {
      double maxDistance = 0d;
      Iterator<Point3D> tester = points.iterator();
      while (tester.hasNext()) {
         Point3D testPoint = tester.next();
         for (Point3D target : points) {
            if (testPoint != target) {
               double distance = testPoint.distance(target);
               if (distance > maxDistance) {
                  maxDistance = distance;
               }
            }
         }
      }
      return (float) maxDistance;
   }
   
    
   public static void main(String[] args) {
      try {
         demoTwoLayeredEpithelium();
      } catch (InterruptedException ex) {
         System.out.println("Got interrupted");
      }
   }

}
