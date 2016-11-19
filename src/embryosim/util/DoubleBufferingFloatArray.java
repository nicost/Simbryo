package embryosim.util;

public class DoubleBufferingFloatArray
{
  private float[] mReadArray, mWriteArray;

  public DoubleBufferingFloatArray(int pLength)
  {
    super();
    allocateBuffers(pLength);
  }

  public void allocateBuffers(int pLength)
  {
    mReadArray = new float[pLength];
    mWriteArray = new float[pLength];
  }

  public float[] getReadArray()
  {
    return mReadArray;
  }

  public float[] getWriteArray()
  {
    return mWriteArray;
  }

  public void copyDefault()
  {
    System.arraycopy(mReadArray,
                     0,
                     mWriteArray,
                     0,
                     mWriteArray.length);
  }

  public void copyAndMult(float pValue)
  {
    int lLength = mWriteArray.length;
    for (int i = 0; i < lLength; i++)
      mWriteArray[i] = pValue * mReadArray[i];
  }

  public void swap()
  {
    float[] lTempRef = mWriteArray;
    mWriteArray = mReadArray;
    mReadArray = lTempRef;
  }

  public float[] getCurrentArray()
  {
    return mReadArray;
  }

  public void copyCurrentArrayTo(float[] pArrayCopy, int pLength)
  {
    System.arraycopy(getCurrentArray(), 0, pArrayCopy, 0, pLength);
  }

}
