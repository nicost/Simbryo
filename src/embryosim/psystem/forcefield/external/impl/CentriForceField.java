package embryosim.psystem.forcefield.external.impl;

import embryosim.psystem.forcefield.external.ExternalForceFieldBase;
import embryosim.psystem.forcefield.external.ExternalForceFieldInterface;
import embryosim.util.DoubleBufferingFloatArray;

/**
 * This force field applies a centri(petal+/fugal-) force to the particles.
 * 
 * @author royer
 */
public class CentriForceField extends ExternalForceFieldBase
                              implements ExternalForceFieldInterface
{

  private float[] mCenter;

  /**
   * Constructs a centri(petal+/fugal-) force field given a force intensity and
   * center. if the the force intensity is positive then it is a centripetal
   * force, otherwise it is a centrifugal force.
   * 
   * @param pForceIntensity
   *          force intensity
   * @param pCenter
   *          force field center
   */
  public CentriForceField(float pForceIntensity, float... pCenter)
  {
    super(pForceIntensity);
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
                                     (float) (mForceIntensity
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
