package simbryo.dynamics.tissue.cellprop.operators;

import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.cellprop.CellProperty;

/**
 * Cell property operators can modify the values of a set of properties over time.
 *
 * @author royer
 */
public interface CellPropertyOperatorInterface<CP extends CellProperty>
{

  /**
   * Apply a simulation step to the provided cell properties .
   * 
   * @param pBeginId
   * @param pEndId
   * @param pTissueDynamics
   * @param pCellProperty
   */
  void apply(int pBeginId,
             int pEndId,
             TissueDynamics pTissueDynamics,
             CP... pCellProperty);

}
