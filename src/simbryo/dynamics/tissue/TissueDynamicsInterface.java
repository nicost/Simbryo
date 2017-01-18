package simbryo.dynamics.tissue;

import simbryo.particles.ParticleSystemInterface;

/**
 * Tissue dynamics interface
 * 
 *
 * @author royer
 */
public interface TissueDynamicsInterface extends
                                         ParticleSystemInterface
{

  /**
   * Returns the current time step index.
   * 
   * @return time step
   */
  long getTimeStepIndex();

}
