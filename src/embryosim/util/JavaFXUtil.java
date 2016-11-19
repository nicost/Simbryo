package embryosim.util;

import java.util.concurrent.CountDownLatch;

import com.sun.javafx.application.PlatformImpl;

import javafx.application.Platform;

@SuppressWarnings("restriction")
public class JavaFXUtil
{
  public static void init()
  {
    PlatformImpl.startup(() -> {
    });
  }

  public static void runAndWait(Runnable pRunnable)
  {
    init();

    CountDownLatch lCountDownLatch = new CountDownLatch(1);

    Platform.runLater(() -> {
      pRunnable.run();
      lCountDownLatch.countDown();
    });

    try
    {
      lCountDownLatch.await();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }

  }
}
