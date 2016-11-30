package simbryo.psystem.forcefield;

/**
 * Base class implementing common fields and methods of all force fields.
 *
 * @author royer
 */
public abstract class ForceFieldBase implements ForceFieldInterface
{
  protected volatile float mForceIntensity;

  /**
   * Constructs a force field with a given force intensity.
   * 
   * @param pForceIntensity
   */
  public ForceFieldBase(float pForceIntensity)
  {
    mForceIntensity = pForceIntensity;
  }

  @Override
  public float getForceIntensity()
  {
    return mForceIntensity;
  }

  @Override
  public void setForceIntensity(float pForce)
  {
    mForceIntensity = pForce;
  }

}
