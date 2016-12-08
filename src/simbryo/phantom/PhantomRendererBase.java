package simbryo.phantom;

import java.util.Arrays;

import simbryo.dynamics.tissue.TissueDynamics;

public abstract class PhantomRendererBase implements PhantomRendererInterface
{
  protected TissueDynamics mEmbryo;
  protected long[] mStackDimensions;
  protected boolean[] mPlaneAlreadyDrawnTable;

  public PhantomRendererBase(TissueDynamics pEmbryo, long... pStackDimensions)
  {
    mEmbryo = pEmbryo;
    mStackDimensions = pStackDimensions;
    mPlaneAlreadyDrawnTable =
                            new boolean[Math.toIntExact(getDepth())];
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

}
