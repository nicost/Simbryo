package simbryo.synthoscopy.phantom;

import static java.lang.Math.floor;
import static java.lang.Math.min;
import static java.lang.Math.toIntExact;

import clearcl.ClearCLDevice;

/**
 * Utilities class containing usefull static methods for phantom rendering.
 *
 * @author royer
 */
public abstract class PhantomRendererUtils
{

  /**
   * Returns optimal grid dimensions for a given OpenCL device and stack
   * dimensions.
   *
   * 
   * @param pDevice
   *          ClearCL device
   * @param pStackDimensionsLong
   *          stack dimensions
   * @return array of grid dimensions
   */
  public static int[] getOptimalGridDimensions(ClearCLDevice pDevice,
                                               long... pStackDimensionsLong)
  {
    int[] lStackDimensionsInteger = new int[pStackDimensionsLong.length];
    for (int i = 0; i < lStackDimensionsInteger.length; i++)
      lStackDimensionsInteger[i] = toIntExact(pStackDimensionsLong[i]);
    return getOptimalGridDimensions(pDevice, lStackDimensionsInteger);
  }

  /**
   * Returns optimal grid dimensions for a given OpenCL device and stack
   * dimensions.
   *
   * 
   * @param pDevice
   *          ClearCL device
   * @param pStackDimensions
   *          stack dimensions
   * @return array of grid dimensions
   */
  public static int[] getOptimalGridDimensions(ClearCLDevice pDevice,
                                               int... pStackDimensions)
  {
    int lDimension = pStackDimensions.length;
    int[] lGridDimensions = new int[lDimension];

    long lMaxWorkGroupSize = pDevice.getMaxWorkGroupSize();

    int lMaxGridDim = (int) floor(min(8,
                                      Math.pow(lMaxWorkGroupSize,
                                               1.0 / 3)));

    for (int d = 0; d < lDimension; d++)
    {
      lGridDimensions[d] = pStackDimensions[d] / lMaxGridDim;
    }

    return lGridDimensions;
  }

}
