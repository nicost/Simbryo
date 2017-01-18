package simbryo.phantom.io;

import coremem.enums.NativeTypeEnum;

/**
 * This base class provides common fields and methods needed by implementations
 * of Phantom writers.
 *
 * @author royer
 */
public abstract class PhantomWriterBase implements
                                        PhantomWriterInterface
{
  private float mScaling, mOffset;
  private boolean mOverwrite;
  private NativeTypeEnum mNativeTypeEnum;

  /**
   * Instanciates a Phantom raw writer. The voxel values produced by the phantom
   * are scaled accoding to y = a*x+b.
   * 
   * @param pScaling
   *          value scaling a
   * @param pOffset
   *          value offset b
   */
  public PhantomWriterBase(float pScaling, float pOffset)
  {
    super();
    setScaling(pScaling);
    setOffset(pOffset);
  }

  /**
   * Sets the native data set to use to store the data in the file
   * 
   * @param pNativeTypeEnum
   *          native data type.
   */
  public void setDataType(NativeTypeEnum pNativeTypeEnum)
  {
    mNativeTypeEnum = pNativeTypeEnum;
  }

  /**
   * Returns the native data type used to store the data in the file.
   * 
   * @return native data type
   */
  public NativeTypeEnum getDataType()
  {
    return mNativeTypeEnum;
  }

  /**
   * Sets whether to overwrite existing files
   * @param pOverwrite true -> overwrites, false othewise
   */
  public void setOverwrite(boolean pOverwrite)
  {
    mOverwrite = pOverwrite;
  }

  /**
   * Returns the state of the overwrite flag
   * @return true -> overwrite, fale otherwise
   */
  public boolean getOverwrite()
  {
    return mOverwrite;
  }

  /**
   * Returns the scaling parameter.
   * @return scaling parameter
   */
  public float getScaling()
  {
    return mScaling;
  }

  /**
   * Sets the scaling parameter
   * @param pScaling scaling parameter
   */
  public void setScaling(float pScaling)
  {
    mScaling = pScaling;
  }

  /**
   * Returns the offset parameter
   * @return offset parameter
   */
  public float getOffset()
  {
    return mOffset;
  }

  /**
   * Sets the offset parameter
   * @param pOffset offset parameter
   */
  public void setOffset(float pOffset)
  {
    mOffset = pOffset;
  }

}
