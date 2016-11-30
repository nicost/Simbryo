package simbryo.morphogen;

import simbryo.embryo.Embryo;
import simbryo.util.DoubleBufferingFloatArray;

/**
 * Morphogens are quantities attached to each cell. Operators can be used to
 * modify these values over time.
 *
 * @author royer
 */
public class Morphogen
{
  private Embryo mEmbryo;
  private final DoubleBufferingFloatArray mMorphogenArray;

  /**
   * Constructs a morphogen for a given embryo.
   * 
   * @param pEmbryo
   */
  public Morphogen(Embryo pEmbryo)
  {
    super();
    mEmbryo = pEmbryo;
    mMorphogenArray =
                    new DoubleBufferingFloatArray(mEmbryo.getMaxNumberOfParticles());
  }

  /**
   * Copies a morphogen value from one cell id to another cell id.
   * 
   * @param pSourceParticleId
   *          source id
   * @param pDestParticleId
   *          destination id
   */
  public void copyValue(int pSourceParticleId, int pDestParticleId)
  {
    float[] lMorphogenArray = mMorphogenArray.getCurrentArray();
    lMorphogenArray[pDestParticleId] =
                                     lMorphogenArray[pSourceParticleId];
  }

  /**
   * Returns the buffered array used by this morphogen
   * 
   * @return buffered array
   */
  public DoubleBufferingFloatArray getArray()
  {
    return mMorphogenArray;
  }

}
