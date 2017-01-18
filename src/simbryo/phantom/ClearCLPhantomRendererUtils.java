package simbryo.phantom;

import static java.lang.Math.floor;
import static java.lang.Math.min;

import clearcl.ClearCLDevice;

/**
 * Utilities class containing usefull static methods for phantom rendering.
 *
 * @author royer
 */
public abstract class ClearCLPhantomRendererUtils
{

  /**
   * Returns optimal grid dimensions for a given OpenCL device and stack dimensions.
   *
   * 
   * @param pDevice ClearCL device
   * @param pStackDimensions stack dimensions
   * @return array of grid dimensions
   */
  public static int[] getOptimalGridDimensions(ClearCLDevice pDevice,
                                               int... pStackDimensions)
  {
    int lDimension = pStackDimensions.length;
    int[] lGridDimensions = new int[lDimension];

    long lMaxWorkGroupSize = pDevice.getMaxWorkGroupSize();
    
    int lMaxGridDim = (int) floor(min(8,Math.pow(lMaxWorkGroupSize, 1.0/3)));
    
    for (int d = 0; d < lDimension; d++)
    {
      lGridDimensions[d] = pStackDimensions[d] / lMaxGridDim;
    }

    return lGridDimensions;
  }

}
