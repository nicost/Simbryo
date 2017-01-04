package simbryo.phantom;

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
   * //TODO: currently it is set to a constant: 8 but if in the future we figure out a way to
   * choose that more wisely, we will. Also, this also depends on the actual max neighboours per grid cell,
   * which further complicates things...
   * 
   * @param pDevice
   * @param pStackDimensions
   * @return
   */
  public static int[] getOptimalGridDimensions(ClearCLDevice pDevice,
                                               int... pStackDimensions)
  {
    int lDimension = pStackDimensions.length;
    int[] lGridDimensions = new int[lDimension];

    
    for (int d = 0; d < lDimension; d++)
    {
      lGridDimensions[d] = pStackDimensions[d] / 8;
    }

    return lGridDimensions;
  }

}
