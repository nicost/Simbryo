package embryosim.util;

/**
 * Instances of this class implement a double-buffering scheme for float arrays.
 * Two arrays are maintained: a read and write array, a method is provided to
 * swap the two arrays, as well as other methods for performing other typical
 * operations.
 *
 * @author royer
 */
public class DoubleBufferingFloatArray
{
  private float[] mReadArray, mWriteArray;

  /**
   * Initialize the two arrays with a given fixed size.
   * 
   * @param pLength
   */
  public DoubleBufferingFloatArray(int pLength)
  {
    super();
    allocateArrays(pLength);
  }

  /**
   * Allocates the two arrays with a (new) length.
   * 
   * @param pLength
   */
  public void allocateArrays(int pLength)
  {
    mReadArray = new float[pLength];
    mWriteArray = new float[pLength];
  }

  /**
   * Returns the read array.
   * 
   * @return read array.
   */
  public float[] getReadArray()
  {
    return mReadArray;
  }

  /**
   * Returns the write array.
   * 
   * @return write array.
   */
  public float[] getWriteArray()
  {
    return mWriteArray;
  }

  /**
   * Returns the current array.
   * 
   * @return current array.
   */
  public float[] getCurrentArray()
  {
    return mReadArray;
  }

  /**
   * Returns th previous array.e previous array
   * 
   * @return
   */
  public float[] getPreviousArray()
  {
    return mWriteArray;
  }

  /**
   * Copies the values from the read array to the write array. This is usefull
   * if you know that only a few values will be changed.
   */
  public void copyDefault()
  {
    System.arraycopy(mReadArray,
                     0,
                     mWriteArray,
                     0,
                     mWriteArray.length);
  }

  /**
   * Copies all values from the read array to the write array after multiplying
   * these values with a constant factor.
   * 
   * @param pValue
   */
  public void copyAndMult(float pValue)
  {
    int lLength = mWriteArray.length;
    for (int i = 0; i < lLength; i++)
      mWriteArray[i] = pValue * mReadArray[i];
  }

  /**
   * Swap arrays.
   */
  public void swap()
  {
    float[] lTempRef = mWriteArray;
    mWriteArray = mReadArray;
    mReadArray = lTempRef;
  }

  /**
   * Copies the contents of the read array to another provided array.
   * 
   * @param pArrayCopy
   *          array to copy contents to.
   * @param pLength
   *          number of entries to copy
   */
  public void copyCurrentArrayTo(float[] pArrayCopy, int pLength)
  {
    System.arraycopy(getCurrentArray(), 0, pArrayCopy, 0, pLength);
  }

}
