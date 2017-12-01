package simbryo.dynamics.tissue.epithelium.zoo;

import static java.lang.Math.pow;
import static java.lang.Math.random;
import static java.lang.Math.sin;
import java.util.Arrays;

import simbryo.dynamics.tissue.cellprop.CellProperty;
import simbryo.dynamics.tissue.epithelium.LayeredEpitheliumDynamics;
import simbryo.particles.isosurf.impl.Plane;

/**
 * Layered epithelium dynamics extend from a tissue dynamics and add the notion
 * of 'epithelium surface'
 *
 * @author royer
 */
public class TwoLayeredEpithelium extends LayeredEpitheliumDynamics
{
  private static final long serialVersionUID = 1L;

  protected static final float Fc = 0.0001f;
  protected static final float D = 0.95f;

  private final float cDefaultLayerForce = 0.0001f;

  private final float cMasterIncrement = 0.00001f;

  private final float cCellCycleIncrement = cMasterIncrement;
  private final float cCellDivisionIncrement = cMasterIncrement
                                                      * 40;

  private final float mExclusionRadius;

  private final CellProperty mCellLabelProperty;
  private final CellProperty mCellStateProperty;

  /**
   * Creates a two-layered epithelium.
   * 
   * @param pInitialNumberOfCells
   *          initial number of cells
   * @param pLayerSpacing
   *          two layer spacing
   * @param pExclusionRadius
   *          cell's exclusion radius
   *
   * 
   */
  public TwoLayeredEpithelium(int pInitialNumberOfCells,
                              float pLayerSpacing,
                              float pExclusionRadius)
  {
    super(Fc, Fc * 0.1f, D, 64, 32, 32, 32);
    mExclusionRadius = pExclusionRadius;

    mCollisionForceField.setForbidOverlap(true);

    Plane lTopLayer = new Plane(0f, 0f, 1f);
    lTopLayer.setPoint(0.5f, 0.5f, 0.5f + 0.5f * pLayerSpacing);

    Plane lBottomLayer = new Plane(0f, 0f, 1f);
    lBottomLayer.setPoint(0.5f, 0.5f, 0.5f - 0.5f * pLayerSpacing);

    super.addLayer(lTopLayer, -cDefaultLayerForce);
    super.addLayer(lBottomLayer, -cDefaultLayerForce);

    mCellLabelProperty = super.addCellProperty();
    mCellStateProperty = super.addCellProperty();

    Pair[] indexedDistances = new Pair[pInitialNumberOfCells];
    for (int i = 0; i < pInitialNumberOfCells; i++)
    {
      float angle = (float) (Math.random() * 2.0f * Math.PI) ;
      float dist = (float) (Math.random());
      indexedDistances[i] = new Pair(i, dist);
      
      float x = dist * (float) Math.cos(angle);
      float y = dist * (float) Math.sin(angle);

      // TODO: figure best range around center.  
      // This depends on the number of particles and their radii in a non-linear way
      final int lId = super.addParticle(0.5f + 0.06f * x,
                                        0.5f + 0.06f * y,
                                        0.5f);
      super.setRadius(lId, getRandomRadius(i));
      super.setTargetRadius(lId, super.getRadius(lId));
      super.assignCellToLayer(i, 1); // (int) (Math.random() * 2)

      mCellStateProperty.set(i, (float) (random()));
    }
    Arrays.sort(indexedDistances);

    for (int j = 0; j < pInitialNumberOfCells; j++)
    {
      mCellLabelProperty.getArray().getCurrentArray()[indexedDistances[j].index] =
                                                                   j+1;
    }

    super.updateNeighborhoodGrid();

  }

  public float[] getPosition(int pParticleId)
  {
    final int index = super.getDimension() * pParticleId; // note: 2 == pGridDimension.length
    float[] pos = new float[super.getDimension()];
    for (int i = 0; i < super.getDimension(); i++) {
       pos[i] = mPositions.getCurrentArray()[index + i];
    }
    return pos;
  }

  private float getRandomRadius(int i)
  {
    return (float) (mExclusionRadius
                    * (1 + 0.1f * (drand(i) - 0.5f)));
  }

  protected float drand(int i)
  {
    return (float) ((sin(i) * 100000) % 1);
  }

  /**
   * Return the cell label property
   * 
   * @return cell label property
   */
  public CellProperty getCellLabelProperty()
  {
    return mCellLabelProperty;
  }

  @Override
  public void simulationSteps(int pNumberOfSteps)
  {
    for (int s = 0; s < pNumberOfSteps; s++)
    {
      super.simulationSteps(1);

      final int lNumberOfParticles = getNumberOfParticles();
      for (int i = 0; i < lNumberOfParticles; i++)
      {

        float lCurrentState = mCellStateProperty.getArray()
                                                .getCurrentArray()[i];

        if (lCurrentState < 1)
        {
          waitingForPullDown(i);
        }
        else if (1 <= lCurrentState && lCurrentState < 2)
        {
          pulldown(i);
        }
        else if (2 <= lCurrentState && lCurrentState < 3)
        {
          divide(i);
        }
        else if (3 <= lCurrentState && lCurrentState < 4)
        {
          waitingForPullUp(i);
        }
        else if (4 <= lCurrentState && lCurrentState < 5)
        {
          pullup(i);
        }
        else if (5 <= lCurrentState && lCurrentState < 6)
        {
          done(i);
        }

      }
    }
  }

  private void waitingForPullDown(int pCellId)
  {
    // counting until we start cell division = {pull down, divide, pull up}
    mCellStateProperty.getArray()
                      .getCurrentArray()[pCellId] +=
                                                  cCellCycleIncrement;
  }

  private void pulldown(int pCellId)
  {
    assignCellToLayer(pCellId, 0);

    mCellStateProperty.getArray()
                      .getCurrentArray()[pCellId] +=
                                                  cCellDivisionIncrement;
  }

  private void divide(int pCellId)
  {
    int lDaughterId1 = pCellId;
    int lDaughterId2 = cloneParticle(pCellId, 0.0001f);

    mCellLabelProperty.getArray()
                      .getCurrentArray()[lDaughterId1] =
                                                       mCellLabelProperty.getArray()
                                                                         .getCurrentArray()[lDaughterId1];
    mCellLabelProperty.getArray()
                      .getCurrentArray()[lDaughterId2] =
                                                       mCellLabelProperty.getArray()
                                                                         .getCurrentArray()[lDaughterId1];
    float lRadius =
                  (float) (getRadius(lDaughterId1) * pow(0.5f, 1.0f));
    setTargetRadius(lDaughterId1, lRadius);
    setTargetRadius(lDaughterId2, lRadius);

    mCellStateProperty.getArray().getCurrentArray()[lDaughterId1] = 3;
    mCellStateProperty.getArray().getCurrentArray()[lDaughterId2] = 3;
  }

  private void waitingForPullUp(int pCellId)
  {
    setTargetRadius(pCellId,
                    getTargetRadius(pCellId) * 0.999f
                             + getRandomRadius(pCellId) * 0.001f);/**/

    mCellStateProperty.getArray()
                      .getCurrentArray()[pCellId] +=
                                                  cCellDivisionIncrement;
  }

  private void pullup(int pCellId)
  {
    assignCellToLayer(pCellId,
                      0.001f * 1f
                               + 0.999f * getCellLayer(pCellId));/**/

    // assignCellToLayer(pCellId, 1);

    mCellStateProperty.getArray()
                      .getCurrentArray()[pCellId] +=
                                                  cCellDivisionIncrement;
  }

  private void done(int pCellId)
  {
    setTargetRadius(pCellId, getRandomRadius(pCellId));/**/
    mCellStateProperty.getArray()
                      .getCurrentArray()[pCellId] =
                                                  (float) (0.1f
                                                           * random());
  }
  
  // Helper class used to sort the cells by distance to the center
  private class Pair implements Comparable<Pair> {
    public final int index;
    public final float value;

    /**
     * Create Pair, should do this using generics 
     * @param index
     * @param value 
     */
    public Pair(int index, float value) {
        this.index = index;
        this.value = value;
    }

    @Override
    public int compareTo(Pair other) {
        //multiplied to -1 as the author need descending sort order
        return Float.valueOf(this.value).compareTo(other.value);
    }
}

}
