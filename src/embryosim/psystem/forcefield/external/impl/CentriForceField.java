package embryosim.psystem.forcefield.external.impl;

import embryosim.neighborhood.NeighborhoodCellGrid;
import embryosim.psystem.forcefield.external.ExternalForceFieldBase;
import embryosim.psystem.forcefield.external.ExternalForceFieldInterface;
import embryosim.util.DoubleBufferingFloatArray;

/**
 * Applies a centri(petal+/fugal-) force to the particles. if the the force is
 * positive then it is a centripetal force, otherwise it is a centrifugal force.
 * 
 * @param pForce
 *          force intensity
 * @param pCenter
 *          force field center
 */
public class CentriForceField extends ExternalForceFieldBase
                              implements ExternalForceFieldInterface
{

  private volatile float mRadius;
  private float[] mCenter;

  public CentriForceField(float pForce,
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

      float lInverseLengthTimesForce =
                                     (float) (mForce
                                              / Math.sqrt(lSquaredLength));

      for (int d = 0; d < pDimension; d++)
      {
        lVelocitiesWrite[i + d] = lVelocitiesRead[i + d]
                                  + lVector[d]
                                    * lInverseLengthTimesForce;
      }

    }

    pVelocities.swap();
  }

}
