package embryosim.psystem.forcefield.interaction.impl;

import java.util.SplittableRandom;

import embryosim.neighborhood.NeighborhoodGrid;
import embryosim.psystem.forcefield.interaction.InteractionForceFieldBase;
import embryosim.psystem.forcefield.interaction.InteractionForceFieldInterface;
import embryosim.util.DoubleBufferingFloatArray;

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

  private static final float cGapCorrectionFactor = 1f;
  private static final double cGapCorrectionNoiseFactor = 1e-9f;

  private float mDrag;
  private boolean mPreventOverlap = false;

  private SplittableRandom mRandom = new SplittableRandom();

  private int[] mNeighboorsArray, mNeighboorsTempArray;

  /**
   * Constructs a collision force field given a force intensity and drag
   * coefficient. The drag coefficient is often necessary to prevent excessive
   * bouncing.
   * 
   * @param pForceIntensity
   *          constant force applied during collision.
   * @param pDrag
   *          drag applied to slow down particles.
   */
  public CollisionForceField(float pForceIntensity, float pDrag)
  {
    super(pForceIntensity);
    mDrag = pDrag;
  }

  @Override
  public void applyForceField(int pDimension,
                              int pBeginId,
                              int pEndId,
                              NeighborhoodGrid mNeighborhood,
                              final DoubleBufferingFloatArray pPositions,
                              final DoubleBufferingFloatArray pVelocities,
                              final DoubleBufferingFloatArray pRadii)
  {

    final int lMaxNumberOfParticlesPerGridCell =
                                               mNeighborhood.getMaxParticlesPerGridCell();
    final int lTotalNumberOfCells = mNeighborhood.getVolume();

    final float[] lPositionsRead = pPositions.getReadArray();
    final float[] lPositionsWrite = pPositions.getWriteArray();
    final float[] lVelocitiesWrite = pVelocities.getWriteArray();
    final float[] lRadii = pRadii.getCurrentArray();

    pVelocities.copyAndMult(mDrag);

    int lNeighboorhoodListMaxLength = lMaxNumberOfParticlesPerGridCell
                                      * lTotalNumberOfCells;
    if (mNeighboorsArray == null
        || mNeighboorsArray.length != lNeighboorhoodListMaxLength)
    {
      mNeighboorsArray = new int[lNeighboorhoodListMaxLength];
      mNeighboorsTempArray = new int[lNeighboorhoodListMaxLength];
    }

    final int[] lNeighboors = mNeighboorsArray;
    final int[] lNeighboorsTemp = mNeighboorsArray;
    final float[] lCellCoord = new float[pDimension];
    final int[] lCellCoordMin = new int[pDimension];
    final int[] lCellCoordMax = new int[pDimension];
    final int[] lCellCoordCurrent = new int[pDimension];

    for (int idu =
                 pBeginId, i = idu
                               * pDimension; idu < pEndId; idu++, i +=
                                                                    pDimension)
    {

      final float ru = lRadii[idu];

      int lNumberOfNeighboors =
                              mNeighborhood.getAllNeighborsForParticle(lNeighboors,
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
        if (idu < idv && detectBoundingBoxCollision(pDimension,
                                                    lPositionsRead,
                                                    ru,
                                                    rv,
                                                    idu,
                                                    idv)) //
        {
          int j = idv * pDimension;
          /// System.out.println("BB collision");
          float lDistance = computeDistance(pDimension,
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

            for (int d = 0; d < pDimension; d++)
            {
              float lDelta = lPositionsRead[i + d]
                             - lPositionsRead[j + d];

              float lAxisVector = lInvDistanceWithForce * lDelta;

              lVelocitiesWrite[i + d] += lAxisVector;
              lVelocitiesWrite[j + d] += -lAxisVector;

              if (mPreventOverlap)
              {

                float lOverlapCorrection = lInvDistance * lDelta
                                           * (cGapCorrectionFactor
                                              * -lGap);
                float lNoise = (float) ((mRandom.nextDouble() - 0.5f)
                                        * cGapCorrectionNoiseFactor);
                lPositionsWrite[i + d] = lPositionsRead[i + d]
                                         + lOverlapCorrection
                                         + lNoise;
                lPositionsWrite[j + d] = lPositionsRead[j + d]
                                         + -lOverlapCorrection
                                         - lNoise;
              }
            }

          }
        }

      }

    }

    if (mPreventOverlap)
      pPositions.swap();

    pVelocities.swap();

  }

  private static float computeDistance(int pDimension,
                                       float[] pPositions,
                                       int pIdu,
                                       int pIdv)
  {
    return (float) Math.sqrt(computeSquaredDistance(pDimension,
                                                    pPositions,
                                                    pIdu,
                                                    pIdv));
  }

  private static float computeSquaredDistance(int pDimension,
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

  private static boolean detectBoundingBoxCollision(int pDimension,
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
