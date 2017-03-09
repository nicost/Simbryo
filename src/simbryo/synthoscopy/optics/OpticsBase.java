package simbryo.synthoscopy.optics;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.util.MatrixUtils;
import simbryo.synthoscopy.ClearCLSynthoscopyBase;
import simbryo.synthoscopy.SynthoscopyInterface;

/**
 * Optics base class providing common firlds and methods for optics related
 * classes
 *
 * @author royer
 */
public abstract class OpticsBase extends ClearCLSynthoscopyBase
                                 implements
                                 SynthoscopyInterface<ClearCLImage>,
                                 OpticsInterface
{

  private static final float cDefaultWavelengthInNormUnits = 0.0005f;
  private float mWavelengthInNormUnits;

  private Matrix4f mTransformMatrix = new Matrix4f();
  private ClearCLBuffer mTransformMatrixBuffer;

  /**
   * Instanciates a optics base class with basic optics related fields.
   * 
   * @param pContext
   *          ClearCL context
   * 
   * @param pImageDimensions
   *          image dimensions
   */
  public OpticsBase(final ClearCLContext pContext,
                    long... pImageDimensions)
  {
    super(pContext, pImageDimensions);
    mWavelengthInNormUnits = cDefaultWavelengthInNormUnits;
    mTransformMatrix.setIdentity();
    mTransformMatrix.setTranslation(new Vector3f(0.1f, 0.1f, 0.1f));
  }

  protected void updateTransformBuffer()
  {
    mTransformMatrixBuffer =
                           MatrixUtils.matrixToBuffer(mContext,
                                                      mTransformMatrixBuffer,
                                                      getTransformMatrix());
  }

  protected ClearCLBuffer getTransformMatrixBuffer()
  {
    return mTransformMatrixBuffer;
  }

  @Override
  public float getLightWavelength()
  {
    return mWavelengthInNormUnits;
  }

  @Override
  public void setLightWavelength(float pWavelengthInNormUnits)
  {
    if (mWavelengthInNormUnits != pWavelengthInNormUnits)
    {
      mWavelengthInNormUnits = pWavelengthInNormUnits;
      requestUpdate();
    }
  }

  @Override
  public Matrix4f getTransformMatrix()
  {
    return mTransformMatrix;
  }

  @Override
  public void setTransformMatrix(Matrix4f pTransformMatrix)
  {
    if (!mTransformMatrix.equals(pTransformMatrix))
    {
      mTransformMatrix = pTransformMatrix;
      requestUpdate();
    }
  }
  
  @Override
  public void setTranslation(Vector3f pTranslationVector)
  {
    Matrix4f lNewMatrix = new Matrix4f();
    lNewMatrix.set(getTransformMatrix());
    lNewMatrix.setTranslation(pTranslationVector);
    setTransformMatrix(lNewMatrix);
  }

}
