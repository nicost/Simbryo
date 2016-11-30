package simbryo.psystem.forcefield.external.impl;

import simbryo.psystem.ParticleSystem;
import simbryo.psystem.forcefield.external.ExternalForceFieldBase;
import simbryo.psystem.forcefield.external.ExternalForceFieldInterface;

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

    final float[] lGradientVector = new float[lDimension];

    for (int i = lIndexStart; i < lIndexEnd; i += lDimension)
    {
      float lSquaredLength = 0;
      for (int d = 0; d < lDimension; d++)
      {
        float px = lPositionsRead[i + d];
        float cx = mCenterAndAxis[d];
        float ax = mCenterAndAxis[lDimension + d];
        float dx = (cx - px) / ax;
        lGradientVector[d] = dx / ax;

        lSquaredLength += dx * dx;
      }

      float lLength = (float) Math.sqrt(lSquaredLength);

      float lInverseLengthTimesForce = mForceIntensity / lLength;

      float lSignedDistanceToEllipsoid = (lLength - mRadius);

      float lForceSign = Math.signum(lSignedDistanceToEllipsoid);

      for (int d = 0; d < lDimension; d++)
      {
        lVelocitiesWrite[i + d] = lVelocitiesRead[i + d]
                                  + lGradientVector[d] * lForceSign
                                    * lInverseLengthTimesForce;
      }

    }

    pParticleSystem.getVelocities().swap();
  }

}
