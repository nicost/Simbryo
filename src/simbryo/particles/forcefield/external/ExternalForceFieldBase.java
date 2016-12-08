package simbryo.particles.forcefield.external;

import simbryo.particles.forcefield.ForceFieldBase;

/**
 * Base class implementing common fields and methods for all external force
 * fields.
 *
 * @author royer
 */
public abstract class ExternalForceFieldBase extends ForceFieldBase
                                             implements
                                             ExternalForceFieldInterface
{

  /**
   * Constructs an external force field with given force intensity.
   * 
   * @param pForceIntensity force intensity
   */
  public ExternalForceFieldBase(float pForceIntensity)
  {
    super(pForceIntensity);
  }

}
