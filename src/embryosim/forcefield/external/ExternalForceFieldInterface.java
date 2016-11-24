package embryosim.forcefield.external;

import embryosim.forcefield.ForceFieldInterface;
import embryosim.util.DoubleBufferingFloatArray;

public interface ExternalForceFieldInterface extends
                                             ForceFieldInterface
{

  void applyForceField(int pDimension,
                       int pBeginId,
                       int pEndId,
                       DoubleBufferingFloatArray pPositions,
                       DoubleBufferingFloatArray pVelocities,
                       DoubleBufferingFloatArray pRadii);

}
