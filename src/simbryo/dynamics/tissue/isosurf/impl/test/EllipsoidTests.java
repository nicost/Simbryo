package simbryo.dynamics.tissue.isosurf.impl.test;

import static org.junit.Assert.*;

import org.junit.Test;

import simbryo.dynamics.tissue.isosurf.impl.Ellipsoid;

public class EllipsoidTests
{

  @Test
  public void test()
  {
    Ellipsoid lEllipsoid = new Ellipsoid(0.5f, 0.5f, 0.5f, 1, 0.5f);

    lEllipsoid.clear();
    lEllipsoid.addCoordinate(0.5f);
    lEllipsoid.addCoordinate(0.75f);

    //System.out.println(lEllipsoid.getDistance());
    //System.out.println(lEllipsoid.getNormalizedGardient(0));
    //System.out.println(lEllipsoid.getNormalizedGardient(1));

    assertEquals(0.0f, lEllipsoid.getDistance(), 0f);
    assertEquals(0.0f, lEllipsoid.getNormalizedGardient(0), 0f);
    assertEquals(-1.0f, lEllipsoid.getNormalizedGardient(1), 0f);
    
    lEllipsoid.clear();
    lEllipsoid.addCoordinate(1f);
    lEllipsoid.addCoordinate(1f);
    
    //System.out.println(lEllipsoid.getDistance());
    //System.out.println(lEllipsoid.getNormalizedGardient(0));
    //System.out.println(lEllipsoid.getNormalizedGardient(1));

    assertEquals(0.61f, lEllipsoid.getDistance(), 0.01f);
    assertEquals(-0.24f, lEllipsoid.getNormalizedGardient(0), 0.01f);
    assertEquals(-0.97f, lEllipsoid.getNormalizedGardient(1), 0.01f);
    
    lEllipsoid.clear();
    lEllipsoid.addCoordinate(1f);
    lEllipsoid.addCoordinate(0.5f);
    
    //System.out.println(lEllipsoid.getDistance());
    //System.out.println(lEllipsoid.getNormalizedGardient(0));
    //System.out.println(lEllipsoid.getNormalizedGardient(1));

    assertEquals(0.0f, lEllipsoid.getDistance(), 0.01f);
    assertEquals(-1f, lEllipsoid.getNormalizedGardient(0), 0.01f);
    assertEquals(-0.0f, lEllipsoid.getNormalizedGardient(1), 0.01f);

  }

}
