package simbryo.psystem.forcefield.external.impl;

import simbryo.psystem.ParticleSystem;
import simbryo.psystem.forcefield.external.ExternalForceFieldBase;
import simbryo.psystem.forcefield.external.ExternalForceFieldInterface;

/**
 * Applies a spheri(petal+/fugal-) force to the particles.
 *
 *
 * @author royer
 */
public class SphericalForceField extends ExternalForceFieldBase
                                 implements
                                 ExternalForceFieldInterface
{

  private volatile float mRadius;
  private float[] mCenter;

  /**
   * Constructs a spheri(petal+/fugal-) force field. If the the force is
   * positive then it is a spheripetal force, otherwise it is a spherifugal
   * force.
   * 
   * @param pForceIntensity
   *          force intensity
   * @param pRadius
   *          sphere radius
   * @param pCenter
   *          sphere center
   */
  public SphericalForceField(float pForceIntensity,
                             float pRadius,
                             float... pCenter)
  {
    super(pForceIntensity);
    mRadius = pRadius;
    mCenter = pCenter;

  }

  @Override
  public void applyForceField(int pBeginId,
                              int pEndId,
                              ParticleSystem pParticleSystem)
  {
    final int lDimension = pParticleSystem.getDimension();
    
    final float[] lPositionsRead = pParticleSystem.getPositions().getReadArray();
    final float[] lPositionsWrite = pParticleSystem.getPositions().getWriteArray();
    final float[] lVelocitiesRead = pParticleSystem.getVelocities().getReadArray();
    final float[] lVelocitiesWrite = pParticleSystem.getVelocities().getWriteArray();

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

      float lDistance = (float) Math.sqrt(lSquaredLength);

      float lInverseLengthTimesForce = mForceIntensity
                                                / lDistance;

      float lSignedDistanceToSphere = (lDistance - mRadius);

      float lForceSign = Math.signum(lSignedDistanceToSphere);

      for (int d = 0; d < lDimension; d++)
      {
        lVelocitiesWrite[i + d] = lVelocitiesRead[i + d]
                                  + lVector[d] * lForceSign
                                    * lInverseLengthTimesForce;
      }

    }

    pParticleSystem.getVelocities().swap();
  }

}
