package simbryo.synthoscopy.microscope.parameters;

/**
 * Camera Parameters
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum CameraParameter implements ParameterInterface<Number>
{
 ROIXMin(0, 0, 2048),
 ROIXMax(0, 0, 2048),
 ROIWidth(1024, 0, 2048),
 ROIHeight(1024, 0, 2048);

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
