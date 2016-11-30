package simbryo.morphogen.operators;

import simbryo.embryo.Embryo;
import simbryo.morphogen.Morphogen;

/**
 * Morphogen operators can modify the values of a set of morphogens over time.
 *
 * @author royer
 */
public interface MorphogenOperatorInterface
{

  /**
   * Apply a simulation step to the morphogens.
   * 
   * @param pBeginId
   * @param pEndId
   * @param pEmbryo
   * @param pMorphogens
   */
  void apply(int pBeginId,
             int pEndId,
             Embryo pEmbryo,
             Morphogen... pMorphogens);

}
