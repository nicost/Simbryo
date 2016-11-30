package embryosim.sequence;

import java.util.HashSet;

public class Sequence
{
  private volatile double mTime = 0;

  private HashSet<Double> mEventTriggeredSet = new HashSet<>();

  public void step(float pDeltaTime)
  {
    setTime(getTime() + pDeltaTime);
  }

  public void run(double pBegin, double pEnd, Runnable pRunnable)
  {
    if (getTime() >= pBegin && getTime() < pEnd)
      pRunnable.run();
  }

  public void run(double pEventTime, Runnable pRunnable)
  {
    if (getTime() >= pEventTime
        && !mEventTriggeredSet.contains(pEventTime))
    {
      pRunnable.run();
      mEventTriggeredSet.add(pEventTime);
    }

  }

  public double getTime()
  {
    return mTime;
  }

  public void setTime(double pTime)
  {
    mTime = pTime;
  }

}
