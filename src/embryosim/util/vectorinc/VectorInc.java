package embryosim.util.vectorinc;

public class VectorInc
{

  public static boolean increment(int[] pMin,
                                  int[] pMax,
                                  int[] pCurrent)
  {
    pCurrent[0] = pCurrent[0]+1;

    propagateCarry(pMin, pMax, pCurrent);

    int lLastIndex = pCurrent.length-1;
    return pCurrent[lLastIndex]<pMax[lLastIndex];
  }

  private static void propagateCarry(int[] pMin,
                                     int[] pMax,
                                     int[] pCurrent)
  {
    int lLength = pMin.length;
    for (int i = 0; i < lLength; i++)
    {
      if (pCurrent[i] >= pMax[i] && i!=pCurrent.length-1)
      {
        pCurrent[i] -= (pMax[i] - pMin[i]);
        pCurrent[i+1]++;
      }
    }

  }
  
  

}
