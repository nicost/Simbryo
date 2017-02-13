package simbryo.util.geom;

/**
 * Geometry utils
 *
 * @author royer
 */
public class GeometryUtils
{
  /**
   * Computes distance between two vectors stored in one contiguous array.
   * 
   * @param pDimension
   *          vector dimension
   * @param pPositions
   *          array
   * @param pIdu
   *          first vector id
   * @param pIdv
   *          second vector id
   * @return distance
   */
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

  /**
   * Computes the squared distance between two vectors stored in one contiguous
   * array.
   * 
   * @param pDimension
   *          vector dimension
   * @param pPositions
   *          array
   * @param pIdu
   *          first vector id
   * @param pIdv
   *          second vector id
   * @return distance
   */
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

  /**
   * Detects bounding box collisions.
   * 
   * @param pDimension
   *          vector dimensions
   * @param pPositions
   *          array of vectors
   * @param pR1
   *          first radius
   * @param pR2
   *          second radius
   * @param pIdu
   *          first vector id
   * @param pIdv
   *          second vector id
   * @return true if bounding boxes collide
   */
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
