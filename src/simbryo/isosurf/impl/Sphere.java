package simbryo.isosurf.impl;

/**
 * A sphere is just a special kind of ellipsoid.
 *
 * @author royer
 */
public class Sphere extends Ellipsoid
{

  public Sphere(float pRadius, float... pCenter)
  {
    super(pRadius, addAxis(pCenter));
  }

  private static float[] addAxis(float[] pCenter)
  {
    final int lDimension = pCenter.length;
    float[] lCenterAndAxis = new float[lDimension * 2];
    for (int d = 0; d < lDimension; d++)
    {
      lCenterAndAxis[d] = pCenter[d];
      lCenterAndAxis[lDimension + d] = 1;
    }
    return lCenterAndAxis;
  }

}
