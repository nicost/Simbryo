package embryosim.forcefield.external;

import embryosim.forcefield.ForceFieldBase;

public abstract class ExternalForceFieldBase extends ForceFieldBase
                                             implements
                                             ExternalForceFieldInterface
{

  public ExternalForceFieldBase(float pForce)
  {
    super(pForce);
  }

}
