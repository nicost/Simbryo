package embryosim.interfaces;

import embryosim.psystem.ParticleSystem;

public interface ParticleViewerInterface
{
  void updateDisplay(ParticleSystem pParticleSystem,
                     boolean pBlocking);
}
