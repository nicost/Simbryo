package embryosim.forcefield.interaction;

import embryosim.forcefield.ForceFieldBase;

public abstract class InteractionForceFieldBase extends ForceFieldBase
                                             implements
                                             InteractionForceFieldInterface
{

  public InteractionForceFieldBase(float pForce)
  {
    super(pForce);
  }

}
