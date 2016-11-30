package simbryo.util.timing;

public class Timming
{
  private volatile Long mLastTime;

  public Timming()
  {

  }

  public void syncAtPeriod(double pPeriodInMilliseconds)
  {
    if (mLastTime != null)
    {
      long lDeadline = (long) (mLastTime
                               + (pPeriodInMilliseconds * 1.e6));

      while (System.nanoTime() < lDeadline)
      {
        try
        {
          Thread.sleep(1);
        }
        catch (InterruptedException e)
        {
        }
      }
    }

    mLastTime = System.nanoTime();
  }
}
