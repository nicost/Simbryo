package embryosim.forcefield.interaction;

import embryosim.forcefield.ForceFieldInterface;
import embryosim.neighborhood.NeighborhoodCellGrid;
import embryosim.util.DoubleBufferingFloatArray;

public interface InteractionForceFieldInterface extends
                                             ForceFieldInterface
{

  void applyForceField(int pDimension,
                       int pBeginId,
                       int pEndId,
                       NeighborhoodCellGrid pMNeighborhood,
                       DoubleBufferingFloatArray pPositions,
                       DoubleBufferingFloatArray pVelocities,
                       DoubleBufferingFloatArray pRadii);

}
