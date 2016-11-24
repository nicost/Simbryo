package embryosim.util.mergeset;

public class MergeSet
{

  public static final int merge(final int[] pSetA,
                                final int[] pSetB,
                                final int[] pSetAuB)
  {
    return merge(pSetA,
                 0,
                 pSetA.length,
                 pSetB,
                 0,
                 pSetB.length,
                 pSetAuB,
                 0);
  }

  public static final int merge(final int[] pSetA,
                                final int pStartA,
                                final int[] pSetB,
                                final int pStartB,
                                final int[] pSetAuB,
                                final int pStartAuB)
  {
    return merge(pSetA,
                 pStartA,
                 Integer.MAX_VALUE/2,
                 pSetB,
                 pStartB,
                 Integer.MAX_VALUE/2,
                 pSetAuB,
                 pStartAuB);
  }

  public static final int merge(final int[] pSetA,
                                final int pStartA,
                                final int pLengthA,
                                final int[] pSetB,
                                final int pStartB,
                                final int pLengthB,
                                final int[] pSetAuB,
                                final int pStartAuB)
  {

    int lIndexA = pStartA;
    int lIndexB = pStartB;
    int lIndexAuB = pStartAuB;

    for (; lIndexA < pStartA + pLengthA
           && lIndexB < pStartB + pLengthB;)
    {
      int lValueA = pSetA[lIndexA];
      int lValueB = pSetB[lIndexB];

      if (lValueA == -1 || lValueB == -1)
        break;

      if (lValueA == lValueB)
      {
        pSetAuB[lIndexAuB] = lValueA;
        lIndexA++;
        lIndexB++;
        lIndexAuB++;
      }
      else
      {
        if (lValueA < lValueB)
        {
          pSetAuB[lIndexAuB] = lValueA;
          lIndexA++;
          lIndexAuB++;
        }
        else
        {
          pSetAuB[lIndexAuB] = lValueB;
          lIndexB++;
          lIndexAuB++;
        }
      }
    }

    while (lIndexA < pStartA + pLengthA)
    {
      int lValueA = pSetA[lIndexA];
      if (lValueA == -1)
        break;
      pSetAuB[lIndexAuB] = lValueA;
      lIndexA++;
      lIndexAuB++;
    }

    while (lIndexB < pStartB + pLengthB)
    {
      int lValueB = pSetB[lIndexB];
      if (lValueB == -1)
        break;
      pSetAuB[lIndexAuB] = lValueB;
      lIndexB++;
      lIndexAuB++;
    }

    return lIndexAuB-pStartAuB;
  }

}
