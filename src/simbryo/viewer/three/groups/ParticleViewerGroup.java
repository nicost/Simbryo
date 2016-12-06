package simbryo.viewer.three.groups;

import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import simbryo.dynamics.tissue.psystem.ParticleSystem;

public class ParticleViewerGroup extends Group
{

  private CountDownLatch mCountDownLatch;
  private volatile boolean mDisplayGrid, mDisplayElapsedTime,
      mDisplayRadius = true;

  private int mWidth, mHeight, mDepth;

  private volatile long mLastUpdateTimeInNanos = System.nanoTime();
  private volatile float mElapsedTime;
  private float[] mPositions;
  private float[] mVelocities;
  private float[] mRadiis;

  private Group mParticlesGroup;
  private final PhongMaterial mParticleMaterial;
  private final PhongMaterial mBoxMaterial;

  public ParticleViewerGroup(int pWidth, int pHeight, int pDepth)
  {
    super();
    mWidth = pWidth;
    mHeight = pHeight;
    mDepth = pDepth;

    mBoxMaterial = new PhongMaterial();
    getBoxMaterial().setDiffuseColor(Color.LIGHTBLUE);
    getBoxMaterial().setSpecularColor(Color.BLACK);
    getBoxMaterial().setSpecularPower(1);

    Box lBoundingBox = new Box(pWidth, pHeight, pDepth);
    lBoundingBox.setTranslateX(-0.0 * pWidth);
    lBoundingBox.setTranslateY(-0.0 * pHeight);
    lBoundingBox.setTranslateZ(-0.0 * pDepth);
    //lBoundingBox.drawModeProperty().set(DrawMode.LINE);
    lBoundingBox.cullFaceProperty().set(CullFace.FRONT);

    lBoundingBox.setMaterial(getBoxMaterial());
    lBoundingBox.setOpacity(0.01);

    mParticleMaterial = new PhongMaterial();
    getParticleMaterial().setDiffuseColor(Color.WHITE);
    getParticleMaterial().setSpecularColor(Color.LIGHTBLUE);

    // box.setMaterial(mParticleMaterial);
    mParticlesGroup = new Group();
    getChildren().addAll(lBoundingBox, mParticlesGroup);
  }

  public void updateDisplay(ParticleSystem pParticleSystem,
                            boolean pBlocking)
  {
    long lNow = System.nanoTime();
    float lNewElapsedTime =
                          (float) (1e-6
                                   * (lNow - mLastUpdateTimeInNanos));

    mLastUpdateTimeInNanos = lNow;

    mElapsedTime = (float) (0.99 * mElapsedTime
                            + 0.01 * lNewElapsedTime);

    // if (lNow % 100 == 0)
    // System.out.format("Elapsed time: %g ms \n", mElapsedTime);

    if (pBlocking)
    {
      try
      {
        if (mCountDownLatch != null)
          mCountDownLatch.await();
      }
      catch (InterruptedException e)
      {
      }
    } /**/
    mCountDownLatch = new CountDownLatch(1);

    final int lDimension = pParticleSystem.getDimension();
    final int lNumberOfParticles =
                                 pParticleSystem.getNumberOfParticles();

    if (mPositions == null
        || mPositions.length != lNumberOfParticles * lDimension)
    {
      mPositions = new float[lNumberOfParticles * lDimension];
      mVelocities = new float[lNumberOfParticles * lDimension];
      mRadiis = new float[lNumberOfParticles];
    }

    pParticleSystem.copyPositions(mPositions);
    pParticleSystem.copyVelocities(mVelocities);
    pParticleSystem.copyRadii(mRadiis);

    Platform.runLater(() -> {

      getChildren().remove(mParticlesGroup);

      final ObservableList<Node> lParticlesSpheres =
                                                   mParticlesGroup.getChildren();

      while (lParticlesSpheres.size() < pParticleSystem.getNumberOfParticles())
      {
        Sphere lSphere = new Sphere(1, mDisplayRadius ? 16 : 6);
        lSphere.setMaterial(getParticleMaterial());
        lSphere.setTranslateX(0);
        lSphere.setTranslateY(0);
        lSphere.setTranslateZ(0);

        lParticlesSpheres.add(lSphere);
        // System.out.println("ADDING");
      }

      while (lParticlesSpheres.size() > pParticleSystem.getNumberOfParticles())
      {
        lParticlesSpheres.remove(lParticlesSpheres.size() - 1);
        // System.out.println("REMOVING");
      }

      for (int id =
                  0, i =
                       0; id < lNumberOfParticles; id++, i +=
                                                           lDimension)
      {
        float x = mPositions[i + 0];
        float y = mPositions[i + 1];
        float z = mPositions[i + 2];
        float r = mRadiis[id];
        float vx = mVelocities[i + 0];
        float vy = mVelocities[i + 1];
        float vz = mVelocities[i + 2];
        // float v = vx * vx + vy * vy + vz * vz;

        double lWorldX = mWidth * (x - 0.5f);
        double lWorldY = mHeight * (y - 0.5f);
        double lWorldZ = mDepth * (z - 0.5f);
        double lRadius =
                       mDisplayRadius ? mWidth * r : mWidth * 0.005f;

        Sphere lSphere = (Sphere) lParticlesSpheres.get(id);

        lSphere.setTranslateX(lWorldX);
        lSphere.setTranslateY(lWorldY);
        lSphere.setTranslateZ(lWorldZ);
        lSphere.setScaleX(lRadius);
        lSphere.setScaleY(lRadius);
        lSphere.setScaleZ(lRadius);

      }

      getChildren().add(mParticlesGroup);

      mCountDownLatch.countDown();

    });

  }

  public boolean isDisplayGrid()
  {
    return mDisplayGrid;
  }

  public void setDisplayGrid(boolean pDisplayGrid)
  {
    mDisplayGrid = pDisplayGrid;
  }

  public boolean isDisplayElapsedTime()
  {
    return mDisplayElapsedTime;
  }

  public void setDisplayElapsedTime(boolean pDisplayElapsedTime)
  {
    mDisplayElapsedTime = pDisplayElapsedTime;
  }

  public void setDisplayRadius(boolean pDisplayRadius)
  {
    mDisplayRadius = pDisplayRadius;
  }

  public PhongMaterial getBoxMaterial()
  {
    return mBoxMaterial;
  }

  public PhongMaterial getParticleMaterial()
  {
    return mParticleMaterial;
  }

}
