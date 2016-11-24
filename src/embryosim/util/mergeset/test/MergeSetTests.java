package embryosim.util.mergeset.test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import embryosim.util.mergeset.MergeSet;

public class MergeSetTests
{

  @Test
  public void testFullArray()
  {
    int[] lSet1 = new int[]
    { 1, 2, 3, 4 };

    int[] lSet2 = new int[]
    { 0, 2, 4, 7, 9 };

    int[] lSet1u2 = new int[lSet1.length + lSet2.length];

    final int lLength = MergeSet.merge(lSet1, lSet2, lSet1u2);

    // System.out.println("lLength=" + lLength);
    // System.out.println(Arrays.toString(lSet1u2));

    assertEquals(7, lLength);

    assertEquals(0, lSet1u2[0]);
    assertEquals(1, lSet1u2[1]);
    assertEquals(2, lSet1u2[2]);
    assertEquals(3, lSet1u2[3]);
    assertEquals(4, lSet1u2[4]);
    assertEquals(7, lSet1u2[5]);
    assertEquals(9, lSet1u2[6]);
    assertEquals(0, lSet1u2[7]);

  }

  @Test
  public void testRangeArray()
  {
    int[] lSet1 = new int[]
    { -1, -1, -1, -1, 1, 2, 3, 4, -1, -1 };

    int[] lSet2 = new int[]
    { -2, -2, -2, 0, 2, 4, 7, 9, -1, -1 };

    int[] lSet1u2 = new int[lSet1.length + lSet2.length];

    final int lLength = MergeSet.merge(lSet1,
                                       4,
                                       3,
                                       lSet2,
                                       3,
                                       4,
                                       lSet1u2,
                                       1);

    System.out.println("lLength=" + lLength);
    System.out.println(Arrays.toString(lSet1u2));

    assertEquals(6, lLength);

    assertEquals(0, lSet1u2[0]);
    assertEquals(0, lSet1u2[1]);
    assertEquals(1, lSet1u2[2]);
    assertEquals(2, lSet1u2[3]);
    assertEquals(3, lSet1u2[4]);
    assertEquals(4, lSet1u2[5]);
    assertEquals(7, lSet1u2[6]);
    assertEquals(0, lSet1u2[7]);

  }

  @Test
  public void testMinusOneTerminatedArray()
  {
    int[] lSet1 = new int[]
    { -1, -1, -1, -1, 1, 2, 3, 4, -1, -1 };

    int[] lSet2 = new int[]
    { -1, -1, -1, 0, 2, 4, 7, 9, -1, -1 };

    int[] lSet1u2 = new int[lSet1.length + lSet2.length];

    final int lLength =
                      MergeSet.merge(lSet1, 4, lSet2, 3, lSet1u2, 1);

    System.out.println("lLength=" + lLength);
    System.out.println(Arrays.toString(lSet1u2));

    assertEquals(7, lLength);

    assertEquals(0, lSet1u2[0]);
    assertEquals(0, lSet1u2[1]);
    assertEquals(1, lSet1u2[2]);
    assertEquals(2, lSet1u2[3]);
    assertEquals(3, lSet1u2[4]);
    assertEquals(4, lSet1u2[5]);
    assertEquals(7, lSet1u2[6]);
    assertEquals(9, lSet1u2[7]);
    assertEquals(0, lSet1u2[8]);

  }

}