package embryosim.psystem.forcefield.interaction.impl;

import embryosim.neighborhood.NeighborhoodCellGrid;
import embryosim.psystem.forcefield.interaction.InteractionForceFieldBase;
import embryosim.psystem.forcefield.interaction.InteractionForceFieldInterface;
import embryosim.util.DoubleBufferingFloatArray;

/**
 * Applies the force for elastic particle-to-particle collision.
 * 
 * @param pForce
 *          constant force applied during collision.
 * @param pDrag
 *          drag applied to slow down particles.
 */
public class CollisionForceField extends InteractionForceFieldBase
                                 implements
                                 InteractionForceFieldInterface
{

  private float mDrag;

  private int[] mNeighboorsArray, mNeighboorsTempArray;

  public CollisionForceField(float pForce, float pDrag)
  {
    super(pForce);
    mDrag = pDrag;
  }

  @Override
  public void applyForceField(int pDimension,
                              int pBeginId,
                              int pEndId,
                              NeighborhoodCellGrid mNeighborhood,
                              final DoubleBufferingFloatArray pPositions,
                              final DoubleBufferingFloatArray pVelocities,
                              final DoubleBufferingFloatArray pRadii)
  {

    final int lMaxNumberOfParticlesPerGridCell =
                                               mNeighborhood.getMaxParticlesPerGridCell();
    final int lTotalNumberOfCells = mNeighborhood.getVolume();

    final float[] lPositionsRead = pPositions.getReadArray();
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

    for (int idu = pBeginId; idu < pEndId; idu++)
    {
      final int i = idu * pDimension;

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
          if (lGap <= 0 && lDistance != 0)
          {

            // Collision -> apply force.
            float lInvDistanceWithAlpha = mForce / lDistance;

            for (int d = 0; d < pDimension; d++)
            {
              float lAxisVector = lInvDistanceWithAlpha
                                  * (lPositionsRead[i + d]
                                     - lPositionsRead[j + d]);

              lVelocitiesWrite[i + d] += lAxisVector;
              lVelocitiesWrite[j + d] += -lAxisVector;
            }
          }
        }

      }

    }

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
