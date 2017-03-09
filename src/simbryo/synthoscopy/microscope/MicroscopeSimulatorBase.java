package simbryo.synthoscopy.microscope;

/**
 * Microscope simulator base class
 *
 * @param <I>
 *          type used to store and manipulate images during optics rendering
 * @author royer
 */
public abstract class MicroscopeSimulatorBase<I> implements
                                             MicroscopeSimulatorInterface<I>
{

  /**
   * Instanciates a microscope simulator
   */
  public MicroscopeSimulatorBase()
  {
    super();
  }

}
