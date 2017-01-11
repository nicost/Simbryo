package simbryo.phantom.io;

public abstract class PhantomWriterBase implements PhantomWriterInterface
{
  protected float mScaling;
  protected float mOffset;

  public PhantomWriterBase(float pScaling, float pOffset)
  {
    super();
    mScaling = pScaling;
    mOffset = pOffset;
  }

}
