package embryosim.util.geom;

public class GeometryUtils
{
  public static float computeDistance(int pDimension,
                                      float[] pPositions,
                                      int pIdu,
                                      int pIdv)
  {
    return (float) Math.sqrt(computeSquaredDistance(pDimension,
                                                    pPositions,
                                                    pIdu,
                                                    pIdv));
  }

  public static float computeSquaredDistance(int pDimension,
                                             float[] pPositions,
                                             int pIdu,
                                             int pIdv)
  {

    final int u = pIdu * pDimension;
    final int v = pIdv * pDimension;

    float lDistance = 0;

    for (int d = 0; d < pDimension; d++)
    {
      float lAxisDistance = pPositions[u + d] - pPositions[v + d];
      lDistance += lAxisDistance * lAxisDistance;
    }

    return lDistance;
  }

  public static boolean detectBoundingBoxCollision(int pDimension,
                                                   float[] pPositions,
                                                   float pR1,
                                                   float pR2,
                                                   int pIdu,
                                                   int pIdv)
  {

    final int u = pIdu * pDimension;
    final int v = pIdv * pDimension;

    for (int d = 0; d < pDimension; d++)
    {
      float lAxisDistance = Math.abs(pPositions[u + d]
                                     - pPositions[v + d]);
      float lAxisGap = lAxisDistance - pR1 - pR2;

      if (lAxisGap > 0)
        return false;
    }

    return true;
  }
}
