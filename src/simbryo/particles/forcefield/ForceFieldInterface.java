package simbryo.particles.forcefield;

import java.io.Serializable;

import simbryo.particles.ParticleSystem;

/**
 * Force fields can be applied to particle systems to influence their movement.
 *
 * @author royer
 */
public interface ForceFieldInterface extends Serializable
{

  /**
   * Returns the intensity of the force field.
   * 
   * @return force intensity
   */
  float getForceIntensity();

  /**
   * Sets the force intensity. If zero the force field should have no influence
   * on the particle dynamics.
   * 
   * @param pForce
   *          force intensity
   */
  void setForceIntensity(float pForce);
  
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
  void applyForceField(int pBeginId,
                       int pEndId,
                       ParticleSystem pParticleSystem);

}
