package simbryo.util.geom;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * Geometry utils
 *
 * @author royer
 */
public class GeometryUtils
{
  
  public static void addTranslation(Matrix4f pMatrix,
                                    float pDeltaX,
                                    float pDeltaY,
                                    float pDeltaZ)
  {
    pMatrix.m03+=pDeltaX;
    pMatrix.m13+=pDeltaY;
    pMatrix.m23+=pDeltaZ;
  }

  
  public static Matrix4f multiply(Matrix4f... pMatrices)
  {
    Matrix4f lMatrix = new Matrix4f();
    lMatrix.setIdentity();
    
    for(int i=0; i<pMatrices.length; i++)
    {
      Matrix4f lMatrixToMultiply = pMatrices[i];
      lMatrix.mul(lMatrixToMultiply);
    }
        
    return lMatrix;
  }

  public static Vector4f pointMultiplication(Matrix4f pMatrix,
                                             Vector4f pVector)
  {
    Matrix4f lMatrixForVector = new Matrix4f();
    lMatrixForVector.setColumn(0, pVector);

    Matrix4f lProduct = new Matrix4f();

    lProduct.mul(pMatrix, lMatrixForVector);

    Vector4f lVectorResult = new Vector4f();
    lProduct.getColumn(0, lVectorResult);
    return lVectorResult;
  }

  public static Vector4f directionMultiplication(Matrix4f pMatrix,
                                                 Vector4f pVector)
  {
    Matrix4f lMatrixWithoutTranslation = new Matrix4f(pMatrix);
    lMatrixWithoutTranslation.setTranslation(new Vector3f(0f,
                                                          0f,
                                                          0f));
    return pointMultiplication(lMatrixWithoutTranslation,pVector);
  }

  public static float homogenousDot(Vector4f pVectorA,
                                    Vector4f pVectorB)
  {
    return pVectorA.x * pVectorB.x + pVectorA.y * pVectorB.y
           + pVectorA.z * pVectorB.z;
  }

  public static void homogenousNormalize(Vector4f pVector)
  {
    float lNorm = (float) Math.sqrt(homogenousDot(pVector, pVector));
    pVector.x /= lNorm;
    pVector.y /= lNorm;
    pVector.z /= lNorm;
  }

  public static Vector4f cross(Vector4f pVectorA, Vector4f pVectorB)
  {
    Vector3f lVectorA = new Vector3f(pVectorA.x,
                                     pVectorA.y,
                                     pVectorA.z);
    Vector3f lVectorB = new Vector3f(pVectorB.x,
                                     pVectorB.y,
                                     pVectorB.z);
    Vector3f lCrossVector = new Vector3f();
    lCrossVector.cross(lVectorA, lVectorB);
    return new Vector4f(lCrossVector.x,
                        lCrossVector.y,
                        lCrossVector.z,
                        1.0f);
  }

  public static Matrix4f rotX(float pAngle, Vector3f pRotationCenter)
  {
    Matrix4f lRotationMatrix = new Matrix4f();
    lRotationMatrix.setIdentity();
    lRotationMatrix.rotX(pAngle);
    return rotAroundCenter(lRotationMatrix, pRotationCenter);
  }

  public static Matrix4f rotY(float pAngle, Vector3f pRotationCenter)
  {
    Matrix4f lRotationMatrix = new Matrix4f();
    lRotationMatrix.setIdentity();
    lRotationMatrix.rotY(pAngle);
    return rotAroundCenter(lRotationMatrix, pRotationCenter);
  }

  public static Matrix4f rotZ(float pAngle, Vector3f pRotationCenter)
  {
    Matrix4f lRotationMatrix = new Matrix4f();
    lRotationMatrix.setIdentity();
    lRotationMatrix.rotZ(pAngle);
    return rotAroundCenter(lRotationMatrix, pRotationCenter);
  }

  public static Matrix4f rotAroundCenter(Matrix4f pRotationMatrix,
                                         Vector3f pRotationCenter)
  {
    Matrix4f lTranslationMatrix = new Matrix4f();
    lTranslationMatrix.setIdentity();
    lTranslationMatrix.setTranslation(pRotationCenter);
    
    Matrix4f lTranslationMatrixInverse = new Matrix4f(lTranslationMatrix);
    lTranslationMatrixInverse.invert();

    Matrix4f lRotationMatrixAroundCenter = multiply(lTranslationMatrix,pRotationMatrix,lTranslationMatrixInverse);

    return lRotationMatrixAroundCenter;
  }

  /**
   * Computes distance between two vectors stored in one contiguous array.
   * 
   * @param pDimension
   *          vector dimension
   * @param pPositions
   *          array
   * @param pIdu
   *          first vector id
   * @param pIdv
   *          second vector id
   * @return distance
   */
  public static float computeDistance(int pDimension,
                                      float[] pPositions,
                                      int pIdu,
                                      int pIdv)
  {
    return (float) Math.sqrt(computeSquaredDistance(pDimension,
                                                    pPositions,
                                                    pIdu,
                                                    pIdv));
  }

  /**
   * Computes the squared distance between two vectors stored in one contiguous
   * array.
   * 
   * @param pDimension
   *          vector dimension
   * @param pPositions
   *          array
   * @param pIdu
   *          first vector id
   * @param pIdv
   *          second vector id
   * @return distance
   */
  public static float computeSquaredDistance(int pDimension,
                                             float[] pPositions,
                                             int pIdu,
                                             int pIdv)
  {

    final int u = pIdu * pDimension;
    final int v = pIdv * pDimension;

    float lDistance = 0;

    for (int d = 0; d < pDimension; d++)
    {
      float lAxisDistance = pPositions[u + d] - pPositions[v + d];
      lDistance += lAxisDistance * lAxisDistance;
    }

    return lDistance;
  }

  /**
   * Detects bounding box collisions.
   * 
   * @param pDimension
   *          vector dimensions
   * @param pPositions
   *          array of vectors
   * @param pR1
   *          first radius
   * @param pR2
   *          second radius
   * @param pIdu
   *          first vector id
   * @param pIdv
   *          second vector id
   * @return true if bounding boxes collide
   */
  public static boolean detectBoundingBoxCollision(int pDimension,
                                                   float[] pPositions,
                                                   float pR1,
                                                   float pR2,
                                                   int pIdu,
                                                   int pIdv)
  {

    final int u = pIdu * pDimension;
    final int v = pIdv * pDimension;

    for (int d = 0; d < pDimension; d++)
    {
      float lAxisDistance = Math.abs(pPositions[u + d]
                                     - pPositions[v + d]);
      float lAxisGap = lAxisDistance - pR1 - pR2;

      if (lAxisGap > 0)
        return false;
    }

    return true;
  }




}
