package embryosim.embryo.zoo;

import embryosim.embryo.EmbryoBase;
import embryosim.psystem.forcefield.external.impl.SphericalForceField;

public class Spheroid extends EmbryoBase
{

  private static final float Fpetal = 0.0001f;
  private static final float Ri = 0.25f;
  private static final float Fradius = 0.20f;
  private SphericalForceField mSphericalForceField;

  public Spheroid(int pInicialNumberOfCells)
  {
    super(3, pInicialNumberOfCells, Ri);

    mRadiusShrinkageFactor = (float) Math.pow(0.5f, 1.0f / 2);

    for (int i = 0; i < pInicialNumberOfCells; i++)
    {
      float x = (float) (0.5f + Ri * (Math.random() - 0.5f));
      float y = (float) (0.5f + Ri * (Math.random() - 0.5f));
      float z = (float) (0.5f + Ri * (Math.random() - 0.5f));

      int lId = addParticle(x, y, z);
      setRadius(lId, Ri);
      setTargetRadius(lId, getRadius(lId));
    }

    mSphericalForceField = new SphericalForceField(Fpetal,
                                                   Fradius,
                                                   0.5f,
                                                   0.5f,
                                                   0.5f);
  }

  @Override
  public void simulationSteps(int pNumberOfSteps)
  {
    for (int i = 0; i < pNumberOfSteps; i++)
    {
      applyForceField(mSphericalForceField);
      super.simulationSteps(1);
    }
  }

}