package embryosim.psystem.forcefield.external;

import embryosim.psystem.forcefield.ForceFieldInterface;
import embryosim.util.DoubleBufferingFloatArray;

/**
 * This is the 'standard' kind of force field: a force that possibly varies in
 * space and time is applied on each particle independently of the position of
 * the other particles.
 *
 * @author royer
 */
public interface ExternalForceFieldInterface extends
                                             ForceFieldInterface
{

  /**
   * Applies the nD force field to particles within a given range of ids (begin
   * inclusive, end exclusive). the positions, velocities and radii of the
   * particles are provided as double buffered float arrays.
   * 
   * @param pDimension
   *          dimension
   * @param pBeginId
   *          particle id range beginning inclusive
   * @param pEndId
   *          particle id range end exclusive
   * @param pPositions
   *          positions
   * @param pVelocities
   *          velocities
   * @param pRadii
   *          radii
   */
  void applyForceField(int pDimension,
                       int pBeginId,
                       int pEndId,
                       DoubleBufferingFloatArray pPositions,
                       DoubleBufferingFloatArray pVelocities,
                       DoubleBufferingFloatArray pRadii);

}
