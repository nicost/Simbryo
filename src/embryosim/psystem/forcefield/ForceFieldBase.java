package embryosim.psystem.forcefield;

public class ForceFieldBase implements ForceFieldInterface
{
  protected volatile float mForce;

  public ForceFieldBase(float pForce)
  {
    mForce = pForce;
  }

  @Override
  public float getForce()
  {
    return mForce;
  }

  @Override
  public void setForce(float pForce)
  {
    mForce = pForce;
  }
  
}
