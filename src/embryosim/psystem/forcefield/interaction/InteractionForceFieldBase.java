package embryosim.psystem.forcefield.interaction;

import embryosim.psystem.forcefield.ForceFieldBase;

public abstract class InteractionForceFieldBase extends ForceFieldBase
                                             implements
                                             InteractionForceFieldInterface
{

  public InteractionForceFieldBase(float pForce)
  {
    super(pForce);
  }

}
