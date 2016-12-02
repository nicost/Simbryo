package simbryo.render;

import java.util.Arrays;

import simbryo.embryo.Embryo;

public abstract class RendererBase implements RendererInterface
{
  protected Embryo mEmbryo;
  protected long[] mStackDimensions;
  protected boolean[] mCacheTable;

  public RendererBase(Embryo pEmbryo, long... pStackDimensions)
  {
    mEmbryo = pEmbryo;
    mStackDimensions = pStackDimensions;
    mCacheTable = new boolean[Math.toIntExact(getDepth())];
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
  public boolean render(int pZPLaneIndex)
  {
    boolean lWasInCache = mCacheTable[pZPLaneIndex];
    mCacheTable[pZPLaneIndex] = true;
    return !lWasInCache;
  }

  @Override
  public void clear()
  {
    Arrays.fill(mCacheTable, false);
  }

}
