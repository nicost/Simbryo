package embryosim.viewer.demo;

import java.util.Optional;

import org.junit.Test;

import embryosim.psystem.ParticleSystem;
import embryosim.util.JavaFXUtil;
import embryosim.viewer.ParticleViewer;
import javafx.scene.control.TextInputDialog;

public class ParticleViewerDemo
{

  public int G = 16;
  public int N = 40;
  public float V = 0.0000001f;
  public float R = (float) (0.5 / Math.sqrt(N));
  public float Rm = 0.001f;
  public float D = 0.9999f;
  public float Fc = 0.00001f;

  private ParticleViewer mParticleViewer;

  @Test
  public void demo() throws InterruptedException
  {

    ParticleSystem lParticleSystem = new ParticleSystem(2,
                                                        N,
                                                        Rm,
                                                        Rm + 0.5f
                                                             * R);

    for (int i = 0; i < N; i++)
    {
      float x = (float) Math.random();
      float y = (float) Math.random();

      lParticleSystem.addParticle(x, y);
      lParticleSystem.setVelocity(i,
                                  (float) (V
                                           * (Math.random() - 0.5f)),
                                  (float) (V
                                           * (Math.random() - 0.5f)));
      lParticleSystem.setRadius(i,
                                (float) (Rm + (Math.random() * R)));
    }

    lParticleSystem.setRadius(0, 0.06f);
    // lParticleSystem.setRadius(1, 0.06f);
    // lParticleSystem.setPosition(1, 0.55f, 0.45f);

    lParticleSystem.updateNeighborhoodCells();

    // System.out.println(Arrays.toString(lParticleSystem.getVelocities()));

    JavaFXUtil.runAndWait(() -> {
      mParticleViewer = new ParticleViewer(lParticleSystem,
                                           "Particles Are Fun",
                                           768,
                                           768);
    });

    while (mParticleViewer.isShowing())
    {

      // lParticleSystem.repelAround(lMouseX, lMouseY, 0.00001f);
      lParticleSystem.applyForcesForElasticParticleCollisions(Fc, D);
      lParticleSystem.intergrateEuler();
      lParticleSystem.enforceBoundsWithElasticBouncing();
      lParticleSystem.updateNeighborhoodCells();

      float lMouseX = (float) (mParticleViewer.getMouseX());
      float lMouseY = (float) (mParticleViewer.getMouseY());

      lParticleSystem.setPosition(0, lMouseX, lMouseY);
      mParticleViewer.updateDisplay(true);
      Thread.sleep(1);
    }

    mParticleViewer.waitWhileShowing();
  }

}
