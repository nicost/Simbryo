package simbryo.dynamics.tissue.embryo;

import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.TissueDynamicsInterface;
import simbryo.particles.isosurf.IsoSurfaceInterface;

/**
 * Embryos extend from a tissue dynamics and add the notion of 'embryo surface'
 *
 * @author royer
 */
public class EmbryoDynamics extends TissueDynamics implements
                            TissueDynamicsInterface,
                            HasSurface
{
  private IsoSurfaceInterface mEmbryoSurface;

  public EmbryoDynamics(float pCollisionForce,
                        float pDrag,
                        int pMaxNumberOfParticlesPerGridCell,
                        int[] pGridDimensions)
  {

    super(pCollisionForce,
          pDrag,
          pMaxNumberOfParticlesPerGridCell,
          pGridDimensions);

  }

  @Override
  public IsoSurfaceInterface getSurface()
  {
    return mEmbryoSurface;
  }

  @Override
  public void setSurface(IsoSurfaceInterface pEmbryoSurface)
  {
    mEmbryoSurface = pEmbryoSurface;
  }

}
