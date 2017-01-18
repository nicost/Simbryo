package simbryo.textures;

/**
 * This base class implements common functionality required by all texture
 * generators
 *
 * @author royer
 */
public abstract class TextureGeneratorBase implements
                                           TextureGeneratorInterface
{
  private int mDimension;
  private float[] mScale;

  /**
   * Instantiates a texture generator with given dimension.
   * 
   * @param pDimension dimension
   */
  public TextureGeneratorBase(int pDimension)
  {
    super();
    mDimension = pDimension;

    float[] lScale = new float[pDimension];
    for (int d = 0; d < getDimension(); d++)
      lScale[d] = 1;
    setScale(lScale);
  }

  @Override
  public abstract TextureGeneratorInterface clone();

  /**
   * Returns scales for all dimensions.
   * 
   * @return scales as an array of floats.
   */
  public float[] getScale()
  {
    return mScale;
  }

  @Override
  public float getScale(int pIndex)
  {
    return mScale[pIndex];
  }

  /**
   * Sets scales for all dimensions.
   * 
   * @param pScale
   *          scales for al dimensions as an array of floats.
   */
  public void setScale(float... pScale)
  {
    mScale = pScale;
  }

  @Override
  public void setScale(int pIndex, float pScale)
  {
    mScale[pIndex] = pScale;
  }

  @Override
  public void setAllScales(float pScale)
  {
    for (int i = 0; i < mScale.length; i++)
      mScale[i] = pScale;
  }

  @Override
  public int getDimension()
  {
    return mDimension;
  }

  /**
   * Return texture volume for given dimensions
   * 
   * @param pDimensions
   * @return
   */
  protected int getVolume(int[] pDimensions)
  {
    long lVolume = 1;

    for (int i = 0; i < pDimensions.length; i++)
      lVolume *= pDimensions[i];

    return Math.toIntExact(lVolume);
  }

  @Override
  public abstract float sampleTexture(int... pCoordinate);

  @Override
  public float[] generateTexture(int... pDimensions)
  {
    float[] mArray = new float[getVolume(pDimensions)];

    if (pDimensions.length == 1)
    {
      long lWidth = pDimensions[0];

      for (int x = 0; x < lWidth; x++)
      {
        int i = Math.toIntExact(x);
        float lValue = sampleTexture(x);
        mArray[i] = lValue;
      }
    }
    else if (pDimensions.length == 2)
    {
      long lWidth = pDimensions[0];
      long lHeight = pDimensions[1];

      for (int y = 0; y < lHeight; y++)
        for (int x = 0; x < lWidth; x++)
        {
          int i = Math.toIntExact(x + y * lWidth);
          float lValue = sampleTexture(x, y);
          mArray[i] = lValue;
        }

    }
    else if (pDimensions.length == 3)
    {
      long lWidth = pDimensions[0];
      long lHeight = pDimensions[1];
      long lDepth = pDimensions[2];

      for (int z = 0; z < lDepth; z++)
        for (int y = 0; y < lHeight; y++)
          for (int x = 0; x < lWidth; x++)
          {
            int i = Math.toIntExact(x + y * lWidth
                                    + z * lWidth * lHeight);
            float lValue = sampleTexture(x, y, z);
            mArray[i] = lValue;
          }

    }

    return normalize(mArray);

  }

  private float[] normalize(float[] pTextureArray)
  {
    final int length = pTextureArray.length;
    
    float lMin=Float.POSITIVE_INFINITY, lMax=Float.NEGATIVE_INFINITY;
    for(int i=0; i<length; i++)
    {
      float lValue = pTextureArray[i];
      lMin = Math.min(lMin, lValue);
      lMax = Math.max(lMax, lValue);
    }
    
    if(lMax==lMin)
      return pTextureArray;
    
    final float a = 1f/(lMax-lMin);
    final float b = lMin;
    
    for(int i=0; i<length; i++)
      pTextureArray[i] = a*(pTextureArray[i]-b);
    
    return pTextureArray;
  }

}
