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
public class EllipsoidalConstraintForceField extends
                                             ExternalForceFieldBase
                                             implements
                                             ExternalForceFieldInterface
{

  private volatile float mRadius;
  private float[] mCenterAndAxis;
  private boolean mZeroInside;
  private boolean mConstraintWithRadius;

  /**
   * Constructs an ellipsoidal constraint force field. The force intensity sign
   * decides whether the force points towards the center or away from the
   * center. A boolean flag decides whether the force is zero inside or outside
   * of the ellipsoid. This force field can be used to constrain the position of
   * particles inside or outside of a given ellipsoid.
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
   * @param pZeroInside
   *          if true the force field is zero inside of the ellipsoid, otherwise
   *          it is zero outside.
   * @param pForceIntensity
   *          force intensity
   * @param pRadius
   *          radius
   * @param pCenterAndAxis
   *          force field center + ellipsoid axes ratios
   */

  public EllipsoidalConstraintForceField(boolean pZeroInside,
                                         boolean pConstraintWithRadius,
                                         float pForceIntensity,
                                         float pRadius,
                                         float... pCenterAndAxis)
  {
    super(pForceIntensity);
    mZeroInside = pZeroInside;
    mConstraintWithRadius = pConstraintWithRadius;
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
    
    final float[] lRadiiRead = pParticleSystem.getRadii().getWriteArray();

    float lForceIntensity = mForceIntensity;
    boolean lConstraintInside = mZeroInside;
    boolean lConstraintWithRadius = mConstraintWithRadius;

    final int lIndexStart = pBeginId * lDimension;
    final int lIndexEnd = pEndId * lDimension;

    final float[] lGradientVector = new float[lDimension];

    for (int i =
               lIndexStart, id =
                               pBeginId; i < lIndexEnd; i +=
                                   lDimension, id++)
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

      float lInverseLengthTimesForce = lForceIntensity / lLength;

      float lRadius = lRadiiRead[id];

      float lSignedDistanceToEllipoid =
                                      (lLength
                                       - (mRadius
                                          + (lConstraintInside ? -1
                                                               : 1)
                                            * (lConstraintWithRadius ? lRadius
                                                                     : 0)));

      if (lConstraintInside && lSignedDistanceToEllipoid >= 0)
        for (int d = 0; d < lDimension; d++)
        {
          lVelocitiesWrite[i + d] = lVelocitiesRead[i + d]
                                    + lGradientVector[d]
                                      * lInverseLengthTimesForce;
        }
      else if (!lConstraintInside && lSignedDistanceToEllipoid < 0)
        for (int d = 0; d < lDimension; d++)
        {
          lVelocitiesWrite[i + d] = lVelocitiesRead[i + d]
                                    - lGradientVector[d]
                                      * lInverseLengthTimesForce;
        }
      else
        for (int d = 0; d < lDimension; d++)
          lVelocitiesWrite[i + d] = lVelocitiesRead[i + d];

    }

    pParticleSystem.getVelocities().swap();
  }

}
