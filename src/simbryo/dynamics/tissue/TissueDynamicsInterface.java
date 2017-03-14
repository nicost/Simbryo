package simbryo.dynamics.tissue;

import java.io.Serializable;

import simbryo.particles.ParticleSystemInterface;

/**
 * Tissue dynamics interface
 * 
 *
 * @author royer
 */
public interface TissueDynamicsInterface extends
                                         ParticleSystemInterface,
                                         Serializable
{

  /**
   * Returns the current time step index.
   * 
   * @return time step
   */
  long getTimeStepIndex();

}
