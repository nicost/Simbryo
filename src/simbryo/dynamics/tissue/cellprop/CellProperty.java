package simbryo.dynamics.tissue.cellprop;

import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.util.DoubleBufferingFloatArray;

/**
 * Cell properties are quantities attached to each cell. Operators can be used to
 * modify these values over time.
 *
 * @author royer
 */
public class CellProperty
{
  private TissueDynamics mEmbryo;
  private final DoubleBufferingFloatArray mPropertyArray;

  /**
   * Constructs a cell property for a given embryo.
   * 
   * @param pEmbryo
   */
  public CellProperty(TissueDynamics pEmbryo)
  {
    super();
    mEmbryo = pEmbryo;
    mPropertyArray =
                    new DoubleBufferingFloatArray(mEmbryo.getMaxNumberOfParticles());
  }

  /**
   * Copies a cell property value from one cell id to another cell id.
   * 
   * @param pSourceParticleId
   *          source id
   * @param pDestParticleId
   *          destination id
   */
  public void copyValue(int pSourceParticleId, int pDestParticleId)
  {
    float[] lMorphogenArray = mPropertyArray.getCurrentArray();
    lMorphogenArray[pDestParticleId] =
                                     lMorphogenArray[pSourceParticleId];
  }

  /**
   * Returns the buffered array used by this cell property
   * 
   * @return buffered array
   */
  public DoubleBufferingFloatArray getArray()
  {
    return mPropertyArray;
  }

}
