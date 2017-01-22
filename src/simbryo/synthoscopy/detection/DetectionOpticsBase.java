package simbryo.synthoscopy.detection;

import simbryo.phantom.PhantomRendererInterface;
import simbryo.synthoscopy.OpticsBase;

/**
 * Detection optics base class providing commonfields and methods nescessary for
 * all implementions of the detection optics interface
 *
 * @param <I> type of images used to store and process detection-side images
 * @author royer
 */
public abstract class DetectionOpticsBase<I> extends OpticsBase<I>
                                         implements
                                         DetectionOpticsInterface<I>
{

  public DetectionOpticsBase()
  {
    super();
  }

}
