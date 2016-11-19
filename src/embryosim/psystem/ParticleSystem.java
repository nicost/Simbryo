package embryosim.psystem;

import java.util.SplittableRandom;

import embryosim.neighborhood.NeighborhoodCellGrid;
import embryosim.util.DoubleBufferingFloatArray;

/**
 * N-dimensional Particle system implementation. Particles have a position and
 * 'radius-of-influence' which can be thought as the proper radius of the
 * particle or as a bounding sphere. This class implements various methods to
 * 'run' the dynamics and apply the forces to and between particles.
 *
 * @author royer
 */
public class ParticleSystem
{
  private final int mDimension;
  private final int mMaxNumberOfParticles;
  private final int mMaxNumberOfParticlesPerGridCell;
  private int mNumberOfParticles;

  private final DoubleBufferingFloatArray mPositions;
  private final DoubleBufferingFloatArray mVelocities;
  private final DoubleBufferingFloatArray mRadii;

  private final NeighborhoodCellGrid mNeighborhood;

  private int[] mNeighboorsTempArray;

  private SplittableRandom mRandom = new SplittableRandom();

  /**
   * Creates a particle system with a give number of dimensions, number of
   * particles, minimal radius, and typical radius.
   * 
   * @param pDimension
   *          dimension
   * @param pMaxNumberOfParticles
   *          max number of particles
   * @param pMinRradius
   *          minimal radius
   * @param pTypicalRadius
   *          typicall radius
   */
  public ParticleSystem(int pDimension,
                        int pMaxNumberOfParticles,
                        float pMinRradius,
                        float pTypicalRadius)
  {
    this(pDimension,
         getOptimalGridSize(pDimension,
                            pMaxNumberOfParticles,
                            pTypicalRadius),
         16 + getOptimalMaxNumberOfParticlesPerGridCell(pDimension,
                                                        pMaxNumberOfParticles,
                                                        pMinRradius,
                                                        pTypicalRadius),
         pMaxNumberOfParticles);
  }

  private static int getOptimalGridSize(int pDimension,
                                        int pMaxNumberOfParticles,
                                        float pTypicalRadius)
  {
    int lOptimalGridSize =
                         (int) Math.max(4,
                                        1 / (3 * 2 * pTypicalRadius));

    return lOptimalGridSize;
  }

  private static int getOptimalMaxNumberOfParticlesPerGridCell(int pDimension,
                                                               int pMaxNumberOfParticles,
                                                               float pMinRradius,
                                                               float pTypicalRadius)
  {
    int lOptimalGridSize = getOptimalGridSize(pDimension,
                                              pMaxNumberOfParticles,
                                              pTypicalRadius);

    int lVolume = (int) Math.pow(lOptimalGridSize, pDimension);

    float lCellVolume = 1.0f / lVolume;
    float lTypicalParticleVolume = (float) Math.pow(2 * pMinRradius,
                                                    pDimension);

    int lMaxNumberOfParticlesPerCell =
                                     (int) Math.ceil(lCellVolume
                                                     / lTypicalParticleVolume);

    return lMaxNumberOfParticlesPerCell;
  }

  /**
   * Constructs a particle system with a given number of dimensions, grid size,
   * max number of particles and particles per cell.
   * 
   * @param pDimension
   *          dimension
   * @param pGridSize
   *          grid size
   * @param pMaxNumberOfParticlesPerGridCell
   *          max number of particles per cell
   * @param pMaxNumberOfParticles
   *          max number of particles
   */
  public ParticleSystem(int pDimension,
                        int pGridSize,
                        int pMaxNumberOfParticlesPerGridCell,
                        int pMaxNumberOfParticles)
  {
    super();

    /*System.out.println("pDimension=" + pDimension);
    System.out.println("pGridSize=" + pGridSize);
    System.out.println("pMaxNumberOfParticlesPerGridCell="
                       + pMaxNumberOfParticlesPerGridCell);
    System.out.println("pMaxNumberOfParticles="
                       + pMaxNumberOfParticles);/**/

    mMaxNumberOfParticles = pMaxNumberOfParticles;
    mMaxNumberOfParticlesPerGridCell =
                                     pMaxNumberOfParticlesPerGridCell;
    mDimension = pDimension;
    mPositions = new DoubleBufferingFloatArray(pMaxNumberOfParticles
                                               * mDimension);
    mVelocities = new DoubleBufferingFloatArray(pMaxNumberOfParticles
                                                * mDimension);
    mRadii = new DoubleBufferingFloatArray(pMaxNumberOfParticles);
    mNeighborhood =
                  new NeighborhoodCellGrid(mDimension,
                                           pGridSize,
                                           pMaxNumberOfParticlesPerGridCell);
  }
  
  /**
   * returns current number of particles.
   * 
   * @return number of particles
   */
  public int getNumberOfParticles()
  {
    return mNumberOfParticles;
  }

  /**
   * Returns dimension
   * 
   * @return dimension
   */
  public int getDimension()
  {
    return mDimension;
  }

  /**
   * Returns grid size
   * 
   * @return grid size
   */
  public int getGridSize()
  {
    return mNeighborhood.getGridSize();
  }

  /**
   * Adds a particle to this particle system at a given position. The particle
   * id is returned.
   * 
   * @param pPosition
   * @return particle id.
   */
  public int addParticle(float... pPosition)
  {
    final int lDimension = mDimension;
    final float[] lPositions = mPositions.getCurrentArray();
    final float[] lVelocities = mVelocities.getCurrentArray();
    final int lParticleId = mNumberOfParticles;
    final int i = lParticleId * lDimension;

    for (int d = 0; d < mDimension; d++)
    {
      lPositions[i + d] = pPosition[d];
      lVelocities[i + d] = 0;
    }

    mNumberOfParticles++;

    return lParticleId;
  }

  /**
   * Removes a particle to this particle system.
   * 
   * @param pPosition
   * @return particle id.
   */
  public void removeParticle(int pParticleId)
  {
    final int lLastParticleId = mNumberOfParticles - 1;
    copyParticle(lLastParticleId, pParticleId);
    mNumberOfParticles--;
  }

  /**
   * Copies a source particle parameters to a destination particle parameters.
   * 
   * @param pSourceParticleId
   *          source id
   * @param pDestinationParticleId
   *          destination id
   */
  public void copyParticle(int pSourceParticleId,
                           int pDestinationParticleId)
  {
    final int lDimension = mDimension;
    final float[] lPositions = mPositions.getCurrentArray();
    final float[] lVelocities = mVelocities.getCurrentArray();
    final float[] lRadii = mRadii.getCurrentArray();

    for (int d = 0; d < lDimension; d++)
    {

      lPositions[pDestinationParticleId * lDimension
                 + d] =
                      lPositions[pSourceParticleId * lDimension + d];
      lVelocities[pDestinationParticleId * lDimension
                  + d] =
                       lVelocities[pSourceParticleId * lDimension
                                   + d];
    }

    lRadii[pDestinationParticleId] = lRadii[pSourceParticleId];
  }

  /**
   * Adds noise to a particle position, velocity, and radius.
   * 
   * @param pParticleId
   *          particle id
   * @param pPositionNoise
   *          position noise
   * @param pVelocityNoise
   *          velocity noise
   */
  public void addNoiseToParticle(int pParticleId,
                                 float pPositionNoise,
                                 float pVelocityNoise,
                                 float pRadiusNoise)
  {
    final int lDimension = mDimension;
    final float[] lPositions = mPositions.getCurrentArray();
    final float[] lVelocities = mVelocities.getCurrentArray();
    final float[] lRadii = mRadii.getCurrentArray();

    for (int d = 0; d < lDimension; d++)
    {
      float lPositionNoiseValue =
                                (float) ((mRandom.nextDouble() - 0.5)
                                         * 2 * pPositionNoise);
      float lVelocityNoiseValue =
                                (float) ((mRandom.nextDouble() - 0.5)
                                         * 2 * pVelocityNoise);
      lPositions[pParticleId * lDimension + d] += lPositionNoiseValue;

      lVelocities[pParticleId * lDimension + d] +=
                                                lVelocityNoiseValue;
    }
    float lRadiusNoiseValue = (float) ((mRandom.nextDouble() - 0.5)
                                       * 2 * pVelocityNoise);
    lRadii[pParticleId] += lRadiusNoiseValue;
  }

  /**
   * Sets the position of a particle
   * 
   * @param pParticleId
   *          particle id
   * @param pParticlePosition
   *          particle new position.
   */
  public void setPosition(int pParticleId, float... pParticlePosition)
  {
    final float[] lPositions = mPositions.getCurrentArray();
    final int i = mDimension * pParticleId;
    for (int d = 0; d < mDimension; d++)
    {
      lPositions[i + d] = pParticlePosition[d];
    }
  }

  /**
   * Sets the velocity of a particle
   * 
   * @param pParticleId
   *          particle id
   * @param pVelocity
   *          new velocity
   */
  public void setVelocity(int pParticleId, float... pVelocity)
  {
    final float[] lVelocities = mVelocities.getCurrentArray();
    final int i = mDimension * pParticleId;
    for (int d = 0; d < mDimension; d++)
    {
      lVelocities[i + d] = pVelocity[d];
    }
  }

  /**
   * Sets the radius of a particle
   * 
   * @param pParticleId
   *          particle id
   * @param pRadius
   *          new radius
   */
  public void setRadius(int pParticleId, float pRadius)
  {
    float[] lRadii = mRadii.getCurrentArray();
    lRadii[pParticleId] = pRadius;
  }

  /**
   * Updates neighborhood cells.
   */
  public void updateNeighborhoodCells()
  {
    mNeighborhood.clear();
    float[] lPositions = mPositions.getCurrentArray();
    float[] lRadii = mRadii.getCurrentArray();
    mNeighborhood.update(lPositions, lRadii, mNumberOfParticles);
  }

  /**
   * Apply a centrifugal force around a point (X,Y)
   * 
   * @param pCenterX
   *          center x coordinate
   * @param pCenterY
   *          center y coordinate
   * @param pFactor
   *          force factor.
   */
  public void repelAround(float pFactor,
                          float pCenterX,
                          float pCenterY)
  {
    final int lDimension = mDimension;
    final float[] lPositionsRead = mPositions.getReadArray();
    final float[] lVelocitiesRead = mVelocities.getReadArray();
    final float[] lVelocitiesWrite = mVelocities.getWriteArray();
    final int lLength = mNumberOfParticles * lDimension;

    for (int i = 0; i < lLength; i += lDimension)
    {

      float x = lPositionsRead[i + 0];
      float y = lPositionsRead[i + 1];
      float ux = x - pCenterX;
      float uy = y - pCenterY;
      float l = (float) Math.sqrt(ux * ux + uy * uy);
      float n = pFactor / l;
      float fx = n * ux;
      float fy = n * uy;

      lVelocitiesWrite[i + 0] = 0.99f * lVelocitiesRead[i + 0] + fx;
      lVelocitiesWrite[i + 1] = 0.99f * lVelocitiesRead[i + 1] + fy;
    }

    mVelocities.swap();

  }

  /**
   * Enforces bounds [0,1]^d by bouncing the particles elastically.
   */
  public void enforceBoundsWithElasticBouncing()
  {
    final int lDimension = mDimension;
    final float[] lPositionsRead = mPositions.getReadArray();
    final float[] lPositionsWrite = mPositions.getWriteArray();
    final float[] lVelocitiesRead = mVelocities.getReadArray();
    final float[] lVelocitiesWrite = mVelocities.getWriteArray();
    final int lLength = mNumberOfParticles * lDimension;

    for (int i = 0; i < lLength; i++)
    {
      if (lPositionsRead[i] < 0)
      {
        lPositionsWrite[i] = 0;
        lVelocitiesWrite[i] = -lVelocitiesRead[i];
      }
      else if (lPositionsRead[i] > 1)
      {
        lPositionsWrite[i] = 1;
        lVelocitiesWrite[i] = -lVelocitiesRead[i];
      }
      else
      {
        lPositionsWrite[i] = lPositionsRead[i];
        lVelocitiesWrite[i] = lVelocitiesRead[i];
      }
    }

    mPositions.swap();
    mVelocities.swap();
  }

  /**
   * Applies a spatially invariant force to the particles.
   * 
   * @param pForce
   *          force.
   */
  public void applyForce(float... pForce)
  {
    final int lDimension = mDimension;
    final float[] lPositionsRead = mPositions.getReadArray();
    final float[] lPositionsWrite = mPositions.getWriteArray();
    final float[] lVelocitiesRead = mVelocities.getReadArray();
    final float[] lVelocitiesWrite = mVelocities.getWriteArray();
    final int lLength = mNumberOfParticles * lDimension;

    for (int i = 0; i < lLength; i += lDimension)
      for (int d = 0; d < lDimension; d++)
        lVelocitiesWrite[i + d] = lVelocitiesRead[i + d] + pForce[d];

    mVelocities.swap();
  }

  /**
   * Euler integration
   */
  public void intergrateEuler()
  {
    final int lDimension = mDimension;
    final float[] lPositionsRead = mPositions.getReadArray();
    final float[] lPositionsWrite = mPositions.getWriteArray();
    final float[] lVelocities = mVelocities.getCurrentArray();
    final int lLength = mNumberOfParticles * lDimension;

    for (int i = 0; i < lLength; i += lDimension)
    {
      for (int d = 0; d < lDimension; d++)
        lPositionsWrite[i + d] = lPositionsRead[i + d]
                                 + lVelocities[i + d];
    }

    mPositions.swap();

  }

  /**
   * Applies the force for elastic particle-to-particle collision.
   * 
   * @param pForce
   *          constant force applied during collision.
   * @param pDrag
   *          drag applied to slow down particles.
   */
  public void applyForcesForElasticParticleCollisions(float pForce,
                                                      float pDrag)
  {
    final int lDimension = mDimension;
    final int lMaxNumberOfParticlesPerGridCell =
                                               mMaxNumberOfParticlesPerGridCell;
    final int lTotalNumberOfCells = mNeighborhood.getVolume();
    final int lNumberOfParticles = mNumberOfParticles;
    final float[] lPositionsRead = mPositions.getReadArray();
    final float[] lVelocitiesWrite = mVelocities.getWriteArray();
    final float[] lRadii = mRadii.getCurrentArray();

    mVelocities.copyAndMult(pDrag);

    int lNeighboorhoodListMaxLength = lMaxNumberOfParticlesPerGridCell
                                      * lTotalNumberOfCells;
    if (mNeighboorsTempArray == null
        || mNeighboorsTempArray.length != lNeighboorhoodListMaxLength)
      mNeighboorsTempArray = new int[lNeighboorhoodListMaxLength];

    final int[] lNeighboors = mNeighboorsTempArray;
    final float[] lCellCoord = new float[lDimension];
    final int[] lCellCoordMin = new int[lDimension];
    final int[] lCellCoordMax = new int[lDimension];
    final int[] lCellCoordCurrent = new int[lDimension];

    for (int idu = 0; idu < lNumberOfParticles; idu++)
    {
      final int i = idu * lDimension;

      final float ru = lRadii[idu];

      int lNumberOfNeighboors =
                              mNeighborhood.getAllNeighborsForParticle(lNeighboors,
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
        if (idu < idv && detectBoundingBoxCollision(lDimension,
                                                    lPositionsRead,
                                                    ru,
                                                    rv,
                                                    idu,
                                                    idv)) //
        {
          int j = idv * lDimension;
          /// System.out.println("BB collision");
          float lDistance = computeDistance(lDimension,
                                            lPositionsRead,
                                            idu,
                                            idv);
          float lGap = lDistance - ru - rv;

          // testing sphere collision:
          if (lGap <= 0 && lDistance != 0)
          {

            // Collision -> apply force.
            float lInvDistanceWithAlpha = pForce / lDistance;

            for (int d = 0; d < lDimension; d++)
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

    mVelocities.swap();

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



  /**
   * Copies the positions to this array.
   * 
   * @param pPositionsCopy
   *          array to copy positions to
   * @return number of particles copied.
   */
  public int copyPositions(float[] pPositionsCopy)
  {
    mPositions.copyCurrentArrayTo(pPositionsCopy,
                                  mNumberOfParticles * mDimension);
    return mNumberOfParticles;
  }

  /**
   * Copies the velocities to this array.
   * 
   * @param pVelocitiesCopy
   *          array to copy velocities to
   * @return number of particles copied.
   */
  public int copyVelocities(float[] pVelocitiesCopy)
  {
    mVelocities.copyCurrentArrayTo(pVelocitiesCopy,
                                   mNumberOfParticles * mDimension);
    return mNumberOfParticles;
  }

  /**
   * Copies the radii to this array.
   * 
   * @param pRadiiCopy
   * @return number of particles copied.
   */
  public int copyRadii(float[] pRadiiCopy)
  {
    mRadii.copyCurrentArrayTo(pRadiiCopy, mNumberOfParticles);
    return mNumberOfParticles;
  }

}
