package embryosim.viewer;

import clearcl.viewer.jfx.PanZoomScene;
import embryosim.psystem.ParticleSystem;
import embryosim.viewer.jfx.ViewParticlesCanvas;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Particle system viewer.
 *
 * @author royer
 */
public class ParticleViewer
{

  private Stage mStage = null;
  private ViewParticlesCanvas mViewParticles2D;
  private ParticleSystem mParticleSystem;
  private PanZoomScene mPanZoomScene;

  /**
   * Creates a view for a given image, window title.
   * 
   * @param pClearCLImage
   *          image
   * @param pWindowTitle
   *          window title
   */
  public ParticleViewer(ParticleSystem pParticleSystem)
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
  public ParticleViewer(ParticleSystem pParticleSystem,
                        String pWindowTitle,
                        int pWindowWidth,
                        int pWindowHeight)
  {
    super();
    mParticleSystem = pParticleSystem;

    mStage = new Stage();
    mStage.setTitle(pWindowTitle);

    mViewParticles2D = new ViewParticlesCanvas(pWindowWidth,
                                               pWindowHeight);

    StackPane lStackPane = new StackPane();

    lStackPane.getChildren().addAll(mViewParticles2D);

    mPanZoomScene = new PanZoomScene(mStage,
                                     lStackPane,
                                     mViewParticles2D,
                                     pWindowWidth,
                                     pWindowHeight,
                                     Color.BLACK);
    mPanZoomScene.setFill(Color.BLACK);

    mStage.setScene(mPanZoomScene);

    mStage.show();

  }

  /**
   * Triggers an update of the view. Must be called after the particle system
   * has been updated.
   * 
   * @param pBlocking
   *          if true, the viewer waits for the previous rendering to finish.
   */
  public void updateDisplay(boolean pBlocking)
  {
    mViewParticles2D.updateDisplay(mParticleSystem, pBlocking);
  }

  /**
   * Waits (blocking call) while window is showing.
   */
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

  /**
   * Returns true if image view is showing.
   * 
   * @return true if showing
   */
  public boolean isShowing()
  {
    return mStage.isShowing();
  }

  /**
   * Returns the mouse's x coordinate
   * 
   * @return mouse x
   */
  public double getMouseX()
  {
    return mPanZoomScene.getMouseX();
  }

  /**
   * Returns the mouse's y coordinate
   * 
   * @return mouse y
   */
  public double getMouseY()
  {
    return mPanZoomScene.getMouseY();
  }

  /**
   * Returns the scene's width
   * @return width
   */
  public double getWidth()
  {
    return mPanZoomScene.getWidth();
  }

  /**
   * Returns the scene's height
   * @return height
   */
  public double getHeight()
  {
    return mPanZoomScene.getHeight();
  }

  /**
   * @param pDisplayGrid
   */
  public void setDisplayGrid(boolean pDisplayGrid)
  {
    mViewParticles2D.setDisplayGrid(pDisplayGrid);
  }

  /**
   * @param pDisplayElapsedTime
   */
  public void setDisplayElapsedTime(boolean pDisplayElapsedTime)
  {
    mViewParticles2D.setDisplayElapsedTime(pDisplayElapsedTime);
  }

}
