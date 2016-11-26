package embryosim.psystem.forcefield;

/**
 * Force fields can be applied to particle systems to influence their movement.
 *
 * @author royer
 */
public interface ForceFieldInterface
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

}
