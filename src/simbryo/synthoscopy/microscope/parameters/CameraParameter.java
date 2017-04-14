package simbryo.synthoscopy.microscope.parameters;

/**
 * Camera Parameters
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum CameraParameter implements ParameterInterface<Number>
{
 Exposure(0, 0.020, 60 * 60), // unit: seconds, longest exposure is 1 hour...
 ROIXMin(0, 0, 2048),
 ROIXMax(0, 0, 2048),
 ROIWidth(1024, 0, 2048),
 ROIHeight(1024, 0, 2048),
 ROIOffsetX(0, -1024, 1024),
 ROIOffsetY(0, -1024, 1024);

  Number mDefaultValue, mMinValue, mMaxValue;

  private CameraParameter(Number pDefaultValue,
                          Number pMinValue,
                          Number pMaxValue)
  {
    mDefaultValue = pDefaultValue;
    mMinValue = pMinValue;
    mMaxValue = pMaxValue;
  }

  @Override
  public Number getDefaultValue()
  {
    return mDefaultValue;
  }

  @Override
  public Number getMinValue()
  {
    return mMinValue;
  }

  @Override
  public Number getMaxValue()
  {
    return mMaxValue;
  }
}
