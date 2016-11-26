package embryosim.psystem.forcefield.external.impl;

import embryosim.psystem.forcefield.external.ExternalForceFieldBase;
import embryosim.psystem.forcefield.external.ExternalForceFieldInterface;
import embryosim.util.DoubleBufferingFloatArray;

/**
 * This force field applies a ellipsoi(petal+/fugal-) force to the particles.
 * 
 *
 * @author royer
 */
public class EllipsoidalForceField extends ExternalForceFieldBase
                                   implements
                                   ExternalForceFieldInterface
{

  private volatile float mRadius;
  private float[] mCenterAndAxis;

  /**
   * Constructs an ellipsoi(petal+/fugal-) force field. If the the force is
   * positive then it is a ellipsoipetal force, otherwise it is a ellipsoifugal
   * force.
   * 
   * For example:
   * 
   * <pre>
   *  {@code}
   *   new EllipsoidalForceField(0.001, 0.5f, 0.5f, 0.5f, 1.0f, 2.0f, 4.0f)
   * </pre>
   * 
   * Sets a force with center (xc,yc,zc) = (0.5, 0.5, 0.5) and (a,b,c) = (1,2,4)
   * 
   * The equation is: ((x-xc)/a)^2+((y-yc)/b)^2+((z-zc)/c)^2 - R^2 =0
   * 
   * @param pForceIntensity
   *          force intensity
   * @param pRadius
   *          radius
   * @param pCenterAndAxis
   *          force field center + ellipsoid axes ratios
   */

  public EllipsoidalForceField(float pForceIntensity,
                               float pRadius,
                               float... pCenterAndAxis)
  {
    super(pForceIntensity);
    mRadius = pRadius;
    mCenterAndAxis = pCenterAndAxis;

  }

  @Override
  public void applyForceField(int pDimension,
                              int pBeginId,
                              int pEndId,
                              final DoubleBufferingFloatArray pPositions,
                              final DoubleBufferingFloatArray pVelocities,
                              final DoubleBufferingFloatArray pRadii)
  {
    final float[] lPositionsRead = pPositions.getReadArray();
    final float[] lPositionsWrite = pPositions.getWriteArray();
    final float[] lVelocitiesRead = pVelocities.getReadArray();
    final float[] lVelocitiesWrite = pVelocities.getWriteArray();

    final int lIndexStart = pBeginId * pDimension;
    final int lIndexEnd = pEndId * pDimension;

    final float[] lVector = new float[pDimension];

    for (int i = lIndexStart; i < lIndexEnd; i += pDimension)
    {
      float lSquaredLength = 0;
      for (int d = 0; d < pDimension; d++)
      {
        float px = lPositionsRead[i + d];
        float cx = mCenterAndAxis[d];
        float ax = mCenterAndAxis[pDimension + d];
        float dx = (cx - px) / (ax * ax);
        lVector[d] = dx;

        lSquaredLength += dx * dx;
      }

      float lLength = (float) Math.sqrt(lSquaredLength);

      float lInverseLengthTimesForce = (float) (mForceIntensity
                                                / lLength);

      float lSignedDistanceToSphere = (lLength - mRadius);

      float lForceSign = Math.signum(lSignedDistanceToSphere);

      for (int d = 0; d < pDimension; d++)
      {
        lVelocitiesWrite[i + d] = lVelocitiesRead[i + d]
                                  + lVector[d] * lForceSign
                                    * lInverseLengthTimesForce;
      }

    }

    pVelocities.swap();
  }

}
