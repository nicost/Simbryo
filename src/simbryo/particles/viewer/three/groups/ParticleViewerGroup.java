package simbryo.particles.viewer.three.groups;

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
import simbryo.dynamics.tissue.cellprop.HasPolarity;
import simbryo.dynamics.tissue.cellprop.VectorCellProperty;
import simbryo.particles.ParticleSystem;

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
  private float[] mPolarities;

  private Group mParticlesGroup, mParticlesPolarityGroup;
  private final PhongMaterial mBoxMaterial, mParticleMaterial,
      mParticlePolarityMaterial;

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
    lBoundingBox.drawModeProperty().set(DrawMode.LINE);
    lBoundingBox.cullFaceProperty().set(CullFace.FRONT);

    lBoundingBox.setMaterial(getBoxMaterial());
    lBoundingBox.setOpacity(0.01);

    mParticleMaterial = new PhongMaterial();
    mParticleMaterial.setDiffuseColor(Color.WHITE);
    mParticleMaterial.setSpecularColor(Color.LIGHTBLUE);

    mParticlePolarityMaterial = new PhongMaterial();
    mParticlePolarityMaterial.setDiffuseColor(Color.LIGHTCORAL);
    mParticlePolarityMaterial.setSpecularColor(Color.LIGHTCORAL);

    // box.setMaterial(mParticleMaterial);
    mParticlesGroup = new Group();
    mParticlesPolarityGroup = new Group();
    getChildren().addAll(lBoundingBox,
                         mParticlesGroup,
                         mParticlesPolarityGroup);
  }

  public void updateDisplay(ParticleSystem pParticleSystem,
                            boolean pBlocking)
  {
    boolean lHasPolarity = pParticleSystem instanceof HasPolarity;
    VectorCellProperty lPolarityProperty = null;
    if (lHasPolarity)
      lPolarityProperty =
                        ((HasPolarity) pParticleSystem).getPolarityProperty();

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

      if (lHasPolarity)
        mPolarities = new float[lNumberOfParticles * lDimension];
    }

    pParticleSystem.copyPositions(mPositions);
    pParticleSystem.copyVelocities(mVelocities);
    pParticleSystem.copyRadii(mRadiis);
    if (lHasPolarity)
      lPolarityProperty.getArray()
                       .copyCurrentArrayTo(mPolarities,
                                           mPolarities.length);

    Platform.runLater(() -> {

      getChildren().remove(mParticlesGroup);
      if (lHasPolarity)
        getChildren().remove(mParticlesPolarityGroup);

      final ObservableList<Node> lParticlesSpheres =
                                                   mParticlesGroup.getChildren();

      final ObservableList<Node> lParticlesPolaritySpheres =
                                                           mParticlesPolarityGroup.getChildren();

      while (lParticlesSpheres.size() < pParticleSystem.getNumberOfParticles())
      {
        Sphere lSphere = new Sphere(1, mDisplayRadius ? 16 : 6);
        lSphere.setMaterial(getParticleMaterial());
        lSphere.setTranslateX(0);
        lSphere.setTranslateY(0);
        lSphere.setTranslateZ(0);

        lParticlesSpheres.add(lSphere);
        // System.out.println("ADDING");

        if (lHasPolarity)
        {
          Sphere lPolaritySphere = new Sphere(1, 3);
          lPolaritySphere.setMaterial(getParticlePolarityMaterial());
          lPolaritySphere.setTranslateX(0);
          lPolaritySphere.setTranslateY(0);
          lPolaritySphere.setTranslateZ(0);

          lParticlesPolaritySpheres.add(lPolaritySphere);
        }
      }

      while (lParticlesSpheres.size() > pParticleSystem.getNumberOfParticles())
      {
        lParticlesSpheres.remove(lParticlesSpheres.size() - 1);

        if (lHasPolarity)
          lParticlesPolaritySpheres.remove(lParticlesPolaritySpheres.size()
                                           - 1);
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

        double lSphereWorldX = mWidth * (x - 0.5f);
        double lSphereWorldY = mHeight * (y - 0.5f);
        double lSphereWorldZ = mDepth * (z - 0.5f);
        double lRadius =
                       mDisplayRadius ? mWidth * r : mWidth * 0.005f;

        Sphere lSphere = (Sphere) lParticlesSpheres.get(id);

        lSphere.setTranslateX(lSphereWorldX);
        lSphere.setTranslateY(lSphereWorldY);
        lSphere.setTranslateZ(lSphereWorldZ);
        lSphere.setScaleX(lRadius);
        lSphere.setScaleY(lRadius);
        lSphere.setScaleZ(lRadius);

        if (lHasPolarity)
        {

          Sphere lPolaritySphere =
                                 (Sphere) lParticlesPolaritySpheres.get(id);

          double lPolarityX = mPolarities[id * lDimension + 0];
          double lPolarityY = mPolarities[id * lDimension + 1];
          double lPolarityZ = mPolarities[id * lDimension + 2];

          double lPolarityWorldX =
                                 lSphereWorldX + lRadius * lPolarityX;
          double lPolarityWorldY =
                                 lSphereWorldY + lRadius * lPolarityY;
          double lPolarityWorldZ =
                                 lSphereWorldZ + lRadius * lPolarityZ;

          lPolaritySphere.setTranslateX(lPolarityWorldX);
          lPolaritySphere.setTranslateY(lPolarityWorldY);
          lPolaritySphere.setTranslateZ(lPolarityWorldZ);
          lPolaritySphere.setScaleX(lRadius / 4);
          lPolaritySphere.setScaleY(lRadius / 4);
          lPolaritySphere.setScaleZ(lRadius / 4);
        }

      }

      getChildren().add(mParticlesGroup);
      if (lHasPolarity)
        getChildren().add(mParticlesPolarityGroup);

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

  public PhongMaterial getParticlePolarityMaterial()
  {
    return mParticlePolarityMaterial;
  }

}
