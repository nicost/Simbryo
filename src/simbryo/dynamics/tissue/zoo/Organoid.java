package simbryo.dynamics.tissue.zoo;

import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.forcefield.external.impl.CentriForceField;

/**
 * 'Organoid' just a clum of cells dividing 14 times.
 *
 * @author royer
 */
public class Organoid extends TissueDynamics
{

  protected static final float Fc = 0.0001f;
  protected static final float D = 0.9f;

  private static final float Fpetal = 0.00005f;
  private static final float Ri = 0.135f;

  private static final float cRadiusShrinkageFactor =
                                                    (float) Math.pow(0.5f,
                                                                     1.0f / 4);

  private CentriForceField mCentriForceField;

  private volatile int mCellDivCount;

  /**
   * Creates an 'Organoid'.
   */
  public Organoid()
  {
    super(Fc, D);

    for (int i = 0; i < 1; i++)
    {
      float x = (float) (0.5f + 0.0001f * (Math.random() - 0.5f));
      float y = (float) (0.5f + 0.0001f * (Math.random() - 0.5f));
      float z = (float) (0.5f + 0.0001f * (Math.random() - 0.5f));

      int lId = addParticle(x, y, z);
      setRadius(lId, Ri);
      setTargetRadius(lId, getRadius(lId));
    }

    updateNeighborhoodCells();

    mCentriForceField =
                      new CentriForceField(Fpetal, 0.5f, 0.5f, 0.5f);
  }

  @Override
  public void simulationSteps(int pNumberOfSteps, float pDeltaTime)
  {
    for (int i = 0; i < pNumberOfSteps; i++)
    {
      if (mCellDivCount <= 14 && getTimeStepIndex() % 500 == 499)
        triggerCellDivision();

      applyForceField(mCentriForceField);
      super.simulationSteps(1, pDeltaTime);
    }
  }

  public void triggerCellDivision()
  {

    int lNumberOfParticles = getNumberOfParticles();

    for (int i = 0; i < lNumberOfParticles; i++)
    {
      int lNewParticleId = cloneParticle(i, 0.001f);
      setTargetRadius(i, getRadius(i) * cRadiusShrinkageFactor);
      setTargetRadius(lNewParticleId,
                      getRadius(lNewParticleId)
                                      * cRadiusShrinkageFactor);
    }

    mCellDivCount++;
  }/**/

}
