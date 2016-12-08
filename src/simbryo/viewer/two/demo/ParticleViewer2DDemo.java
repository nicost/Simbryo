package simbryo.viewer.two.demo;

import org.junit.Test;

import simbryo.particles.ParticleSystem;
import simbryo.particles.forcefield.interaction.impl.CollisionForceField;
import simbryo.util.timing.Timming;
import simbryo.viewer.two.ParticleViewer2D;

public class ParticleViewer2DDemo
{

  @Test
  public void demo2D() throws InterruptedException
  {
    int G = 16;
    int N = 5000;
    float V = 0.0000001f;
    float R = (float) (0.4 / Math.sqrt(N));
    float Rm = 0.001f;
    float D = 0.99f;
    float Db = 0.9f;
    float Fc = 0.00001f;
    float Fg = 0.0000001f;

    CollisionForceField lCollisionForceField =
                                             new CollisionForceField(Fc,
                                                                     D,
                                                                     true);

    ParticleSystem lParticleSystem = new ParticleSystem(2,
                                                        N,
                                                        Rm,
                                                        Rm + 0.5f
                                                             * R);

    for (int i = 0; i < N; i++)
    {
      float x = (float) Math.random();
      float y = (float) Math.random();

      int lId = lParticleSystem.addParticle(x, y);
      lParticleSystem.setVelocity(lId,
                                  (float) (V
                                           * (Math.random() - 0.5f)),
                                  (float) (V
                                           * (Math.random() - 0.5f)));
      lParticleSystem.setRadius(lId, Rm + (R));// Math.random() *
    }

    lParticleSystem.setRadius(0, 0.06f);
    // lParticleSystem.setRadius(1, 0.06f);
    // lParticleSystem.setPosition(1, 0.55f, 0.45f);

    lParticleSystem.updateNeighborhoodCells();

    // System.out.println(Arrays.toString(lParticleSystem.getVelocities()));

    ParticleViewer2D lParticleViewer2D =
                                       ParticleViewer2D.view(lParticleSystem,
                                                             "Particles Are Fun",
                                                             768,
                                                             768);

    Timming lTimming = new Timming();

    while (lParticleViewer2D.isShowing())
    {
      lTimming.syncAtPeriod(3);

      // lParticleSystem.repelAround(lMouseX, lMouseY, 0.00001f);
      lParticleSystem.applyForceField(lCollisionForceField);
      if (Fg > 0)
        lParticleSystem.applyForce(0f, Fg);
      lParticleSystem.intergrateEuler();
      lParticleSystem.enforceBounds(Db);
      lParticleSystem.updateNeighborhoodCells();

      float lMouseX = (float) (lParticleViewer2D.getMouseX());
      float lMouseY = (float) (lParticleViewer2D.getMouseY());

      lParticleSystem.setPosition(0, lMouseX, lMouseY);
      lParticleViewer2D.updateDisplay(true);

    }

    lParticleViewer2D.waitWhileShowing();
  }

}
