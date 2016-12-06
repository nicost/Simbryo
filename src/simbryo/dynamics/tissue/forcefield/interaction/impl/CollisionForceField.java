package simbryo.dynamics.tissue.forcefield.interaction.impl;

import simbryo.dynamics.tissue.forcefield.interaction.InteractionForceFieldBase;
import simbryo.dynamics.tissue.forcefield.interaction.InteractionForceFieldInterface;
import simbryo.dynamics.tissue.neighborhood.NeighborhoodGrid;
import simbryo.dynamics.tissue.psystem.ParticleSystem;
import simbryo.util.geom.GeometryUtils;

/**
 * This interaction force field applies a force to each particle that prevents
 * particles influence radii from overlapping. This in effect is a way to handle
 * particle-particle collisions.
 * 
 * 
 */
public class CollisionForceField extends InteractionForceFieldBase
                                 implements
                                 InteractionForceFieldInterface
{

  private static final float cGapCorrectionFactor = 0.95f;

  private float mDrag;
  private boolean mForbidOverlap = true;

  private int[] mNeighboorsArray;

  /**
   * Constructs a collision force field given a force intensity and drag
   * coefficient. The drag coefficient is often necessary to prevent excessive
   * bouncing.
   * 
   * @param pForceIntensity
   *          constant force applied during collision.
   * @param pDrag
   *          drag applied to slow down particles.
   * 
   * @param pForbidOverlap
   *          if true, overlaps between particles spheres of influence will be
   *          forbidden by forcibly displacing the spheres after an overlap is
   *          detected.
   */
  public CollisionForceField(float pForceIntensity,
                             float pDrag,
                             boolean pForbidOverlap)
  {
    super(pForceIntensity);
    mDrag = pDrag;
    mForbidOverlap = pForbidOverlap;
  }

  @Override
  public void applyForceField(int pBeginId,
                              int pEndId,
                              ParticleSystem pParticleSystem)
  {
    final int lDimension = pParticleSystem.getDimension();

    NeighborhoodGrid lNeighborhoodGrid =
                                       pParticleSystem.getNeighborhoodGrid();
    final int lMaxNumberOfParticlesPerGridCell =
                                               lNeighborhoodGrid.getMaxParticlesPerGridCell();
    final int lTotalNumberOfCells = lNeighborhoodGrid.getVolume();

    final float[] lPositionsRead = pParticleSystem.getPositions()
                                                  .getReadArray();
    final float[] lPositionsWrite = pParticleSystem.getPositions()
                                                   .getWriteArray();
    final float[] lVelocitiesRead = pParticleSystem.getVelocities()
                                                   .getReadArray();
    final float[] lVelocitiesWrite = pParticleSystem.getVelocities()
                                                    .getWriteArray();
    final float[] lRadii =
                         pParticleSystem.getRadii().getCurrentArray();

    pParticleSystem.getVelocities().copyAndMult(pBeginId * lDimension,
                                                pEndId * lDimension,
                                                mDrag);

    int lNeighboorhoodListMaxLength = lMaxNumberOfParticlesPerGridCell
                                      * lTotalNumberOfCells;
    if (mNeighboorsArray == null
        || mNeighboorsArray.length != lNeighboorhoodListMaxLength)
    {
      mNeighboorsArray = new int[lNeighboorhoodListMaxLength];
    }

    final int[] lNeighboors = mNeighboorsArray;
    final int[] lNeighboorsTemp = mNeighboorsArray;
    final float[] lCellCoord = new float[lDimension];
    final int[] lCellCoordMin = new int[lDimension];
    final int[] lCellCoordMax = new int[lDimension];
    final int[] lCellCoordCurrent = new int[lDimension];

    for (int idu =
                 pBeginId, i = idu
                               * lDimension; idu < pEndId; idu++, i +=
                                                                    lDimension)
    {

      final float ru = lRadii[idu];

      int lNumberOfNeighboors =
                              lNeighborhoodGrid.getAllNeighborsForParticle(lNeighboors,
                                                                           lNeighboorsTemp,
                                                                           lPositionsRead,
                                                                           idu,
                                                                           ru,
                                                                           lCellCoord,
                                                                           lCellCoordMin,
                                                                           lCellCoordMax,
                                                                           lCellCoordCurrent);

      for (int k = 0; k < lNumberOfNeighboors; k++)
      {
        final int idv = lNeighboors[k];

        final float rv = lRadii[idv];

        // testing bounding box collision:
        if (idu < idv
            && GeometryUtils.detectBoundingBoxCollision(lDimension,
                                                        lPositionsRead,
                                                        ru,
                                                        rv,
                                                        idu,
                                                        idv)) //
        {
          int j = idv * lDimension;
          /// System.out.println("BB collision");
          float lDistance = GeometryUtils.computeDistance(lDimension,
                                                          lPositionsRead,
                                                          idu,
                                                          idv);
          float lGap = lDistance - ru - rv;

          // testing sphere collision:
          if (lGap < 0 && lDistance != 0)
          {

            // Collision -> apply force.
            float lInvDistance = 1.0f / lDistance;
            float lInvDistanceWithForce = mForceIntensity
                                          * lInvDistance;

            for (int d = 0; d < lDimension; d++)
            {
              float lDelta = lPositionsRead[i + d]
                             - lPositionsRead[j + d];

              float lAxisVector = lInvDistanceWithForce * lDelta;

              lVelocitiesWrite[i + d] += lAxisVector;
              lVelocitiesWrite[j + d] += -lAxisVector;

              if (mForbidOverlap)
              {

                float lOverlapCorrection = lInvDistance * lDelta
                                           * (cGapCorrectionFactor
                                              * -lGap);
                lPositionsWrite[i + d] = lPositionsRead[i + d]
                                         + lOverlapCorrection;
                lPositionsWrite[j + d] = lPositionsRead[j + d]
                                         + -lOverlapCorrection;
              }
            }

          }
        }

      }

    }

    if (mForbidOverlap)
      pParticleSystem.getPositions().swap();

    pParticleSystem.getVelocities().swap();

  }

}
