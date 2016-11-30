package simbryo.viewer.sandbox;

import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class Demo3D extends Application
{
  private static final double CAMERA_INITIAL_DISTANCE = -1000;
  private static final double CAMERA_NEAR_CLIP = 0.1;
  private static final double CAMERA_FAR_CLIP = 10000.0;
  
  final Group mRoot = new Group();
  final WorldGroup mWorldGroup = new WorldGroup();
  final PerspectiveCamera mPerspectiveCamera = new PerspectiveCamera(true);
  final CameraGroup mCameraGroup = new CameraGroup();

  double mousePosX, mousePosY, mouseOldX, mouseOldY, mouseDeltaX,
      mouseDeltaY;
  double mouseFactorX, mouseFactorY;

  @Override
  public void start(Stage primaryStage)
  {
    mRoot.getChildren().add(mWorldGroup);
    mRoot.setDepthTest(DepthTest.ENABLE);
    buildCamera();
    buildBodySystem();
    Scene scene = new Scene(mRoot, 800, 600, true);
    scene.setFill(Color.GREY);
    handleMouse(scene);
    primaryStage.setTitle("TrafoTest");
    primaryStage.setScene(scene);
    primaryStage.show();
    scene.setCamera(mPerspectiveCamera);
    mouseFactorX = 180.0 / scene.getWidth();
    mouseFactorY = 180.0 / scene.getHeight();
  }

  private void buildCamera()
  {
    mRoot.getChildren().add(mCameraGroup);
    mCameraGroup.getChildren().add(mPerspectiveCamera);
    mPerspectiveCamera.setNearClip(CAMERA_NEAR_CLIP);
    mPerspectiveCamera.setFarClip(CAMERA_FAR_CLIP);
    mPerspectiveCamera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
  }

  private void buildBodySystem()
  {
    PhongMaterial whiteMaterial = new PhongMaterial();
    whiteMaterial.setDiffuseColor(Color.WHITE);
    whiteMaterial.setSpecularColor(Color.LIGHTBLUE);
    Box box = new Box(400, 200, 100);
    box.setMaterial(whiteMaterial);
    PhongMaterial redMaterial = new PhongMaterial();
    redMaterial.setDiffuseColor(Color.DARKRED);
    redMaterial.setSpecularColor(Color.RED);
    Sphere sphere = new Sphere(5);
    sphere.setMaterial(redMaterial);
    sphere.setTranslateX(200.0);
    sphere.setTranslateY(-100.0);
    sphere.setTranslateZ(-50.0);
    mWorldGroup.getChildren().addAll(box);
    mWorldGroup.getChildren().addAll(sphere);
  }

  private void handleMouse(Scene scene)
  {
    scene.setOnMousePressed((MouseEvent me) -> {
      mousePosX = me.getSceneX();
      mousePosY = me.getSceneY();
      mouseOldX = me.getSceneX();
      mouseOldY = me.getSceneY();
    });
    scene.setOnMouseDragged((MouseEvent me) -> {
      mouseOldX = mousePosX;
      mouseOldY = mousePosY;
      mousePosX = me.getSceneX();
      mousePosY = me.getSceneY();
      mouseDeltaX = (mousePosX - mouseOldX);
      mouseDeltaY = (mousePosY - mouseOldY);
      if (me.isPrimaryButtonDown())
      {
        mCameraGroup.ry(mouseDeltaX * 180.0 / scene.getWidth());
        mCameraGroup.rx(-mouseDeltaY * 180.0 / scene.getHeight());
      }
      else if (me.isSecondaryButtonDown())
      {
        mPerspectiveCamera.setTranslateZ(mPerspectiveCamera.getTranslateZ() + mouseDeltaY);
      }
    });
  }

  public static void main(String[] args)
  {
    launch(args);
  }

}

class WorldGroup extends Group
{
  final Translate t = new Translate(0.0, 0.0, 0.0);
  final Rotate rx = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
  final Rotate ry = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
  final Rotate rz = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);

  public WorldGroup()
  {
    super();
    this.getTransforms().addAll(t, rx, ry, rz);
  }
}

class CameraGroup extends Group
{
  Point3D px = new Point3D(1.0, 0.0, 0.0);
  Point3D py = new Point3D(0.0, 1.0, 0.0);
  Rotate r;
  Transform t = new Rotate();

  public CameraGroup()
  {
    super();
  }

  public void rx(double angle)
  {
    r = new Rotate(angle, px);
    this.t = t.createConcatenation(r);
    this.getTransforms().clear();
    this.getTransforms().addAll(t);
  }

  public void ry(double angle)
  {
    r = new Rotate(angle, py);
    this.t = t.createConcatenation(r);
    this.getTransforms().clear();
    this.getTransforms().addAll(t);
  }

}
