package simbryo.synthoscopy.microscope.lightsheet;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import clearcl.ClearCLContext;
import simbryo.util.geom.GeometryUtils;

/**
 * This class knows how to build orthogonal simultaneous multi-view lightsheet
 * microscope simulators with different number of illumination and detection
 * arms
 *
 * @author royer
 */
public class LightSheetMicroscopeSimulatorOrtho extends
                                                LightSheetMicroscopeSimulator
{

  /**
   * Instanciates a simulator with given ClearCL context, number of detection
   * and illumination arms as well as main phantom dimensions
   * 
   * @param pContext
   *          ClearCL context
   * @param pNumberOfDetectionArms
   *          number of detection arms
   * @param pNumberOfIlluminationArms
   *          number of illuination arms
   * @param pMainPhantomDimensions
   *          phantom main dimensions
   */
  public LightSheetMicroscopeSimulatorOrtho(ClearCLContext pContext,
                                            int pNumberOfDetectionArms,
                                            int pNumberOfIlluminationArms,
                                            int... pMainPhantomDimensions)
  {
    super(pContext, pMainPhantomDimensions);

    if (pMainPhantomDimensions.length != 3)
      throw new IllegalArgumentException("Phantom dimensions must have 3 components: (width,height,depth).");

    if (pNumberOfIlluminationArms >= 1)
    {
      Vector3f lIlluminationAxisVector = new Vector3f(1, 0, 0);
      Vector3f lIlluminationNormalVector = new Vector3f(0, 0, 1);

      addLightSheet(lIlluminationAxisVector,
                    lIlluminationNormalVector);
    }

    if (pNumberOfIlluminationArms >= 2)
    {
      Vector3f lIlluminationAxisVector = new Vector3f(-1, 0, 0);
      Vector3f lIlluminationNormalVector = new Vector3f(0, 0, -1);

      addLightSheet(lIlluminationAxisVector,
                    lIlluminationNormalVector);
    }

    int lMaxCameraImageWidth = 2 * getWidth();
    int lMaxCameraImageHeight = 2 * getHeight();

    if (pNumberOfDetectionArms >= 1)
    {
      Matrix4f lDetectionMatrix = new Matrix4f();
      lDetectionMatrix.setIdentity();

      Vector3f lDetectionUpDownVector = new Vector3f(0, 1, 0);

      addDetectionPath(lDetectionMatrix,
                       lDetectionUpDownVector,
                       lMaxCameraImageWidth,
                       lMaxCameraImageHeight);
    }

    if (pNumberOfDetectionArms >= 2)
    {
      Matrix4f lDetectionMatrix =
                                GeometryUtils.rotY((float) Math.PI,
                                                   new Vector3f(0.5f,
                                                                0.5f,
                                                                0.5f));

      /*
      Matrix4f lVector = new Matrix4f();
      lVector.setColumn(0, new Vector4f(0.5f, 0.5f, 0.5f, 1.0f));
      
      lDetectionMatrix.mul(lVector);
      System.out.println(lDetectionMatrix);/**/

      Vector3f lDetectionUpDownVector = new Vector3f(0, 1, 0);

      addDetectionPath(lDetectionMatrix,
                       lDetectionUpDownVector,
                       lMaxCameraImageWidth,
                       lMaxCameraImageHeight);
    }
  }

}
