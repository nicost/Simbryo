package simbryo.synthoscopy;

/**
 * Synthoscopy base class providing common fields and methods fof synthoscopy
 * modules
 *
 * @param <I>
 *          image type used to store and process images
 * @author royer
 */
public abstract class SynthoscopyBase<I> implements
                                     SynthoscopyInterface<I>
{

  private volatile float mIntensity = 1;
  private final long[] mImageDimensions;

  private volatile boolean mUpdateNeeded = true;

  /**
   * Instanciates a optics base class with basic optics related fields.
   * 
   * @param pImageDimensions
   *          image dimensions
   */
  public SynthoscopyBase(long... pImageDimensions)
  {
    mImageDimensions = pImageDimensions;
  }

  /**
   * Returns true if parameters have changed and an update is needed.
   * 
   * @return true if update needed
   */
  public boolean isUpdateNeeded()
  {
    return mUpdateNeeded;
  }

  /**
   * Sets the 'update needed' flag
   * 
   * @param pUpdateNeeded
   *          new flag state
   */
  public void setUpdateNeeded(boolean pUpdateNeeded)
  {
    mUpdateNeeded = pUpdateNeeded;
  }

  /**
   * Requests update
   */
  public void requestUpdate()
  {
    mUpdateNeeded = true;
  }

  /**
   * Returns intensity
   * 
   * @return intensity
   */
  public float getIntensity()
  {
    return mIntensity;
  }

  /**
   * Sets intensity
   * 
   * @param pIntensity
   *          light intensity
   */
  public void setIntensity(float pIntensity)
  {
    if (pIntensity != mIntensity)
    {
      mIntensity = pIntensity;
      requestUpdate();
    }
  }

  /**
   * Sets the image width
   * 
   * @param pWidth
   *          width
   */
  public void setWidth(long pWidth)
  {
    mImageDimensions[0] = pWidth;
  }

  /**
   * Sets the image height
   * 
   * @param pHeight
   *          height
   */
  public void setHeight(long pHeight)
  {
    mImageDimensions[1] = pHeight;
  }

  /**
   * Returns image depth
   * 
   * @param pDepth
   *          depth
   */
  public void setDepth(long pDepth)
  {
    mImageDimensions[2] = pDepth;
  }

  /**
   * Returns image width
   * 
   * @return width
   */
  @Override
  public long getWidth()
  {
    return mImageDimensions[0];
  }

  /**
   * Returns image height
   * 
   * @return height
   */
  @Override
  public long getHeight()
  {
    return mImageDimensions[1];
  }

  /**
   * Returns image depth
   * 
   * @return depth
   */
  @Override
  public long getDepth()
  {
    return mImageDimensions[2];
  }

  @Override
  public void clear(boolean pWaitToFinish)
  {
    requestUpdate();
  }

  @Override
  public void render(boolean pWaitToFinish)
  {
    setUpdateNeeded(false);
  }

}
