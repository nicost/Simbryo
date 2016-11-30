package embryosim.psystem.forcefield.external.impl;

import embryosim.psystem.ParticleSystem;
import embryosim.psystem.forcefield.external.ExternalForceFieldBase;
import embryosim.psystem.forcefield.external.ExternalForceFieldInterface;

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
  public void applyForceField(int pBeginId,
                              int pEndId,
                              ParticleSystem pParticleSystem)
  {
    final int lDimension = pParticleSystem.getDimension();

    final float[] lPositionsRead = pParticleSystem.getPositions()
                                                  .getReadArray();
    final float[] lPositionsWrite = pParticleSystem.getPositions()
                                                   .getWriteArray();
    final float[] lVelocitiesRead = pParticleSystem.getVelocities()
                                                   .getReadArray();
    final float[] lVelocitiesWrite = pParticleSystem.getVelocities()
                                                    .getWriteArray();

    final int lIndexStart = pBeginId * lDimension;
    final int lIndexEnd = pEndId * lDimension;

    final float[] lVector = new float[lDimension];

    for (int i = lIndexStart; i < lIndexEnd; i += lDimension)
    {
      float lSquaredLength = 0;
      for (int d = 0; d < lDimension; d++)
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

      for (int d = 0; d < lDimension; d++)
      {
        lVelocitiesWrite[i + d] = lVelocitiesRead[i + d]
                                  + lVector[d]
                                    * lInverseLengthTimesForce;
      }

    }

    pParticleSystem.getVelocities().swap();
  }

}
