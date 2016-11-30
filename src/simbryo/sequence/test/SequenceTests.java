package simbryo.sequence.test;

import static org.junit.Assert.*;

import org.junit.Test;

import simbryo.sequence.Sequence;

public class SequenceTests
{

  @Test
  public void test()
  {

    Sequence lSequence = new Sequence();

    float dt = 0.001f;

    for (int i = 0; i < 1000; i++, lSequence.step(dt))
    {

      final int fi = i;
      lSequence.run(0.1, 0.2, () -> {
        assertTrue(fi >= 100 && fi < 200);
      });
      
      lSequence.run(0.15, 0.5, () -> {
        assertTrue(fi >= 150 && fi < 500);
      });

    }

  }

}
