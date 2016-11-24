package embryosim.embryo.zoo;

import embryosim.embryo.EmbryoBase;
import embryosim.forcefield.external.impl.EllipsoidalForceField;

public class Drosophila extends EmbryoBase
{

  private static final float Fpetal = 0.0001f;
  private static final float Ri = 0.219f;
  private static final float Fradius = 0.15f;
  private EllipsoidalForceField mEllipsoidalForceField;

  public Drosophila(int pInicialNumberOfCells)
  {
    super(3, pInicialNumberOfCells, Ri);

    for (int i = 0; i < pInicialNumberOfCells; i++)
    {
      float x = (float) (0.5f + Ri * (Math.random() - 0.5f));
      float y = (float) (0.5f + Ri * (Math.random() - 0.5f));
      float z = (float) (0.5f + Ri * (Math.random() - 0.5f));

      int lId = addParticle(x, y, z);
      setRadius(lId, Ri);
      setTargetRadius(lId, getRadius(lId));
    }

    mEllipsoidalForceField = new EllipsoidalForceField(Fpetal,
                                                       Fradius,
                                                       0.5f,
                                                       0.5f,
                                                       0.5f,
                                                       1.3f,
                                                       1,
                                                       1);

  }

  @Override
  public void simulationSteps(int pNumberOfSteps)
  {
    for (int i = 0; i < pNumberOfSteps; i++)
    {
      applyForceField(mEllipsoidalForceField);
      super.simulationSteps(1);
    }
  }

  public void triggerCellDivision()
  {

    int lNumberOfParticles = getNumberOfParticles();

    float lRadiusFactor = (float) Math.pow(0.5f, 1.0f / 2);

    for (int i = 0; i < lNumberOfParticles; i++)
    {
      int lNewParticleId = cloneParticle(i, 0.001f);
      setTargetRadius(i, getRadius(i) * lRadiusFactor);
      setTargetRadius(lNewParticleId,
                      getRadius(lNewParticleId) * lRadiusFactor);
    }

  }

}
