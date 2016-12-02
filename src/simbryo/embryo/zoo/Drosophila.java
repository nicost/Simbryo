package simbryo.embryo.zoo;

import simbryo.embryo.Embryo;
import simbryo.isosurf.impl.Ellipsoid;
import simbryo.morphogen.Morphogen;
import simbryo.morphogen.operators.impl.StrogatzWaveOperator;
import simbryo.psystem.forcefield.ForceFieldInterface;
import simbryo.psystem.forcefield.external.impl.OneSidedIsoSurfaceForceField;

/**
 * Drosophila melanogster embryo (First 14 divisions).
 *
 * @author royer
 */
public class Drosophila extends Embryo
{

  private static final float cCellDivisionRadiusShrinkage =
                                                          (float) Math.pow(0.5f,
                                                                           1.0f / 2);
  protected static final float Fc = 0.00008f;
  protected static final float D = 0.9f;

  private static final float Finside = 0.0002f;
  private static final float Fafc = 0.00005f;

  private static final float Ri = 0.08f;

  private ForceFieldInterface mOutsideEllipseForceField;
  private ForceFieldInterface mInsideEllipseForceField;

  private Morphogen mCellCycleMorphogen;
  private StrogatzWaveOperator mStrogatzOscillator;

  private volatile int mCellDivCount;

  /**
   * Creates a Drosophila embryo.
   */
  public Drosophila()
  {
    super(3, Fc, D);

    for (int i = 0; i < 1; i++)
    {
      float x = (float) (Ri - 0.02 * Math.random());
      float y = (float) (0.5f + Ri * (Math.random() - 0.5f));
      float z = (float) (0.5f + Ri * (Math.random() - 0.5f));

      int lId = addParticle(x, y, z);
      setRadius(lId, Ri);
      setTargetRadius(lId, getRadius(lId));
    }

    updateNeighborhoodCells();

    Ellipsoid lEllipsoid =
                         new Ellipsoid(0.48f,
                                       0.5f,
                                       0.5f,
                                       0.5f,
                                       1f,
                                       0.43f,
                                       0.43f);

    mOutsideEllipseForceField =
                              new OneSidedIsoSurfaceForceField(true,
                                                                true,
                                                                Finside,
                                                                lEllipsoid);
    mInsideEllipseForceField =
                             new OneSidedIsoSurfaceForceField(false,
                                                               false,
                                                               Fafc,
                                                               lEllipsoid);


    mCellCycleMorphogen = addMorphogen();
    mStrogatzOscillator = new StrogatzWaveOperator(0.001f,
                                                   0.01f,
                                                   0.1f)
    {

      @Override
      public float eventHook(boolean pEvent,
                             int pId,
                             float[] pPositions,
                             float[] pVelocities,
                             float[] pRadii,
                             float pNewMorphogenValue)
      {
        return cellDivisionHook(pEvent, pId, pNewMorphogenValue);
      }

    };

  }

  private float cellDivisionHook(boolean pEvent,
                                 int pId,
                                 float pNewMorphogenValue)
  {
    int lDimension = getDimension();

    if (pEvent && pNewMorphogenValue < 14)
    {

      int lNewParticleId = cloneParticle(pId, 0.001f);

      mCellCycleMorphogen.getArray()
                         .getWriteArray()[lNewParticleId] =
                                                          pNewMorphogenValue;

      if (pNewMorphogenValue >= 6)
      {
        setTargetRadius(pId,
                        getRadius(pId)
                             * cCellDivisionRadiusShrinkage);
        setTargetRadius(lNewParticleId,
                        getRadius(lNewParticleId)
                                        * cCellDivisionRadiusShrinkage);
      }

      if (pNewMorphogenValue >= 5)
      {
        float[] lPositions = mPositions.getReadArray();
        float x = lPositions[pId * lDimension + 0];
        if (x > 0.9f)
          return (float) (pNewMorphogenValue + 0.1f * Math.pow(x, 4));
      }

    }

    if ((int) pNewMorphogenValue > mCellDivCount)
      System.out.println("Division: " + mCellDivCount);

    mCellDivCount = Math.max(mCellDivCount, (int) pNewMorphogenValue);

    return pNewMorphogenValue;
  }

  @Override
  public void simulationSteps(int pNumberOfSteps, float pDeltaTime)
  {
    for (int i = 0; i < pNumberOfSteps; i++)
    {
      applyOperator(mStrogatzOscillator, mCellCycleMorphogen);

      adjustForceFieldInsideEllipse();

      applyForceField(mOutsideEllipseForceField);
      applyForceField(mInsideEllipseForceField);

      super.simulationSteps(1, pDeltaTime);
    }
  }

  private void adjustForceFieldInsideEllipse()
  {
    final float lForce;

    switch (mCellDivCount)
    {
    case 0:
      lForce = -1f * Fafc;
      break;
    case 1:
      lForce = -1f * Fafc;
      break;
    case 2:
      lForce = -1f * Fafc;
      break;
    case 3:
      lForce = -1f * Fafc;
      break;
    case 4:
      lForce = +0.1f * Fafc;
      break;
    case 5:
      lForce = +0.5f * Fafc;
      break;

    default:
      lForce = Fafc;

    }

    mInsideEllipseForceField.setForceIntensity(lForce);
  }

}
