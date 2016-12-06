package simbryo.dynamics.tissue.cellprop.operators;

import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.cellprop.CellProperty;

/**
 * Cell property operators can modify the values of a set of properties over time.
 *
 * @author royer
 */
public interface CellPropertyOperatorInterface
{

  /**
   * Apply a simulation step to the provided cell properties .
   * 
   * @param pBeginId
   * @param pEndId
   * @param pEmbryo
   * @param pCellProperty
   */
  void apply(int pBeginId,
             int pEndId,
             TissueDynamics pEmbryo,
             CellProperty... pCellProperty);

}
