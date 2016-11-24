package embryosim.psystem.forcefield.external;

import embryosim.psystem.forcefield.ForceFieldBase;

public abstract class ExternalForceFieldBase extends ForceFieldBase
                                             implements
                                             ExternalForceFieldInterface
{

  public ExternalForceFieldBase(float pForce)
  {
    super(pForce);
  }

}
