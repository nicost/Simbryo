package embryosim.psystem.forcefield.interaction;

import embryosim.neighborhood.NeighborhoodGrid;
import embryosim.psystem.forcefield.ForceFieldInterface;
import embryosim.util.DoubleBufferingFloatArray;

/**
 * Interaction force fields implement forces that arise from the interaction
 * from particles with each-other.
 *
 * @author royer
 */
public interface InteractionForceFieldInterface extends
                                                ForceFieldInterface
{

  /**
   * Applies the nD force field to particles within a given range of ids (begin
   * inclusive, end exclusive). the positions, velocities and radii of the
   * particles are provided as double buffered float arrays. Moreover, the
   * neighborhood grid data structure is provided so that the neighbors of a
   * particle can be efficiently queried.
   * 
   * @param pDimension
   *          dimension
   * @param pBeginId
   *          particle id range beginning inclusive
   * @param pEndId
   *          particle id range end exclusive
   * @param pNeighborhoodGrid
   *          neighborhood grid dta structure
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
                       NeighborhoodGrid pNeighborhoodGrid,
                       DoubleBufferingFloatArray pPositions,
                       DoubleBufferingFloatArray pVelocities,
                       DoubleBufferingFloatArray pRadii);

}
