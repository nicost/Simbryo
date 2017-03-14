package simbryo.dynamics.tissue.cellprop;

import java.io.Serializable;

import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.util.DoubleBufferingFloatArray;

/**
 * Cell properties are quantities attached to each cell. Operators can be used
 * to modify these values over time.
 *
 * @author royer
 */
public class CellProperty implements Serializable
{
  private static final long serialVersionUID = 1L;

  private final TissueDynamics mEmbryo;
  private final int mDimension;

  protected final DoubleBufferingFloatArray mPropertyArray;

  /**
   * Constructs a 1D cell property for a given embryo.
   * 
   * @param pTissueDynamics
   *          tissue dynamics
   */
  public CellProperty(TissueDynamics pTissueDynamics)
  {
    this(pTissueDynamics, 1);
  }

  /**
   * Constructs a nD cell property for a given embryo.
   * 
   * @param pTissueDynamics
   *          tissue dynamics
   * @param pDimension
   *          dimension of cell property
   */
  public CellProperty(TissueDynamics pTissueDynamics, int pDimension)
  {
    super();
    mEmbryo = pTissueDynamics;
    mDimension = pDimension;
    mPropertyArray =
                   new DoubleBufferingFloatArray(getDimension()
                                                 * mEmbryo.getMaxNumberOfParticles());
  }

  /**
   * Returns the maximum number of particles.
   * 
   * @return max number of particles
   */
  public int getMaxNumberOfParticles()
  {
    return mEmbryo.getMaxNumberOfParticles();
  }

  /**
   * Returns dimension of property.
   * 
   * @return dimension
   */
  public int getDimension()
  {
    return mDimension;
  }

  /**
   * Copies values from read to write arrays within a given range.
   * 
   * @param pBeginId
   *          begin id of range
   * @param pEndId
   *          end id of range
   */
  public void copyDefault(int pBeginId, int pEndId)
  {
    mPropertyArray.copyDefault(pBeginId * mDimension,
                               pEndId * mDimension);
  }

  /**
   * Clear cell property to zero.
   * 
   * @param pBeginId
   *          begin id of range
   * @param pEndId
   *          end id of range
   */
  public void clear(int pBeginId, int pEndId)
  {
    mPropertyArray.clear(pBeginId * mDimension, pEndId * mDimension);
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
    lMorphogenArray[getDimension()
                    * pDestParticleId] =
                                       lMorphogenArray[getDimension()
                                                       * pSourceParticleId];
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
