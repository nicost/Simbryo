package simbryo.synthoscopy.illumination;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import simbryo.synthoscopy.ClearCLOpticsBase;

/**
 * Ilumination optics base class for illumination optics computation based on
 * CLearCL
 *
 * @author royer
 */
public abstract class ClearCLIlluminationOpticsBase extends
                                                    ClearCLOpticsBase
                                                    implements
                                                    IlluminationOpticsInterface<ClearCLImage>,
                                                    AutoCloseable
{


  /**
   * Instanciates a ClearCL powered illumination optics base class given the
   * wavelength of light, the light intensity, ClearCL context, and the light
   * map image dimensions.
   * 
   * @param pContext
   *          ClearCL context
   * @param pWavelengthInNormUnits
   *          light's wavelength
   * @param pLightIntensity
   *          light's intensity
   * @param pLightMapDimensions
   *          light map image dimensions
   */
  public ClearCLIlluminationOpticsBase(final ClearCLContext pContext,
                                       float pWavelengthInNormUnits,
                                       float pLightIntensity,
                                       long... pLightMapDimensions)
  {
    super(pContext,
          pWavelengthInNormUnits,
          pLightIntensity,
          pLightMapDimensions);

    mContext = pContext;

    mImage =
                   mContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                     getWidth(),
                                                     getHeight(),
                                                     getDepth());

    mImage.fillZero(true, false);
  }

  @Override
  public ClearCLImage getLightMapImage()
  {
    return mImage;
  }

  @Override
  public ClearCLImage render(ClearCLImage pScatteringPhantomImage)
  {
    // not doing anything here, derived classes must actually cmpute something
    // into mLightMapImage
    mImage.notifyListenersOfChange(mContext.getDefaultQueue());
    return mImage;
  }


}
