package embryosim.viewer.three;

import embryosim.psystem.ParticleSystem;
import embryosim.util.jfx.JavaFXUtil;
import embryosim.viewer.ParticleViewerInterface;
import embryosim.viewer.three.groups.CameraGroup;
import embryosim.viewer.three.groups.ParticleViewerGroup;
import embryosim.viewer.three.groups.WorldGroup;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

/**
 * 2D Particle system viewer.
 *
 * @author royer
 */
public class ParticleViewer3D extends Stage
                              implements ParticleViewerInterface
{

  private static final double CAMERA_INITIAL_DISTANCE = -1000;
  private static final double CAMERA_NEAR_CLIP = 0.1;
  private static final double CAMERA_FAR_CLIP = 10000.0;

  private final Group mRoot = new Group();
  private final WorldGroup mWorldGroup = new WorldGroup();
  private final PerspectiveCamera mPerspectiveCamera =
                                                     new PerspectiveCamera(true);
  private final CameraGroup mCameraGroup = new CameraGroup();
  private final ParticleViewerGroup mParticleViewerGroup;

  private double mousePosX, mousePosY, mouseOldX, mouseOldY,
      mouseDeltaX, mouseDeltaY;
  private double mouseFactorX, mouseFactorY;

  private ParticleSystem mParticleSystem;
  private SubScene mSubScene;

  /**
   * Opens up a particle viewer, taking care that JavaFX is initialized.
   * 
   * @param pParticleSystem
   *          particle system
   * @param pWindowTitle
   *          window title
   * @param pWindowWidth
   *          window width
   * @param pWindowHeight
   *          window height
   * @return viewer.
   */
  public static ParticleViewer3D view(ParticleSystem pParticleSystem,
                                      String pWindowTitle,
                                      int pWindowWidth,
                                      int pWindowHeight)
  {
    return JavaFXUtil.runAndWait(() -> {
      return new ParticleViewer3D(pParticleSystem,
                                  pWindowTitle,
                                  pWindowWidth,
                                  pWindowHeight);
    });
  }

  /**
   * Creates a view for a given image, window title.
   * 
   * @param pClearCLImage
   *          image
   * @param pWindowTitle
   *          window title
   */
  public ParticleViewer3D(ParticleSystem pParticleSystem)
  {
    this(pParticleSystem, "Particle System Viewer", 512, 512);
  }

  /**
   * Creates a view for a given particle system. Window title, and window
   * dimensions can be specified.
   * 
   * @param pParticleSystem
   *          particle system
   * @param pWindowTitle
   *          window title
   * @param pWindowWidth
   *          window width
   * @param pWindowHeight
   *          window height
   */
  @SuppressWarnings("restriction")
  public ParticleViewer3D(ParticleSystem pParticleSystem,
                          String pWindowTitle,
                          int pWindowWidth,
                          int pWindowHeight)
  {
    super();
    mParticleSystem = pParticleSystem;

    setTitle(pWindowTitle);

    // Creating Ambient Light

    // AmbientLight lAmbientLight = new AmbientLight();
    // lAmbientLight.setColor(Color.rgb(255, 255, 255, 0.01));
    // mRoot.getChildren().add(lAmbientLight);

    // Creating Point Light

    PointLight lPointLight1 = new PointLight();
    lPointLight1.setColor(Color.rgb(246, 224, 239, 0.2));
    lPointLight1.setTranslateX(0);
    lPointLight1.setTranslateY(1000);
    lPointLight1.setTranslateZ(-800);
    mRoot.getChildren().add(lPointLight1);

    PointLight lPointLight2 = new PointLight();
    lPointLight2.setColor(Color.rgb(219, 241, 238, 0.1));
    lPointLight2.setTranslateX(-600);
    lPointLight2.setTranslateY(-600);
    lPointLight2.setTranslateZ(-800);
    mRoot.getChildren().add(lPointLight2);

    PointLight lPointLight3 = new PointLight();
    lPointLight3.setColor(Color.rgb(255, 251, 232, 0.3));
    lPointLight3.setTranslateX(6000);
    lPointLight3.setTranslateY(-6000);
    lPointLight3.setTranslateZ(+8000);
    mRoot.getChildren().add(lPointLight3);

    mParticleViewerGroup = new ParticleViewerGroup(512, 512, 512);
    lPointLight1.getScope().add(mParticleViewerGroup);
    lPointLight2.getScope().add(mParticleViewerGroup);
    lPointLight3.getScope().add(mParticleViewerGroup);
    mWorldGroup.getChildren().add(mParticleViewerGroup);

    mRoot.getChildren().add(mWorldGroup);
    mRoot.setDepthTest(DepthTest.ENABLE);

    buildCamera();

    Scene lScene =
                 new Scene(mRoot, pWindowWidth, pWindowHeight, true);
    lScene.setFill(Color.BLACK);
    handleMouse(lScene);

    lScene.setCamera(mPerspectiveCamera);/**/

    handleMouse(lScene);

    setScene(lScene);
    show();
  }

  private void buildCamera()
  {
    mRoot.getChildren().add(mCameraGroup);
    mCameraGroup.getChildren().add(mPerspectiveCamera);
    mPerspectiveCamera.setNearClip(CAMERA_NEAR_CLIP);
    mPerspectiveCamera.setFarClip(CAMERA_FAR_CLIP);
    mPerspectiveCamera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
  }

  private void handleMouse(Scene pScene)
  {
    pScene.setOnMousePressed((MouseEvent e) -> {

      if (e.getClickCount() == 2)
      {
        Platform.runLater(() -> setFullScreen(!isFullScreen()));
      }
      else
      {

        mousePosX = e.getSceneX();
        mousePosY = e.getSceneY();
        mouseOldX = e.getSceneX();
        mouseOldY = e.getSceneY();
      }
    });

    pScene.setOnMouseDragged((MouseEvent e) -> {
      mouseOldX = mousePosX;
      mouseOldY = mousePosY;
      mousePosX = e.getSceneX();
      mousePosY = e.getSceneY();
      mouseDeltaX = (mousePosX - mouseOldX);
      mouseDeltaY = (mousePosY - mouseOldY);
      if (e.isPrimaryButtonDown())
      {
        mCameraGroup.ry(mouseDeltaX * 180.0 / pScene.getWidth());
        mCameraGroup.rx(-mouseDeltaY * 180.0 / pScene.getHeight());
      }
      else if (e.isSecondaryButtonDown())
      {
        mPerspectiveCamera.setTranslateX(mPerspectiveCamera.getTranslateX()
                                         - mouseDeltaX);
        mPerspectiveCamera.setTranslateY(mPerspectiveCamera.getTranslateY()
                                         - mouseDeltaY);
      }
    });

    pScene.setOnScroll((e) -> {

      double lDeltaZ = e.getDeltaY();

      mPerspectiveCamera.setTranslateZ(mPerspectiveCamera.getTranslateZ()
                                       + lDeltaZ);

      e.consume();
    });/**/
  }

  /**
   * Triggers an update of the view. Must be called after the particle system
   * has been updated.
   * 
   * @param pBlocking
   *          if true, the viewer waits for the previous rendering to finish.
   */
  @Override
  public void updateDisplay(boolean pBlocking)
  {
    mParticleViewerGroup.updateDisplay(mParticleSystem, pBlocking);
  }

  /**
   * Waits (blocking call) while window is showing.
   */
  @Override
  public void waitWhileShowing()
  {
    while (isShowing())
    {
      try
      {
        Thread.sleep(100);
      }
      catch (InterruptedException e)
      {
      }
    }
  }

}
