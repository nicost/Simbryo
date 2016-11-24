package embryosim.psystem.forcefield.external;

import embryosim.psystem.forcefield.ForceFieldInterface;
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
