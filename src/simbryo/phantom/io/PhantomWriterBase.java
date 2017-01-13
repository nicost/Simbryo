package simbryo.phantom.io;

import coremem.enums.NativeTypeEnum;

public abstract class PhantomWriterBase implements PhantomWriterInterface
{
  private float mScaling,mOffset;
  private boolean mOverwrite;
  private NativeTypeEnum mNativeTypeEnum;

  public PhantomWriterBase(float pScaling, float pOffset)
  {
    super();
    setScaling(pScaling);
    setOffset(pOffset);
  }
  
  public void setDataType(NativeTypeEnum pNativeTypeEnum)
  {
    mNativeTypeEnum = pNativeTypeEnum;
  }
  
  public NativeTypeEnum getDataType()
  {
    return mNativeTypeEnum;
  }
  
  public void setOverwrite(boolean pOverwrite)
  {
    mOverwrite = pOverwrite;
  }
  
  public boolean getOverwrite()
  {
    return mOverwrite;
  }

  public float getScaling()
  {
    return mScaling;
  }

  public void setScaling(float pScaling)
  {
    mScaling = pScaling;
  }

  public float getOffset()
  {
    return mOffset;
  }

  public void setOffset(float pOffset)
  {
    mOffset = pOffset;
  }





}
