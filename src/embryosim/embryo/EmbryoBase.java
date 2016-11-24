package embryosim.embryo;

import embryosim.psystem.ParticleSystem;
import embryosim.viewer.ParticleViewerInterface;
import embryosim.viewer.three.ParticleViewer3D;

public abstract class EmbryoBase extends ParticleSystem
{

  protected static final int cMaximumNumberOfCells = 100000;
  protected static final float V = 0.0001f;
  protected static final float Rt = 0.01f;
  protected static final float Rm = 0.005f;

  protected static final float D = 0.9f;
  protected static final float Db = 0.9f;
  protected static final float Fc = 0.0001f;
  protected static final float Fg = 0.000001f;

  protected static final float Ar = 0.05f;

  private final float[] mTargetRadii;
  protected float mRadiusShrinkageFactor = 1;

  private ParticleViewer3D mParticleViewer3D;

  protected volatile long mTimeStepIndex;

  public EmbryoBase(int pDimension,
                    int pInicialNumberOfCells,
                    float pInicialRadius)
  {
    super(pDimension, cMaximumNumberOfCells, Rm, Rt);

    mTargetRadii = new float[cMaximumNumberOfCells];

    for (int i = 0; i < pInicialNumberOfCells; i++)
    {
      float x = (float) (0.5f + 0.0001f * (Math.random() - 0.5f));
      float y = (float) (0.5f + 0.0001f * (Math.random() - 0.5f));
      float z = (float) (0.5f + 0.0001f * (Math.random() - 0.5f));

      int lId = addParticle(x, y, z);
      setRadius(lId, pInicialRadius);
      setTargetRadius(lId, getRadius(lId));
    }

    updateNeighborhoodCells();

  }

  public void setTargetRadius(int pParticleId, float pTargetRadius)
  {
    mTargetRadii[pParticleId] = pTargetRadius;
  }

  public void triggerCellDivision()
  {

    int lNumberOfParticles = getNumberOfParticles();

    for (int i = 0; i < lNumberOfParticles; i++)
    {
      int lNewParticleId = cloneParticle(i, 0.001f);
      setTargetRadius(i, getRadius(i) * mRadiusShrinkageFactor);
      setTargetRadius(lNewParticleId,
                      getRadius(lNewParticleId)
                                      * mRadiusShrinkageFactor);
    }

  }

  public void simulationSteps(int pNumberOfSteps)
  {
    for (int i = 0; i < pNumberOfSteps; i++)
    {
      smoothToTargetRadius(Ar);
      applyForcesForParticleCollisions(Fc, D);
      intergrateEuler();
      enforceBounds(Db);
      updateNeighborhoodCells();
      mTimeStepIndex++;
    }

    if (mParticleViewer3D != null)
      mParticleViewer3D.updateDisplay(true);

  }

  public long getTimeStepIndex()
  {
    return mTimeStepIndex;
  }

  public void smoothToTargetRadius(float pAlpha)
  {
    final float[] lRadiiReadArray = mRadii.getReadArray();
    final float[] lRadiiWriteArray = mRadii.getWriteArray();
    final int lNumberOfParticles = getNumberOfParticles();

    for (int id = 0; id < lNumberOfParticles; id++)
    {
      lRadiiWriteArray[id] = (1 - pAlpha) * lRadiiReadArray[id]
                             + pAlpha * mTargetRadii[id];
    }

    mRadii.swap();
  }

  public ParticleViewer3D open3DViewer()
  {
    if (mParticleViewer3D == null)
      mParticleViewer3D = ParticleViewer3D.view(this,
                                                "Viewing: "
                                                      + getClass().getSimpleName(),
                                                768,
                                                768);
    return mParticleViewer3D;
  }

  public ParticleViewerInterface getViewer()
  {
    return mParticleViewer3D;
  }

}
