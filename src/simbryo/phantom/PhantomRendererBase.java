package simbryo.phantom;

import java.util.Arrays;

import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.TissueDynamicsInterface;

/**
 * Base class providing common fields and methods for all classes implementing
 * the phantom renderer interface
 *
 * @author royer
 */
public abstract class PhantomRendererBase implements
                                          PhantomRendererInterface
{
  private final TissueDynamicsInterface mTissue;
  protected long[] mStackDimensions;
  protected boolean[] mPlaneAlreadyDrawnTable;

  private float mSignalIntensity = 1, mNoiseOverSignalRatio = 0f;

  /**
   * Instantiates a Phantom renderer for a given tissue dynamics and stack
   * dimensions.
   * 
   * @param pTissue tissue dynamics
   * @param pStackDimensions stack dimensions
   */
  public PhantomRendererBase(TissueDynamicsInterface pTissue,
                             long... pStackDimensions)
  {
    mTissue = pTissue;
    mStackDimensions = pStackDimensions;
    mPlaneAlreadyDrawnTable =
                            new boolean[Math.toIntExact(getDepth())];
  }
  
  @Override
  public TissueDynamicsInterface getTissue()
  {
    return mTissue;
  }

  @Override
  public long getWidth()
  {
    return mStackDimensions[0];
  }

  @Override
  public long getHeight()
  {
    return mStackDimensions[1];
  }

  @Override
  public long getDepth()
  {
    return mStackDimensions[2];
  }

  @Override
  public void invalidate(int pZ)
  {
    mPlaneAlreadyDrawnTable[pZ] = false;
  }

  @Override
  public boolean render(int pZPlaneIndex)
  {
    boolean lAlreadyDrawn = mPlaneAlreadyDrawnTable[pZPlaneIndex];
    mPlaneAlreadyDrawnTable[pZPlaneIndex] = true;
    return !lAlreadyDrawn;
  }

  @Override
  public abstract void render(int pZPlaneIndexBegin,
                              int pZPlaneIndexEnd);

  @Override
  public int renderSmart(int pZPlaneIndexBegin, int pZPlaneIndexEnd)
  {
    int lCounter = 0;

    int zi = pZPlaneIndexBegin;

    while (zi < pZPlaneIndexEnd)
    {
      while (zi < pZPlaneIndexEnd && mPlaneAlreadyDrawnTable[zi])
        zi++;

      int zj = zi;
      while (zj < pZPlaneIndexEnd && !mPlaneAlreadyDrawnTable[zj])
        zj++;
      render(zi, zj);

      for (int zk = zi; zk < zj; zk++)
      {
        mPlaneAlreadyDrawnTable[zk] = true;
        lCounter++;
      }
      zi = zj;
    }

    return lCounter;
  }

  @Override
  public void clear()
  {
    Arrays.fill(mPlaneAlreadyDrawnTable, false);
  }

  @Override
  public float getSignalIntensity()
  {
    return mSignalIntensity;
  }

  @Override
  public void setSignalIntensity(float pSignalIntensity)
  {
    mSignalIntensity = pSignalIntensity;
  }

  @Override
  public float getNoiseOverSignalRatio()
  {
    return mNoiseOverSignalRatio;
  }

  @Override
  public void setNoiseOverSignalRatio(float pNoiseOverSignalRatio)
  {
    mNoiseOverSignalRatio = pNoiseOverSignalRatio;
  }



}
