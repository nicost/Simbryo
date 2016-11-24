package embryosim.psystem.forcefield.external.impl;

import embryosim.psystem.forcefield.external.ExternalForceFieldBase;
import embryosim.psystem.forcefield.external.ExternalForceFieldInterface;
import embryosim.util.DoubleBufferingFloatArray;

/**
 * Applies a spheri(petal+/fugal-) force to the particles. if the the force is
 * positive then it is a spheripetal force, otherwise it is a spherifugal force.
 * 
 * @param pForce
 *          force intesnity
 * @param pRadius
 *          sphere radius
 * @param pCenter
 *          sphere center
 */
public class SphericalForceField extends ExternalForceFieldBase
                                 implements
                                 ExternalForceFieldInterface
{

  private volatile float mRadius;
  private float[] mCenter;

  public SphericalForceField(float pForce,
                             float pRadius,
                             float... pCenter)
  {
    super(pForce);
    mRadius = pRadius;
    mCenter = pCenter;

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
        float cx = mCenter[d];
        float dx = cx - px;
        lVector[d] = dx;

        lSquaredLength += dx * dx;
      }

      float lDistance = (float) Math.sqrt(lSquaredLength);

      float lInverseLengthTimesForce = (float) (mForce / lDistance);

      float lSignedDistanceToSphere = (lDistance - mRadius);

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
