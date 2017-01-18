package simbryo.phantom.io;

import java.io.File;

import simbryo.phantom.PhantomRendererInterface;

/**
 * Phantom writer interface
 *
 * @author royer
 */
public interface PhantomWriterInterface
{

  /**
   * Renders a phantom stack to a file.
   * @param pPhantomRenderer phantom renderer
   * @param pFile file to write to
   * @return true if file written, false otherwise
   * @throws Throwable if anything goes wrong...
   */
  boolean write(PhantomRendererInterface<?> pPhantomRenderer, File pFile) throws Throwable;

}
