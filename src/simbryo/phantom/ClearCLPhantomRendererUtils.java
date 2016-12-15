package simbryo.phantom;

import clearcl.ClearCLDevice;

public abstract class ClearCLPhantomRendererUtils 
{

  public static int[] getOptimalGridDimensions(ClearCLDevice pFastestGPUDevice,
                                               int... pStackDimensions)
  {
    int lDimension = pStackDimensions.length;
    int[] lGridDimensions = new int[lDimension];
    
    
    for(int d=0; d<lDimension; d++)
    {
      lGridDimensions[d] = pStackDimensions[d]/8;
    }
    
    return lGridDimensions;
  }

  
}
