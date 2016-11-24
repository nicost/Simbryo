package embryosim.demos;

import java.util.Optional;

import embryosim.psystem.ParticleSystem;
import embryosim.psystem.forcefield.interaction.impl.CollisionForceField;
import embryosim.util.timing.Timming;
import embryosim.viewer.two.ParticleViewer2D;
import javafx.application.Application;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class Collider extends Application
{

  public int N = 20;
  public float V = 0.000000f;
  public float Fc = 0.0001f;
  public float R;
  public float D = 0.99f;
  public float Db = 0.5f;
  public float Fg = 0.000001f;
  public int G;

  Thread mThread;

  @Override
  public void start(Stage primaryStage)
  {
    primaryStage.close();

    try
    {

      TextInputDialog dialog = new TextInputDialog("100");

      dialog.setTitle("Collider");
      dialog.setHeaderText("Parameters");
      dialog.setContentText("Number of particles:");

      // Traditional way to get the response value.
      Optional<String> result = dialog.showAndWait();

      result.ifPresent(name -> {
        try
        {
          N = Integer.parseInt(name);
          R = (float) (0.395 / Math.sqrt(N));
          G = (int) (1 / R);

          System.out.println("G=" + G);
        }
        catch (Throwable e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      });

      CollisionForceField lCollisionForceField =
                                               new CollisionForceField(Fc,
                                                                       D);

      ParticleSystem lParticleSystem = new ParticleSystem(2, N, R, R);

      ParticleViewer2D lParticleViewer =
                                       new ParticleViewer2D(lParticleSystem,
                                                            "Particles Are Fun",
                                                            768,
                                                            768);
      lParticleViewer.setDisplayGrid(false);
      lParticleViewer.setDisplayElapsedTime(true);

      Runnable lRunnable = () -> {

        for (int i = 0; i < N; i++)
        {
          float x = (float) Math.random();
          float y = (float) Math.random();

          lParticleSystem.addParticle(x, y);
          lParticleSystem.setVelocity(i,
                                      (float) (V * (Math.random()
                                                    - 0.5f)),
                                      (float) (V * (Math.random()
                                                    - 0.5f)));
          lParticleSystem.setRadius(i, (float) (1e-9 + (1 * R))); // Math.sqrt(Math.random())
        }

        lParticleSystem.setRadius(0, 0.06f);
        // lParticleSystem.setRadius(1, 0.06f);
        // lParticleSystem.setPosition(1, 0.55f, 0.45f);

        lParticleSystem.updateNeighborhoodCells();

        // System.out.println(Arrays.toString(lParticleSystem.getVelocities()));

        Timming lTimming = new Timming();

        while (lParticleViewer.isShowing())
        {
          lTimming.syncAtPeriod(1);

          // lParticleSystem.repelAround(lMouseX, lMouseY, 0.00001f);
          lParticleSystem.updateNeighborhoodCells();
          lParticleSystem.applyForceField(lCollisionForceField);
          if (Fg > 0)
            lParticleSystem.applyForce(0f, Fg);
          lParticleSystem.intergrateEuler();
          lParticleSystem.enforceBounds(Db);

          float lMouseX = (float) (lParticleViewer.getMouseX());
          float lMouseY = (float) (lParticleViewer.getMouseY());

          lParticleSystem.setPosition(0, lMouseX, lMouseY);
          lParticleViewer.updateDisplay(true);

        }

        lParticleViewer.waitWhileShowing();
      };

      mThread = new Thread(lRunnable);
      mThread.setDaemon(true);
      mThread.start();
    }
    catch (Throwable e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void sleep(int pMillis)
  {
    try
    {
      Thread.sleep(pMillis);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws InterruptedException
  {
    launch(args);

  }

}
