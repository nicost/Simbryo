package simbryo.synthoscopy.illumination;

import clearcl.ClearCLImage;

/**
 * Ilumination optics base class for illumination optics computation based on
 * CLearCL
 *
 * @author royer
 */
public abstract class ClearCLIlluminationOpticsBase extends
                                                    IlluminationOpticsBase<ClearCLImage>
                                                    implements
                                                    IlluminationOpticsInterface<ClearCLImage>
{

  @Override
  public ClearCLImage getIlluminationImage()
  {
    return null;
  }

}
