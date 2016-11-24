package embryosim.forcefield;

import embryosim.psystem.ParticleSystem;

public class SphericalForceField implements ForceField
{

  private float mForce;
  private float mRadius;
  private float[] mCenter;

  public SphericalForceField(float pForce,
                             float pRadius,
                             float... pCenter)
  {
    super();
    mForce = pForce;
    mRadius = pRadius;
    mCenter = pCenter;

  }

  @Override
  public void applyForceField(ParticleSystem pParticleSystem)
  {
    // TODO Auto-generated method stub
    
  }

}
