package simbryo.particles.forcefield.external.impl;

import simbryo.particles.ParticleSystem;
import simbryo.particles.forcefield.external.ExternalForceFieldBase;
import simbryo.particles.forcefield.external.ExternalForceFieldInterface;
import simbryo.particles.isosurf.IsoSurfaceInterface;

/**
 * This force field applies a force field (towards+/away-) from an iso-surface.
 * 
 *
 * @author royer
 */
public class IsoSurfaceForceField extends ExternalForceFieldBase
                                  implements
                                  ExternalForceFieldInterface
{

  private IsoSurfaceInterface mIsoSurfaceInterface;

  public IsoSurfaceForceField(float pForceIntensity,
                              IsoSurfaceInterface pIsoSurfaceInterface)
  {
    super(pForceIntensity);
    mIsoSurfaceInterface = pIsoSurfaceInterface;
  }

  @Override
  public void applyForceField(int pBeginId,
                              int pEndId,
                              ParticleSystem pParticleSystem)
  {
    final float lForceIntensity = mForceIntensity;

    final int lDimension = pParticleSystem.getDimension();

    final float[] lPositionsRead = pParticleSystem.getPositions()
                                                  .getReadArray();
    final float[] lPositionsWrite = pParticleSystem.getPositions()
                                                   .getWriteArray();
    final float[] lVelocitiesRead = pParticleSystem.getVelocities()
                                                   .getReadArray();
    final float[] lVelocitiesWrite = pParticleSystem.getVelocities()
                                                    .getWriteArray();

    final int lIndexStart = pBeginId * lDimension;
    final int lIndexEnd = pEndId * lDimension;

    for (int i = lIndexStart; i < lIndexEnd; i += lDimension)
    {
      mIsoSurfaceInterface.clear();

      for (int d = 0; d < lDimension; d++)
      {
        float px = lPositionsRead[i + d];
        mIsoSurfaceInterface.addCoordinate(px);
      }

      float lDistance = mIsoSurfaceInterface.getDistance();

      float lForceSign = Math.signum(lDistance);

      for (int d = 0; d < lDimension; d++)
      {
        float dx = mIsoSurfaceInterface.getNormalizedGardient(d);
        lVelocitiesWrite[i + d] = lVelocitiesRead[i + d]
                                  + dx * lForceSign * lForceIntensity;
      }

    }

    pParticleSystem.getVelocities().swap();
  }

}
