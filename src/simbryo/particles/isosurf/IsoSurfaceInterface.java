package simbryo.particles.isosurf;

import java.io.Serializable;

/**
 * Iso-surfaces
 *
 * @author royer
 */
public interface IsoSurfaceInterface extends Serializable
{

  /**
   * Returns the dimension (1D, 2D, ...) of the iso-surface.
   * 
   * @return dimension
   */
  int getDimension();

  /**
   * Resets the calculation of the distance and gradient.
   */
  void clear();

  /**
   * Adds a new coordinate. As many as the dimension should be added.
   * 
   * @param pValue
   */
  void addCoordinate(float pValue);

  /**
   * Returns the distance to the surface. This may not be a geometric distance
   * but a proportional, 'transformed' or geodesic distance, depending on what
   * is practical or required. But it should be continuous and be zero at the
   * surface.
   * 
   * @return distance to surface
   */
  float getDistance();

  /**
   * Returns the normalized gradient component at a given index.
   * 
   * @param pIndex
   *          index of the gradient vector component.
   * @return gradient component value
   */
  float getNormalizedGardient(int pIndex);

}
